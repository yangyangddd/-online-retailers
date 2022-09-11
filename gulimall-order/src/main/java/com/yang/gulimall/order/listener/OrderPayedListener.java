package com.yang.gulimall.order.listener;

import com.alipay.api.AlipayApiException;
import com.alipay.api.internal.util.AlipaySignature;
import com.yang.gulimall.order.config.AlipayTemplate;
import com.yang.gulimall.order.service.OrderService;
import com.yang.gulimall.order.vo.PayAsyncVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

@RestController
public class OrderPayedListener {
    @Autowired
    OrderService orderService;
    @Autowired
    AlipayTemplate alipayTemplate;
    @PostMapping("/payed/notify")
    public String handleAlipayed(PayAsyncVo vo,HttpServletRequest request) throws AlipayApiException, UnsupportedEncodingException {
        //验证签名
        boolean b = VerifySignature(request);
        if(b)
        {
            System.out.println("签名认证成功");
            String result=orderService.handlePayResult(vo);
            return result;
        }
        else {
            System.out.println("签名验证失败");
            return "error";
        }

    }

    private boolean VerifySignature(HttpServletRequest request) throws UnsupportedEncodingException, AlipayApiException {
        //验证签名
        Map<String,String> params = new HashMap<String,String>();
        Map<String,String[]> requestParams = request.getParameterMap();
        for (Iterator<String> iter = requestParams.keySet().iterator(); iter.hasNext();) {
            String name = (String) iter.next();
            String[] values = (String[]) requestParams.get(name);
            String valueStr = "";
            for (int i = 0; i < values.length; i++) {
                valueStr = (i == values.length - 1) ? valueStr + values[i]
                        : valueStr + values[i] + ",";
            }
            //乱码解决，这段代码在出现乱码时使用
            valueStr = new String(valueStr.getBytes("ISO-8859-1"), "utf-8");
            params.put(name, valueStr);
        }

        boolean signVerified = AlipaySignature.rsaCheckV1(params, alipayTemplate.getAlipay_public_key(), alipayTemplate.getCharset(), alipayTemplate.getSign_type()); //调用SDK验证签名

        return true;//验证成功

    }
}
