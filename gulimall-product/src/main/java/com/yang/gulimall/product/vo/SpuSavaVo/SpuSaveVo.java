package com.yang.gulimall.product.vo.SpuSavaVo;

/**
 * Copyright 2022 json.cn
 */
import lombok.Data;

import java.util.List;

/**
 * Auto-generated: 2022-08-10 22:23:26
 *
 * @author json.cn (i@json.cn)
 * @website http://www.json.cn/java2pojo/
 */
@Data
public class SpuSaveVo {

    private String spuName;
    private String spuDescription;
    private int catalogId;
    private int brandId;
    private int weight;
    private int publishStatus;
    private List<String> decript;
    private List<String> images;
    private Bounds bounds;
    private List<BaseAttrs> baseAttrs;
    private List<Skus> skus;


}
