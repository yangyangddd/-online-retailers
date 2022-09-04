package com.yang.gulimall.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yang.common.utils.PageUtils;
import com.yang.common.utils.Query;
import com.yang.gulimall.product.dao.SkuInfoDao;
import com.yang.gulimall.product.entity.SkuImagesEntity;
import com.yang.gulimall.product.entity.SkuInfoEntity;
import com.yang.gulimall.product.entity.SpuInfoDescEntity;
import com.yang.gulimall.product.service.*;
import com.yang.gulimall.product.vo.SkuItemVo;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;


@Service("skuInfoService")
public class SkuInfoServiceImpl extends ServiceImpl<SkuInfoDao, SkuInfoEntity> implements SkuInfoService {



    @Autowired
    private SkuImagesService skuImagesService;
    @Autowired
    private SpuInfoDescService spuInfoDescService;
    @Autowired
    private AttrGroupService attrGroupService;
    @Autowired
    private SkuSaleAttrValueService skuSaleAttrValueService;
    @Autowired
    ThreadPoolExecutor executor;
    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        QueryWrapper<SkuInfoEntity> wrapper = new QueryWrapper<>();
        String key= (String) params.get("key");
        String catelogId=(String)params.get("catelogId");
        String brandId=(String)params.get("brandId");
        String max=(String)params.get("max");
        String min=(String)params.get("min");
        if(!StringUtils.isEmpty(key))
        {
            wrapper.and(w->
                    w.eq("sku_id",key).or().like("sku_name",key));
        }
        if(!StringUtils.isEmpty(catelogId)&&!"0".equals(catelogId))
        {
            wrapper.eq("catalog_id",catelogId);
        }
        if(!StringUtils.isEmpty(brandId)&&!"0".equals(brandId))
        {
            wrapper.eq("brand_id",brandId);
        }
        if(!StringUtils.isEmpty(max))
        {
            try {
                BigDecimal bigDecimal = new BigDecimal(max);
                if(bigDecimal.compareTo(new BigDecimal("0"))==1)
                wrapper.le("price",max);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if(!StringUtils.isEmpty(min))
        {
            wrapper.ge("price",min);
        }
                IPage<SkuInfoEntity> page = this.page(
                new Query<SkuInfoEntity>().getPage(params),
                wrapper
        );
        return new PageUtils(page);
    }

    //根据spuid得到所有的sku信息
    @Override
    public List<SkuInfoEntity> getSkusBySpuId(Long spuId) {
        return this.list(new QueryWrapper<SkuInfoEntity>().eq("spu_id", spuId));

    }

    @Override
    public SkuItemVo item(Long skuId) {
        SkuItemVo skuItemVo = new SkuItemVo();
        CompletableFuture<SkuInfoEntity> infoFuture = CompletableFuture.supplyAsync(() -> {
            //1.sku基本信息获取 pms_sku_info
            SkuInfoEntity info = getById(skuId);
            skuItemVo.setInfo(info);
            return info;
        }, executor);
        CompletableFuture<Void> saleAttrFuture = infoFuture.thenAcceptAsync((info) ->
        {
            //3.获取spu的销售属性组合
            Long spuId = info.getSpuId();
            List<SkuItemVo.SkuItemSaleAttrVo> saleAttrVos = this.getSaleAttrsBySpuId(spuId);
            skuItemVo.setSaleAttr(saleAttrVos);
        }, executor);
        CompletableFuture<Void> descFuture = infoFuture.thenAcceptAsync((res) ->
                {
                    //4.获取spu的介绍 pms_spu_info_desc

                    SpuInfoDescEntity spuInfoDesc = spuInfoDescService.getById(res.getSpuId());
                    skuItemVo.setDesp(spuInfoDesc);
                }
                , executor);
        CompletableFuture<Void> baseAttrFuture = infoFuture.thenAcceptAsync((res) ->
        {
            //5.获取spu的规格参数信息
            List<SkuItemVo.SpuItemAttrGroupVo> spuItemAttrGroupVos = attrGroupService.listBySkuId(skuId, res.getSpuId());
            skuItemVo.setGroupAttrs(spuItemAttrGroupVos);
            System.out.println(skuItemVo);
        }, executor);
        CompletableFuture<Void> imgFuture = CompletableFuture.runAsync(() ->
        {
            //2.sku的图片信息 pms_sku_images
            List<SkuImagesEntity> images = skuImagesService.getImagesBySkuId(skuId);
            skuItemVo.setImages(images);
        }, executor);

        //等待所有任务完成
        try {
            CompletableFuture.allOf(infoFuture,baseAttrFuture,imgFuture,descFuture,saleAttrFuture)
                    .get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        return skuItemVo;
    }

    @Override
    public List<SkuItemVo.SkuItemSaleAttrVo> getSaleAttrsBySpuId(Long spuId) {

        return this.baseMapper.getSaleAttrsBySpuId(spuId);


    }

    @Override
    public Map<Long,BigDecimal> getNewPriceBySkuIds(List<Long> skuIds) {
        Map<Long,BigDecimal> map=new HashMap<>();
        List<SkuInfoEntity> skuInfoEntities = this.list(new QueryWrapper<SkuInfoEntity>().in("sku_id", skuIds));
        skuInfoEntities.forEach(e->
                map.put(e.getSkuId(),e.getPrice())
        );

        return map;
    }


//    @Override
//    public List<SkuItemVo.SkuItemSaleAttrVo> getSaleAttrsBySpuId(Long spuId) {
//        List<SkuInfoEntity> skusBySpuId = this.getSkusBySpuId(spuId);
//        List<Long> skuIds = skusBySpuId.stream().map(SkuInfoEntity::getSkuId).collect(Collectors.toList());
//        List<SkuSaleAttrValueEntity> skuSaleAttrValueEntities =skuSaleAttrValueService.listByIds(skuIds);
//        //为避免循环查找库存，故提前查找出`pms_sku_sale_attr_value`集合
//        List<SkuSaleAttrValueEntity> skuSaleAttrValueEntities1 = skuSaleAttrValueService.list(new QueryWrapper<>());
//        List<SkuItemVo.SkuItemSaleAttrVo> skuItemSaleAttrVos=new ArrayList<>();
//        Map<Long,List<AttrValueWithSkuIdVo>> attrMap=new HashMap<>();
//        skuSaleAttrValueEntities.forEach(item->
//        {
//            //查出拥有该值所对应的所有skuId集合
//            List<Long> skuIds1 = skuSaleAttrValueEntities1.stream().map(e ->
//            {
//                if (e.getAttrValue() .equals( item.getAttrValue())) {
//                    return e.getSkuId();
//                }
//                return null;
//            }).filter(Objects::nonNull).collect(Collectors.toList());
//            if(!attrMap.containsKey(item.getAttrId()))
//            {
//                //设置setAttrValues
//                SkuItemVo.SkuItemSaleAttrVo skuItemSaleAttrVo=new SkuItemVo.SkuItemSaleAttrVo();
//                skuItemSaleAttrVo.setAttrId(item.getAttrId());
//                skuItemSaleAttrVo.setAttrName(item.getAttrName());
//                List<AttrValueWithSkuIdVo> list =new ArrayList<>();
//                AttrValueWithSkuIdVo attrValueWithSkuIdVo = new AttrValueWithSkuIdVo();
//                attrValueWithSkuIdVo.setAttrValue(item.getAttrValue());
//                attrValueWithSkuIdVo.setSkuIds(skuIds1);
//                list.add(attrValueWithSkuIdVo);
//                skuItemSaleAttrVo.setAttrValues(list);
//                attrMap.put(item.getAttrId(),list);
//                skuItemSaleAttrVos.add(skuItemSaleAttrVo);
//            }
//            else {
//                AttrValueWithSkuIdVo attrValueWithSkuIdVo = new AttrValueWithSkuIdVo();
//                attrValueWithSkuIdVo.setAttrValue(item.getAttrValue());
//                //查出拥有该值所对应的所有skuId集合
//                attrValueWithSkuIdVo.setSkuIds(skuIds1);
//                attrMap.get(item.getAttrId()).add(attrValueWithSkuIdVo);
//            }
//        });
//        //1 查出所有
//        //以上可用以下sql替代
//        /**
//         * SELECT
//         * skuSale.attr_id attrId,
//         * skuSale.attr_name attrName,
//         * skuSale.`attr_value`,
//         * GROUP_CONCAT(DISTINCT skuInfo.`sku_id`)
//         * FROM
//         * `pms_sku_info` skuInfo
//         * LEFT JOIN `pms_sku_sale_attr_value` skuSale
//         * ON skuInfo.sku_id=skuSale.sku_id
//         * WHERE spu_id=#{spuId}
//         * GROUP BY  skuSale.attr_id,skuSale.attr_name,skuSale.`attr_value`
//         */
//
//        return skuItemSaleAttrVos;

//        //使用dao完成一次查询和封装
//       return skuSaleAttrValueService.getSaleAttrsBySpuId(spuId);
//    }
}