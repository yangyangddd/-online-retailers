package com.yang.gulimall.coupon.service.impl;

import cn.hutool.core.date.LocalDateTimeUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yang.common.utils.PageUtils;
import com.yang.common.utils.Query;
import com.yang.gulimall.coupon.dao.SeckillSessionDao;
import com.yang.gulimall.coupon.entity.SeckillSessionEntity;
import com.yang.gulimall.coupon.entity.SeckillSkuRelationEntity;
import com.yang.gulimall.coupon.service.SeckillSessionService;
import com.yang.gulimall.coupon.service.SeckillSkuRelationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service("seckillSessionService")
public class SeckillSessionServiceImpl extends ServiceImpl<SeckillSessionDao, SeckillSessionEntity> implements SeckillSessionService {

    @Autowired
    SeckillSkuRelationService seckillSkuRelationService;
    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SeckillSessionEntity> page = this.page(
                new Query<SeckillSessionEntity>().getPage(params),
                new QueryWrapper<SeckillSessionEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public List<SeckillSessionEntity> getLast3DaySession() {;
        //自动上架3天内上架的商品
        //计算最近三天
        List<SeckillSessionEntity> list = this.list(new QueryWrapper<SeckillSessionEntity>()
                .between("start_time", startTime(), endTime()));
        if(list!=null&&list.size()>0)
        {
            List<SeckillSessionEntity> promotion_session_id = list.stream().peek(e ->
            {
                e.setRelationSkus(seckillSkuRelationService.list(new QueryWrapper<SeckillSkuRelationEntity>()
                        .eq("promotion_session_id", e.getId())));
            }).collect(Collectors.toList());
            return promotion_session_id;
        }
        return list;
    }

    private String startTime()
    {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime start = LocalDateTimeUtil.beginOfDay(now);
        return LocalDateTimeUtil.formatNormal(start);
    }
    private String endTime()
    {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime offset = LocalDateTimeUtil.offset(now, 2, ChronoUnit.DAYS);
        LocalDateTime end = LocalDateTimeUtil.beginOfDay(offset);
        return LocalDateTimeUtil.formatNormal(end);
    }

}