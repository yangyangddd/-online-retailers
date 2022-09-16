package com.yang.gulimall.seckill.controller;

import com.yang.common.utils.R;
import com.yang.gulimall.seckill.service.SeckillService;
import com.yang.gulimall.seckill.to.SeckillSkuRedisTo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class SeckillController {
    /**
     *
     * 返回当前时间可以参与的秒杀商品信息
     */
    @Autowired
    SeckillService seckillService;
    @GetMapping("/currentSeckillSkus")
    public R getCurrentSeckillSkus()
    {
       List<SeckillSkuRedisTo> vos=seckillService.getCurrentSeckillSkus();
        return R.ok().setData(vos);
    }
    @GetMapping("/sku/seckill/{skuId}")
    public R getSkuSeckillInfo(@PathVariable("skuId") Long skuId){
        SeckillSkuRedisTo to=seckillService.getSkuSeckillInfo(skuId);
        return R.ok().setData(to);
    }
    @GetMapping("/kill")
    public R seckKill(@RequestParam("killId") String killId,
                      @RequestParam("key") String key,
                      @RequestParam("num") Integer num){
        //判断是否登录
      String orderSn=seckillService.kill(killId,key,num);
        return R.ok().setData(orderSn);
    }
}
