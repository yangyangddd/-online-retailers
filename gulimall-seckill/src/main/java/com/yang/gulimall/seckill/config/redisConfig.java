package com.yang.gulimall.seckill.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class redisConfig {
    @Bean(destroyMethod = "shutdown")
    public RedissonClient redisson() {
        Config config=new Config();
        config.useSingleServer().setAddress("redis://192.168.234.128:6379");
        return Redisson.create(config);
    }
}
