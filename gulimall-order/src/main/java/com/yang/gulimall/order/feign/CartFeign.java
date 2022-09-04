package com.yang.gulimall.order.feign;

import com.yang.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@FeignClient("gulimall-cart")
public interface CartFeign {
    //获取所有被选中的购物车的信息
    @GetMapping("/getCheckItem")
    @ResponseBody
    public R getCheckItem();
}
