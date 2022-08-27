package com.yang.gulimall.search.vo;

import lombok.Data;

import java.util.List;

//封装页面所有可能传递过来的查询条件
@Data
public class SearchParam {
    private String keyword;//页面传递过来的全文匹配关键字
    private Long catalog3Id;//三级分类id
    /**
     * sort=saleCount_asc/desc
     * sort=skuPrice_asc/desc
     * sort=hotScorePrice_asc/desc
     */
    private String sort;//排序条件
    /**
     * 过滤条件
     * hasStock(是否有货)，skuPrice区间、brandId、catalog3Id、attrs
//     * hasStock=0/1
//     * skuPrice=1-500/_500/500_
     */
    private Integer hasStock;//是否有货
    private String skuPrice;
    private List<Long> brandId;//按照品盘进行查询，可以多选
    /**
     * &attrs=1_5寸：8寸&2_16G:8G
     */
    private List<String> attrs;//按照属性进行筛选
    private Integer pageNum;//页码
    private String queryString;//查询字符串
}
