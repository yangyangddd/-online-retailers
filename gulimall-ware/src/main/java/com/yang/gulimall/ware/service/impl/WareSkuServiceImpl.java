package com.yang.gulimall.ware.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yang.common.utils.PageUtils;
import com.yang.common.utils.Query;
import com.yang.gulimall.ware.dao.WareSkuDao;
import com.yang.gulimall.ware.entity.WareSkuEntity;
import com.yang.gulimall.ware.service.WareSkuService;
import com.yang.gulimall.ware.vo.HasStock2Vo;
import com.yang.gulimall.ware.vo.HasStockVo;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


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

    @Override
    public List<WareSkuEntity> getBySkuId(List<Long> skuId) {
        return this.list(new QueryWrapper<WareSkuEntity>().eq("sku_id",skuId));
    }

    @Override
    public List<HasStockVo> HasStock(List<Long> skuIds) {
        if (!skuIds.isEmpty()) {
            WareSkuDao baseMapper = this.baseMapper;
//        SELECT  sku_id,SUM(stock) FROM `wms_ware_sku` WHERE sku_id IN(2,28) GROUP BY sku_id
            List<HasStock2Vo> list = baseMapper.selectSumStock(skuIds);
            if(list!=null) {
                List<HasStockVo> collect = list.stream().map(e ->
                {
                    HasStockVo hasStockVo = new HasStockVo();
                    hasStockVo.setSkuId(e.getSkuId());
                    hasStockVo.setHasStock(e.getHasStock() > 0);
                    return hasStockVo;
                }).collect(Collectors.toList());
                //根据skuId查询其在所有仓库的总量
                return collect;
            }
        }
        return null;
    }


}