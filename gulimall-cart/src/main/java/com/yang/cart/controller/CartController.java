package com.yang.cart.controller;

import com.yang.cart.To.UserInfoTo;
import com.yang.cart.interceptor.CartInterceptor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import javax.servlet.http.HttpSession;

@Controller
public class CartController {
    @GetMapping("/cart.html")
    public String cartListPage(HttpSession session){

        UserInfoTo userInfoTo = CartInterceptor.toThreadLocal.get();
        System.out.println(userInfoTo);
        return "cartList";
    }
    //添加商品至购物车
    @GetMapping("/addToCart")
    public String addToCart(){

        return "success";

    }
}
