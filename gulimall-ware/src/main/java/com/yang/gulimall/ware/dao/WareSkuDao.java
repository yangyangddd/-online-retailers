package com.yang.gulimall.ware.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yang.gulimall.ware.entity.WareSkuEntity;
import com.yang.gulimall.ware.vo.HasStock2Vo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 商品库存
 * 
 * @author yang
 * @email yang@gmail.com
 * @date 2022-06-12 09:56:16
 */
@Mapper
public interface WareSkuDao extends BaseMapper<WareSkuEntity> {
    //SELECT  sku_id,SUM(stock) FROM `wms_ware_sku` WHERE sku_id IN(2,28) GROUP BY sku_id
    List<HasStock2Vo> selectSumStock(@Param("skuIds") List<Long> skuIds);
}
