package com.yang.gulimall.seckill.scheduled;

import com.yang.gulimall.seckill.service.SeckillService;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@Slf4j
//秒杀商品的定时上架
//每天晚上3点；上架最近三天需要秒杀的商品
//当天00:00:00 -23:59:59
//明天00:00:00 -23:59:59
//后天00:00:00 -23:59:59
public class SeckillSkuScheduled {
    @Autowired
    SeckillService seckillService;
    @Autowired
    RedissonClient redissonClient;
    private final String upload_lock="seckill:upload:lock:";
    @Scheduled(cron = "0 * * * * ?")
    //TODO 秒杀服务的幂等性处理
    //TODO 1.使用分布式锁解决多台机器同时执行该定时任务是的线程安全问题
    public void uploadSeckillSkuLatest3Days()
    {
        RLock lock = redissonClient.getLock(upload_lock);
        lock.lock();
        try {
            seckillService.uploadSeckillSkuLatest3Days();
        } finally {
            lock.unlock();
        }

    }

}
