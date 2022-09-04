package com.yang.gulimall.order.vo;

import com.yang.gulimall.order.entity.OrderEntity;
import lombok.Data;

@Data
public class SubmitOrderResponseVo {
    private OrderEntity order;
    private Integer code;//0就是成功 错误状态码

}
