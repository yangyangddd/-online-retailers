package com.yang.gulimall.order.feign;

import com.yang.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient("gulimall-product")
public interface ProductFeign {
    //获取所有被选中的购物车的信息
    @GetMapping("product/spuinfo/skuId/{skuId}")
    public R getSpuInfoBySkuId(@PathVariable("skuId") Long skuId);
}
