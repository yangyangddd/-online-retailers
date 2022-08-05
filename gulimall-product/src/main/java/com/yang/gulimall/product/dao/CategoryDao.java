package com.yang.gulimall.product.dao;

import com.yang.gulimall.product.entity.CategoryEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 商品三级分类
 * 
 * @author yang
 * @email yang@gmail.com
 * @date 2022-06-11 19:48:15
 */
@Mapper
public interface CategoryDao extends BaseMapper<CategoryEntity> {
	
}
