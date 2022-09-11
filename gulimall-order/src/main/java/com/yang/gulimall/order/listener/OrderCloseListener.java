package com.yang.gulimall.order.listener;

import com.rabbitmq.client.Channel;
import com.yang.gulimall.order.entity.OrderEntity;
import com.yang.gulimall.order.service.OrderService;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
@RabbitListener(queues = "order.release.order.queue")
public class OrderCloseListener {
    @Autowired
    OrderService orderService;
    @RabbitHandler
    public void listener(OrderEntity entity, Channel channel, Message message) throws IOException {
        System.out.println("收到过期的订单");
        try {
            orderService.closeOrder(entity);//关闭订单
            //手动调用支付宝收单

            channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
        } catch (Exception e) {
            channel.basicReject(message.getMessageProperties().getDeliveryTag(),true);
        }
    }
}
