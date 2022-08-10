package com.yang.gulimall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yang.common.utils.PageUtils;
import com.yang.gulimall.product.entity.CategoryBrandRelationEntity;
import com.yang.gulimall.product.vo.categoryBrandRelationVo;

import java.util.List;
import java.util.Map;

/**
 * 品牌分类关联
 *
 * @author yang
 * @email yang@gmail.com
 * @date 2022-06-11 19:48:15
 */
public interface CategoryBrandRelationService extends IService<CategoryBrandRelationEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void saveDetail(CategoryBrandRelationEntity categoryBrandRelation);

    void updateBrand(Long brandId, String name);

    void updateCategory(Long catId, String name);

    List<categoryBrandRelationVo> getCategoryBrandRelation(Long catId);
}

