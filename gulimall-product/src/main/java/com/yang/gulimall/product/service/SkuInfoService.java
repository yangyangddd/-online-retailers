package com.yang.gulimall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yang.common.utils.PageUtils;
import com.yang.gulimall.product.entity.SkuInfoEntity;
import com.yang.gulimall.product.vo.SkuItemVo;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * sku信息
 *
 * @author yang
 * @email yang@gmail.com
 * @date 2022-06-11 19:48:15
 */
public interface SkuInfoService extends IService<SkuInfoEntity> {

    PageUtils queryPage(Map<String, Object> params);

    List<SkuInfoEntity> getSkusBySpuId(Long spuId);

    SkuItemVo item(Long skuId);

    List<SkuItemVo.SkuItemSaleAttrVo> getSaleAttrsBySpuId(Long spuId);

    Map<Long,BigDecimal> getNewPriceBySkuIds(List<Long> skuIds);
}

