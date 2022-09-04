package com.yang.gulimall.order.config;

import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {
//    @Autowired
//    RabbitTemplate rabbitTemplate;
    @Bean
    public MessageConverter messageConverter()
    {
        return new Jackson2JsonMessageConverter();
    }

//    @PostConstruct
//    public void initRabbitTemplate()
//    {
//        rabbitTemplate.setReturnsCallback(System.out::println);
//        rabbitTemplate.setConfirmCallback(
//                /**
//                 * correlationData 当前消息唯一id，
//                 * b 消息是否成功还是失败
//                 * s 失败的原因
//                 */
//                (correlationData, b, s) ->
//        {
//            System.out.println(correlationData);
//            System.out.println(b);
//            System.out.println(s);
//
//        });
//    }
}
