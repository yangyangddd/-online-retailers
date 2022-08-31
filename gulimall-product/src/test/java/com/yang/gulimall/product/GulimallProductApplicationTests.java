package com.yang.gulimall.product;

import com.yang.gulimall.product.entity.BrandEntity;
import com.yang.gulimall.product.service.BrandService;
import com.yang.gulimall.product.service.SpuInfoService;
import org.junit.jupiter.api.Test;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.concurrent.TimeUnit;

@SpringBootTest
class GulimallProductApplicationTests {

    @Autowired
    SpuInfoService spuInfoService;
    @Autowired
    BrandService brandService;
    @Autowired
    private StringRedisTemplate template;
    @Autowired
    private RedissonClient redissonClient;
    @Test
    void contextLoads() {
        BrandEntity brandEntity = new BrandEntity();
        brandEntity.setName("苹果");
        brandEntity.setBrandId(6L);
        brandService.updateById(brandEntity);
    }
    @Test
    void test2()
    {
        String num = template.opsForValue().get("num");
        System.out.println(num);
    }
    @Test
    void test3() throws InterruptedException {
        RLock lock = redissonClient.getLock("lock");
        System.out.println(redissonClient);
        lock.lock(10L, TimeUnit.SECONDS);
        Thread.sleep(30000);
        lock.unlock();
    }
    @Test
    void add()
    {
        spuInfoService.up(13L);
    }

}
