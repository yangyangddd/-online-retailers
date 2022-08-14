package com.yang.gulimall.ware.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yang.common.utils.PageUtils;
import com.yang.gulimall.ware.entity.PurchaseEntity;
import com.yang.gulimall.ware.vo.MergeVo;
import com.yang.gulimall.ware.vo.doneVo;

import java.util.List;
import java.util.Map;

/**
 * 采购信息
 *
 * @author yang
 * @email yang@gmail.com
 * @date 2022-06-12 09:56:16
 */
public interface PurchaseService extends IService<PurchaseEntity> {

    PageUtils queryPage(Map<String, Object> params);

    PageUtils queryPageUnreceive(Map<String, Object> params);

    void merge(MergeVo vo);

    void received(List<Long> ids);


    void done(doneVo vo);
}

