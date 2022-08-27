package com.yang.gulimall.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yang.common.utils.PageUtils;
import com.yang.common.utils.Query;
import com.yang.constant.ProductConstant;
import com.yang.gulimall.product.dao.AttrAttrgroupRelationDao;
import com.yang.gulimall.product.dao.AttrDao;
import com.yang.gulimall.product.dao.AttrGroupDao;
import com.yang.gulimall.product.entity.AttrAttrgroupRelationEntity;
import com.yang.gulimall.product.entity.AttrEntity;
import com.yang.gulimall.product.entity.AttrGroupEntity;
import com.yang.gulimall.product.service.AttrAttrgroupRelationService;
import com.yang.gulimall.product.service.AttrService;
import com.yang.gulimall.product.vo.AttrGroupRelationVo;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;


@Service("attrAttrgroupRelationService")
public class AttrAttrgroupRelationServiceImpl extends ServiceImpl<AttrAttrgroupRelationDao, AttrAttrgroupRelationEntity> implements AttrAttrgroupRelationService {

    @Autowired
    private AttrService attrService;
    @Autowired
    private AttrGroupDao attrGroupDao;
    @Autowired
    private AttrDao attrDao;
    @Autowired
    private AttrAttrgroupRelationDao attrAttrgroupRelationDao;
    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<AttrAttrgroupRelationEntity> page = this.page(
                new Query<AttrAttrgroupRelationEntity>().getPage(params),
                new QueryWrapper<AttrAttrgroupRelationEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public List<AttrEntity> getByAttrGroupId(Long attrGroupId) {
        QueryWrapper<AttrAttrgroupRelationEntity> wrapper=new QueryWrapper<AttrAttrgroupRelationEntity>()
                .eq("attr_group_id",attrGroupId);
        List<AttrAttrgroupRelationEntity> list = this.list(wrapper);
        List<Long> collect = list.stream().//得到attrId集合
                map(AttrAttrgroupRelationEntity::getAttrId).collect(Collectors.toList());
        if(collect.size()==0)
            return null;
      return attrService.listByIds(collect);

    }

    @Override
    public void addRelationByGroupIdAndAttrId(AttrGroupRelationVo[] vos) {

        List<AttrAttrgroupRelationEntity> list=new ArrayList<>();
        for (AttrGroupRelationVo vo : vos) {
            AttrAttrgroupRelationEntity relationEntity = new AttrAttrgroupRelationEntity();
            relationEntity.setAttrId(vo.getAttrId());
            relationEntity.setAttrGroupId(vo.getAttrGroupId());
            list.add(relationEntity);
        }

        this.saveBatch(list);
    }

    @Override
    public void delRelationByGroupIdAndAttrId(AttrGroupRelationVo[] vos) {
       Map<String,Object> map=new HashMap<>();

        for (AttrGroupRelationVo vo : vos) {
            if(vo.getAttrId()!=null)
           map.put("attr_id",vo.getAttrId());
            if(vo.getAttrGroupId()!=null)
           map.put("attr_group_id", vo.getAttrGroupId());
        }
        this.removeByMap(map);
    }

    @Override
    public PageUtils getNoRelationAttr(Map<String, Object> params, Long attrGroupId) {

        //当前分组只能关联自己所属分类的所有属性
        AttrGroupEntity attrGroupEntity = attrGroupDao.selectById(attrGroupId);
        Long catelogId = attrGroupEntity.getCatelogId();
        List<AttrGroupEntity> attrGroupEntities = attrGroupDao.selectList(new QueryWrapper<AttrGroupEntity>().
                eq("catelog_id", catelogId));
        //1.首先获取在相同分类下能够选择的分组
        //2.然后再从关系表即中间表获得所有可选分组的关联属性id集合
        List<Long> collect=new ArrayList<>();
        if(!attrGroupEntities.isEmpty()){
           collect =attrGroupEntities.stream().map((AttrGroupEntity::getAttrGroupId)).collect(Collectors.toList());

        }
        List<AttrAttrgroupRelationEntity> attrAttrgroupRelationEntities=new ArrayList<>();
        QueryWrapper<AttrAttrgroupRelationEntity> wrapper = new QueryWrapper<>();
        if(!collect.isEmpty()){
        attrAttrgroupRelationEntities =
                attrAttrgroupRelationDao.selectList
                        (wrapper.in("attr_group_id",collect)
                                );
        }
        List<Long> collect1 = attrAttrgroupRelationEntities.stream().
                map(AttrAttrgroupRelationEntity::getAttrId).collect(Collectors.toList());//获取attrid集合
        //从当前分类移除这些属性
        QueryWrapper<AttrEntity> queryWrapper = new QueryWrapper<AttrEntity>();
        queryWrapper.eq("catelog_id", catelogId).eq("attr_type", ProductConstant.AttrEnum.ATTR_TYPE_BASE.getCode());

        if(!collect1.isEmpty())
        {
           queryWrapper.notIn("attr_id", collect1);
        }
        String key=(String) params.get("key");
        if(!StringUtils.isEmpty(key)){
            queryWrapper.and(w->
                    w.eq("attr_id",key).or().like("attr_name",key));
        }

        IPage<AttrEntity> page = attrService.page(new Query<AttrEntity>().getPage(params), queryWrapper);
        PageUtils pageUtils=new PageUtils(page);
        return pageUtils;
        //最后根据查出来的id得到可选属性列表返回至前端
    }

    //根据attrId获取所有的AttrAttrgroupRelationEntity集合
    @Override
    public List<AttrAttrgroupRelationEntity> listByAttrIds(List<Long> attrIds) {
        return this.baseMapper.selectByAttrIds(attrIds);
    }

}