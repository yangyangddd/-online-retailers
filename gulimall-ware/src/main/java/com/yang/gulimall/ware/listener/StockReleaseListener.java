package com.yang.gulimall.ware.listener;

import cn.hutool.core.lang.TypeReference;
import com.rabbitmq.client.Channel;
import com.yang.common.utils.R;
import com.yang.gulimall.ware.entity.WareOrderTaskDetailEntity;
import com.yang.gulimall.ware.entity.WareOrderTaskEntity;
import com.yang.gulimall.ware.feign.OrderFeignService;
import com.yang.gulimall.ware.service.WareOrderTaskDetailService;
import com.yang.gulimall.ware.service.WareOrderTaskService;
import com.yang.gulimall.ware.service.WareSkuService;
import com.yang.to.OrderVo;
import com.yang.to.mq.StockLockedTo;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
@RabbitListener(queues = "stock.release.stock.queue")
public class StockReleaseListener {
    @Autowired
    WareOrderTaskDetailService orderTaskDetailService;
    @Autowired
    WareOrderTaskService orderTaskService;
    @Autowired
    OrderFeignService orderFeignService;
    @Autowired
    WareSkuService wareSkuService;
    @RabbitHandler
    //只有解锁库存成功，才进行队列的ACK
    public void handeStockLockedRelease(StockLockedTo stockLockedTo,Message message,Channel channel) throws IOException {
        System.out.println("收到解锁库存的消息");
        try {
            wareSkuService.unlockStock(stockLockedTo);
            channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
        } catch (Exception e) {
            channel.basicReject(message.getMessageProperties().getDeliveryTag(),true);
        }
    }
    @RabbitHandler
    public void handleOrderCloseRelease(OrderVo vo, Message message, Channel channel) throws IOException {
        System.out.println("收到订单关闭的逻辑准备解锁库存");
        try {
            wareSkuService.unlockStock(vo);
            channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
        } catch (Exception e) {
            channel.basicReject(message.getMessageProperties().getDeliveryTag(),true);
        }

    }
    //之前的解锁逻辑
    private void unlockStock(StockLockedTo stockLockedTo, Message message, Channel channel) throws IOException {
        System.out.println("收到解锁库存的消息");
        Long id = stockLockedTo.getId();//库存工作单的id
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
            OrderVo data = r.getData(new TypeReference<OrderVo>() {
            });
            if(r.getCode()==0) {
                if (data == null || data.getStatus() == 4) {
                    //订单不存在，或订单已被取消
                    wareSkuService.unLockStock(byId.getSkuId(), byId.getWareId(), byId.getSkuNum(), id);
                    channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
                }
                else {
                    //消息拒绝以后重新放入队列中，让别人继续消费解锁
                    channel.basicReject(message.getMessageProperties().getDeliveryTag(),true);
                }
            }

        }
        else {
            //没有，库存锁定失败了，库存回滚了，无需解锁
            channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
        }
        //监听死信,事务回滚
    }
}
