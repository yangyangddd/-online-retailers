package com.yang.gulimall.order;

import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;
//
//@EnableAutoDataSourceProxy
@EnableAspectJAutoProxy(exposeProxy = true)
@EnableFeignClients("com.yang.gulimall.order.feign")
@EnableRedisHttpSession
@EnableDiscoveryClient
@EnableRabbit
@SpringBootApplication
public class GulimallOrderApplication {
    public static void main(String[] args) {
        SpringApplication.run(GulimallOrderApplication.class, args);
    }

}
