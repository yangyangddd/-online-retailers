package com.yang.gulimall.order.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Configuration
//@EnableConfigurationProperties(TheadPoolConfigProperties.class)
public class ThreadConfig {

    @Bean
    public ThreadPoolExecutor threadPoolExecutor(TheadPoolConfigProperties theadPoolConfigProperties)
    {
        return new ThreadPoolExecutor(theadPoolConfigProperties.getCoreSize(),theadPoolConfigProperties.getMaxSize(), theadPoolConfigProperties.getKeepAliveTime(), TimeUnit.SECONDS,
                new LinkedBlockingDeque<>(100000), Executors.defaultThreadFactory(),new ThreadPoolExecutor.AbortPolicy());
    }
}
