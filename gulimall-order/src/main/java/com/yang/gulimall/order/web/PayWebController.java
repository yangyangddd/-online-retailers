package com.yang.gulimall.order.web;

import com.alipay.api.AlipayApiException;
import com.yang.gulimall.order.config.AlipayTemplate;
import com.yang.gulimall.order.service.OrderService;
import com.yang.gulimall.order.vo.PayVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class PayWebController {
    @Autowired
    AlipayTemplate alipayTemplate;
    @Autowired
    OrderService orderService;
    @GetMapping(value = "/payOrder",produces = "text/html")
    @ResponseBody
    public String payOrder(@RequestParam String orderSn) throws AlipayApiException {
        PayVo payVo=orderService.getOrderPay(orderSn);
        String pay = alipayTemplate.pay(payVo);
//        System.out.println(pay);
        return pay;
    }
}
