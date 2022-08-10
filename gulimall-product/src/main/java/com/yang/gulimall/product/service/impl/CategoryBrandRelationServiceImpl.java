package com.yang.gulimall.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yang.common.utils.PageUtils;
import com.yang.common.utils.Query;
import com.yang.gulimall.product.dao.BrandDao;
import com.yang.gulimall.product.dao.CategoryBrandRelationDao;
import com.yang.gulimall.product.dao.CategoryDao;
import com.yang.gulimall.product.entity.CategoryBrandRelationEntity;
import com.yang.gulimall.product.service.CategoryBrandRelationService;
import com.yang.gulimall.product.vo.categoryBrandRelationVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service("categoryBrandRelationService")
public class CategoryBrandRelationServiceImpl extends ServiceImpl<CategoryBrandRelationDao, CategoryBrandRelationEntity> implements CategoryBrandRelationService {

    @Autowired
    private BrandDao brandDao;
    @Autowired
    private CategoryDao categoryDao;
    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<CategoryBrandRelationEntity> page = this.page(
                new Query<CategoryBrandRelationEntity>().getPage(params),
                new QueryWrapper<CategoryBrandRelationEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public void saveDetail(CategoryBrandRelationEntity categoryBrandRelation) {
        Long brandId = categoryBrandRelation.getBrandId();
        Long catelogId = categoryBrandRelation.getCatelogId();
        categoryBrandRelation.setBrandName(brandDao.selectById(brandId).getName());
        categoryBrandRelation.setCatelogName(categoryDao.selectById(catelogId).getName());
        this.save(categoryBrandRelation);
    }

    @Override
    public void updateBrand(Long brandId, String name) {
        CategoryBrandRelationEntity relationEntity=new CategoryBrandRelationEntity();
        relationEntity.setBrandName(name);
        relationEntity.setBrandId(brandId);
        this.update(relationEntity,new QueryWrapper<CategoryBrandRelationEntity>().eq("brand_id",brandId));
    }

    @Override
    @Transactional
    public void updateCategory(Long catId, String name) {
        this.baseMapper.updateCategory(catId,name);
    }

    @Override
    public List<categoryBrandRelationVo> getCategoryBrandRelation(Long catId) {
        //根据catId即分类id，获取品牌id，最后根据品牌id获得品牌对象
        QueryWrapper<CategoryBrandRelationEntity> queryWrapper=new QueryWrapper<>();
        queryWrapper.eq("catelog_id",catId);
        return this.list(queryWrapper).stream().map((entity)->
                {
                    categoryBrandRelationVo categoryBrandRelationVo = new categoryBrandRelationVo();
                    categoryBrandRelationVo.setBrandId(entity.getBrandId());
                    categoryBrandRelationVo.setBrandName(entity.getBrandName());
                    return categoryBrandRelationVo;
                }
                ).collect(Collectors.toList());

    }


}