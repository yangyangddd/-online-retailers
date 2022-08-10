package com.yang.gulimall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yang.common.utils.PageUtils;
import com.yang.gulimall.product.entity.AttrEntity;
import com.yang.gulimall.product.entity.AttrGroupEntity;
import com.yang.gulimall.product.vo.AttrGroupRelationVo;
import com.yang.gulimall.product.vo.GroupWithAttrVo;

import java.util.List;
import java.util.Map;

/**
 * 属性分组
 *
 * @author yang
 * @email yang@gmail.com
 * @date 2022-06-11 19:48:15
 */
public interface AttrGroupService extends IService<AttrGroupEntity> {

    PageUtils queryPage(Map<String, Object> params);

    PageUtils queryPage(Map<String, Object> params, Long catelogId);

    List<AttrEntity> getRelationAttr(Long attrGroupId);

    void addRelationByGroupIdAndAttrId(AttrGroupRelationVo[] vos);

    void delRelationByGroupIdAndAttrId(AttrGroupRelationVo[] vos);

    PageUtils getNoRelationAttr(Map<String, Object> params, Long attrGroupId);

    List<GroupWithAttrVo> getALlGroupWithAttr(Long catelogId);
}

