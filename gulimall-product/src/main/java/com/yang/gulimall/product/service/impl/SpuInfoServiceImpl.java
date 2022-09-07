package com.yang.gulimall.product.service.impl;

import cn.hutool.core.lang.TypeReference;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yang.common.utils.PageUtils;
import com.yang.common.utils.Query;
import com.yang.common.utils.R;
import com.yang.constant.ProductConstant;
import com.yang.gulimall.product.dao.SpuInfoDao;
import com.yang.gulimall.product.dao.SpuInfoDescDao;
import com.yang.gulimall.product.entity.*;
import com.yang.gulimall.product.feign.CouponFeignService;
import com.yang.gulimall.product.feign.SearchFeignService;
import com.yang.gulimall.product.feign.WareFeignService;
import com.yang.gulimall.product.service.*;
import com.yang.gulimall.product.vo.SpuInfoVo;
import com.yang.gulimall.product.vo.SpuSavaVo.*;
import com.yang.to.HasStockTo;
import com.yang.to.MemberPrice;
import com.yang.to.SkuReductionTo;
import com.yang.to.SpuBoundTo;
import com.yang.to.es.SkuEsModel;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
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
    @Autowired
    private AttrService attrService;
    @Autowired
    private WareFeignService wareFeignService;
    @Autowired
    private SearchFeignService searchFeignService;
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
//商品上架
    @Override
    public void up(Long spuId) {
//        List<SkuEsModel> uoProducts=new ArrayList<>();
        //组装需要的数据
//        SkuEsModel skuEsModel = new SkuEsModel();
//        List<SkuEsModel.Attrs> attrsList=new ArrayList<>();
        //1.查出当前spuid对应的所有sku信息，品牌的名字。
        List<SkuInfoEntity> skuInfoEntities=skuInfoService.getSkusBySpuId(spuId);
        List<Long> skuIdList = skuInfoEntities.stream().map(SkuInfoEntity::getSkuId).collect(Collectors.toList());

        //封装每个sku信息
        //查询sku的所有可以被检索的规格属性，根据spuId查询对应的catalogId
        List<ProductAttrValueEntity> attrs = productAttrValueService.baseAttrListForSpu(spuId);
        List<Long> collect1 = attrs.stream().map(ProductAttrValueEntity::getAttrId
        ).collect(Collectors.toList());
       List<Long> searchAttrIds= attrService.selectSearchAttrs(collect1);
        Set<Long> idSet=new HashSet<>(searchAttrIds);
        List<SkuEsModel.Attrs> attrsList1= attrs.stream().filter(item ->
        {
            return idSet.contains(item.getAttrId());

        }).map(e ->
        {
            SkuEsModel.Attrs attrs1 = new SkuEsModel.Attrs();
            BeanUtils.copyProperties(e, attrs1);
            return attrs1;
        }).collect(Collectors.toList());
        //发送远程请求，库存系统检查是否有库存
        List<HasStockTo> data=null;
        try {
            R r = wareFeignService.hasStock(skuIdList);
            data=r.getData(new TypeReference<List<HasStockTo>>() {});
//             data= JSONUtil.toList
//                    (JSONUtil.toJsonStr(r.get("data")),HasStockTo.class);
        }catch (Exception e)
        {
            log.error("spuInfoServiceimpl出现异常");
        }
        List<HasStockTo> finalData = data;
        List<SkuEsModel> collect = skuInfoEntities.stream().map(sku ->
        {
            SkuEsModel esModel = new SkuEsModel();
            BeanUtils.copyProperties(sku,esModel);
            esModel.setSkuPrice(sku.getPrice());
            esModel.setSkuImg(sku.getSkuDefaultImg());
            finalData.forEach(e->
            {
                if(Objects.equals(sku.getSkuId(), e.getSkuId()))
                {
                    esModel.setHasStock(e.getHasStock());
                }
                else {
                    esModel.setHasStock(false);
                }
            });
            //TODO 热度评分
            esModel.setHotScore(0L);
            //查询品牌和分类的名字信息
            BrandEntity byId = brandService.getById(sku.getBrandId());
            esModel.setBrandName(byId.getName());
            esModel.setBrandImg(byId.getLogo());

            CategoryEntity byId1 = categoryService.getById(esModel.getCatalogId());
            esModel.setCatalogName(byId1.getName());

            esModel.setAttrs(attrsList1);
            return esModel;
        }).collect(Collectors.toList());
        //将数据发送给es进行保存
        R r = searchFeignService.productStatusUp(collect);
        if(r.getCode()==0)
        {
            //远程调用成功
            //修改spu状态为上架状态
            SpuInfoEntity spuInfo = new SpuInfoEntity();
            spuInfo.setUpdateTime(new Date());
            spuInfo.setId(spuId);
            spuInfo.setPublishStatus(ProductConstant.StatusEnum.SPU_up.getCode());
            this.updateById(spuInfo);
        }

        else {
            //调用失败
//            TODO 7. 重复调用？又名接口幂等性，重试机制？
                //TODO feign调用流程
                //1.构造请求数据，将对象转为json
//            RequestTemplate template=buildTemplateFromArgs.creat(argv);
                //2.发送请求进行执行(执行成功会解码响应数据)
//                executeAndDecode(template);
                //3.执行请求会有重试机制
//                        while (true)
//                        {
//                            try {
//                                executeAndDecode(template);
//                            }catch ()
//                            {
//                                try {
//                                    retryer.continueOrPropagate(e);
//                                }catch (){
//                                    throw ex;
//                                    continue;
//                                }
//                            }
//                        }
        }

    }

    @Override
    public SpuInfoEntity getSpuInfoBySkuId(Long skuId) {
        //根据skuId获取spu信息
        SkuInfoEntity skuInfoEntity = skuInfoService.getById(skuId);
        Long spuId = skuInfoEntity.getSpuId();
        return getById(spuId);
    }
}