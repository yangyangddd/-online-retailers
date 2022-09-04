package com.yang.gulimall.order;

import com.yang.gulimall.order.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@Slf4j
@SpringBootTest
class GulimallOrderApplicationTests {

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


}
