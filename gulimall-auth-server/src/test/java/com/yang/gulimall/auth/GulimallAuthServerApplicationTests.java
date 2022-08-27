package com.yang.gulimall.auth;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import javax.annotation.Resource;

@SpringBootTest
class GulimallAuthServerApplicationTests {

    @Resource
    JavaMailSender javaMailSender;
    @Autowired
    SimpleMailMessage message;
    @Test
    void contextLoads() {

    }
    @Test
    public void sendSimpleMail() {

    }
}
