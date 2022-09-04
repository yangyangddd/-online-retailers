package com.yang.gulimall.product.web;

import com.yang.gulimall.product.service.SkuInfoService;
import com.yang.gulimall.product.vo.SkuItemVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class ItemController {
    @Autowired
    private SkuInfoService skuInfoService;
    @GetMapping("/{skuId}.html")
    public String skuItem(@PathVariable("skuId") Long skuId, Model model)
    {
        System.out.println("准备查询+"+skuId+"详情");
        SkuItemVo vo=skuInfoService.item(skuId);
        model.addAttribute("item",vo);
        //因为前端不支持long数组序列化（自己找不到解决方案),将其转换成字符串
        return "item";
    }

}
