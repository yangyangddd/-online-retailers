package com.yang.gulimall.product.feign;

import com.yang.common.utils.R;
import com.yang.to.es.SkuEsModel;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient("gulimall-search")
public interface SearchFeignService {
    @PostMapping("search/product")
    public R productStatusUp(@RequestBody List<SkuEsModel> skuEsModels);
}
