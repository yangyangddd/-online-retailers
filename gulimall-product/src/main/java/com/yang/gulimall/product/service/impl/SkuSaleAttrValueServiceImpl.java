package com.yang.gulimall.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yang.common.utils.PageUtils;
import com.yang.common.utils.Query;
import com.yang.gulimall.product.dao.SkuSaleAttrValueDao;
import com.yang.gulimall.product.entity.SkuSaleAttrValueEntity;
import com.yang.gulimall.product.service.SkuSaleAttrValueService;
import com.yang.gulimall.product.vo.SkuItemVo;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;


@Service("skuSaleAttrValueService")
public class SkuSaleAttrValueServiceImpl extends ServiceImpl<SkuSaleAttrValueDao, SkuSaleAttrValueEntity> implements SkuSaleAttrValueService {



    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SkuSaleAttrValueEntity> page = this.page(
                new Query<SkuSaleAttrValueEntity>().getPage(params),
                new QueryWrapper<SkuSaleAttrValueEntity>()
        );
        return new PageUtils(page);
    }

    @Override
    public List<SkuItemVo.SkuItemSaleAttrVo> getSaleAttrsBySpuId(Long spuId) {
       return this.baseMapper.getSaleAttrsBySpuId(spuId);
    }

    @Override
    public List<String> getSkuSaleAttrValuesAsStringList(Long skuId) {
        SkuSaleAttrValueDao baseMapper = this.baseMapper;
        //根据skuId获取所有销售属性组合
        return baseMapper.getSkuSaleAttrValuesAsStringList(skuId);
    }


}