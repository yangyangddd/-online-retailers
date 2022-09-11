package com.yang.gulimall.ware.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yang.common.utils.PageUtils;
import com.yang.gulimall.ware.entity.WareSkuEntity;
import com.yang.gulimall.ware.vo.HasStockVo;
import com.yang.gulimall.ware.vo.LockStockResultVo;
import com.yang.gulimall.ware.vo.WareSkuLockVo;
import com.yang.to.OrderVo;
import com.yang.to.mq.StockLockedTo;

import java.util.List;
import java.util.Map;

/**
 * 商品库存
 *
 * @author yang
 * @email yang@gmail.com
 * @date 2022-06-12 09:56:16
 */
public interface WareSkuService extends IService<WareSkuEntity> {

    PageUtils queryPage(Map<String, Object> params);

    List<WareSkuEntity> getBySkuId(List<Long> skuId);

    List<HasStockVo> HasStock(List<Long> skuIds);

    List<LockStockResultVo> orderLockStock(WareSkuLockVo vo);
    public void unLockStock(Long skuId,Long wareId,Integer num,Long taskDetailId);

    void unlockStock(StockLockedTo stockLockedTo);

    void unlockStock(OrderVo vo);
}

