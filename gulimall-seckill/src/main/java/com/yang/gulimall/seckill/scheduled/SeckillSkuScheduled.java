package com.yang.gulimall.seckill.scheduled;

import com.yang.gulimall.seckill.service.SeckillService;
import lombok.extern.slf4j.Slf4j;
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
    @Scheduled(cron = "0 0 3 * * ?")
    public void uploadSeckillSkuLatest3Days()
    {
        seckillService.uploadSeckillSkuLatest3Days();
    }

}
