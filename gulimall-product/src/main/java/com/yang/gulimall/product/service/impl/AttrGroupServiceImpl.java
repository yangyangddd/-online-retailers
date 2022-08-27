package com.yang.gulimall.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yang.common.utils.PageUtils;
import com.yang.common.utils.Query;
import com.yang.gulimall.product.dao.AttrDao;
import com.yang.gulimall.product.dao.AttrGroupDao;
import com.yang.gulimall.product.entity.*;
import com.yang.gulimall.product.service.*;
import com.yang.gulimall.product.vo.AttrGroupRelationVo;
import com.yang.gulimall.product.vo.GroupWithAttrVo;
import com.yang.gulimall.product.vo.SkuItemVo;
import com.yang.gulimall.product.vo.attrVo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;


@Service("attrGroupService")
public class AttrGroupServiceImpl extends ServiceImpl<AttrGroupDao, AttrGroupEntity> implements AttrGroupService {

    @Autowired
    private AttrAttrgroupRelationService attrAttrgroupRelationService;
    @Autowired
    private AttrDao attrDao;
    @Autowired
    private ProductAttrValueService productAttrValueService;


    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<AttrGroupEntity> page = this.page(
                new Query<AttrGroupEntity>().getPage(params),
                new QueryWrapper<>()
        );

        return new PageUtils(page);
    }

    //如果catalogId为0，则默认查询所有，否则
    @Override
    public PageUtils queryPage(Map<String, Object> params, Long catelogId) {
        String key=(String) params.get("key");
        if((StringUtils.isEmpty(key)))
        {
            QueryWrapper<AttrGroupEntity> attrGroupEntityQueryWrapper = new QueryWrapper<>();
            if(catelogId!=0)
            {
                attrGroupEntityQueryWrapper.eq("catelog_id", catelogId);
            }
            IPage<AttrGroupEntity> page = this.page(new Query<AttrGroupEntity>().getPage(params),
                    attrGroupEntityQueryWrapper);
            return new PageUtils(page);
        }
        else {

            QueryWrapper<AttrGroupEntity> wrapper=new QueryWrapper<>();
            if(catelogId!=0) {
                 wrapper.eq("catelog_id", catelogId);
            }

            wrapper.and((obj)-> obj.eq("attr_group_id",key).or().like("attr_group_name",key));


            IPage<AttrGroupEntity> page = this.page(new Query<AttrGroupEntity>().getPage(params), wrapper);

            return new PageUtils(page);

        }
    }

    @Override
    public List<AttrEntity> getRelationAttr(Long attrGroupId) {
       return attrAttrgroupRelationService.getByAttrGroupId(attrGroupId);

    }

    @Override
    public void addRelationByGroupIdAndAttrId(AttrGroupRelationVo[] vos) {
        attrAttrgroupRelationService.addRelationByGroupIdAndAttrId(vos);
    }

    @Override
    public void delRelationByGroupIdAndAttrId(AttrGroupRelationVo[] vos) {
        attrAttrgroupRelationService.delRelationByGroupIdAndAttrId(vos);
    }

    @Override
    public PageUtils getNoRelationAttr(Map<String, Object> params, Long attrGroupId) {
        //获取属性分组里面还没有关联的本分类里面的其他基本属性，方便添加新的关联
        return attrAttrgroupRelationService.getNoRelationAttr(params,attrGroupId);
    }

    @Override
    public List<GroupWithAttrVo> getALlGroupWithAttr(Long catelogId) {
        //获取分类下所有分组&关联属性
        //首先根据catelogId查询所有相对应的分组对象
//        List<Long> longs=new ArrayList<>();//保存要查的分组id，以便批量查询attr表
        List<AttrGroupEntity> attrGroupEntityList =
                this.list(new QueryWrapper<AttrGroupEntity>().eq("catelog_id", catelogId));
        List<GroupWithAttrVo> list=attrGroupEntityList.stream().map((entity -> {//将得到的属性集合封装至vo中
//            longs.add(entity.getAttrGroupId());
            GroupWithAttrVo groupWithAttrVo = new GroupWithAttrVo();
            BeanUtils.copyProperties(entity,groupWithAttrVo);
            return groupWithAttrVo;
        })).collect(Collectors.toList());
        //再根据分组对象集合的id查询关系表，为每个GroupWithAttrVo的attr集合赋值
      return   list.stream().peek(entity->{
            Long attrGroupId = entity.getAttrGroupId();
            List<AttrAttrgroupRelationEntity> list1=attrAttrgroupRelationService.
                    list(new QueryWrapper<AttrAttrgroupRelationEntity>().eq("attr_group_id",attrGroupId));
            List<Long> collect =
                    list1.stream().map(AttrAttrgroupRelationEntity::getAttrId).collect(Collectors.toList());//
            // 得到每个分组的属性id集合
          List<attrVo> collect1=new ArrayList<>();
          if(!collect.isEmpty()) {
              List<AttrEntity> attrEntities= attrDao.selectBatchIds(collect);
              collect1= attrEntities.stream().map(attrEntity -> {
                  attrVo attrVo = new attrVo();
                  BeanUtils.copyProperties(attrEntity, attrVo);
                  return attrVo;
              }).collect(Collectors.toList());
          }
          entity.setAttrs(collect1);
          //得到每个分组的attrVo集合,并封装至vo对象中
        }).collect(Collectors.toList());
    }
    //根据skuId获得其所有的分组和属性信息
    @Override
    public List<SkuItemVo.SpuItemAttrGroupVo> listBySkuId(Long skuId, Long spuId) {
        List<SkuItemVo.SpuItemAttrGroupVo> spuItemAttrGroupVos=new ArrayList<>();
        //根据skuId获取所有SkuSaleAttrValueEntity集合
        List<ProductAttrValueEntity> productAttrValueEntities = productAttrValueService.list(new QueryWrapper
                <ProductAttrValueEntity>().eq("spu_id", spuId));
        //获取所有对应的attrId集合
        List<Long> attrIds = productAttrValueEntities.stream().
                map(ProductAttrValueEntity::getAttrId).collect(Collectors.toList());
//        //获取所有的attr集合
//        List<AttrEntity> attrEntities = attrService.listByIds(attrIds);
        //根据attr集合获取所有AttrAttrgroupRelationEntity集合
        if(attrIds.isEmpty())
        {
            return null;
        }
       List<AttrAttrgroupRelationEntity> attrAttrgroupRelationEntities=
               attrAttrgroupRelationService.listByAttrIds(attrIds);
        List<Long> attrGroupIds = attrAttrgroupRelationEntities.stream().
                map(AttrAttrgroupRelationEntity::getAttrGroupId).collect(Collectors.toList());
        //根据AttrAttrgroupRelationEntity集合获取其所有分组
        List<AttrGroupEntity> attrGroupEntityList = this.listByIds(attrGroupIds);
        //根据分组集合获取每个分组下的所有属性,并封装spuItemAttrGroupVo；
        attrGroupEntityList.forEach(group->
        {
            SkuItemVo.SpuItemAttrGroupVo spuItemAttrGroupVo = new SkuItemVo.SpuItemAttrGroupVo();
            spuItemAttrGroupVo.setGroupName(group.getAttrGroupName());
            List<SkuItemVo.SpuBaseAttrVo> spuBaseAttrVoList=new ArrayList<>();
            //获得每个分组下的所有属性
            attrAttrgroupRelationEntities.forEach(relation->
                    {
                        SkuItemVo.SpuBaseAttrVo baseAttrVo=new SkuItemVo.SpuBaseAttrVo();
                        if(Objects.equals(group.getAttrGroupId(), relation.getAttrGroupId()))
                        {
                            //根据attrId将属性设置到baseAttrVo
                            Long attrId = relation.getAttrId();
                            productAttrValueEntities.forEach(e->
                            {
                                if(Objects.equals(e.getAttrId(), attrId))
                                {
                                    baseAttrVo.setAttrName(e.getAttrName());
                                    baseAttrVo.setAttrValue(e.getAttrValue());
                                }
                            });
                        }
                        //加入属性进入集合中
                        spuBaseAttrVoList.add(baseAttrVo);
                    });
                    spuItemAttrGroupVo.setAttrs(spuBaseAttrVoList);
                    spuItemAttrGroupVos.add(spuItemAttrGroupVo);
        });
        return spuItemAttrGroupVos;
    }

}