package com.yang.gulimall.search;

import cn.hutool.json.JSONUtil;
import com.yang.gulimall.search.config.EsConfig;
import com.yang.gulimall.search.pojo.bank;
import lombok.Data;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.aggregations.metrics.Avg;
import org.elasticsearch.search.aggregations.metrics.AvgAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.query.Query;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@SpringBootTest
class GulimallSearchApplicationTests {

    @Autowired
    private RestHighLevelClient restHighLevelClient;
    @Autowired
    private ElasticsearchRestTemplate template;
    @Test
    void contextLoads() {
        System.out.println(template);
    }
    @Test
    public void indexData() throws IOException {
//        IndexRequest indexRequest=new IndexRequest("users");
//        indexRequest.id("1");
//        user user = new user();
//        user.setAge(9);
//        user.setUsername("yang");
//        user.setGender("f");
//        user.setId("1");
//        user user = template.get("1", user.class);
//        String delete = template.delete("1", user.class);
//        System.out.println(delete);
//        user save = template.save(user);
//        System.out.println(user);

//        String s = JSONUtil.toJsonStr(user);
//        indexRequest.source(s, XContentType.JSON);
//        System.out.println(restHighLevelClient.index(indexRequest, EsConfig.COMMON_OPTIONS));
    }
    @Data
    class user{
        private String id;
        private String username;
        private String gender;
        private Integer age;
    }

    @Test
    public void bankTest()
    {

        Query query = template.matchAllQuery();

        List<String> fields=new ArrayList<>();
        fields.add("address");
        fields.add("balance");
        fields.add("age");
        query.setFields(fields);
        List<Object> searchAfter = query.getSearchAfter();
        if(searchAfter!=null)
        System.out.println(searchAfter.size());
    }
    @Test
    public void bankTest2() throws IOException {
        SearchRequest request = new SearchRequest();
        request.indices("bank");
        SearchSourceBuilder builder=new SearchSourceBuilder();
        builder.query(QueryBuilders.matchQuery("address","mill"));
        TermsAggregationBuilder ageAgg = AggregationBuilders.terms("ageAgg").field("age").size(10);
        AvgAggregationBuilder balanceAvg = AggregationBuilders.avg("balanceAvg").field("balance");
        builder.aggregation(ageAgg);
        builder.aggregation(balanceAvg);
        System.out.println(builder.toString());
        System.out.println("...................................................");
        request.source(builder);
        SearchResponse search = restHighLevelClient.search(request, EsConfig.COMMON_OPTIONS);
        System.out.println(search.toString());
        SearchHits hits = search.getHits();
        SearchHit[] hitsHits = hits.getHits();
        List<bank> list=new ArrayList<>();
        for (SearchHit hitsHit : hitsHits) {
            String sourceAsString = hitsHit.getSourceAsString();
            bank bank = JSONUtil.toBean(sourceAsString, bank.class);
            list.add(bank);
        }
        list.forEach(System.out::println);
        Aggregations aggregations = search.getAggregations();
        Terms ageAgg1 = aggregations.get("ageAgg");
        for (Terms.Bucket bucket : ageAgg1.getBuckets()) {
            String keyAsString = bucket.getKeyAsString();
            System.out.println("age"+keyAsString);
        }
        Avg balanceAvg1 = aggregations.get("balanceAvg");
        System.out.println("平均薪资"+balanceAvg1.getValue());

    }
}
