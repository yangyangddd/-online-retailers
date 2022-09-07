package com.yang.gulimall.order.feign;

import com.yang.common.utils.R;
import com.yang.gulimall.order.vo.WareSkuLockVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient("gulimall-ware")
public interface WmsFeign {
    @PostMapping("ware/waresku/hasstock")
    public R hasStock(@RequestBody List<Long> skuIds);
    @GetMapping("ware/wareinfo/fare")
    public R getFare(@RequestParam("addrId") Long addrId);
    @PostMapping("ware/waresku/lock/order")
    public R orderLockStock(@RequestBody WareSkuLockVo vo);
}
