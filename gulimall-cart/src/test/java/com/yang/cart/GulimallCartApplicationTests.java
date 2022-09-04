package com.yang.cart;

import com.yang.cart.service.CartService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.ExecutionException;

@SpringBootTest
class GulimallCartApplicationTests {

    @Autowired
    CartService cartService;
    @Test
    void contextLoads() throws ExecutionException, InterruptedException {
//        cartService.getCart();
    }

}
