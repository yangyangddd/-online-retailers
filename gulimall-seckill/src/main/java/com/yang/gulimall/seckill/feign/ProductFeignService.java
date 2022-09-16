package com.yang.gulimall.seckill.feign;

import com.yang.common.utils.R;
import com.yang.gulimall.seckill.vo.SeckillSkuVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient("gulimall-product")
public interface ProductFeignService {
    @PostMapping("product/skuinfo/seckill/getSkuInfo/promotionId")
    public R seckillGetSkuInfoByPromotionId(@RequestBody List<SeckillSkuVo> vo);
}
