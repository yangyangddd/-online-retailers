package com.yang.gulimall.auth.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties(prefix = "gulimall.mail")
@Component
@Data
public class MailConfigProperties {
    private String Subject;//邮件主题
    private String setTo;//邮件发送者
}
