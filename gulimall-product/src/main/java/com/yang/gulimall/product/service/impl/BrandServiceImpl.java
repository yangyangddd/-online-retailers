package com.yang.gulimall.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yang.common.utils.PageUtils;
import com.yang.common.utils.Query;
import com.yang.gulimall.product.dao.BrandDao;
import com.yang.gulimall.product.entity.BrandEntity;
import com.yang.gulimall.product.service.BrandService;
import com.yang.gulimall.product.service.CategoryBrandRelationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;


@Service("brandService")
public class BrandServiceImpl extends ServiceImpl<BrandDao, BrandEntity> implements BrandService {

    @Autowired
    private CategoryBrandRelationService categoryBrandRelationService;
    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        String key = (String) params.get("key");
        QueryWrapper<BrandEntity> queryWrapper=new QueryWrapper<>();
        if(StringUtils.isNotEmpty(key))
        {
            queryWrapper.eq("brand_id",key).or().like("name",key);
        }
        IPage<BrandEntity> page = this.page(
                new Query<BrandEntity>().getPage(params),
                new QueryWrapper<BrandEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    @Transactional
    public void updateDetail(BrandEntity brand) {
        this.updateById(brand);
        if(StringUtils.isNotEmpty(brand.getName()))//更新了名字，则关系表也需要更新
        {
            //TODO 更新其他关联表
            categoryBrandRelationService.updateBrand(brand.getBrandId(),brand.getName());
        }
    }

}