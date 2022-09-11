package com.yang.gulimall.order.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class MqConfig {

    @Bean
    public Queue orderDelayQueue()
    {
        Map<String,Object> arguments=new HashMap<>();
        arguments.put("x-dead-letter-exchange","order-event-exchange");
        arguments.put("x-dead-letter-routing-key","order.release.order");
        arguments.put("x-message-ttl",60000);

        return new Queue("order.delay.queue", true, false, false,arguments);
    }
    @Bean
    public Queue orderReleaseOrderQueue()
    {
        return new Queue("order.release.order.queue", true, false, false);
    }
    @Bean
    public Exchange orderEventExchange(){
        return new TopicExchange("order-event-exchange",true,false);

    }
    @Bean
    public Binding orderCreateOrderBinding(){
        return new Binding("order.delay.queue", Binding.DestinationType.QUEUE,"order-event-exchange","order.create.order",null);
    }
    @Bean
    public Binding orderReleaseOrderBinding(){
        return new Binding("order.release.order.queue", Binding.DestinationType.QUEUE,"order-event-exchange","order.release.order",null);
    }
    @Bean
    //订单释放直接和库存释放进行绑定
    public Binding orderReleaseOtherBinding(){
        return new Binding("stock.release.stock.queue", Binding.DestinationType.QUEUE,"order-event-exchange","order.release.other.#",null);
    }
}
