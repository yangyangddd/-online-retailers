package com.yang.cart.service;


import com.yang.cart.vo.Cart;
import com.yang.cart.vo.CartItem;

import java.util.List;
import java.util.concurrent.ExecutionException;

public interface CartService {
    CartItem addToCart(Long skuId, Integer num) throws ExecutionException, InterruptedException;

    CartItem getCartItem(Long skuId);


    //获取购物车信息
    Cart getCart() throws ExecutionException, InterruptedException;

    void checkItem(Long skuId, Integer check);

    void changeItemCount(Long skuId, Integer num);

    void deleteItem(Long skuId);

    List<CartItem> getCheckCart();

}
