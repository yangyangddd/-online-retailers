package com.yang.gulimall.auth.controller;

import cn.hutool.core.lang.TypeReference;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONUtil;
import com.yang.common.utils.R;
import com.yang.constant.AuthServerConstant;
import com.yang.gulimall.auth.config.GiteeLoginConfigProperties;
import com.yang.gulimall.auth.feign.MemberFeign;
import com.yang.vo.MemberRespVo;
import com.yang.gulimall.auth.vo.SocialUser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;

@Controller
@Slf4j
public class OAuthController {
    @Autowired
    private GiteeLoginConfigProperties gitee;
    @Autowired
    MemberFeign memberFeign;
    @GetMapping("/giteeLogin")
    public String giteeLogin()
    {
        String url = "https://gitee.com/oauth/authorize?response_type=code" +
                "&client_id=" + gitee.getClientId() +
                "&redirect_uri=" + gitee.getUrl() +
                "&scope=user_info";
        return "redirect:"+url;
    }
    @GetMapping("/oauth/gitee/success")
    public String gitee(@RequestParam("code") String code, HttpSession session)
    {
        Map<String,Object> param=new HashMap<>();
        param.put("grant_type","authorization_code");
        param.put("code",code);
        param.put("client_id",gitee.getClientId());
        param.put("redirect_uri","http://auth.gulimall.com/oauth/gitee/success");
        param.put("client_secret",gitee.getClientSecret());
        String post = HttpUtil.post("https://gitee.com/oauth/token", param);
       if(post!=null) {
           SocialUser socialUser = JSONUtil.toBean(post, SocialUser.class);
           //根据code获取accessToken
           R r = memberFeign.oauthLogin(socialUser);

           if (r.getCode() == 0) {
               //登录成功就跳回首页
               MemberRespVo data = r.getData(new TypeReference<MemberRespVo>() {
               });
               log.info("登录成功"+data.toString());
               session.setAttribute(AuthServerConstant.LOGIN_USER,data);
               return "redirect:http://gulimall.com";
           } else {
               //登录失败就返回登录页面
               return "redirect:http://auth.gulimall.com";
           }
       }
        return "redirect:http://auth.gulimall.com";
    }
}
