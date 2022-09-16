package com.yang.gulimall.seckill.interceptor;


import cn.hutool.core.text.AntPathMatcher;
import cn.hutool.json.JSONUtil;
import com.yang.constant.AuthServerConstant;
import com.yang.vo.MemberRespVo;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
public class LoginUserInterceptor implements HandlerInterceptor {
    public static ThreadLocal<MemberRespVo> loginUser= new ThreadLocal<>();
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String requestURI = request.getRequestURI();
        AntPathMatcher antPathMatcher = new AntPathMatcher();
        boolean match = antPathMatcher.match("/kill", requestURI);
        if(!match)
        {
            return true;
        }
        Object attribute =  request.getSession().getAttribute(AuthServerConstant.LOGIN_USER);
        MemberRespVo memberRespVo = JSONUtil.toBean(JSONUtil.toJsonStr(attribute), MemberRespVo.class);
        if(attribute!=null)
        {
            loginUser.set(memberRespVo);
            return true;
        }
        else {
            request.getSession().setAttribute("msg","请先进行登录");
            response.sendRedirect("http://auth.gulimall.com/login.html");
            return false;
        }
    }
}
