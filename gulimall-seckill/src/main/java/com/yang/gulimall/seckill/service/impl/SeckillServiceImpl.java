package com.yang.gulimall.seckill.service.impl;

import cn.hutool.core.date.DateUnit;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.lang.TypeReference;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.yang.common.utils.R;
import com.yang.gulimall.seckill.feign.CouponFeignService;
import com.yang.gulimall.seckill.feign.ProductFeignService;
import com.yang.gulimall.seckill.interceptor.LoginUserInterceptor;
import com.yang.gulimall.seckill.service.SeckillService;
import com.yang.gulimall.seckill.to.SeckillSkuRedisTo;
import com.yang.gulimall.seckill.vo.SeckillSessionsWithSkus;
import com.yang.gulimall.seckill.vo.SeckillSkuVo;
import com.yang.to.SkuInfoEntityTo;
import com.yang.to.mq.SeckillOrderTo;
import com.yang.vo.MemberRespVo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.redisson.api.RSemaphore;
import org.redisson.api.RedissonClient;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

@Service
@Slf4j
public class SeckillServiceImpl implements SeckillService {
    @Autowired
    RedissonClient redissonClient;
    @Autowired
    CouponFeignService couponFeignService;
    @Autowired
    ProductFeignService productFeignService;
    @Autowired
    RabbitTemplate rabbitTemplate;
    @Autowired
    StringRedisTemplate redisTemplate;
    private final String SESSION_CACHE_PREFIX="seckill:sessions:";
    private final String SKUKILL_CACHE_PREFIX="seckill:skus:";
    private final String SKU_STOCK_SEMAPHORE="seckill:stock:";//商品随机码
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

    //返回当前时间可以参与的秒杀商品信息
    @Override
    public List<SeckillSkuRedisTo> getCurrentSeckillSkus() {
//        1.确认当前时间属于哪个秒杀场次
        long time = new Date().getTime();//获取当前时间
        Set<String> keys = redisTemplate.keys(SESSION_CACHE_PREFIX + "*");
        if (keys!=null&&keys.size()>0) {
            for (String key : keys) {
                String replace = key.replace(SESSION_CACHE_PREFIX, "");
                String[] s = replace.split("_");
                long start = Long.parseLong(s[0]);
                long end = Long.parseLong(s[1]);
                if(time>=start&&time<=end)
                {
                    List<String> range = redisTemplate.opsForList().
                            range(key, 0, -1);//取出当前场次所有的商品信息
                    //        2.获取这个秒杀场次需要的所有商品信息
                    BoundHashOperations<String, Object, Object> ops = redisTemplate.boundHashOps(SKUKILL_CACHE_PREFIX);
                    if(range!=null){
                        List<SeckillSkuRedisTo> collect = range.stream().map(e ->
                        {
                            String o = (String) ops.get(e);
                            SeckillSkuRedisTo redis = JSONUtil.toBean(o, SeckillSkuRedisTo.class);
                            return redis;
                        }).collect(Collectors.toList());
                        return collect;
                    }
                    break;
                }
            }
        }
        return null;
    }
    
    @Override
    public SeckillSkuRedisTo getSkuSeckillInfo(Long skuId) {
        SeckillSessionsWithSkus lately = getLately(skuId);//找到最近该商品的场次信息
        if(lately!=null) {
            SeckillSkuVo seckillSkuVo = lately.getRelationSkus().get(0);//商品信息
            Long skuId1 = seckillSkuVo.getSkuId();
            Long id = lately.getId();//场次id
            //1.找到所有需要参与秒杀的商品key
            BoundHashOperations<String, String, String> ops = redisTemplate.boundHashOps(SKUKILL_CACHE_PREFIX);
            String s=id+"_"+skuId1;
            if(Boolean.TRUE.equals(ops.hasKey(s)))
            {
                String s1 = ops.get(s);
                SeckillSkuRedisTo redis = JSONUtil.toBean(s1, SeckillSkuRedisTo.class);
                long time = new Date().getTime();
                if(time<redis.getStartTime()&&time>redis.getEndTime())
                {
                    redis.setRandomCode(null);//未开始秒杀就不返回随机码
                }
                return redis;
            }
//   f         Set<String> keys = ops.keys();
//            if(keys!=null&& !keys.isEmpty())
//            for (String key : keys) {
//                String[] s = key.split("_");
//                if(s[0].equals(id.toString())&&s[1].equals(skuId1.toString()))//场次编号和商品编号都相同
//                {
//                    String s1 = ops.get(key);
//                    SeckillSkuRedisTo redis = JSONUtil.toBean(s1, SeckillSkuRedisTo.class);
//                    return redis;
//                }
//            }
            //以上是老师的写法


//            Set<String> keys = ops.keys();
//            if (keys != null && keys.size() > 0) {
//                for (String key : keys) {
//                    //查找当前时间最近的场次该商品的信息并返回出去
//                    String[] arr = key.split("_");
//                    if (s.equals(skuId.toString())) {
//                        String json = ops.get(s);
//                        SeckillSkuRedisTo redis = JSONUtil.toBean(json, SeckillSkuRedisTo.class);
//
//                    }
//                }
//            }
        }
        return null;
    }

    @Override
    public String kill(String killId, String key, Integer num) {
        long start = System.currentTimeMillis();
        MemberRespVo memberRespVo = LoginUserInterceptor.loginUser.get();
        //获取当前秒杀商品的详细信息
        BoundHashOperations<String, String, String> ops = redisTemplate.boundHashOps(SKUKILL_CACHE_PREFIX);
        String s = ops.get(killId);
        if(StringUtils.isEmpty(s)){
            return null;
        }
            SeckillSkuRedisTo redis = JSONUtil.toBean(s, SeckillSkuRedisTo.class);
            //校验合法性
            //秒杀时间是否过了
            Long endTime = redis.getEndTime();
            Long startTime = redis.getStartTime();
            long now = new Date().getTime();
            long ttl=endTime-startTime;//秒杀剩余时间
            if(now<startTime&&now>endTime)
            {
                return null;
            }//校验随机码
        String randomCode = redis.getRandomCode();
        String skuId = redis.getPromotionSessionId()+"_"+redis.getSkuId();
        if(!randomCode.equals(key))
        {
            return null;
        }
        //验证购物数量是否合理
        if(num>redis.getSeckillLimit().intValue())
        {
            return null;
        }
        String redisKey=memberRespVo.getId()+"_"+skuId;
        //验证这个人是否买过。幂等性
        Boolean aBoolean = redisTemplate.opsForValue().setIfAbsent(redisKey, num.toString(), ttl,
                TimeUnit.MILLISECONDS);
        if(Boolean.FALSE.equals(aBoolean)){
            //买过了
            return null;
        }
        //从来没有买过
        RSemaphore semaphore = redissonClient.getSemaphore(SKU_STOCK_SEMAPHORE + randomCode);

            boolean b = semaphore.tryAcquire(num);
            if(!b){
                return null;
            }
            //秒杀成功
            //快速下单，发送mq消息
            String timeId= IdWorker.getTimeId();
            SeckillOrderTo orderTo = new SeckillOrderTo();
            orderTo.setOrderSn(timeId);
            orderTo.setMemberId(memberRespVo.getId());
            orderTo.setNum(num);
            orderTo.setPromotionSessionId(redis.getPromotionSessionId());
            orderTo.setSkuId(redis.getSkuId());
            orderTo.setSeckillPrice(redis.getSeckillPrice());
            rabbitTemplate.convertAndSend("order-event-exchange","order.seckill.order",orderTo);
        long end = System.currentTimeMillis();
        log.info("秒杀服务总耗时="+(end-start));
        return timeId;

    }

    private SeckillSessionsWithSkus getLately(Long skuId) {


        //1.扫描最近三天需要参与秒杀的活动
        R session = couponFeignService.getLast3DaySession();

        if(session.getCode()==0) {

            //获取成功
            List<SeckillSessionsWithSkus> data = session.getData(new TypeReference<List<SeckillSessionsWithSkus>>() {
            });
            //遍历出当前产品参与所有SeckillSessionsWithSkus集合
            Date now = new Date();
            AtomicReference<Long> min= new AtomicReference<>(1000 * 60 * 60 * 24 * 3L);//该变量存储最近3天内该商品最近的秒杀活动
            List<SeckillSessionsWithSkus> collect = data.stream().filter(e ->
            {
                return now.before(e.getEndTime());//秒杀尚未开始，或在秒杀时间段都算
            })//过滤秒杀时间已过去和没有当前商品的场次
                    .peek(e ->
                    {
                        //将拥有该商品的场次商品信息设置至collect1，该collect1有且只有一个数据
                        List<SeckillSkuVo> collect1 = e.getRelationSkus().stream().filter(item ->
                        {
                            return skuId.equals(item.getSkuId());
                        }).collect(Collectors.toList());
                        e.setRelationSkus(collect1);
                        if(!collect1.isEmpty()) {//只有包含该商品才计算
                            long between = DateUtil.between(now, e.getStartTime(), DateUnit.MS);
                            if (between < min.get())
                                min.set(between);
                        }
                    }).filter(e ->
                    {
                        Long between = DateUtil.between(now, e.getStartTime(), DateUnit.MS);
                        if (between.equals(min.get()))//当前时间最短
                            return true;
                        else
                            return false;
                    }).collect(Collectors.toList());
            if(!collect.isEmpty())//不为空
            {
                SeckillSessionsWithSkus seckillSessionsWithSkus = collect.get(0);
                //集合中只有一个数据
                return seckillSessionsWithSkus;
            }
        }
        return null;
    }

    private void saveSessionInfos(List<SeckillSessionsWithSkus> sessions) {
        sessions.forEach(session->
        {
            long start = session.getStartTime().getTime();
            long end =  session.getEndTime().getTime();
            String key=SESSION_CACHE_PREFIX+start+"_"+end;
            List<String> collect = session.getRelationSkus().stream().map(e ->e.getPromotionSessionId().toString()+"_"+e.getSkuId().toString())
                    .collect(Collectors.toList());
            Boolean aBoolean = redisTemplate.hasKey(key);
            if(!collect.isEmpty()&& Boolean.FALSE.equals(aBoolean))
            redisTemplate.opsForList().leftPushAll(key,collect);
        });
    }
    private void saveSessionSkuInfos(List<SeckillSessionsWithSkus> sessions){
        //跟老师写的不一样
//        product远程服务根据List<SeckillSessionsWithSkus>将其中的商品详细信息缓存进来
        sessions.forEach(session->
        {
            BoundHashOperations<String, Object, Object> ops = redisTemplate.boundHashOps(SKUKILL_CACHE_PREFIX);
            R r = productFeignService.seckillGetSkuInfoByPromotionId(session.getRelationSkus());
            List<SkuInfoEntityTo> data = r.getData(new TypeReference<List<SkuInfoEntityTo>>() {
            });
            session.getRelationSkus().forEach(e->
            {
                if(!Boolean.TRUE.equals(ops.hasKey(e.getPromotionSessionId().toString()+"_"+e.getSkuId().toString())))//redis没有该数据才进行缓存
                {
                    SeckillSkuRedisTo redisTo=new SeckillSkuRedisTo();
                    //1.缓存商品
                    for (SkuInfoEntityTo datum : data) {
                        if(e.getSkuId().equals(datum.getSkuId()))
                        {
                            redisTo.setSkuInfo(datum);
                            break;
                        }
                    }
//                2.sku的秒杀信息
                    BeanUtils.copyProperties(e,redisTo);
//                3.设置上当前商品的秒杀信息
                    redisTo.setStartTime(session.getStartTime().getTime());
                    redisTo.setEndTime(session.getEndTime().getTime());
                    //4. 商品的随机码，防止脚本,在秒杀开始时暴露随机码
                    String token = UUID.randomUUID().toString().replace("-", "");
                    redisTo.setRandomCode(token);
                    RSemaphore semaphore = redissonClient.getSemaphore(SKU_STOCK_SEMAPHORE + token);
                    semaphore.trySetPermits(e.getSeckillCount().intValue());
                    String s = JSONUtil.toJsonStr(redisTo);
                    ops.put(e.getPromotionSessionId()+"_"+e.getSkuId().toString(),s);
                }

            });
        });
    }
}
