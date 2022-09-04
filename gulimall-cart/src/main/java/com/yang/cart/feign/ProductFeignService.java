package com.yang.cart.feign;

import com.yang.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;


@FeignClient("gulimall-product")
public interface ProductFeignService {
    @RequestMapping("product/skuinfo/info/{skuId}")
    public R getSkuInfo(@PathVariable("skuId") Long skuId);
    @GetMapping("product/skusaleattrvalue/stringlist/{skuId}")
    public List<String> getSkuSaleAttrValues(@PathVariable("skuId") Long skuId);
    @PostMapping("product/skuinfo/product/skuinfo/newPrice")
    public Map<Long,BigDecimal> getNewPrice(@RequestBody List<Long> skuIds);
}
