package com.yang.gulimall.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yang.common.utils.PageUtils;
import com.yang.common.utils.Query;
import com.yang.gulimall.product.dao.SkuInfoDao;
import com.yang.gulimall.product.entity.SkuInfoEntity;
import com.yang.gulimall.product.service.SkuInfoService;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;


@Service("skuInfoService")
public class SkuInfoServiceImpl extends ServiceImpl<SkuInfoDao, SkuInfoEntity> implements SkuInfoService {



    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        QueryWrapper<SkuInfoEntity> wrapper = new QueryWrapper<>();
        String key= (String) params.get("key");
        String catelogId=(String)params.get("catelogId");
        String brandId=(String)params.get("brandId");
        String max=(String)params.get("max");
        String min=(String)params.get("min");
        if(!StringUtils.isEmpty(key))
        {
            wrapper.and(w->
            {
                w.eq("sku_id",key).or().like("sku_name",key);
            });
        }
        if(!StringUtils.isEmpty(catelogId)&&!"0".equals(catelogId))
        {
            wrapper.eq("catalog_id",catelogId);
        }
        if(!StringUtils.isEmpty(brandId)&&!"0".equals(brandId))
        {
            wrapper.eq("brand_id",brandId);
        }
        if(!StringUtils.isEmpty(max))
        {
            try {
                BigDecimal bigDecimal = new BigDecimal(max);
                if(bigDecimal.compareTo(new BigDecimal("0"))==1)
                wrapper.le("price",max);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if(!StringUtils.isEmpty(min))
        {
            wrapper.ge("price",min);
        }
                IPage<SkuInfoEntity> page = this.page(
                new Query<SkuInfoEntity>().getPage(params),
                wrapper
        );
        return new PageUtils(page);
    }

    //根据spuid得到所有的sku信息
    @Override
    public List<SkuInfoEntity> getSkusBySpuId(Long spuId) {
        return this.list(new QueryWrapper<SkuInfoEntity>().eq("spu_id", spuId));

    }


}