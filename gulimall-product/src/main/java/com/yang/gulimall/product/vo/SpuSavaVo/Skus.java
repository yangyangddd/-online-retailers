package com.yang.gulimall.product.vo.SpuSavaVo;

/**
 * Copyright 2022 json.cn
 */
import lombok.Data;

import java.util.List;

/**
 * Auto-generated: 2022-08-10 22:23:26
 *
 * @author json.cn (i@json.cn)
 * @website http://www.json.cn/java2pojo/
 */
@Data
public class Skus {

    private List<Attr> attr;
    private String skuName;
    private int price;
    private String skuTitle;
    private String skuSubtitle;
    private List<Images> images;
    private List<String> descar;
    private int fullCount;
    private int discount;
    private int countStatus;
    private int fullPrice;
    private int reducePrice;
    private int priceStatus;
    private List<MemberPrice> memberPrice;


}