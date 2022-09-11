package com.yang.to.mq;

import lombok.Data;

@Data
public class StockLockedTo {
    private Long id;//库存工作单id
    private StockDetailTo detailDetail;//工作单详情
}
