package com.yang.gulimall.order.vo;

import lombok.Data;

@Data
public class PayVo {
    private String out_trade_no; // 商户订单号 必填
    private String subject; // 订单名称 必填
    private String total_amount;  // 付款金额 必填
    private String body; // 商品描述 可空
    //   String out_trade_no = vo.getOut_trade_no();
    //        //付款金额，必填
    //        String total_amount = vo.getTotal_amount();
    //        //订单名称，必填
    //        String subject = vo.getSubject();
    //        //商品描述，可空
    //        String body = vo.getBody();
}
