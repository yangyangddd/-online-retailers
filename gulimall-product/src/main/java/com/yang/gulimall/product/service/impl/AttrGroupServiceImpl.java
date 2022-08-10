package com.yang.gulimall.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yang.common.utils.PageUtils;
import com.yang.common.utils.Query;
import com.yang.gulimall.product.dao.AttrDao;
import com.yang.gulimall.product.dao.AttrGroupDao;
import com.yang.gulimall.product.entity.AttrAttrgroupRelationEntity;
import com.yang.gulimall.product.entity.AttrEntity;
import com.yang.gulimall.product.entity.AttrGroupEntity;
import com.yang.gulimall.product.service.AttrAttrgroupRelationService;
import com.yang.gulimall.product.service.AttrGroupService;
import com.yang.gulimall.product.service.AttrService;
import com.yang.gulimall.product.vo.AttrGroupRelationVo;
import com.yang.gulimall.product.vo.GroupWithAttrVo;
import com.yang.gulimall.product.vo.attrVo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service("attrGroupService")
public class AttrGroupServiceImpl extends ServiceImpl<AttrGroupDao, AttrGroupEntity> implements AttrGroupService {

    @Autowired
    private AttrAttrgroupRelationService attrAttrgroupRelationService;
    @Autowired
    private AttrDao attrDao;
    @Autowired
    private AttrService attrService;
    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<AttrGroupEntity> page = this.page(
                new Query<AttrGroupEntity>().getPage(params),
                new QueryWrapper<AttrGroupEntity>()
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

            wrapper.and((obj)->{
                obj.eq("attr_group_id",key).or().like("attr_group_name",key);
            });


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

}