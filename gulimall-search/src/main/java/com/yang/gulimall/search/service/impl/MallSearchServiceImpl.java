package com.yang.gulimall.search.service.impl;

import cn.hutool.json.JSONUtil;
import com.yang.gulimall.search.config.EsConfig;
import com.yang.gulimall.search.constant.EsConstant;
import com.yang.gulimall.search.service.MallSearchService;
import com.yang.gulimall.search.vo.SearchParam;
import com.yang.gulimall.search.vo.searchResult;
import com.yang.to.es.SkuEsModel;
import org.apache.commons.lang.StringUtils;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.NestedQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.nested.NestedAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.nested.ParsedNested;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedLongTerms;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedStringTerms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MallSearchServiceImpl implements MallSearchService {

    @Autowired
    private RestHighLevelClient restHighLevelClient;
    @Override
    public searchResult search(SearchParam param) {
        SearchRequest request=buildSearchRequest(param);
        searchResult result=null;
        try {
            SearchResponse response=restHighLevelClient.search(request, EsConfig.COMMON_OPTIONS);
            result=buildSearchResult(response,param);
        } catch (IOException e) {
            e.printStackTrace();
        }
         return result;

    }
    //准备检索请求
    private SearchRequest buildSearchRequest(SearchParam param) {
        SearchSourceBuilder searchSourceBuilder=new SearchSourceBuilder();
        //查询：模糊匹配，过滤（按照属性，分类，品牌，价格区间，库存）
        BoolQueryBuilder queryBuilder= QueryBuilders.boolQuery();
        //1.must
        if(StringUtils.isNotEmpty(param.getKeyword())){
            queryBuilder.must(QueryBuilders.matchQuery("skuTitle",param.getKeyword()));
        }
        //2.filter
        //分类
        if(param.getCatalog3Id()!=null){
            queryBuilder.filter(QueryBuilders.termQuery("catalogId",param.getCatalog3Id()));
        }
        //品牌
        if(param.getBrandId()!=null&&param.getBrandId().size()>0)
        {
            queryBuilder.filter(QueryBuilders.termsQuery("brandId",param.getBrandId()));
        }
        //属性
        if(param.getAttrs()!=null&&param.getAttrs().size()>0)
        {
            for (String attr : param.getAttrs()) {
                BoolQueryBuilder nestedBoolQuery = QueryBuilders.boolQuery();
                String[] s = attr.split("_");
                String attrId=s[0];
                String[] attrValues=s[1].split(":");
                nestedBoolQuery.must(QueryBuilders.termQuery("attrs.attrId",attrId));
                nestedBoolQuery.must(QueryBuilders.termsQuery("attrs.attrValue",attrValues));
                NestedQueryBuilder nestedQueryBuilder=QueryBuilders.nestedQuery("attrs",nestedBoolQuery,ScoreMode.None);
                queryBuilder.filter(nestedQueryBuilder);
            }
        }
        //是否有库存
        if(param.getHasStock()!=null)
        {
            queryBuilder.filter(QueryBuilders.termQuery("hasStock",param.getHasStock()==1));
        }
        //按照价格区间
        if(StringUtils.isNotEmpty(param.getSkuPrice()))
        {
            RangeQueryBuilder skuPrice = QueryBuilders.rangeQuery("skuPrice");
            String[] s = param.getSkuPrice().split("_");
            if(s.length==2)
            {
                if("".equals(s[0]))
                {
                    skuPrice.lte(s[1]);
                }
                else {
                    skuPrice.gte(s[0]).lte(s[1]);
                }
            }
            else if(s.length==1)
            {
                skuPrice.gte(s[0]);
            }
            queryBuilder.filter(skuPrice);
        }
        searchSourceBuilder.query(queryBuilder);
        //排序，分页，高亮
        //排序
        if(StringUtils.isNotEmpty(param.getSort()))
        {
            String sort = param.getSort();
            String[] s = sort.split("_");
            searchSourceBuilder.sort(s[0],SortOrder.fromString(s[1]));
        }
        //分页
//        02 22 42
        if(param.getPageNum()==null) {
        param.setPageNum(1);
        }
        searchSourceBuilder.from((param.getPageNum()-1)*EsConstant.PRODUCT_PAGESIZE);
        searchSourceBuilder.size(EsConstant.PRODUCT_PAGESIZE);


        //高亮
        HighlightBuilder highlightBuilder=new HighlightBuilder();
        highlightBuilder.field("skuTitle").preTags("<b style='color:red'>").postTags("</b>");
        searchSourceBuilder.highlighter(highlightBuilder);
        //聚合


        //brand_agg
        TermsAggregationBuilder brandAgg = AggregationBuilders.terms("brand_agg").field("brandId").size(10);
        brandAgg.subAggregation(AggregationBuilders.terms("brand_name_agg").field("brandName").size(10));
        brandAgg.subAggregation(AggregationBuilders.terms("brand_img_agg").field("brandImg").size(10));
        searchSourceBuilder.aggregation(brandAgg);
        //catalog_agg
        TermsAggregationBuilder catalogAgg = AggregationBuilders.terms("catalog_agg").field("catalogId").size(10).subAggregation(
                AggregationBuilders.terms("catalog_name").field("catalogName").size(10)
        );
        searchSourceBuilder.aggregation(catalogAgg);
        //attr_agg
        TermsAggregationBuilder attr_id_agg = AggregationBuilders.terms("attr_id_agg").field("attrs.attrId").size(10);
        attr_id_agg.subAggregation(AggregationBuilders.terms("attr_name_agg").field("attrs.attrName").size(10));
        attr_id_agg.subAggregation(AggregationBuilders.terms("attr_value_agg").field("attrs.attrValue").size(10));
        NestedAggregationBuilder nested = AggregationBuilders.nested("attr_agg", "attrs").subAggregation(attr_id_agg);
        searchSourceBuilder.aggregation(nested);
                String s=searchSourceBuilder.toString();
        System.out.println("构建的dsl语句"+s);
        SearchRequest searchRequest=new SearchRequest(new String[]{EsConstant.PRODUCT_INDEX},searchSourceBuilder);
        return searchRequest;
    }
    private searchResult buildSearchResult(SearchResponse response,SearchParam param) {
        searchResult result=new searchResult();
        //1.返回所有查询到的商品
        SearchHits hits = response.getHits();
        List<SkuEsModel> skuEsModelList=new ArrayList<>();
        if(hits.getHits()!=null&&hits.getHits().length>0)
        for (SearchHit hit : hits.getHits()) {
            String sourceAsString = hit.getSourceAsString();
            SkuEsModel esModel = JSONUtil.toBean(sourceAsString, SkuEsModel.class);
            if(StringUtils.isNotEmpty(param.getKeyword()))
            { HighlightField skuTitle=hit.getHighlightFields().get("skuTitle");
            String string = skuTitle.getFragments()[0].string();
            esModel.setSkuTitle(string);
            }
            skuEsModelList.add(esModel);
        }
        result.setProducts(skuEsModelList);
        //2.当前所有商品涉及到的所有属性信息
        List<searchResult.AttrVo> attrVoList=new ArrayList<>();
        ParsedNested attr_agg = response.getAggregations().get("attr_agg");
        ParsedLongTerms attr_id_agg = attr_agg.getAggregations().get("attr_id_agg");
        for (Terms.Bucket bucket : attr_id_agg.getBuckets()) {
            searchResult.AttrVo attrVo = new searchResult.AttrVo();
            //属性的id
            long attrId = bucket.getKeyAsNumber().longValue();
            //属性的名字
            ParsedStringTerms attr_name_agg = bucket.getAggregations().get("attr_name_agg");
            String attrName = attr_name_agg.getBuckets().get(0).getKeyAsString();
            //属性的所有值
            ParsedStringTerms attr_value_agg = bucket.getAggregations().get("attr_value_agg");
            List<String> values = attr_value_agg.getBuckets().stream().map(e -> e.getKeyAsString()).collect(Collectors.toList());
             attrVo.setAttrId(attrId);
             attrVo.setAttrName(attrName);
             attrVo.setAttrValue(values);
             attrVoList.add(attrVo);
        }
        result.setAttrs(attrVoList);
        //3.当前所有商品涉及到的所有品牌信息
        List<searchResult.BrandVo> brandVoList=new ArrayList<>();
        ParsedLongTerms brand_agg = response.getAggregations().get("brand_agg");
        for (Terms.Bucket bucket : brand_agg.getBuckets()) {
            searchResult.BrandVo brandVo = new searchResult.BrandVo();
            //得到品牌的id
            long brandId = bucket.getKeyAsNumber().longValue();
            brandVo.setBrandId(brandId);
            //得到品牌的名字
            ParsedStringTerms brand_name_agg = bucket.getAggregations().get("brand_name_agg");
            String brand_name = brand_name_agg.getBuckets().get(0).getKeyAsString();
            brandVo.setBrandName(brand_name);
            //得到品牌的图片
            ParsedStringTerms brand_img_agg = bucket.getAggregations().get("brand_img_agg");
            String brand_img = brand_img_agg.getBuckets().get(0).getKeyAsString();
            brandVo.setBrandImg(brand_img);
            brandVoList.add(brandVo);
        }
        result.setBrands(brandVoList);
        //4.当前所有商品涉及到的所有分类信息
        ParsedLongTerms catalog_agg = response.getAggregations().get("catalog_agg");
        List<searchResult.CatalogVo> catalogVos=new ArrayList<>();
        for (Terms.Bucket bucket : catalog_agg.getBuckets()) {
            searchResult.CatalogVo catalogVo = new searchResult.CatalogVo();
            //得到分类id
            String keyAsString = bucket.getKeyAsString();
            catalogVo.setCatalogId(Long.parseLong(keyAsString));
            //获取子聚合
            ParsedStringTerms catalog_name_agg = bucket.getAggregations().get("catalog_name");
            String catalog_name = catalog_name_agg.getBuckets().get(0).getKeyAsString();
            catalogVo.setCatalogName(catalog_name);
            catalogVos.add(catalogVo);
        }
        result.setCatalogs(catalogVos);

        //5.分页信息
        if(param.getPageNum()!=null)
        {
            result.setPageNum(param.getPageNum());
        }
        //6.总记录数
        long total = hits.getTotalHits().value;
        result.setTotal(total);
        //7.总页码
        int totalPage = (int) (total - 1) / EsConstant.PRODUCT_PAGESIZE + 1;
        result.setTotalPages(totalPage);
        List<Integer> pageNavs=new ArrayList<>();//构建可遍历的页码
        Integer pageNum = param.getPageNum();//当前所处页
    if(pageNum!=null) {
        if (totalPage <= EsConstant.PAGELIMIT) {
            for (int i = 1; i <= totalPage; i++) {
                pageNavs.add(i);
            }
        } else if (pageNum < EsConstant.PAGELIMIT / 2 + 2)//前EsConstant.PAGELIMIT页显示前PAGELIMIT页
        {
            for (int i = 1; i <= EsConstant.PAGELIMIT; i++) {
                pageNavs.add(i);
            }
        } else if (pageNum + EsConstant.PAGELIMIT / 2 + 1 > totalPage) {//最后五页，显示最后五页
            for (int i = totalPage - EsConstant.PAGELIMIT + 1; i <= totalPage; i++) {
                pageNavs.add(i);
            }
        } else {//中间的情况
            for (int i = pageNum; i <= pageNum + EsConstant.PAGELIMIT; i++) {
                pageNavs.add(i);
            }
        }
    }
        result.setPageNavs(pageNavs);
        //构建面包屑导航
        if(param.getAttrs()!=null&&param.getAttrs().size()>0){
            List<searchResult.NavVo> navVo =
                    param.getAttrs().stream().map(attr->
                            {
                                searchResult.NavVo navVo1=new searchResult.NavVo();
                                String []s=attr.split("_");
                                navVo1.setNavValue(s[1]);
                                for (SkuEsModel esModel : result.getProducts()) {
                                    for (SkuEsModel.Attrs esModelAttr : esModel.getAttrs()) {
                                        if(esModelAttr.getAttrId()==Long.parseLong(s[0])){
                                            navVo1.setNavName(esModelAttr.getAttrName());
                                            break;
                                        }
                                    }
                                    if(navVo1.getNavName()!=null)
                                        break;
                                }
                                //拿到所有的查询条件，去掉当前.
                                String attr1=null;
                                try {
                                     attr1= URLEncoder.encode(attr, "UTF-8");
                                     attr1=attr1.replace("+","%20");//浏览器对空格编码和java不一样
                                } catch (UnsupportedEncodingException e) {
                                    e.printStackTrace();
                                }
                                String replace = param.getQueryString().replace("&attrs=" + attr1, "");
                                navVo1.setLink("http://search.gulimall.com/list.html?"+replace);
                                return navVo1;
                            }
                    ).collect(Collectors.toList());
            result.setNavs(navVo);
        }
        if(param.getBrandId()!=null&&param.getBrandId().size()>0)
        {

        }
        System.out.println("查询出的数据："+result);
        return result;
    }


}
