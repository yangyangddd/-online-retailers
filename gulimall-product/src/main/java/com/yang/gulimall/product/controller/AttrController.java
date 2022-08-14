package com.yang.gulimall.product.controller;

import com.yang.common.utils.PageUtils;
import com.yang.common.utils.R;
import com.yang.gulimall.product.entity.ProductAttrValueEntity;
import com.yang.gulimall.product.service.AttrService;
import com.yang.gulimall.product.service.ProductAttrValueService;
import com.yang.gulimall.product.vo.AttrRespVo;
import com.yang.gulimall.product.vo.attrVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;



/**
 * 商品属性
 *
 * @author yang
 * @email yang@gmail.com
 * @date 2022-06-11 19:48:15
 */
@RestController
@RequestMapping("product/attr")
public class AttrController {
    @Autowired
    private AttrService attrService;
    @Autowired
    private ProductAttrValueService productAttrValueService;

    /**
     * 列表
     */
    @RequestMapping("/list")
    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = attrService.queryPage(params);

        return R.ok().put("page", page);
    }

    /**
     /product/attr/base/list/{catelogId}
     */
    @RequestMapping("/{attrType}/list/{catelogId}")
    public R baseAttrList(@RequestParam Map<String, Object> params ,@PathVariable("catelogId") Long catelogId,
    @PathVariable("attrType") String type){
//        PageUtils page = attrService.queryPage(params);
        PageUtils page = attrService.queryTypePage(params,catelogId,type);
        return R.ok().put("page", page);
    }
///product/attr/base/listforspu/{spuId}
@GetMapping("/base/listforspu/{spuId}")
public R listForSpu(@PathVariable("spuId") Long spuId){
        List<ProductAttrValueEntity> entities=productAttrValueService.baseAttrListForSpu(spuId);
    return R.ok().put("data", entities);
}
///product/attr/update/{spuId}
@PostMapping("/update/{spuId}")
public R updateForSpu(@RequestBody List<ProductAttrValueEntity> entities, @PathVariable("spuId") Long spuId){
    productAttrValueService.baseAttrUpdateForSpu(spuId,entities);
    return R.ok();
}
    /**
     * 信息
     */
    @RequestMapping("/info/{attrId}")
    public R info(@PathVariable("attrId") Long attrId){
//		AttrEntity attr = attrService.getById(attrId);
        AttrRespVo attr=attrService.getAttrInfo(attrId);
        return R.ok().put("attr", attr);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    public R save(@RequestBody attrVo attr){
		attrService.saveAttr(attr);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    public R update(@RequestBody attrVo attr){
		attrService.updateAttr(attr);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    public R delete(@RequestBody Long[] attrIds){
		attrService.removeByIds(Arrays.asList(attrIds));

        return R.ok();
    }

}
