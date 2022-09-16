package com.yang.gulimall.seckill.service;

import com.yang.gulimall.seckill.to.SeckillSkuRedisTo;

import java.util.List;

public interface SeckillService {

    void uploadSeckillSkuLatest3Days();

    List<SeckillSkuRedisTo> getCurrentSeckillSkus();

    SeckillSkuRedisTo getSkuSeckillInfo(Long skuId);

    String kill(String killId, String key, Integer num);
}
