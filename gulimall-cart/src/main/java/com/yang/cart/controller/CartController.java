package com.yang.cart.controller;

import com.yang.cart.service.CartService;
import com.yang.cart.vo.Cart;
import com.yang.cart.vo.CartItem;
import com.yang.common.utils.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.concurrent.ExecutionException;

@Controller
public class CartController {
    @Autowired
    CartService cartService;
    @GetMapping("/deleteItem")
    public String deleteItem(@RequestParam("skuId") Long skuId){
        cartService.deleteItem(skuId);
        return "redirect:http://cart.gulimall.com/cart.html";
    }
    @GetMapping("/countItem")
    public String countItem(@RequestParam("skuId") Long skuId,
                            @RequestParam("num") Integer num){
        cartService.changeItemCount(skuId,num);
        return "redirect:http://cart.gulimall.com/cart.html";
    }

    @GetMapping("/checkItem")
    public String checkItem(@RequestParam("skuId") Long skuId,
                            @RequestParam("check") Integer check)
    {
        cartService.checkItem(skuId,check);
        return "redirect:http://cart.gulimall.com/cart.html";
    }

    //获取购物车的信息
    @GetMapping("/cart.html")
    public String cartListPage(Model model) throws ExecutionException, InterruptedException {
        Cart cart=cartService.getCart();
        model.addAttribute("cart",cart);
        return "cartList";
    }
    //获取所有被选中的购物车的信息
    @GetMapping("/getCheckItem")
    @ResponseBody
    public R getCheckItem()  {
        List<CartItem> cart=cartService.getCheckCart();
        if (cart==null)
            return R.error("购物车为空");
        return R.ok().put("checkCart",cart);
    }
    //添加商品至购物车
    @GetMapping("/addToCart")
    public String addToCart(@RequestParam("skuId")Long skuId,
                            @RequestParam("num")Integer num,
                            RedirectAttributes model) throws ExecutionException, InterruptedException {
      cartService.addToCart(skuId,num);
//       model.addAttribute("item",cartItem);
        model.addAttribute("skuId",skuId);
        return "redirect:http://cart.gulimall.com/addToCartSuccess.html";
    }
    //跳转到成功页
    @GetMapping("/addToCartSuccess.html")
    public String addToCartSuccessPage(@RequestParam("skuId") Long skuId,Model model)
    {

        //重定向到成功页面，再次查询购物车数据即可
        CartItem item=cartService.getCartItem(skuId);

        model.addAttribute("item",item);
        return "success";
    }


}
