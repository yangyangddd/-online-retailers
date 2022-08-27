package com.yang.gulimall.search.service.impl;

import cn.hutool.json.JSONUtil;
import com.yang.gulimall.search.config.EsConfig;
import com.yang.gulimall.search.constant.EsConstant;
import com.yang.gulimall.search.service.ProductSaveService;
import com.yang.to.es.SkuEsModel;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.bulk.BulkItemResponse;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ProductSaveServiceImpl implements ProductSaveService {
    @Autowired
    private RestHighLevelClient restHighLevelClient;
    @Override
    public boolean productStatusUp(List<SkuEsModel> skuEsModels) throws IOException {
        //保存到es中
        //给es建立一个索引。product,建立好映射关系。
        BulkRequest bulkRequest = new BulkRequest();
        for (SkuEsModel skuEsModel : skuEsModels) {
            //构造保存请求
            IndexRequest request = new IndexRequest(EsConstant.PRODUCT_INDEX);
            request.id(skuEsModel.getSkuId().toString());
            String s = JSONUtil.toJsonStr(skuEsModel);
            request.source(s, XContentType.JSON);
            bulkRequest.add(request);
        }

        BulkResponse bulk = restHighLevelClient.bulk(bulkRequest, EsConfig.COMMON_OPTIONS);
        //TODO 1.如果批量错误
        boolean b = bulk.hasFailures();
        List<String> collect = Arrays.stream(bulk.getItems()).
                map(BulkItemResponse::getId).collect(Collectors.toList());
        log.info("商品上架成功:{}",collect);
        return b;
    }
}
