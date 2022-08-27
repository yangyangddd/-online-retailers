package com.yang.gulimall.search.controller;

import com.yang.gulimall.search.service.MallSearchService;
import com.yang.gulimall.search.vo.SearchParam;
import com.yang.gulimall.search.vo.searchResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import javax.servlet.http.HttpServletRequest;

@Controller
public class SearchController {

    @Autowired
    private MallSearchService mallSearchService;
    @GetMapping("/list.html")
    public String listPage(SearchParam param, Model model, HttpServletRequest request)
    {
        String queryString = request.getQueryString();
        param.setQueryString(queryString);
        searchResult result = mallSearchService.search(param);
        model.addAttribute("result",result);
        return "list";
    }
}

