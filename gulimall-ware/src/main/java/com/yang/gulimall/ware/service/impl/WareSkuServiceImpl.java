package com.yang.gulimall.ware.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yang.common.utils.PageUtils;
import com.yang.common.utils.Query;
import com.yang.gulimall.ware.dao.WareSkuDao;
import com.yang.gulimall.ware.entity.WareSkuEntity;
import com.yang.gulimall.ware.exception.NoStockException;
import com.yang.gulimall.ware.service.WareSkuService;
import com.yang.gulimall.ware.vo.*;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service("wareSkuService")
public class WareSkuServiceImpl extends ServiceImpl<WareSkuDao, WareSkuEntity> implements WareSkuService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        QueryWrapper<WareSkuEntity> wrapper = new QueryWrapper<>();
      String skuId= (String) params.get("skuId");
      if(StringUtils.isNotEmpty(skuId))
      {
          wrapper.eq("sku_id",skuId);
      }
        String wareId= (String) params.get("wareId");
      if(StringUtils.isNotEmpty(wareId))
      {
          wrapper.eq("ware_id",wareId);
      }
        IPage<WareSkuEntity> page = this.page(
                new Query<WareSkuEntity>().getPage(params),
                wrapper
        );

        return new PageUtils(page);
    }

    @Override
    public List<WareSkuEntity> getBySkuId(List<Long> skuId) {
        return this.list(new QueryWrapper<WareSkuEntity>().eq("sku_id",skuId));
    }

    @Override
    public List<HasStockVo> HasStock(List<Long> skuIds) {
        if (!skuIds.isEmpty()) {
            WareSkuDao baseMapper = this.baseMapper;
//        SELECT  sku_id,SUM(stock) FROM `wms_ware_sku` WHERE sku_id IN(2,28) GROUP BY sku_id
            List<HasStock2Vo> list = baseMapper.selectSumStock(skuIds);
            if(list!=null) {
                List<HasStockVo> collect = list.stream().map(e ->
                {
                    HasStockVo hasStockVo = new HasStockVo();
                    hasStockVo.setSkuId(e.getSkuId());
                    hasStockVo.setHasStock(e.getHasStock() > 0);
                    return hasStockVo;
                }).collect(Collectors.toList());
                //根据skuId查询其在所有仓库的总量
                return collect;
            }
        }
        return null;
    }

    //为某个订单锁定库存
    @Override
//    @Transactional(rollbackFor = NoStockException.class)
    public List<LockStockResultVo> orderLockStock(WareSkuLockVo vo) {
        List<OrderItemVo> locks = vo.getLocks();

        //查询每个商品在哪里有库存
        List<Long> skuIds = locks.stream().map(OrderItemVo::getSkuId).collect(Collectors.toList());
        List<WareSkuEntity> wareSkuEntities = this.list(new QueryWrapper<WareSkuEntity>()
                .in("sku_id",skuIds).ge("stock",0));
      //为每一个商品设置SkuWareStock
        List<SkuWareStock> collect1 = skuIds.stream().map(skuId -> {
            SkuWareStock skuWareStock = new SkuWareStock();
            final Integer[] num = {0};
            List<Long> collect = wareSkuEntities.stream().filter(e ->
                            skuId.equals(e.getSkuId())).map(e->
                            {
                               num[0] +=e.getStock();
                               return e.getId();
                            }
                    )
                    .collect(Collectors.toList());

            skuWareStock.setNum(num[0]);
            skuWareStock.setSkuId(skuId);
            skuWareStock.setWareId(collect);
            return skuWareStock;
        }).peek(e->
        {
            //设置库存所需要的量
            for (OrderItemVo lock : locks) {
                if(e.getSkuId().equals(lock.getSkuId()))
                {
                    e.setCount(lock.getCount());
                    break;
                }
            }
        }).collect(Collectors.toList());
        List<LockStockResultVo> stockResultVos=new ArrayList<>();
        for (SkuWareStock skuWareStock : collect1) {
            Long skuId = skuWareStock.getSkuId();
            List<Long> wareId = skuWareStock.getWareId();

            if(wareId==null||wareId.size()==0||skuWareStock.getCount()>skuWareStock.getNum())
            {
                throw new NoStockException(skuId);
                //没有仓库有库存或库存量少于需求量
            }
            //
            //为每个仓库锁定库存
            List<WareSkuEntity> wareSkuEntities1 = this.listByIds(wareId);//查出所有仓库
            Integer count = skuWareStock.getCount();//获取需求总量

            for (WareSkuEntity wareSkuEntity : wareSkuEntities1) {
                LockStockResultVo lockStockResultVo = new LockStockResultVo();

                if(wareSkuEntity.getStock()>count)//当前仓库存量大于当前需求量
                {
                    wareSkuEntity.setStockLocked(count+wareSkuEntity.getStock()-wareSkuEntity.getStockLocked());
                    lockStockResultVo.setNum(skuWareStock.getCount());
                    lockStockResultVo.setLocked(true);
                    lockStockResultVo.setSkuId(skuWareStock.getSkuId());
                    wareSkuEntity.setStockLocked(count+wareSkuEntity.getStockLocked());//当前锁定仓库即为count值
                    this.updateById(wareSkuEntity);
                    break;
                }
                else {
                    //当前仓库存量小于当前需求量
                    lockStockResultVo.setNum//锁了多少件设置为当前仓库总量和自身的值
                            (wareSkuEntity.getStock()-wareSkuEntity.getStockLocked()+ lockStockResultVo.getNum());
                    lockStockResultVo.setLocked(false);
                    lockStockResultVo.setSkuId(skuWareStock.getSkuId());
                    count-=wareSkuEntity.getStock();//count减去当前仓库的库存值
                    wareSkuEntity.setStockLocked(wareSkuEntity.getStock());//设置锁定库存数量
                }
                stockResultVos.add(lockStockResultVo);
                this.updateById(wareSkuEntity);
            }

        }
        return stockResultVos;
    }


}