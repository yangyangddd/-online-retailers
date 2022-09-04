package com.yang.gulimall.order.service.impl;

import cn.hutool.core.lang.TypeReference;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yang.common.utils.PageUtils;
import com.yang.common.utils.Query;
import com.yang.common.utils.R;
import com.yang.gulimall.order.constant.OrderConstant;
import com.yang.gulimall.order.dao.OrderDao;
import com.yang.gulimall.order.entity.OrderEntity;
import com.yang.gulimall.order.feign.CartFeign;
import com.yang.gulimall.order.feign.MemberFeign;
import com.yang.gulimall.order.feign.WmsFeign;
import com.yang.gulimall.order.interceptor.LoginUserInterceptor;
import com.yang.gulimall.order.service.OrderService;
import com.yang.gulimall.order.vo.*;
import com.yang.vo.MemberRespVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import java.util.List;
import java.util.Map;
import java.util.UUID;
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
        stringRedisTemplate.opsForValue().set(OrderConstant.USER_ORDER_TOKEN_PREFIX+memberRespVo.getId(),token,30, TimeUnit.MILLISECONDS);
        confirmVo.setOrderToken(token);
        CompletableFuture.allOf(completableFuture,completableFuture1).get();
        return confirmVo;
    }

    @Override
    public SubmitOrderResponseVo submitOrder(OrderSubmitVo vo) {
        //去创建订单，验令牌，验价格，锁库存
        //1.验证令牌
        String orderToken = vo.getOrderToken();
        return null;
    }

}