package com.yang.gulimall.order.listener;

import com.rabbitmq.client.Channel;
import com.yang.gulimall.order.service.OrderService;
import com.yang.to.mq.SeckillOrderTo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

@RabbitListener(queues = "order.seckill.order.queue")
@Component
@Slf4j
public class OrderSeckillListener {
    @Autowired
    OrderService orderService;
    @RabbitHandler
    public void listener(SeckillOrderTo seckillOrderTo, Channel channel, Message message) throws IOException {
        try {
            log.info("准备创建秒杀单的详细信息...");
            orderService.createSeckillOrder(seckillOrderTo);
            //手动调用支付宝收单
            channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
        } catch (Exception e) {
            channel.basicReject(message.getMessageProperties().getDeliveryTag(),true);
        }
    }
}
