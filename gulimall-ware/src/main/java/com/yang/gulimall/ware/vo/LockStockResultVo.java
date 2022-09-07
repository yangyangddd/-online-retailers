package com.yang.gulimall.ware.vo;

import lombok.Data;

@Data
public class LockStockResultVo {
    private Long skuId;
    private Integer num;//锁了多少件
    private Boolean locked;
}
