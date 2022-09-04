package com.yang.gulimall.order.feign;

import com.yang.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@FeignClient("gulimall-member")
public interface MemberFeign {
    @RequestMapping("member/memberreceiveaddress/member/{id}")
    public R getByMemberId(@PathVariable("id") Long id);
}
