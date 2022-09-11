package com.yang.gulimall.coupon;

import cn.hutool.core.date.LocalDateTimeUtil;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@SpringBootTest
class GulimallCouponApplicationTests {

    @Test
    void contextLoads() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime offset = LocalDateTimeUtil.offset(now, 2, ChronoUnit.DAYS);
        LocalDateTime start = LocalDateTimeUtil.beginOfDay(now);
        LocalDateTime end = LocalDateTimeUtil.endOfDay(offset);
        System.out.println(start);
        System.out.println(end);
        String s = LocalDateTimeUtil.formatNormal(start);
        System.out.println(LocalDateTimeUtil.formatNormal(end));
        System.out.println(s);
    }

}
