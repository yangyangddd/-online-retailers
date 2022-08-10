package com.yang.gulimall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yang.common.utils.PageUtils;
import com.yang.gulimall.product.entity.AttrAttrgroupRelationEntity;
import com.yang.gulimall.product.entity.AttrEntity;
import com.yang.gulimall.product.vo.AttrGroupRelationVo;

import java.util.List;
import java.util.Map;

/**
 * 属性&属性分组关联
 *
 * @author yang
 * @email yang@gmail.com
 * @date 2022-06-11 19:48:15
 */
public interface AttrAttrgroupRelationService extends IService<AttrAttrgroupRelationEntity> {

    PageUtils queryPage(Map<String, Object> params);

    List<AttrEntity> getByAttrGroupId(Long attrGroupId);

    void addRelationByGroupIdAndAttrId(AttrGroupRelationVo[] vos);

    void delRelationByGroupIdAndAttrId(AttrGroupRelationVo[] vos);

    PageUtils getNoRelationAttr(Map<String, Object> params, Long attrGroupId);
}

