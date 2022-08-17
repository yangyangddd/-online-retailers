package com.yang.gulimall.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yang.common.utils.PageUtils;
import com.yang.common.utils.Query;
import com.yang.gulimall.product.dao.CategoryDao;
import com.yang.gulimall.product.entity.CategoryEntity;
import com.yang.gulimall.product.service.CategoryBrandRelationService;
import com.yang.gulimall.product.service.CategoryService;
import com.yang.gulimall.product.vo.Catelog2Vo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;


@Service("categoryService")
public class CategoryServiceImpl extends ServiceImpl<CategoryDao, CategoryEntity> implements CategoryService {
//    @Resource
//    CategoryDao categoryDao;//已经在父类注入过了
    @Autowired
    CategoryBrandRelationService categoryBrandRelationService;
    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<CategoryEntity> page = this.page(
                new Query<CategoryEntity>().getPage(params),
                new QueryWrapper<CategoryEntity>()
        );
        return new PageUtils(page);
    }
    @Override
    public List<CategoryEntity> listWithTree() {
        //查出所有分类
        //组装成父子的树形结构
        List<CategoryEntity> entities = baseMapper.selectList(null);
        List<CategoryEntity> level1Menus = entities.stream().filter(categoryEntity -> categoryEntity.getParentCid() == 0)
                .sorted((meu1,mue2)->{
                    return ((meu1.getSort()==null?0:meu1.getSort())-(mue2.getSort()==null?0:mue2.getSort()));
                }) .collect(Collectors.toList());
        for (CategoryEntity level1Menu : level1Menus) {
//            level1Menu.setChildren(entities.stream().filter(categoryEntity ->
//                    Objects.equals(categoryEntity.getParentCid(), level1Menu.getCatId())).collect(Collectors.toList()));
            level1Menu.setChildren(getChildren(level1Menu,entities));
        }
        return level1Menus;
    }

    @Override
    public void removeMenuByIds(List<Long> asList) {
        //TODO  1检查当前删除的菜单，是否被其他地方引用
        //逻辑删除
        baseMapper.deleteBatchIds(asList);
    }

    /**
     *
     * @param catelogId 传入的菜单id值
     * @return 返回其完整路径，即其父节点，父节点的父节点，和当前节点组成的long数组
     */
    @Override
    public Long[] findCatelogPath(Long catelogId) {
        List<Long> catelogPath1 = findCatelogPath1(catelogId);
        Collections.reverse(catelogPath1);
        Long[] longs = catelogPath1.toArray(new Long[0]);
        return longs;
    }

    //级联更新所有关联的数据
    @Override
    @Transactional
    public void updateCascade(CategoryEntity category) {
        this.updateById(category);
        if(StringUtils.isNotEmpty(category.getName()))
        {
            categoryBrandRelationService.updateCategory(category.getCatId(),category.getName());
        }

    }

    @Override
    public List<CategoryEntity> getLevel1Categorys() {
        return this.list(new QueryWrapper<CategoryEntity>().eq("cat_level",1));
    }

    @Override
    public Map<String, List<Catelog2Vo>> getCatalogJson() {
        List<CategoryEntity> level1Categorys = getLevel1Categorys();
        Map<String, List<Catelog2Vo>> parent = level1Categorys.stream().collect(Collectors.toMap(k -> k.getCatId().toString(),
                v -> {
                    List<CategoryEntity> entities = baseMapper.
                            selectList(new QueryWrapper<CategoryEntity>()
                                    .eq("parent_cid", v.getCatId()));
                    List<Catelog2Vo> collect = null;
                    if (entities != null) {
                        collect = entities.stream().map(item -> {
                            Catelog2Vo catelog2Vo = new Catelog2Vo(v.getCatId().toString(),
                                    null, item.getCatId().toString(), item.getName());
                            List<CategoryEntity> level3Catelog = baseMapper.selectList(new QueryWrapper<CategoryEntity>().
                                    eq("parent_cid", item.getCatId()));
                            if(level3Catelog!=null)
                            {
                                List<Catelog2Vo.Catelog3Vo> collect1 = level3Catelog.stream().map(l3 ->
                                {
                                    Catelog2Vo.Catelog3Vo catelog3Vo =
                                            new Catelog2Vo.Catelog3Vo(item.getCatId().toString(), l3.getCatId().toString(), l3.getName());
                                    return catelog3Vo;
                                }).collect(Collectors.toList());
                            catelog2Vo.setCatalog3List(collect1);
                            }
                            return catelog2Vo;
                        }).collect(Collectors.toList());
                    }
                    return collect;
                }));
        return parent;
    }

    public List<Long> findCatelogPath1(Long catelogId)
    {
        List<Long> list=new ArrayList<>();
        list.add(catelogId);
        QueryWrapper<CategoryEntity> wrapper = new QueryWrapper<>();
        wrapper.eq("cat_id", catelogId);
        CategoryEntity entity = baseMapper.selectOne(wrapper);
        if(entity!=null)
        {
            Long parentCid = entity.getParentCid();
            if(parentCid!=0)
            list.addAll(findCatelogPath1(parentCid));
        }
        return list;
    }

    /**
     *查找root节点的所有子目录
     * @param root 待查找节点
     * @param list 待查找的集合
     * @return 查找后的结果
     */
    private List<CategoryEntity> getChildren(CategoryEntity root,List<CategoryEntity> list)
    {
        return list.stream().filter(c->Objects.equals(c.getParentCid(), root.getCatId())).peek((c)->{
                List<CategoryEntity> children = getChildren(c, list);
                if(children!=null)
                    c.setChildren(children);
        }).sorted((meu1,mue2)->{
            return ((meu1.getSort()==null?0:meu1.getSort())-(mue2.getSort()==null?0:mue2.getSort()));
        }) .collect(Collectors.toList());
    }

}