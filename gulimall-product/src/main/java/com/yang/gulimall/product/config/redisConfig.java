package com.yang.gulimall.product.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

@Configuration
public class redisConfig{
    @Bean(destroyMethod = "shutdown")
    public RedissonClient redisson() throws IOException
    {
        Config config=new Config();
        config.useSingleServer().setAddress("redis://192.168.234.128:6379");
        return Redisson.create(config);
    }
}
