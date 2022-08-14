package com.yang.gulimall.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yang.common.utils.PageUtils;
import com.yang.common.utils.Query;
import com.yang.gulimall.product.dao.ProductAttrValueDao;
import com.yang.gulimall.product.entity.AttrEntity;
import com.yang.gulimall.product.entity.ProductAttrValueEntity;
import com.yang.gulimall.product.service.AttrService;
import com.yang.gulimall.product.service.ProductAttrValueService;
import com.yang.gulimall.product.vo.SpuSavaVo.BaseAttrs;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service("productAttrValueService")
public class ProductAttrValueServiceImpl extends ServiceImpl<ProductAttrValueDao, ProductAttrValueEntity> implements ProductAttrValueService {

    @Autowired
    private AttrService attrService;
    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<ProductAttrValueEntity> page = this.page(
                new Query<ProductAttrValueEntity>().getPage(params),
                new QueryWrapper<ProductAttrValueEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public void saveBySpu(Long id, List<BaseAttrs> baseAttrs) {
            if(!baseAttrs.isEmpty())
            {
                List<ProductAttrValueEntity> collect = baseAttrs.stream().map(attr -> {
                    ProductAttrValueEntity productAttrValueEntity = new ProductAttrValueEntity();
                    productAttrValueEntity.setSpuId(id);
                    productAttrValueEntity.setAttrId(attr.getAttrId());
                    productAttrValueEntity.setAttrValue(attr.getAttrValues());
                    productAttrValueEntity.setQuickShow(attr.getShowDesc());
                    AttrEntity byId = attrService.getById(attr.getAttrId());
                    productAttrValueEntity.setAttrName(byId.getAttrName());
                    return productAttrValueEntity;
                }).collect(Collectors.toList());
                this.saveBatch(collect);
            }
    }

    @Override
    public List<ProductAttrValueEntity> baseAttrListForSpu(Long spuId) {
        List<ProductAttrValueEntity> entities = this.baseMapper.selectList(new QueryWrapper<ProductAttrValueEntity>().eq("spu_id", spuId));

        return entities;
    }

    @Override
    @Transactional
    public void baseAttrUpdateForSpu(Long spuId, List<ProductAttrValueEntity> entities) {
        //1.删除这个spuId之前对应的所有属性
        this.baseMapper.delete(new QueryWrapper<ProductAttrValueEntity>().eq("spu_id",spuId));
        List<ProductAttrValueEntity> collect = entities.stream().peek(item ->
        {
            item.setSpuId(spuId);
        }).collect(Collectors.toList());
        this.saveBatch(collect);
    }

}