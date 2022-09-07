package com.yang.gulimall.ware.controller;

import com.yang.common.utils.PageUtils;
import com.yang.common.utils.R;
import com.yang.exception.BizCodeEnum;
import com.yang.gulimall.ware.entity.WareSkuEntity;
import com.yang.gulimall.ware.exception.NoStockException;
import com.yang.gulimall.ware.service.WareSkuService;
import com.yang.gulimall.ware.vo.HasStockVo;
import com.yang.gulimall.ware.vo.LockStockResultVo;
import com.yang.gulimall.ware.vo.WareSkuLockVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;



/**
 * 商品库存
 *
 * @author yang
 * @email yang@gmail.com
 * @date 2022-06-12 09:56:16
 */
@RestController
@RequestMapping("ware/waresku")
public class WareSkuController {
    @Autowired
    private WareSkuService wareSkuService;

    //锁定库存
    @PostMapping("/lock/order")
    public R orderLockStock(@RequestBody WareSkuLockVo vo)
    {
        List<LockStockResultVo> stockResultVos= null;
        try {
            stockResultVos = wareSkuService.orderLockStock(vo);
        } catch (NoStockException e) {
            return R.error(BizCodeEnum.NO_STOCK_EXCEPTION.getCode(),BizCodeEnum.NO_STOCK_EXCEPTION.getMsg());
        }
        return R.ok().setData(stockResultVos);
    }
    @PostMapping("/hasstock")
    public R hasStock(@RequestBody List<Long> skuIds){
        List<HasStockVo> wareSkus = wareSkuService.HasStock(skuIds);
        return R.ok().put("data", wareSkus);
    }
    /**
     * 列表
     */
    @RequestMapping("/list")
    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = wareSkuService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    public R info(@PathVariable("id") Long id){
		WareSkuEntity wareSku = wareSkuService.getById(id);

        return R.ok().put("wareSku", wareSku);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    public R save(@RequestBody WareSkuEntity wareSku){
		wareSkuService.save(wareSku);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    public R update(@RequestBody WareSkuEntity wareSku){
		wareSkuService.updateById(wareSku);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    public R delete(@RequestBody Long[] ids){
		wareSkuService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

}
