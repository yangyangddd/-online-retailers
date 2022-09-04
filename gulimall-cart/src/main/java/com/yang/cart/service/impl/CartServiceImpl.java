package com.yang.cart.service.impl;

import cn.hutool.core.lang.TypeReference;
import cn.hutool.json.JSONUtil;
import com.yang.cart.To.UserInfoTo;
import com.yang.cart.feign.ProductFeignService;
import com.yang.cart.interceptor.CartInterceptor;
import com.yang.cart.service.CartService;
import com.yang.cart.vo.Cart;
import com.yang.cart.vo.CartItem;
import com.yang.cart.vo.SkuInfoVo;
import com.yang.common.utils.R;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

@Service
public class CartServiceImpl implements CartService {
    @Autowired
    StringRedisTemplate redisTemplate;
    @Autowired
    ProductFeignService productFeignService;
    @Autowired
    ThreadPoolExecutor executor;
    private final String CART_PREFIX="gulimall:cart:";
    //完成添加购物车的功能
    @Override
    public CartItem addToCart(Long skuId, Integer num) throws ExecutionException, InterruptedException {
        BoundHashOperations<String, Object, Object> operations = getCartOps();

       String res= (String)operations.get(skuId.toString());
        CartItem cartItem;
        if(StringUtils.isNotEmpty(res))
       {

           cartItem = JSONUtil.toBean(res, CartItem.class);
           cartItem.setCount(cartItem.getCount()+num);
           operations.put(skuId.toString(),JSONUtil.toJsonStr(cartItem));
           //购物车有此商品
       }
       else {
            cartItem = new CartItem();
           CompletableFuture<Void> getSkuInfoTask = CompletableFuture.runAsync(() ->
           {

               //1.远程查询当前要添加商品的信息
               R skuInfo = productFeignService.getSkuInfo(skuId);
               SkuInfoVo data = skuInfo.getData("skuInfo", new TypeReference<SkuInfoVo>() {
               });
               //2.新商品添加到购物车

               cartItem.setCheck(true);
               cartItem.setCount(num);
               cartItem.setImage(data.getSkuDefaultImg());
               cartItem.setTitle(data.getSkuTitle());
               cartItem.setSkuId(skuId);
               cartItem.setPrice(data.getPrice());
           }, executor);
           //3.远程查出sku组合信息
           CompletableFuture<Void> getSkuSaleAttrValues = CompletableFuture.runAsync(() -> {
               List<String> skuSaleAttrValues = productFeignService.getSkuSaleAttrValues(skuId);
               cartItem.setSkuAttr(skuSaleAttrValues);
           }, executor);
           CompletableFuture.allOf(getSkuInfoTask, getSkuSaleAttrValues).get();
           String s = JSONUtil.toJsonStr(cartItem);
           operations.put(skuId.toString(), s);
        }
        return cartItem;
    }

    @Override
    public CartItem getCartItem(Long skuId) {
        BoundHashOperations<String, Object, Object> cartOps = getCartOps();
        String res = (String) cartOps.get(skuId.toString());
        CartItem cartItem = JSONUtil.toBean(res, CartItem.class);

        return cartItem;
    }

    @Override
    public Cart getCart() throws ExecutionException, InterruptedException {
        Cart cart = new Cart();
        UserInfoTo userInfoTo = CartInterceptor.toThreadLocal.get();
        String cartKey = "";
        List<CartItem> cartItems;
        //未登录就获取当前临时用户的购物车
        if (userInfoTo.getUserId() != null) {
            //用户已登录
            cartKey = CART_PREFIX + userInfoTo.getUserId();

            //判断是否有有之前临时用户存在
            List<CartItem> cartItems1 = getCartItems(CART_PREFIX+userInfoTo.getUserKey());
            if(cartItems1!=null)
            {
                //有临时用户
                //合并购物车
                for (CartItem cartItem : cartItems1) {
                    addToCart(cartItem.getSkuId(),cartItem.getCount());
                }
                //清空临时购物车信息
                redisTemplate.delete(CART_PREFIX+userInfoTo.getUserKey());

            }

        } else {
            //用户未登录
            cartKey=CART_PREFIX+userInfoTo.getUserKey();

        }
        cartItems = getCartItems(cartKey);
        cart.setItems(cartItems);
        cart.setReduce(new BigDecimal(0));
        cart.getTotalAmount();
        return cart;
    }

    @Override
    public void checkItem(Long skuId, Integer check) {
        BoundHashOperations<String, Object, Object> cartOps = getCartOps();
        CartItem cartItem = getCartItem(skuId);
        cartItem.setCheck(check == 1);
        String s = JSONUtil.toJsonStr(cartItem);
        cartOps.put(skuId.toString(),s);


    }

    //更改购物车数量
    @Override
    public void changeItemCount(Long skuId, Integer num) {
        CartItem cartItem = getCartItem(skuId);
        cartItem.setCount(num);
        BoundHashOperations<String, Object, Object> cartOps = getCartOps();
        cartOps.put(skuId.toString(),JSONUtil.toJsonStr(cartItem));
    }

    @Override
    public void deleteItem(Long skuId) {
        BoundHashOperations<String, Object, Object> cartOps = getCartOps();
        cartOps.delete(skuId.toString());
    }

    //获取所有被选中的所有条目
    @Override
    public List<CartItem> getCheckCart()  {

        UserInfoTo userInfoTo = CartInterceptor.toThreadLocal.get();
        List<CartItem> cartItems = getCartItems(CART_PREFIX+userInfoTo.getUserId());
        if(cartItems==null)
        {
            return null;//购物车没数据
        }
        List<CartItem> collect = cartItems.stream().

                filter(CartItem::getCheck).
                collect(Collectors.toList());
        //更新加入购物车前后的价格
        List<Long> collect1 = collect.stream().map(CartItem::getSkuId).collect(Collectors.toList());
        Map<Long,BigDecimal> NewPrices= productFeignService.getNewPrice(collect1);
        List<CartItem> collect2 = collect.stream().peek(e ->
        {
            if (NewPrices.containsKey(e.getSkuId())) {
                e.setPrice(NewPrices.get(e.getSkuId()));
            }
        }).collect(Collectors.toList());
        return collect2;
    }

    //获取我们要操作的购物车
    private BoundHashOperations<String, Object, Object> getCartOps() {
        UserInfoTo userInfoTo = CartInterceptor.toThreadLocal.get();
        String cartKey ="";
        if(userInfoTo.getUserId()!=null)
        {
            //用户已登录
            cartKey=CART_PREFIX+userInfoTo.getUserId();
        }
        else {
            //用户未登录
            cartKey=CART_PREFIX+userInfoTo.getUserKey();
        }
        BoundHashOperations<String, Object, Object> operations=
                redisTemplate.boundHashOps(cartKey);
        //绑定一个键，使用operations完成对cartKey的增删改查
        return operations;
    }
    private List<CartItem> getCartItems(String key){

            BoundHashOperations<String, Object, Object> cartOps = redisTemplate.boundHashOps(key);
                List<Object> values = cartOps.values();
                List<CartItem> collect=null;
                if(values!=null&&values.size()>0) {
                    collect = values.stream().map(e ->
                    {
                        String s = JSONUtil.toJsonStr(e);
                        return JSONUtil.toBean(s, CartItem.class);
                    }).collect(Collectors.toList());
                }


            //绑定一个键，使用operations完成对cartKey的增删改查
            return collect;
        }


}
