package com.yang.gulimall.search.controller;

import com.yang.common.utils.R;
import com.yang.exception.BizCodeEnum;
import com.yang.gulimall.search.service.ProductSaveService;
import com.yang.to.es.SkuEsModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;

@RequestMapping("search")
@RestController
@Slf4j
public class ElasticSaveController {
    @Autowired
    private ProductSaveService productSaveService;
    //上架商品
    @PostMapping("/product")
    public R productStatusUp(@RequestBody List<SkuEsModel>skuEsModels)
    {
        boolean b=false;
        try {
             b = productSaveService.productStatusUp(skuEsModels);
        } catch (IOException e) {
            log.error("ElasticSaveController商品上架错误:{}",e);
            return R.error(BizCodeEnum.PRODUCT_UP_EXCEPTION.
                    getCode(),BizCodeEnum.PRODUCT_UP_EXCEPTION.getMsg());
        }
        if(!b)
        return R.ok();
        else
            return R.error(BizCodeEnum.PRODUCT_UP_EXCEPTION.
                    getCode(),BizCodeEnum.PRODUCT_UP_EXCEPTION.getMsg());
    }
}
