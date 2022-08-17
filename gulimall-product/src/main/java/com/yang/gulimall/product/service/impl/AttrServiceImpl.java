package com.yang.gulimall.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yang.common.utils.PageUtils;
import com.yang.common.utils.Query;
import com.yang.constant.ProductConstant;
import com.yang.gulimall.product.dao.AttrAttrgroupRelationDao;
import com.yang.gulimall.product.dao.AttrDao;
import com.yang.gulimall.product.dao.AttrGroupDao;
import com.yang.gulimall.product.dao.CategoryDao;
import com.yang.gulimall.product.entity.*;
import com.yang.gulimall.product.service.AttrService;
import com.yang.gulimall.product.vo.AttrRespVo;
import com.yang.gulimall.product.vo.attrVo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service("attrService")
public class AttrServiceImpl extends ServiceImpl<AttrDao, AttrEntity> implements AttrService {

    @Autowired
    private AttrAttrgroupRelationDao relationDao;
    @Autowired
    private AttrGroupDao attrGroupDao;
    @Autowired
    private CategoryDao categoryDao;
    @Autowired
    private CategoryServiceImpl categoryService;
    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<AttrEntity> page = this.page(
                new Query<AttrEntity>().getPage(params),
                new QueryWrapper<AttrEntity>()
        );

        return new PageUtils(page);
    }

    @Transactional
    @Override
    public void saveAttr(attrVo attr) {
        AttrEntity entity=new AttrEntity();
        BeanUtils.copyProperties(attr,entity);
        this.save(entity);
        if(attr.getAttrType()== ProductConstant.AttrEnum.ATTR_TYPE_BASE.getCode()&&attr.getAttrGroupId()!=null) {
            AttrAttrgroupRelationEntity relationEntity = new AttrAttrgroupRelationEntity();
            relationEntity.setAttrId(entity.getAttrId());
            relationEntity.setAttrGroupId(attr.getAttrGroupId());
            relationDao.insert(relationEntity);
        }
    }






    @Override
    public AttrRespVo getAttrInfo(Long attrId) {
        AttrEntity byId = this.getById(attrId);
        AttrRespVo attrRespVo = new AttrRespVo();
        BeanUtils.copyProperties(byId,attrRespVo);

        if(attrRespVo.getAttrId()!=null)
            //设置分类信息
        attrRespVo.setCatelogPath(categoryService.findCatelogPath(attrRespVo.getCatelogId()));

        //设置vo的CatelogName
        CategoryEntity categoryEntity = categoryDao.selectById(attrRespVo.getCatelogId());
        if(categoryEntity!=null) {
            attrRespVo.setCatelogName(categoryEntity.getName());
        }
        if(byId.getAttrType()== ProductConstant.AttrEnum.ATTR_TYPE_BASE.getCode()) {//只有基本属性才回显分组信息
            QueryWrapper<AttrAttrgroupRelationEntity> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("attr_id", attrRespVo.getAttrId());
            //查询`pms_attr_attrgroup_relation`表的attrId
            AttrAttrgroupRelationEntity relationEntity = relationDao.selectOne(queryWrapper);
            if (relationEntity != null) {
                attrRespVo.setAttrGroupId(relationEntity.getAttrGroupId());
                AttrGroupEntity attrGroupEntity = attrGroupDao.selectById(relationEntity.getAttrGroupId());
                if (attrGroupEntity != null) {
                    attrRespVo.setGroupName((attrGroupEntity.getAttrGroupName()));
                }
            }
        }
        return attrRespVo;
    }

    @Override
    @Transactional
    public void updateAttr(attrVo attr) {
        AttrEntity attrEntity=new AttrEntity();
        BeanUtils.copyProperties(attr,attrEntity);
        this.updateById(attrEntity);

        if(attrEntity.getAttrType()== ProductConstant.AttrEnum.ATTR_TYPE_BASE.getCode()) {
            Long count = relationDao.selectCount(new UpdateWrapper<AttrAttrgroupRelationEntity>().
                    eq("attr_id", attr.getAttrId()));
            AttrAttrgroupRelationEntity relationEntity = new AttrAttrgroupRelationEntity();
            relationEntity.setAttrGroupId(attr.getAttrGroupId());
            relationEntity.setAttrId(attr.getAttrId());
            if (count > 0) {
                relationDao.update(relationEntity, new UpdateWrapper<AttrAttrgroupRelationEntity>().
                        eq("attr_id", attr.getAttrId()));
            } else {
                relationDao.insert(relationEntity);
            }
        }
    }

    @Override
    @Transactional
    /**
     *
     * @param params 前端传入的map参数列表
     * @param catelogId 前端传入的菜单id
     * @param type 判断传入的是销售属性还是基本属性
     * @return 返回给前端的page集合
     */
    public PageUtils queryTypePage(Map<String, Object> params, Long catelogId, String type) {
        String key = (String) params.get("key");
        QueryWrapper<AttrEntity> wrapper = new QueryWrapper<AttrEntity>().eq("attr_type","base".equalsIgnoreCase(type)?
                ProductConstant.AttrEnum.ATTR_TYPE_BASE.getCode(): ProductConstant.AttrEnum.ATTR_TYPE_SALE.getCode());
        IPage<AttrEntity> page;
        //查询所有的情况
        if((catelogId==0&& StringUtils.isEmpty(key))||StringUtils.isEmpty(key))
        {
            page = this.page(new Query<AttrEntity>().getPage(params), wrapper);

        }
        //key不为空即查询条件不为空，或catelogId不为空的情况都需要重新封装
        else
        {

            if(catelogId!=0)
            {
                wrapper.eq("catelog_id",catelogId);
            }
            wrapper.eq("attr_id",key).or().like("attr_name",key)
                    .like("Value_select",key);
            page = this.page(new Query<AttrEntity>().getPage(params), wrapper);

        }
        PageUtils pageUtils = new PageUtils(page);
        List<AttrEntity> records = page.getRecords();
        List<AttrRespVo> collect = records.stream().map((attrEntity) -> {
            AttrRespVo attrRespVo = new AttrRespVo();
            BeanUtils.copyProperties(attrEntity, attrRespVo);


            QueryWrapper<AttrAttrgroupRelationEntity> queryWrapper=new QueryWrapper<>();
            queryWrapper.eq("attr_id",attrEntity.getAttrId());
            //查询`pms_attr_attrgroup_relation`表的attrId
            if("base".equalsIgnoreCase(type)) {
                AttrAttrgroupRelationEntity relationEntity = relationDao.selectOne(queryWrapper);

                if (relationEntity != null&&relationEntity.getAttrGroupId()!=null) {
                    //根据attrID找到`pms_attr_group`表中分组的名字
                    AttrGroupEntity attrGroupEntity = attrGroupDao.selectById(relationEntity.getAttrGroupId());
                    attrRespVo.setGroupName(attrGroupEntity.getAttrGroupName());

                }
            }
            //根据attr表中的CatelogId找到对应的菜单名字
            CategoryEntity categoryEntity = categoryDao.selectById(attrEntity.getCatelogId());

            if (categoryEntity != null)
                attrRespVo.setCatelogName(categoryEntity.getName());


            return attrRespVo;
        }).collect(Collectors.toList());

        pageUtils.setList(collect);
        return pageUtils;

    }

    @Override
    public List<Long> selectSearchAttrs(List<Long> attrs) {
        AttrDao baseMapper = this.baseMapper;
        return baseMapper.selectSearchAttrs(attrs);

    }


}