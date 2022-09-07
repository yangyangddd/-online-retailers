package com.yang.gulimall.order.service.impl;

import cn.hutool.core.lang.TypeReference;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yang.common.utils.PageUtils;
import com.yang.common.utils.Query;
import com.yang.common.utils.R;
import com.yang.exception.NoStockException;
import com.yang.gulimall.order.constant.OrderConstant;
import com.yang.gulimall.order.dao.OrderDao;
import com.yang.gulimall.order.entity.OrderEntity;
import com.yang.gulimall.order.entity.OrderItemEntity;
import com.yang.gulimall.order.enume.OrderStatusEnum;
import com.yang.gulimall.order.feign.CartFeign;
import com.yang.gulimall.order.feign.MemberFeign;
import com.yang.gulimall.order.feign.ProductFeign;
import com.yang.gulimall.order.feign.WmsFeign;
import com.yang.gulimall.order.interceptor.LoginUserInterceptor;
import com.yang.gulimall.order.service.OrderItemService;
import com.yang.gulimall.order.service.OrderService;
import com.yang.gulimall.order.to.OrderCreateTo;
import com.yang.gulimall.order.vo.*;
import com.yang.vo.MemberRespVo;
//import io.seata.spring.annotation.GlobalTransactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


@Service("orderService")
public class OrderServiceImpl extends ServiceImpl<OrderDao, OrderEntity> implements OrderService {

    @Autowired
    MemberFeign memberFeign;
    @Autowired
    CartFeign cartFeign;
    @Autowired
    WmsFeign wmsFeign;
    @Autowired
    StringRedisTemplate stringRedisTemplate;
    @Autowired
    ThreadPoolExecutor executor;
    @Autowired
    ProductFeign productFeign;
    @Autowired
    OrderDao orderDao;
    @Autowired
    OrderItemService orderItemService;
    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<OrderEntity> page = this.page(
                new Query<OrderEntity>().getPage(params),
                new QueryWrapper<OrderEntity>()
        );

        return new PageUtils(page);
    }

    //订单确认页返回需要的数据
    @Override
    public OrderConfirmVo confirmOrder() throws ExecutionException, InterruptedException {
        OrderConfirmVo confirmVo = new OrderConfirmVo();
        //根据当前用户id查询用户的地址
        MemberRespVo memberRespVo = LoginUserInterceptor.loginUser.get();
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        //异步查询地址
        CompletableFuture<Void> completableFuture = CompletableFuture.runAsync(() ->
        {
            RequestContextHolder.setRequestAttributes(requestAttributes);
            R r = memberFeign.getByMemberId(memberRespVo.getId());
            if (r.getCode() != 0) {
                //地址为空
            }
            List<MemberAddressVo> memberReceiveAddress = r.getData("memberReceiveAddress", new TypeReference<List<MemberAddressVo>>() {
            });
            if (memberReceiveAddress != null)
                confirmVo.setAddress(memberReceiveAddress);
        }, executor);
//        //异步查询选中的购物车信息
        CompletableFuture<Void> completableFuture1 = CompletableFuture.runAsync(() ->
        {
            RequestContextHolder.setRequestAttributes(requestAttributes);

            //根据用户id查询用户购物车选中的信息
            R checkItem = cartFeign.getCheckItem();
            if (checkItem.getCode() != 0) {
                //购物车为空
            }
            List<OrderItemVo> checkCart = checkItem.getData("checkCart", new TypeReference<List<OrderItemVo>>() {
            });
            confirmVo.setItems(checkCart);
        }, executor).thenRunAsync(()->
        {
            List<OrderItemVo> items = confirmVo.getItems();
            List<Long> collect = items.stream().map(OrderItemVo::getSkuId).collect(Collectors.toList());
            R r = wmsFeign.hasStock(collect);
            List<HasStockVo> data = r.getData(new TypeReference<List<HasStockVo>>() {
            });
            if(data!=null) {
                Map<Long, Boolean> collect1 = data.stream().collect(Collectors.toMap(HasStockVo::getSkuId, HasStockVo::getHasStock));
                confirmVo.setStocks(collect1);

            }
        },executor);
        //用户积分
        confirmVo.setIntegration(memberRespVo.getIntegration());
        //防重令牌
        String token = UUID.randomUUID().toString().replace("-", "");
        stringRedisTemplate.opsForValue().set(OrderConstant.USER_ORDER_TOKEN_PREFIX+memberRespVo.getId(),token,30, TimeUnit.MINUTES);
        confirmVo.setOrderToken(token);
        CompletableFuture.allOf(completableFuture,completableFuture1).get();
        return confirmVo;
    }
//    @GlobalTransactional
    @Transactional
    @Override
    public SubmitOrderResponseVo submitOrder(OrderSubmitVo vo) {
        //去创建订单，验令牌，验价格，锁库存
        //1.验证令牌

        SubmitOrderResponseVo responseVo = new SubmitOrderResponseVo();
       responseVo.setCode(0);
        MemberRespVo memberRespVo = LoginUserInterceptor.loginUser.get();//获得用户id
                Long id = memberRespVo.getId();
                String orderToken = vo.getOrderToken();
        //原子验证令牌和删除令牌
                String script="if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";//使用lua脚本保证验证令牌和删除令牌为原子性
        Long result = stringRedisTemplate.execute(new DefaultRedisScript<Long>(script, Long.class),
                Arrays.asList(OrderConstant.USER_ORDER_TOKEN_PREFIX + id), orderToken);
        if(result==0L)
        //验证失败
        {
            responseVo.setCode(1);
            return responseVo;
        }
        else {
            //验证成功
            OrderCreateTo order = createOrder(vo);// 创建订单
            //验价
            BigDecimal payAmount = order.getOrder().getPayAmount();
            BigDecimal payPrice = vo.getPayPrice();
            if (payPrice.subtract(payAmount).abs().doubleValue()<0.01) {
                //金额对比
                //保存订单
                saveOrder(order);
                //库存锁定，只要有异常就回滚订单数据
                //订单号，所有订单项(skuId,skuName,num)
                WareSkuLockVo lockVo=new WareSkuLockVo();
                lockVo.setOrderSn(order.getOrder().getOrderSn());
                List<OrderItemVo> collect = order.getOrderItems().stream().map(e ->
                {
                    OrderItemVo orderItemVo = new OrderItemVo();
                    orderItemVo.setSkuId(e.getSkuId());
                    orderItemVo.setCount(e.getSkuQuantity());
                    orderItemVo.setTitle(e.getSkuName());
                    return orderItemVo;
                }).collect(Collectors.toList());
                lockVo.setLocks(collect);
                R r = wmsFeign.orderLockStock(lockVo);
                if(r.getCode()==0)
                {
                    //TODO 远程锁库存
                    responseVo.setOrder(order.getOrder());
                    return responseVo;
                    //锁定成功
                }
                else {
                   String msg=(String) r.get("msg");
                   throw new NoStockException(msg);
                    //锁定失败
                }
            }
            else
            {
                responseVo.setCode(2);//金额对比失败
                return responseVo;
            }

        }
//        String redisToken = stringRedisTemplate.opsForValue().get(OrderConstant.USER_ORDER_TOKEN_PREFIX + id);
//        if (orderToken==null||!orderToken.equals(redisToken)) {//验证令牌是否相等
//            //令牌不相等
//            return responseVo;
//        }
//
//        //通过则删除令牌，并创建订单号
//        stringRedisTemplate.delete(OrderConstant.USER_ORDER_TOKEN_PREFIX + id);
//        String s = UUID.randomUUID().toString();
//        return null;
    }

    // 保存订单数据
    private void saveOrder(OrderCreateTo order) {
        OrderEntity orderEntity = order.getOrder();
        orderEntity.setModifyTime(new Date());
        orderEntity.setDeliveryCompany("申通");
        orderDao.insert(orderEntity);
        List<OrderItemEntity> orderItems = order.getOrderItems();
        orderItemService.saveBatch(orderItems);
    }

    private OrderCreateTo createOrder(OrderSubmitVo vo)
    {
        //生成订单号
        String orderSn = IdWorker.getTimeId();
        OrderCreateTo orderCreateTo = new OrderCreateTo();
        //1.设置订单
        OrderEntity orderEntity = buildOrder(vo, orderSn);
        orderCreateTo.setOrder(orderEntity);
        //2.设置订单项
        List<OrderItemEntity> orderItemEntities = buildOrderItems(orderSn);
        orderCreateTo.setOrderItems(orderItemEntities);
        //3.计算价格,积分等相关信息
        computePrice(orderEntity,orderItemEntities);
        return orderCreateTo;
    }

    private void computePrice(OrderEntity orderEntity, List<OrderItemEntity> orderItemEntities) {
        BigDecimal total=new BigDecimal("0.0");
        //订单的总额，叠加每一个订单项的总额信息
        BigDecimal coupon=new BigDecimal("0.0");//优惠券优惠总额
        BigDecimal integration=new BigDecimal("0.0");//积分优惠总额
        BigDecimal promotion=new BigDecimal("0.0");//打折优惠总额
        Integer gift=0;//总积分
        Integer growth=0;//总成长值
        for (OrderItemEntity orderItemEntity : orderItemEntities) {
            BigDecimal realAmount = orderItemEntity.getRealAmount();
            coupon=coupon.add(orderItemEntity.getCouponAmount());
            integration= integration.add(orderItemEntity.getIntegrationAmount());
            promotion =promotion.add(orderItemEntity.getPromotionAmount());
            total = total.add(realAmount);
           gift+=orderItemEntity.getGiftIntegration();
            growth +=orderItemEntity.getGiftGrowth();

        }
        orderEntity.setTotalAmount(total);
        //应付总额
        orderEntity.setPayAmount(total.add(orderEntity.getFreightAmount()));
        orderEntity.setPromotionAmount(promotion);
        orderEntity.setIntegrationAmount(integration);
        orderEntity.setCouponAmount(coupon);
        //设置积分等信息
        orderEntity.setIntegration(gift);
        orderEntity.setGrowth(growth);
        orderEntity.setDeleteStatus(0);//未删除
    }

    private  List<OrderItemEntity> buildOrderItems(String orderSn) {
        R checkItem = cartFeign.getCheckItem();
        List<OrderItemVo> checkCart = checkItem.getData("checkCart", new TypeReference<List<OrderItemVo>>() {
        });
        List<OrderItemEntity> collect = checkCart.stream().map(e ->
        {
            OrderItemEntity orderItemEntity = buildOrderItem(e);
            orderItemEntity.setOrderSn(orderSn);//订单号
            return orderItemEntity;
        }).collect(Collectors.toList());
        return collect;
    }

    private OrderItemEntity buildOrderItem(OrderItemVo e) {
        OrderItemEntity itemEntity = new OrderItemEntity();
        //构建订单项
        //1.订单信息
        //2.商品spu信息
        R r = productFeign.getSpuInfoBySkuId(e.getSkuId());
        SpuInfoVo data = r.getData(new TypeReference<SpuInfoVo>() {
        });
        itemEntity.setSpuId(data.getId());
        itemEntity.setSpuBrand(data.getBrandId().toString());
        itemEntity.setSpuName(data.getSpuName());
        itemEntity.setCategoryId(data.getCatalogId());
        //3.商品的sku信息
        itemEntity.setSkuId(e.getSkuId());
        itemEntity.setSkuName(e.getTitle());
        itemEntity.setSkuName(e.getImage());
        itemEntity.setSkuPrice(e.getPrice());
        //TODO 数据库要求json 老师使用string分隔
        String s = JSONUtil.toJsonStr(e.getSkuAttr());
        itemEntity.setSkuAttrsVals(s);
        itemEntity.setSkuQuantity(e.getCount());
        //4.商品的优惠信息
        //5.积分信息
        itemEntity.setGiftGrowth(e.getPrice().multiply(new BigDecimal(e.getCount().toString())).intValue());
        itemEntity.setGiftIntegration(e.getPrice().multiply(new BigDecimal(e.getCount().toString())).intValue());
        //6.订单项的价格信息
        itemEntity.setPromotionAmount(new BigDecimal("0"));
        itemEntity.setCouponAmount(new BigDecimal("0"));
        itemEntity.setIntegrationAmount(new BigDecimal("0"));
        //当前订单项的实际金额
        BigDecimal orign = itemEntity.getSkuPrice().multiply(new BigDecimal(itemEntity.getSkuQuantity().toString()));
        BigDecimal subtract = orign.subtract(itemEntity.getCouponAmount()).subtract(itemEntity.getPromotionAmount())
                .subtract(itemEntity.getIntegrationAmount());
        itemEntity.setRealAmount(subtract);
        return itemEntity;
    }

    private OrderEntity buildOrder(OrderSubmitVo vo, String orderSn) {
        MemberRespVo memberRespVo = LoginUserInterceptor.loginUser.get();

        OrderEntity orderEntity = new OrderEntity();

        //订单数据
        orderEntity.setOrderSn(orderSn);
        orderEntity.setMemberId(memberRespVo.getId());
        //获取收获地址信息
        R fare = wmsFeign.getFare(vo.getAddrId());
        FareVo data = fare.getData(new TypeReference<FareVo>() {
        });
        //设置运费
        orderEntity.setFreightAmount(data.getFare());
        //设置收货人信息
        orderEntity.setReceiverCity(data.getAddress().getCity());
        orderEntity.setReceiverName(data.getAddress().getName());
        orderEntity.setReceiverDetailAddress(data.getAddress().getDetailAddress());
        orderEntity.setReceiverPhone(data.getAddress().getPhone());
        orderEntity.setReceiverProvince(data.getAddress().getProvince());
        orderEntity.setReceiverPostCode(data.getAddress().getPostCode());
        orderEntity.setReceiverRegion(data.getAddress().getRegion());
        //设置订单的相关状态信息
        orderEntity.setStatus(OrderStatusEnum.CREATE_NEW.getCode());

        return orderEntity;
    }
}