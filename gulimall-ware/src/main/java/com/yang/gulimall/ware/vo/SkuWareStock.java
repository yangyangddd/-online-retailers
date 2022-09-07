package com.yang.gulimall.ware.vo;

import lombok.Data;

import java.util.List;

@Data
public class SkuWareStock{
    private Long skuId;//商品id
    private List<Long> wareId;//有该商品库存的仓库id集合
    private Integer num;//商品总库存
    private Integer count;//需求总量
}