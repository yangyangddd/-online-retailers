package com.yang.gulimall.ware;

import com.yang.gulimall.ware.service.WareSkuService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;

@SpringBootTest
class GulimallWareApplicationTests {

    @Autowired
    private WareSkuService wareSkuService;
    @Test
    void contextLoads() {
        List<Long> objects = new ArrayList<>();
        objects.add(28L) ;
        objects.add(2L);
        wareSkuService.HasStock(objects);
    }

}
