package com.yang.gulimall.ware.service.impl;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yang.common.utils.PageUtils;
import com.yang.common.utils.Query;
import com.yang.common.utils.R;
import com.yang.constant.WarePurchaseConstant;
import com.yang.gulimall.ware.dao.PurchaseDao;
import com.yang.gulimall.ware.entity.PurchaseDetailEntity;
import com.yang.gulimall.ware.entity.PurchaseEntity;
import com.yang.gulimall.ware.entity.WareSkuEntity;
import com.yang.gulimall.ware.feign.ProductFeignService;
import com.yang.gulimall.ware.service.PurchaseDetailService;
import com.yang.gulimall.ware.service.PurchaseService;
import com.yang.gulimall.ware.service.WareSkuService;
import com.yang.gulimall.ware.vo.MergeVo;
import com.yang.gulimall.ware.vo.PurchaseItemDoneVo;
import com.yang.gulimall.ware.vo.doneVo;
import com.yang.to.SkuInfoEntityTo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service("purchaseService")
public class PurchaseServiceImpl extends ServiceImpl<PurchaseDao, PurchaseEntity> implements PurchaseService {

    @Autowired
    private PurchaseDetailService purchaseDetailService;
    @Autowired
    private WareSkuService wareSkuService;
    @Autowired
    private ProductFeignService productFeignService;
    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<PurchaseEntity> page = this.page(
                new Query<PurchaseEntity>().getPage(params),
                new QueryWrapper<PurchaseEntity>()
        );
        return new PageUtils(page);
    }
    @Override
    public PageUtils queryPageUnreceive(Map<String, Object> params) {
        QueryWrapper<PurchaseEntity> wrapper = new QueryWrapper<>();
        IPage<PurchaseEntity> page = this.page(
                new Query<PurchaseEntity>().getPage(params),
                wrapper.eq("status",0).or().eq("status",1)
        );
        return new PageUtils(page);
    }
    @Override
    @Transactional
    public void merge(MergeVo vo) {
        Long purchaseId = vo.getPurchaseId();
        if(purchaseId==null) { //没有选择任何【采购单】，将自动创建新单进行合并
            PurchaseEntity purchaseEntity = new PurchaseEntity();
            purchaseEntity.setStatus(WarePurchaseConstant.CREATED.getCode());
            purchaseEntity.setCreateTime(new Date());
            purchaseEntity.setUpdateTime(new Date());
            this.save(purchaseEntity);
            purchaseId = purchaseEntity.getId();
        }
        //确认采购状态是0，1才可以合并

        List<Long> items = vo.getItems();
        if(items!=null&&!items.isEmpty()) {
            List<PurchaseDetailEntity> purchaseDetailEntities = purchaseDetailService.listByIds(items);
            for (PurchaseDetailEntity purchaseDetailEntity : purchaseDetailEntities) {
                if(purchaseDetailEntity.getStatus()==WarePurchaseConstant.WarePurchaseDetailStatusConstant.CREATED.getCode()
                ||purchaseDetailEntity.getStatus()==WarePurchaseConstant.WarePurchaseDetailStatusConstant.ASSIGNED.getCode()) {
                    purchaseDetailEntity.setPurchaseId(purchaseId);
                    purchaseDetailEntity.setStatus(WarePurchaseConstant.WarePurchaseDetailStatusConstant.ASSIGNED.getCode());
                }
            }
            purchaseDetailService.updateBatchById(purchaseDetailEntities);
            PurchaseEntity purchase=new PurchaseEntity();
            purchase.setId(purchaseId);
            purchase.setUpdateTime(new Date());
            this.updateById(purchase);
        }
    }
    @Override
    @Transactional
    public void received(List<Long> ids) {
        //1. 确认当前采购单是新建或者已分配状态
        List<PurchaseEntity> purchaseEntities = this.listByIds(ids);
        List<PurchaseEntity> collect = purchaseEntities.stream().filter(item -> {
                    if (item.getStatus() == WarePurchaseConstant.WarePurchaseDetailStatusConstant.CREATED.getCode()
                            || item.getStatus() == WarePurchaseConstant.WarePurchaseDetailStatusConstant.ASSIGNED.getCode()) ;
                    return true;
                }
        ).map(item->
        {
            item.setStatus(WarePurchaseConstant.WarePurchaseDetailStatusConstant.RECEIVE.getCode());
            item.setUpdateTime(new Date());
            return item;
        }).collect(Collectors.toList());
        //2.改变采购单的状态
        this.updateBatchById(collect);
        //3.改变采购项的状态
        collect.forEach(item->
        {
            List<PurchaseDetailEntity> entities=purchaseDetailService.listDetailByPurchaseId(item.getId());
            List<PurchaseDetailEntity> collect1 = entities.stream().map(entity ->
            {
                PurchaseDetailEntity purchaseDetailEntity = new PurchaseDetailEntity();
                purchaseDetailEntity.setId(entity.getId());
                purchaseDetailEntity.setStatus(WarePurchaseConstant.WarePurchaseDetailStatusConstant.RECEIVE.getCode());
                return purchaseDetailEntity;
            }).collect(Collectors.toList());
            purchaseDetailService.updateBatchById(collect1);
        });
    }

    @Override
    @Transactional
    public void done(doneVo vo) {
        //1.改变采购单状态
        Long id = vo.getId();
        PurchaseEntity purchase = new PurchaseEntity();
        purchase.setId(id);
        purchase.setUpdateTime(new Date());
        List<PurchaseItemDoneVo> items = vo.getItems();
        for (PurchaseItemDoneVo e : items) {//获取item的采购项id集合
            if(e.getStatus()==WarePurchaseConstant.WarePurchaseDetailStatusConstant.HASERROR.getCode())
            {
                purchase.setStatus(WarePurchaseConstant.HASERROR.getCode());

                break;
            }
        }
        if(purchase.getStatus()==null)
            purchase.setStatus(WarePurchaseConstant.FINISH.getCode());
        this.updateById(purchase);
        //2.改变采购项的状态
        List<PurchaseDetailEntity> list=new ArrayList<>();
        List<PurchaseItemDoneVo> collect = items.stream().peek(e ->
        {
            PurchaseDetailEntity purchaseDetailEntity = new PurchaseDetailEntity();
            purchaseDetailEntity.setId(e.getItemId());
            purchaseDetailEntity.setStatus(e.getStatus());
            list.add(purchaseDetailEntity);
        }).filter(e->
        e.getStatus()==WarePurchaseConstant.WarePurchaseDetailStatusConstant.FINISH.getCode()
        ).collect(Collectors.toList());
        purchaseDetailService.updateBatchById(list);
        //3.将成功采购的进行入库
        collect.forEach(e->
                {
                    PurchaseDetailEntity byId = purchaseDetailService.getById(e.getItemId());//根据成功采购的id集合获取每个采购项
                    Long skuId = byId.getSkuId();
                    Integer skuNum = byId.getSkuNum();
                    WareSkuEntity wareSkuEntity = new WareSkuEntity();
                    wareSkuEntity.setSkuId(skuId);
                    wareSkuEntity.setStock(skuNum);
                    WareSkuEntity wareSkuEntity1 = wareSkuService.
                            getOne(new QueryWrapper<WareSkuEntity>().eq("sku_id", skuId));
                    if(wareSkuEntity1!=null){//加库存
                        wareSkuEntity1.setStock(wareSkuEntity1.getStock()+skuNum);
                        wareSkuService.updateById(wareSkuEntity1);
                    }
                    else {//创建新的商品并加上该库存
                        wareSkuEntity1=new WareSkuEntity();
                        wareSkuEntity1.setSkuId(skuId);
                        wareSkuEntity1.setWareId(byId.getWareId());
                        //根据skuId获取sku_name//这里需要远程调用商品服务
                        //TODO 查询SKU的名字,如果失败，整个事务无需回滚
                        //TODO 还可以用什么办法让异常出现后不回滚
                        R info = null;
                        try {
                            info = productFeignService.info(skuId);
                            if(info.getCode()!=0)
                            {
                                log.error("调用商品服务失败");
                            }
                        } catch (Exception ex) {

                        }
                        Object skuInfo1 = info.get("skuInfo");
                        String s = JSONUtil.toJsonStr(skuInfo1);
                        SkuInfoEntityTo skuInfo = JSONUtil.toBean(s, SkuInfoEntityTo.class);
                        wareSkuEntity1.setSkuName(skuInfo.getSkuName());
                        wareSkuEntity1.setStock(byId.getSkuNum());
                        wareSkuEntity1.setStockLocked(0);
                        wareSkuService.save(wareSkuEntity1);
                    }
                }
                );
    }
}