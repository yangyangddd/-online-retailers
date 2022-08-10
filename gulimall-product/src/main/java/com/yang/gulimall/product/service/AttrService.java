package com.yang.gulimall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yang.common.utils.PageUtils;
import com.yang.gulimall.product.entity.AttrEntity;
import com.yang.gulimall.product.vo.AttrRespVo;
import com.yang.gulimall.product.vo.attrVo;

import java.util.Map;

/**
 * 商品属性
 *
 * @author yang
 * @email yang@gmail.com
 * @date 2022-06-11 19:48:15
 */
public interface AttrService extends IService<AttrEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void saveAttr(attrVo attr);


    AttrRespVo getAttrInfo(Long attrId);

    void updateAttr(attrVo attr);

    PageUtils queryTypePage(Map<String, Object> params, Long catelogId, String type);
}

