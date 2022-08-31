package com.yang.gulimall.product.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;

@Configuration
public class GulimallSessionConfig {
//    @Bean
//    public CookieSerializer cookieSerializer()
//    {
//        DefaultCookieSerializer defaultCookieSerializer = new DefaultCookieSerializer();
//        defaultCookieSerializer.setDomainName("gulimall.com");
//        defaultCookieSerializer.setCookieName("GULISESSION");
//        return defaultCookieSerializer;
//    }
    @Bean
    public RedisSerializer<Object> springSessionDefaultRedisSerializer()
    {
        return new Jackson2JsonRedisSerializer<Object>(Object.class);
    }
}
