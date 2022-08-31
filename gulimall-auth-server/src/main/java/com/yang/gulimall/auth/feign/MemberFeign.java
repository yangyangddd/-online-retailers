package com.yang.gulimall.auth.feign;

import com.yang.common.utils.R;
import com.yang.gulimall.auth.vo.SocialUser;
import com.yang.gulimall.auth.vo.UserLoginVo;
import com.yang.gulimall.auth.vo.UserRegistVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient("gulimall-member")
public interface MemberFeign {
    @PostMapping("member/member/regist")
     R regist(@RequestBody UserRegistVo vo);
    @PostMapping("member/member/login")
     R login(@RequestBody UserLoginVo vo);
    @PostMapping("member/member/oauth/login")
     R oauthLogin(@RequestBody SocialUser socialUser);
}
