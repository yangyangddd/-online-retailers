package com.yang.gulimall.ware.service.impl;

import cn.hutool.core.lang.TypeReference;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yang.common.utils.PageUtils;
import com.yang.common.utils.Query;
import com.yang.common.utils.R;
import com.yang.gulimall.ware.dao.WareInfoDao;
import com.yang.gulimall.ware.entity.WareInfoEntity;
import com.yang.gulimall.ware.feign.MemberFeignService;
import com.yang.gulimall.ware.service.WareInfoService;
import com.yang.gulimall.ware.vo.FareVo;
import com.yang.gulimall.ware.vo.MemberAddressVo;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Map;


@Service("wareInfoService")
public class WareInfoServiceImpl extends ServiceImpl<WareInfoDao, WareInfoEntity> implements WareInfoService {

    @Autowired
    MemberFeignService memberFeignService;
    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        QueryWrapper<WareInfoEntity> wrapper = new QueryWrapper<>();
        String key= (String) params.get("key");
        if(StringUtils.isNotEmpty(key))
        {
            wrapper.eq("id",key).or().like("name",key).or().like("address",key).or().
                    like("areacode",key);
        }
        IPage<WareInfoEntity> page = this.page(
                new Query<WareInfoEntity>().getPage(params),
               wrapper
        );

        return new PageUtils(page);
    }

    @Override
    public FareVo getFare(Long addrId) {
        FareVo fareVo = new FareVo();
        R info = memberFeignService.addrInfo(addrId);
        MemberAddressVo data = info.getData("memberReceiveAddress",new TypeReference<MemberAddressVo>() {
        });
        if(data!=null) {
            String phone = data.getPhone();
            String substring = phone.substring(phone.length() - 1, phone.length());
            fareVo.setAddress(data);
            fareVo.setFare(new BigDecimal(substring));
            return fareVo;
        }
        return null;
    }

}