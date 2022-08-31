package com.yang.gulimall.auth.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties(prefix = "gitee.oauth")
@Component
@Data
public class GiteeLoginConfigProperties {
    public String ClientId;
    public String ClientSecret;
    public String Url;
}
