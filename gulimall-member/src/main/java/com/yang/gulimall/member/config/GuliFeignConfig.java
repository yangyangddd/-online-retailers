package com.yang.gulimall.member.config;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

@Configuration
public class GuliFeignConfig {
    @Bean
    public RequestInterceptor requestInterceptor()
    {
        return new RequestInterceptor() {
            @Override
            public void apply(RequestTemplate template) {
                //拿到请求头的信息
                ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
                if(requestAttributes!=null) {
                    HttpServletRequest request = requestAttributes.getRequest();//老请求
                    if (request != null) {
                        String cookie = request.getHeader("Cookie");
                        template.header("Cookie", cookie);
                    }
                }
            }
        };
    }
}
