package com.yang.gulimall.order.feign;

import org.springframework.cloud.openfeign.FeignClient;

@FeignClient("gulimall-product")
public interface ProductFeign {
    //获取所有被选中的购物车的信息

}
