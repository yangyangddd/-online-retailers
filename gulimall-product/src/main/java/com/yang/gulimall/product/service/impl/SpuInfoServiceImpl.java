package com.yang.gulimall.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yang.common.utils.PageUtils;
import com.yang.common.utils.Query;
import com.yang.common.utils.R;
import com.yang.gulimall.product.dao.SpuInfoDao;
import com.yang.gulimall.product.dao.SpuInfoDescDao;
import com.yang.gulimall.product.entity.*;
import com.yang.gulimall.product.feign.CouponFeignService;
import com.yang.gulimall.product.service.*;
import com.yang.gulimall.product.vo.SpuInfoVo;
import com.yang.gulimall.product.vo.SpuSavaVo.*;
import com.yang.to.MemberPrice;
import com.yang.to.SkuReductionTo;
import com.yang.to.SpuBoundTo;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service("spuInfoService")
public class SpuInfoServiceImpl extends ServiceImpl<SpuInfoDao, SpuInfoEntity> implements SpuInfoService {
    @Autowired
    private SpuInfoDescDao spuInfoDescDao;
    @Autowired
    private SpuImagesService spuImagesService;
    @Autowired
    private ProductAttrValueService productAttrValueService;
    @Autowired
    private SkuInfoService skuInfoService;
    @Autowired
    private SkuImagesService skuImagesService;
    @Autowired
    private SkuSaleAttrValueService skuSaleAttrValueService;
    @Autowired
    private CouponFeignService couponFeignService;
    @Autowired
    private CategoryService categoryService;
    @Autowired
    private BrandService brandService;
    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SpuInfoEntity> page = this.page(
                new Query<SpuInfoEntity>().getPage(params),
                new QueryWrapper<SpuInfoEntity>()
        );

        return new PageUtils(page);
    }

    //TODO 涉及远程调用，需要分布式事务
    @Transactional
    @Override
    public void saveSpuInfo(SpuSaveVo vo) {
        //1.保存spu基本信息 `pms_spu_info`
        SpuInfoEntity spuInfoEntity = new SpuInfoEntity();
        BeanUtils.copyProperties(vo,spuInfoEntity);
        spuInfoEntity.setCreateTime(new Date());
        spuInfoEntity.setUpdateTime(new Date());
        this.save(spuInfoEntity);
        //2.保存spu的描述图片 `pms_spu_info_desc`
        List<String> decripts = vo.getDecript();
        SpuInfoDescEntity spuInfoDescEntity = new SpuInfoDescEntity();
        spuInfoDescEntity.setSpuId(spuInfoEntity.getId());
        spuInfoDescEntity.setDecript(String.join(",",decripts));
        spuInfoDescDao.insert(spuInfoDescEntity);
        //3.保存spu的图片集`pms_spu_images`
        List<String> images = vo.getImages();
        spuImagesService.saveImages(spuInfoEntity.getId(),images);
        //4.保存spu的规格参数 `pms_product_attr_value`
        List<BaseAttrs> baseAttrs = vo.getBaseAttrs();
        productAttrValueService.saveBySpu(spuInfoEntity.getId(),baseAttrs);

        //5保存当前spu对应的所有sku信息
        //5.1sku的基本信息；pms_sku_info
        List<Skus> skus = vo.getSkus();
        skus.forEach(item->
        {
            String defaultImg="";
            for (Images image : item.getImages()) {
                if(image.getDefaultImg()==1)
                    defaultImg=image.getImgUrl();
            }
            SkuInfoEntity skuInfoEntity=new SkuInfoEntity();
            BeanUtils.copyProperties(item,skuInfoEntity);
            skuInfoEntity.setBrandId(spuInfoEntity.getBrandId());
            skuInfoEntity.setCatalogId(spuInfoEntity.getCatalogId());
            skuInfoEntity.setSaleCount(0L);
            skuInfoEntity.setSpuId(spuInfoEntity.getId());
            skuInfoEntity.setSkuDefaultImg(defaultImg);
            skuInfoService.save(skuInfoEntity);
            Long skuId = skuInfoEntity.getSkuId();
            //5.2 sku的图片信息pms_sku_images
            //TODO 没有图片：路径无需保存
            List<SkuImagesEntity> imagesEntity = item.getImages().stream().map(img ->
            {
                SkuImagesEntity skuImagesEntity = new SkuImagesEntity();
                skuImagesEntity.setSkuId(skuId);
                skuImagesEntity.setImgUrl(img.getImgUrl());
                skuImagesEntity.setDefaultImg(img.getDefaultImg());
                return skuImagesEntity;
            }).filter(entity->
                    !StringUtils.isEmpty(entity.getImgUrl())).collect(Collectors.toList());
            skuImagesService.saveBatch(imagesEntity);
            //5.3 sku的销售属性信息 pms_sku_sale_attr_value
            List<Attr> attr = item.getAttr();
            List<SkuSaleAttrValueEntity> collect = attr.stream().map(attr1 -> {
                SkuSaleAttrValueEntity skuSaleAttrValueEntity = new SkuSaleAttrValueEntity();
                BeanUtils.copyProperties(attr1, skuSaleAttrValueEntity);
                skuSaleAttrValueEntity.setSkuId(skuId);
                return skuSaleAttrValueEntity;
            }).collect(Collectors.toList());
            skuSaleAttrValueService.saveBatch(collect);

            //5.保存spu的积分信息；gulimall_sms -> sms_spu_bounds
            Bounds bounds = vo.getBounds();
            SpuBoundTo spuBoundTo = new SpuBoundTo();
            BeanUtils.copyProperties(bounds,spuBoundTo);
            spuBoundTo.setSpuId(spuInfoEntity.getId());
            R r = couponFeignService.saveSpuBouds(spuBoundTo);
            if(r.getCode()!=0)
            {
                log.error("远程保存spu积分信息失败");
            }
            //5.4 sku的优惠满减等信息:gulimall_sms ->`sms_sku_ladder`  `sms_sku_full_reduction``sms_member_price`
            SkuReductionTo skuReductionTo = new SkuReductionTo();
            BeanUtils.copyProperties(item,skuReductionTo);
            List<MemberPrice> memberPrice = item.getMemberPrice();
            skuReductionTo.setMemberPrice(memberPrice);
            skuReductionTo.setSkuId(skuId);
            if(skuReductionTo.getFullPrice().compareTo(new BigDecimal(0))==1||skuReductionTo.getFullCount()>0) {
                R r1 = couponFeignService.saveSkuReduction(skuReductionTo);

                if (r1.getCode() != 0) {
                    log.error("远程保存sku优惠    信息失败");
                }

            }});
    }

    @Override
    public PageUtils queryPageByCondition(Map<String, Object> params) {
        QueryWrapper<SpuInfoEntity> wrapper = new QueryWrapper<>();
//                 key: '华为',//检索关键字
//                catelogId: 6,//三级分类id
//                brandId: 1,//品牌id
//                status: 0,//商品状态
        String key = (String) params.get("key");
        String catelogId = (String) params.get("catelogId");
        String brandId=(String)params.get("brandId");
        String status=(String) params.get("status");
        if(!StringUtils.isEmpty(key))//非空
        {
            wrapper.and(w->{
                w.eq("id",key).or().like("spu_name",key).or().like("spu_description",key);
            });
        }
        if(!StringUtils.isEmpty(catelogId))
        {
            wrapper.eq("catalog_id",catelogId);
        }
        if(!StringUtils.isEmpty(brandId))
        {
            wrapper.eq("brand_id",brandId);
        }
        if(!StringUtils.isEmpty(status))
        {
            wrapper.eq("publish_status",status);
        }
        List<SpuInfoEntity> list = this.list(wrapper);
        List<SpuInfoVo> collect = list.stream().map(entity ->
        {
            SpuInfoVo spuInfoVo = new SpuInfoVo();
            BeanUtils.copyProperties(entity, spuInfoVo);
            spuInfoVo.setCatalogName(categoryService.getById(entity.getCatalogId()).getName());
            spuInfoVo.setBrandName(brandService.getById(entity.getBrandId()).getName());
            return spuInfoVo;
        }).collect(Collectors.toList());
        IPage<SpuInfoEntity> page = this.page(
                new Query<SpuInfoEntity>().getPage(params),
                new QueryWrapper<SpuInfoEntity>()
        );
        PageUtils pageUtils = new PageUtils(page);
        pageUtils.setList(collect);
        return pageUtils;
    }

}