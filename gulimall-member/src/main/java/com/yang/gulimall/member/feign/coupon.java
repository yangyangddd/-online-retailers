package com.yang.gulimall.member.feign;

import com.yang.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

@FeignClient(name="gulimall-coupon")
public interface coupon {
    @RequestMapping("/coupon/coupon/list")
    public R list(@RequestParam Map<String, Object> params);
}
