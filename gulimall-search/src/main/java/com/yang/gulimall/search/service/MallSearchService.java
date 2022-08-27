package com.yang.gulimall.search.service;

import com.yang.gulimall.search.vo.SearchParam;
import com.yang.gulimall.search.vo.searchResult;

public interface MallSearchService {
   searchResult search(SearchParam param);
}
