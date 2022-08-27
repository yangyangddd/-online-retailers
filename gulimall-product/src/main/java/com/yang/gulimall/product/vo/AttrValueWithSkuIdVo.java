package com.yang.gulimall.product.vo;

import lombok.Data;

@Data
public class AttrValueWithSkuIdVo {
    private String attrValue;//attr的值
    private String skuIds;//拥有该attr的值所有skuId集合
}
