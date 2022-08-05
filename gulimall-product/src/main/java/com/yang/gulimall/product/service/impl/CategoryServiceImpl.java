package com.yang.gulimall.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yang.common.utils.PageUtils;
import com.yang.common.utils.Query;
import com.yang.gulimall.product.dao.CategoryDao;
import com.yang.gulimall.product.entity.CategoryEntity;
import com.yang.gulimall.product.service.CategoryService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;


@Service("categoryService")
public class CategoryServiceImpl extends ServiceImpl<CategoryDao, CategoryEntity> implements CategoryService {
//    @Resource
//    CategoryDao categoryDao;//已经在父类注入过了
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