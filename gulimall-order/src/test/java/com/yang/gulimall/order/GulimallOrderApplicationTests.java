package com.yang.gulimall.order;

import com.yang.gulimall.order.constant.OrderConstant;
import com.yang.gulimall.order.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.concurrent.TimeUnit;

@Slf4j
@SpringBootTest
class GulimallOrderApplicationTests {
    @Autowired
    StringRedisTemplate redisTemplate;
    @Autowired
    OrderService orderService;

//    @Autowired
//    AmqpAdmin amqpAdmin;
//    @Test
//    void contextLoads() {
//        DirectExchange directExchange = new DirectExchange("hello-java-exchange",true,false);
//        amqpAdmin.declareExchange(directExchange);
//    }
//    @Test
//    void creatQueue()
//    {
//        Queue queue=new Queue("hello-java-queue",true,false,true);
//        amqpAdmin.declareQueue(queue);
//    }
//    @Test
//    void test1()
//    {
//        OrderConfirmVo confirmVo = orderService.confirmOrder();
//        System.out.println(confirmVo);
//    }

    @Test
    void test1()
    {


        Long UserId=2L;
        redisTemplate.opsForValue().set(OrderConstant.USER_ORDER_TOKEN_PREFIX+UserId,"dsadsadsad",30, TimeUnit.MINUTES);
    }

}
