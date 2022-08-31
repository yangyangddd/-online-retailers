package com.yang.gulimall.auth;

import com.yang.gulimall.auth.config.GiteeLoginConfigProperties;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mail.SimpleMailMessage;

@SpringBootTest
class GulimallAuthServerApplicationTests {

//    @Resource
//    JavaMailSender javaMailSender;
    @Autowired
    SimpleMailMessage message;
    @Autowired
    GiteeLoginConfigProperties giteeLoginConfigProperties;
    @Test
    void contextLoads() {

    }
    @Test
    public void sendSimpleMail() {
        System.out.println(giteeLoginConfigProperties.ClientId);
    }
}
