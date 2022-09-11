package com.yang.gulimall.order.web;

import com.yang.gulimall.order.entity.OrderEntity;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Date;
import java.util.UUID;

@Controller
public class testController {
    @Autowired
    RabbitTemplate rabbitTemplate;
    @ResponseBody
    @GetMapping("/test/createOrder")
    public String createOrderTest()
    {
        OrderEntity orderEntity = new OrderEntity();
        orderEntity.setModifyTime(new Date());
        orderEntity.setOrderSn(UUID.randomUUID().toString());
        rabbitTemplate.convertAndSend("order-event-exchange",
                "order.create.order",orderEntity);
        return "ok";
    }
}
