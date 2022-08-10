package com.yang.gulimall.product.controller;

import com.yang.common.utils.PageUtils;
import com.yang.common.utils.R;
import com.yang.gulimall.product.entity.BrandEntity;
import com.yang.gulimall.product.service.BrandService;
import com.yang.valid.addGroup;
import com.yang.valid.updateGroup;
import com.yang.valid.updateShowStatusGroup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.Map;


/**
 * 品牌
 *
 * @author yang
 * @email yang@gmail.com
 * @date 2022-06-11 19:48:15
 */
@RestController
@RequestMapping("product/brand")
public class BrandController {
    @Autowired
    private BrandService brandService;

    /**
     * 列表
     */
    @RequestMapping("/list")
    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = brandService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{brandId}")
    public R info(@PathVariable("brandId") Long brandId){
		BrandEntity brand = brandService.getById(brandId);

        return R.ok().put("brand", brand);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    public R save(@Validated({addGroup.class}) @RequestBody BrandEntity brand/*, BindingResult result*/){
//        Map<String,String> map=new HashMap<>();
//        result.getFieldErrors().forEach((item)->
//        {
//            String message = item.getDefaultMessage();
//            String field = item.getField();
//            map.put(field,message);
//        });
//        if(result.hasErrors())
//        {
//         return R.error(400,"提交的数据不合法").put("data",map);
//        }
//        else {
            brandService.save(brand);
//        }
        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    public R update(@Validated(updateGroup.class) @RequestBody BrandEntity brand){
		brandService.updateDetail(brand);

        return R.ok();
    }
    /**
     * 修改状态
     */
    @RequestMapping("/update/status")
    public R updateShowStatus(@Validated(updateShowStatusGroup.class) @RequestBody BrandEntity brand){
        brandService.updateById(brand);

        return R.ok();
    }
    /**
     * 删除
     */
    @RequestMapping("/delete")
    public R delete(@RequestBody Long[] brandIds){
		brandService.removeByIds(Arrays.asList(brandIds));

        return R.ok();
    }

}
