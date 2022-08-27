package com.yang.gulimall.auth.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.SimpleMailMessage;

import java.util.Date;

@Configuration
public class MailConfig {
    @Bean
    public SimpleMailMessage simpleMailMessage(MailConfigProperties mail){
        SimpleMailMessage message = new SimpleMailMessage();
        // 设置邮件主题
        message.setSubject(mail.getSubject());
        // 设置邮件发送者，这个跟application.yml中设置的要一致
        message.setFrom(mail.getSetTo());
        // 设置邮件接收者，可以有多个接收者，中间用逗号隔开，以下类似
        // message.setTo("1*****@qq.com","2*****qq.com");
        // 设置邮件发送日期
        message.setSentDate(new Date());
        return message;
    }
}
