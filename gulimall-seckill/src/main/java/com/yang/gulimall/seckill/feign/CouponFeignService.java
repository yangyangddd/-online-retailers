package com.yang.gulimall.seckill.feign;

import com.yang.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient("gulimall-coupon")
public interface CouponFeignService {
    @GetMapping("coupon/seckillsession/lasts3DaySession")
    public R getLast3DaySession();
}
