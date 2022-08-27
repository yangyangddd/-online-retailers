package com.yang.gulimall.search.vo;

import com.yang.to.es.SkuEsModel;
import lombok.Data;

import java.util.List;

@Data
public class searchResult {
    private List<SkuEsModel> products;//查询到的所有信息
    private Integer pageNum;//当前页面
    private Integer totalPages;//总页码
    private Long total;//总记录数
    private List<BrandVo> brands;//当前查询到的结果，所有涉及到的品牌
    private List<AttrVo> attrs;//当前查询到的结果，所有涉及到的所有属性
    private List<CatalogVo> catalogs;//当前查询到的结果，所有涉及到的所有属性
    private List<Integer> pageNavs;//可遍历的页码
    //以上是返回给页面的所有信息

    //面包屑导航数据
    private List<NavVo> navs;
    @Data
    public static class NavVo
    {
        private String navName;
        private String navValue;
        private String link;
    }
    @Data
    public static class BrandVo{
        private Long brandId;
        private String brandName;
        private String brandImg;
    }
    @Data
    public static class AttrVo{
        private Long attrId;
        private String attrName;
        private List<String> attrValue;

    }
    @Data
    public static class CatalogVo{
        private Long catalogId;
        private String catalogName;
    }
}
