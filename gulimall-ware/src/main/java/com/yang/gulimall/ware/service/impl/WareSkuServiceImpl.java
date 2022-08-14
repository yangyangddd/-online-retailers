package com.yang.gulimall.ware.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yang.common.utils.PageUtils;
import com.yang.common.utils.Query;
import com.yang.gulimall.ware.dao.WareSkuDao;
import com.yang.gulimall.ware.entity.WareSkuEntity;
import com.yang.gulimall.ware.service.WareSkuService;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;

import java.util.Map;


@Service("wareSkuService")
public class WareSkuServiceImpl extends ServiceImpl<WareSkuDao, WareSkuEntity> implements WareSkuService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        QueryWrapper<WareSkuEntity> wrapper = new QueryWrapper<>();
      String skuId= (String) params.get("skuId");
      if(StringUtils.isNotEmpty(skuId))
      {
          wrapper.eq("sku_id",skuId);
      }
        String wareId= (String) params.get("wareId");
      if(StringUtils.isNotEmpty(wareId))
      {
          wrapper.eq("ware_id",wareId);
      }
        IPage<WareSkuEntity> page = this.page(
                new Query<WareSkuEntity>().getPage(params),
                wrapper
        );

        return new PageUtils(page);
    }

}