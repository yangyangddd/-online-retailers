package com.yang.gulimall.product.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.yang.common.utils.PageUtils;
import com.yang.common.utils.R;
import com.yang.gulimall.product.entity.CategoryBrandRelationEntity;
import com.yang.gulimall.product.service.CategoryBrandRelationService;
import com.yang.gulimall.product.vo.categoryBrandRelationVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;



/**
 * 品牌分类关联
 *
 * @author yang
 * @email yang@gmail.com
 * @date 2022-06-11 19:48:15
 */
@RestController
@RequestMapping("product/categorybrandrelation")
public class CategoryBrandRelationController {
    @Autowired
    private CategoryBrandRelationService categoryBrandRelationService;

    /**
     * 列表
     */
    @RequestMapping("/list")
    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = categoryBrandRelationService.queryPage(params);

        return R.ok().put("page", page);
    }
    @GetMapping("/catelog/list")
    public R catelogList(@RequestParam("brandId")Long brandId){
        List<CategoryBrandRelationEntity> data=
                categoryBrandRelationService.list(new QueryWrapper<CategoryBrandRelationEntity>()
                        .eq("brand_id",brandId));
        return R.ok().put("data", data);
    }

    @GetMapping("/brands/list")
    public R getCategoryBrandRelation(@RequestParam(value = "catId")Long catId){
        List<categoryBrandRelationVo> data=
                categoryBrandRelationService.getCategoryBrandRelation(catId);
        return R.ok().put("data", data);
    }
    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    public R info(@PathVariable("id") Long id){
		CategoryBrandRelationEntity categoryBrandRelation = categoryBrandRelationService.getById(id);

        return R.ok().put("categoryBrandRelation", categoryBrandRelation);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    public R save(@RequestBody CategoryBrandRelationEntity categoryBrandRelation){
		categoryBrandRelationService.saveDetail(categoryBrandRelation);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    public R update(@RequestBody CategoryBrandRelationEntity categoryBrandRelation){
		categoryBrandRelationService.updateById(categoryBrandRelation);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    public R delete(@RequestBody Long[] ids){
		categoryBrandRelationService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

}
