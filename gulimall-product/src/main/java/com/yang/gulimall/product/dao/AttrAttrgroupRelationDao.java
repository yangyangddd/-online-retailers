package com.yang.gulimall.product.dao;

import com.yang.gulimall.product.entity.AttrAttrgroupRelationEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 属性&属性分组关联
 * 
 * @author yang
 * @email yang@gmail.com
 * @date 2022-06-11 19:48:15
 */
@Mapper
public interface AttrAttrgroupRelationDao extends BaseMapper<AttrAttrgroupRelationEntity> {

    List<AttrAttrgroupRelationEntity> selectByAttrIds(@Param("attrIdList") List<Long> attrIds);
}
