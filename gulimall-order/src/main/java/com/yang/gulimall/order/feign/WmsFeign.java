package com.yang.gulimall.order.feign;

import com.yang.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient("gulimall-ware")
public interface WmsFeign {
    @PostMapping("ware/waresku/hasstock")
    public R hasStock(@RequestBody List<Long> skuIds);
}
