package com.yang.gulimall.ware.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class OrderItemVo {
    private Long skuId;
    private Boolean check=true;
    private String title;
    private String image;
    private List<String> skuAttr;
    private BigDecimal price;
    private Integer count;
    private BigDecimal totalPrice;
    //TODO 查询库存状态
    private boolean hasStock;
    //商品重量
    private BigDecimal weight;
}
