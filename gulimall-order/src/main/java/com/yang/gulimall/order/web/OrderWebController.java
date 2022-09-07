package com.yang.gulimall.order.web;

import com.yang.gulimall.order.service.OrderService;
import com.yang.gulimall.order.vo.OrderConfirmVo;
import com.yang.gulimall.order.vo.OrderSubmitVo;
import com.yang.gulimall.order.vo.SubmitOrderResponseVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.concurrent.ExecutionException;

@Controller
public class OrderWebController {

    @Autowired
    OrderService orderService;
    @GetMapping("/toTrade")
    public String toTrade(Model model) throws ExecutionException, InterruptedException {
        //展示订单的数据
        OrderConfirmVo confirmVo=orderService.confirmOrder();
        model.addAttribute("orderConfirmData",confirmVo);
        return "confirm";
    }
    @PostMapping("/submitOrder")
    public String submitOrder(OrderSubmitVo vo, Model model, RedirectAttributes redirectAttributes)
    {   //去创建订单，验令牌，验价格，锁库存
        //下单成功来到支付选择页
        //下单失败回到订单确认页重新确认订单信息
        SubmitOrderResponseVo responseVo=orderService.submitOrder(vo);
        if (responseVo.getCode()==0)
        {
            model.addAttribute("submitOrderResp",responseVo);
            //下单成功来到支付选择页
            return "pay";
        }
        else {
            String msg="下单失败";
            switch (responseVo.getCode())
            {
                case 1: msg+="订单信息过期，请刷新再次提交";break;
                case 2: msg+="订单商品价格发生变化，请确认后再次提交";break;
                case 3: msg+="库存锁定失败，商品库存不足";break;

            }
            redirectAttributes.addAttribute("msg",msg);
            return "redirect:http://order.gulimall.com/toTrade";
        }
    }
}
