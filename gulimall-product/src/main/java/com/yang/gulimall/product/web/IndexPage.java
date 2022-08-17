package com.yang.gulimall.product.web;

import com.yang.gulimall.product.entity.CategoryEntity;
import com.yang.gulimall.product.service.CategoryService;
import com.yang.gulimall.product.vo.Catelog2Vo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Map;

@Controller
public class IndexPage {
    @Autowired
    private CategoryService categoryService;
    @GetMapping({"/","/index.html"})
    public String indexPage(Model model)
    {
        List<CategoryEntity> list=categoryService.getLevel1Categorys();
        model.addAttribute("categorys",list);
        return "index";
    }
    @ResponseBody
    @GetMapping("index/json/catalog.json")
    public Map<String, List<Catelog2Vo>> getCatalogJson(){
        Map<String, List<Catelog2Vo>> map=  categoryService.getCatalogJson();
        return map;
    }

}
