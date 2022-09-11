package com.yang.gulimall.ware.service.impl;

import cn.hutool.core.lang.TypeReference;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yang.common.utils.PageUtils;
import com.yang.common.utils.Query;
import com.yang.common.utils.R;
import com.yang.gulimall.ware.dao.WareSkuDao;
import com.yang.gulimall.ware.entity.WareOrderTaskDetailEntity;
import com.yang.gulimall.ware.entity.WareOrderTaskEntity;
import com.yang.gulimall.ware.entity.WareSkuEntity;
import com.yang.gulimall.ware.exception.NoStockException;
import com.yang.gulimall.ware.feign.OrderFeignService;
import com.yang.gulimall.ware.service.WareOrderTaskDetailService;
import com.yang.gulimall.ware.service.WareOrderTaskService;
import com.yang.gulimall.ware.service.WareSkuService;
import com.yang.gulimall.ware.vo.*;
import com.yang.to.OrderVo;
import com.yang.to.mq.StockDetailTo;
import com.yang.to.mq.StockLockedTo;
import org.apache.commons.lang.StringUtils;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service("wareSkuService")
public class WareSkuServiceImpl extends ServiceImpl<WareSkuDao, WareSkuEntity> implements WareSkuService {

    @Autowired
    RabbitTemplate rabbitTemplate;
    @Autowired
    WareOrderTaskService orderTaskService;
    @Autowired
    WareOrderTaskDetailService orderTaskDetailService;
    @Autowired
    OrderFeignService orderFeignService;
    @Override
    public void unlockStock(StockLockedTo stockLockedTo) {

        Long id = stockLockedTo.getDetailDetail().getId();//库存工作单的id

        //由于库存是跨库调度的，所以一个订单可能产生多个`wms_ware_order_task_detail`中的信息,但这里是每次锁定库存都会创建
        WareOrderTaskDetailEntity byId = orderTaskDetailService.getById(id);
        //解锁
        //1.查询数据库关于这个订单的锁定库存信息
        //有
        if(byId!=null)
        {
            //解锁
            //获取订单号
            WareOrderTaskEntity byId1 = orderTaskService.getById(stockLockedTo.getId());
            String orderSn = byId1.getOrderSn();
            //远程查询订单情况
            R r = orderFeignService.getOrderStatus(orderSn);

            if(r.getCode()==0) {
                OrderVo data = r.getData(new TypeReference<OrderVo>() {
                });
                if (data == null || data.getStatus() == 4) {
                    //订单不存在，或订单已被取消
                    if(byId.getLockStatus()==1) {
                        unLockStock(byId.getSkuId(), byId.getWareId(), byId.getSkuNum(), id);
                    }
                    }
                }
            else {
                //远程调用失败
                throw new RuntimeException("远程服务失败");
            }
            }
    }

    @Override
    @Transactional
    public void unlockStock(OrderVo vo) {
        //防止订单服务卡顿导致订单状态一直改不了，库存消息优先到期。查询订单的新建状态，什么都不做就走了
        //导致卡顿的订单，永远不能解锁库存
        //查一下最新库存的状态，防止重复解锁库存
        String orderSn = vo.getOrderSn();
       WareOrderTaskEntity task= orderTaskService.getOrderTaskByOrderSn(orderSn);
        Long id = task.getId();//根据工作单找到所有没有解锁的库存，进行解锁
        List<WareOrderTaskDetailEntity> detailEntities = orderTaskDetailService.list(new QueryWrapper<WareOrderTaskDetailEntity>()
                .eq("task_id", id).eq("lock_status",1));
        for (WareOrderTaskDetailEntity entity : detailEntities) {
            unLockStock(entity.getSkuId(),entity.getWareId(),entity.getSkuNum(),entity.getId());
        }
        //TODO 此处可考虑批量操作
    }

    public void unLockStock(Long skuId,Long wareId,Integer num,Long taskDetailId)
    {
        //解锁库存方法

        this.baseMapper.unLockStock(skuId, wareId,num);
        WareOrderTaskDetailEntity entity=new WareOrderTaskDetailEntity();
        entity.setId(taskDetailId);
        entity.setLockStatus(2);//已解锁
        orderTaskDetailService.updateById(entity);

    }


    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        QueryWrapper<WareSkuEntity> wrapper = new QueryWrapper<>();
      String skuId= (String) params.get("skuId");
      if(StringUtils.isNotEmpty(skuId))
      {
          wrapper.eq("sku_id",skuId);
      }
        String wareId= (String) params.get("wareId");
      if(StringUtils.isNotEmpty(wareId))
      {
          wrapper.eq("ware_id",wareId);
      }
        IPage<WareSkuEntity> page = this.page(
                new Query<WareSkuEntity>().getPage(params),
                wrapper
        );

        return new PageUtils(page);
    }

    @Override
    public List<WareSkuEntity> getBySkuId(List<Long> skuId) {
        return this.list(new QueryWrapper<WareSkuEntity>().eq("sku_id",skuId));
    }

    @Override
    public List<HasStockVo> HasStock(List<Long> skuIds) {
        if (!skuIds.isEmpty()) {
            WareSkuDao baseMapper = this.baseMapper;
//        SELECT  sku_id,SUM(stock) FROM `wms_ware_sku` WHERE sku_id IN(2,28) GROUP BY sku_id
            List<HasStock2Vo> list = baseMapper.selectSumStock(skuIds);
            if(list!=null) {
                List<HasStockVo> collect = list.stream().map(e ->
                {
                    HasStockVo hasStockVo = new HasStockVo();
                    hasStockVo.setSkuId(e.getSkuId());
                    hasStockVo.setHasStock(e.getHasStock() > 0);
                    return hasStockVo;
                }).collect(Collectors.toList());
                //根据skuId查询其在所有仓库的总量
                return collect;
            }
        }
        return null;
    }

    //为某个订单锁定库存
    //库存解锁的场景
//    1.下订单成功，订单过期没有支付系统自动取消、被用户手动取消
//    2.下订单成功、库存锁定成功。接下来的业务调用失败，导致订单回滚，锁定的库存自动解锁
//
    @Override
    @Transactional(rollbackFor = NoStockException.class)
    public List<LockStockResultVo> orderLockStock(WareSkuLockVo vo) {
        WareOrderTaskEntity taskEntity=new WareOrderTaskEntity();
        taskEntity.setOrderSn(vo.getOrderSn());
        orderTaskService.save(taskEntity);
        List<OrderItemVo> locks = vo.getLocks();

        //查询每个商品在哪里有库存
        List<Long> skuIds = locks.stream().map(OrderItemVo::getSkuId).collect(Collectors.toList());
        List<WareSkuEntity> wareSkuEntities = this.list(new QueryWrapper<WareSkuEntity>()
                .in("sku_id",skuIds).ge("stock",0));
      //为每一个商品设置SkuWareStock
        List<SkuWareStock> collect1 = skuIds.stream().map(skuId -> {
            SkuWareStock skuWareStock = new SkuWareStock();
            final Integer[] num = {0};
            List<Long> collect = wareSkuEntities.stream().filter(e ->
                            skuId.equals(e.getSkuId())).map(e->
                            {
                               num[0] +=e.getStock()-e.getStockLocked();
                               return e.getId();
                            }
                    )
                    .collect(Collectors.toList());

            skuWareStock.setNum(num[0]);
            skuWareStock.setSkuId(skuId);
            skuWareStock.setWareId(collect);
            return skuWareStock;
        }).peek(e->
        {
            //设置库存所需要的量
            for (OrderItemVo lock : locks) {
                if(e.getSkuId().equals(lock.getSkuId()))
                {
                    e.setCount(lock.getCount());
                    break;
                }
            }
        }).collect(Collectors.toList());
        List<LockStockResultVo> stockResultVos=new ArrayList<>();
        //此处选用的方案为为选择每个仓库，获得每个拥有该商品的仓库，遍历这些仓库
        //依次与当前用户的需求量锁定每个库存，直到count值为0则结束当前商品的循环
        //不考虑任何商品调度与运费问题，只要库存够
        for (SkuWareStock skuWareStock : collect1) {
            Long skuId = skuWareStock.getSkuId();
            List<Long> wareId = skuWareStock.getWareId();

            if(wareId==null||wareId.size()==0||skuWareStock.getCount()>skuWareStock.getNum())
            {
                throw new NoStockException(skuId);
                //没有仓库有库存或库存量少于需求量
            }
            //
            //为每个仓库锁定库存
            List<WareSkuEntity> wareSkuEntities1 = this.listByIds(wareId);//查出所有仓库
            Integer count = skuWareStock.getCount();//获取需求总量

            //1.如果每一个商品都锁定成功，将当前商品锁定了几件的工作单记录发送给mq
            //2.锁定失败，前面保存的工作单信息就回滚了，发出去的消息，即使要解锁记录，
//            由于去数据库查不到id，所以就不用解锁
            for (WareSkuEntity wareSkuEntity : wareSkuEntities1) {
                LockStockResultVo lockStockResultVo = new LockStockResultVo();

                if(wareSkuEntity.getStock()-wareSkuEntity.getStockLocked()>count)//当前仓库存量大于当前需求量
                {
                    wareSkuEntity.setStockLocked(wareSkuEntity.getStockLocked()+count);//当前锁定仓库即为count值
                    lockStockResultVo.setNum(skuWareStock.getCount());
                    lockStockResultVo.setLocked(true);
                    lockStockResultVo.setSkuId(skuWareStock.getSkuId());
                    this.updateById(wareSkuEntity);
                    count=0;
                }
                else {
                    //当前仓库存量小于当前需求量
                    lockStockResultVo.setNum//锁了多少件设置为当前仓库总量和自身的值
                            (wareSkuEntity.getStock()-wareSkuEntity.getStockLocked()+ lockStockResultVo.getNum());
                    lockStockResultVo.setLocked(false);
                    lockStockResultVo.setSkuId(skuWareStock.getSkuId());
                    count-=wareSkuEntity.getStock()-wareSkuEntity.getStockLocked();//count减去当前仓库的库存值
                    wareSkuEntity.setStockLocked(wareSkuEntity.getStock());//设置锁定库存数量
                }
                stockResultVos.add(lockStockResultVo);
                this.updateById(wareSkuEntity);
                //锁定完成就发送消息至队列中
                WareOrderTaskDetailEntity wareOrderTaskDetailEntity = new WareOrderTaskDetailEntity();
                wareOrderTaskDetailEntity.setSkuId(skuId);
                wareOrderTaskDetailEntity.setSkuNum(lockStockResultVo.getNum());//当前锁定的库存量

                wareOrderTaskDetailEntity.setTaskId(taskEntity.getId());
                wareOrderTaskDetailEntity.setWareId(wareSkuEntity.getWareId());
                wareOrderTaskDetailEntity.setLockStatus(1);
                orderTaskDetailService.save(wareOrderTaskDetailEntity);

                //发送锁定库存的消息
                StockLockedTo stockLockedTo = new StockLockedTo();
                stockLockedTo.setId(taskEntity.getId());
                StockDetailTo stockDetailTo = new StockDetailTo();
                BeanUtils.copyProperties(wareOrderTaskDetailEntity,stockDetailTo);
                stockLockedTo.setDetailDetail(stockDetailTo);
                rabbitTemplate.convertAndSend("stock-event-exchange","stock.locked",stockLockedTo);
                if(count==0)
                {
                    break;//库存为0就退出循环
                }
            }


        }
        return stockResultVos;
    }


}