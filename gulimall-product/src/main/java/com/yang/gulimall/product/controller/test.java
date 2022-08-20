package com.yang.gulimall.product.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class test {
    @GetMapping("product/server")
    public String list(){

        return "hello";
    }
}
