package com.yang.gulimall.product.controller;

import com.yang.common.utils.PageUtils;
import com.yang.common.utils.R;
import com.yang.gulimall.product.entity.AttrEntity;
import com.yang.gulimall.product.entity.AttrGroupEntity;
import com.yang.gulimall.product.service.AttrGroupService;
import com.yang.gulimall.product.service.CategoryService;
import com.yang.gulimall.product.vo.AttrGroupRelationVo;
import com.yang.gulimall.product.vo.GroupWithAttrVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;



/**
 * 属性分组
 *
 * @author yang
 * @email yang@gmail.com
 * @date 2022-06-11 19:48:15
 */
@RestController
@RequestMapping("product/attrgroup")
public class AttrGroupController {
    @Autowired
    private AttrGroupService attrGroupService;
    @Autowired
    private CategoryService categoryService;
    @GetMapping("{attrgroupId}/attr/relation")
    public R attRelation(@PathVariable("attrgroupId") Long attrGroupId)
    {
        List<AttrEntity> entityList =attrGroupService.getRelationAttr(attrGroupId);
        //根据分组id获取其对应的属性分组信息，即根据arrtGroupId查询对应的attrId
        return R.ok().put("data",entityList);
    }

    @PostMapping("/attr/relation")
    public R addAttRelation(@RequestBody AttrGroupRelationVo[] vos)//根据参数向分组id添加对应的属性
    {
        attrGroupService.addRelationByGroupIdAndAttrId(vos);
        return R.ok();
    }
    @PostMapping("/attr/relation/delete")
    public R delAttRelation(@RequestBody AttrGroupRelationVo[] vos)//根据参数向分组id添加对应的属性
    {
        attrGroupService.delRelationByGroupIdAndAttrId(vos);
        return R.ok();
    }
    ///product/attrgroup/{attrgroupId}/noattr/relation
    @GetMapping("{attrgroupId}/noattr/relation")
    public R getAttRelation(@PathVariable("attrgroupId") Long attrGroupId,
                            @RequestParam Map<String, Object> params)
    {
        PageUtils page =attrGroupService.getNoRelationAttr(params,attrGroupId);
        //获取属性分组里面还没有关联的本分类里面的其他基本属性，方便添加新的关联
        return R.ok().put("page",page);
    }
    @GetMapping("/{catelogId}/withattr")
    public R getALlGroupWithAttr(@PathVariable("catelogId") Long catelogId)
    {
        //获取分类下所有分组&关联属性
        List<GroupWithAttrVo> data=attrGroupService.getALlGroupWithAttr(catelogId);
        return R.ok().put("data",data);
    }


    /**
     * 列表
     */
    @RequestMapping("/list/{catelogId}")
    public R list(@RequestParam Map<String, Object> params,@PathVariable("catelogId") Long catelogId){
//        PageUtils page = attrGroupService.queryPage(params);
        PageUtils page =attrGroupService.queryPage(params,catelogId);
        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{attrGroupId}")
    public R info(@PathVariable("attrGroupId") Long attrGroupId){
		AttrGroupEntity attrGroup = attrGroupService.getById(attrGroupId);
        Long catelogId = attrGroup.getCatelogId();
       Long[] path= categoryService.findCatelogPath(catelogId);
       attrGroup.setCatelogPath(path);
        return R.ok().put("attrGroup", attrGroup);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    public R save(@RequestBody AttrGroupEntity attrGroup){
		attrGroupService.save(attrGroup);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    public R update(@RequestBody AttrGroupEntity attrGroup){
		attrGroupService.updateById(attrGroup);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    public R delete(@RequestBody Long[] attrGroupIds){
		attrGroupService.removeByIds(Arrays.asList(attrGroupIds));

        return R.ok();
    }

}
