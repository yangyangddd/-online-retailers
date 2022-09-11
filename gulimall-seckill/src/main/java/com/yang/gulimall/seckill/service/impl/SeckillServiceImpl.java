package com.yang.gulimall.seckill.service.impl;

import cn.hutool.core.lang.TypeReference;
import cn.hutool.json.JSONUtil;
import com.yang.common.utils.R;
import com.yang.gulimall.seckill.feign.CouponFeignService;
import com.yang.gulimall.seckill.service.SeckillService;
import com.yang.gulimall.seckill.to.SeckillSkuRedisTo;
import com.yang.gulimall.seckill.vo.SeckillSessionsWithSkus;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SeckillServiceImpl implements SeckillService {
    @Autowired
    CouponFeignService couponFeignService;
    @Autowired
    StringRedisTemplate redisTemplate;
    private final String SESSION_CACHE_PREFIX="seckill:sessions:";
    private final String SKUKILL_CACHE_PREFIX="seckill:skus:";
    @Override
    public void uploadSeckillSkuLatest3Days() {
        //1.扫描最近三天需要参与秒杀的活动
        R session = couponFeignService.getLast3DaySession();
        if(session.getCode()==0)
        {
            //获取成功，上架商品
            List<SeckillSessionsWithSkus> data = session.getData(new TypeReference<List<SeckillSessionsWithSkus>>() {
            });
            //缓存至redis
//            1.缓存活动信息
            saveSessionInfos(data);
//            2.缓存活动的关联商品信息
            saveSessionSkuInfos(data);
        }
    }

    private void saveSessionInfos(List<SeckillSessionsWithSkus> sessions) {
        sessions.forEach(session->
        {
            long start = session.getStartTime().getTime();
            long end =  session.getEndTime().getTime();
            String key=SESSION_CACHE_PREFIX+start+"_"+end;
            List<String> collect = session.getRelationSkus().stream().map(e -> e.getSkuId().toString())
                    .collect(Collectors.toList());
            redisTemplate.opsForList().leftPushAll(key,collect);
        });
    }
    private void saveSessionSkuInfos(List<SeckillSessionsWithSkus> sessions){
        sessions.forEach(session->
        {
            BoundHashOperations<String, Object, Object> ops = redisTemplate.boundHashOps(SKUKILL_CACHE_PREFIX);
            session.getRelationSkus().forEach(e->
            {
                SeckillSkuRedisTo redisTo=new SeckillSkuRedisTo();
                //1.缓存商品
                
//                2.sku的秒杀信息
                BeanUtils.copyProperties(e,redisTo);
//                3.
                String s = JSONUtil.toJsonStr(redisTo);
                ops.put(e.getId(),s);
            });
        });
    }
}
