package com.yang.gulimall.product.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@Data
@NoArgsConstructor
public class Catelog2Vo {
    private String catalog1Id;
    private List<Catelog3Vo> catalog3List;//三级子分类
    private String id;
    private String name;
    @AllArgsConstructor
    @Data
    @NoArgsConstructor
    public static class Catelog3Vo{
        private String catalog2Id;
        private String id;
        private String name;
    }
}
