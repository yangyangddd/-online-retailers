package com.yang.cart.interceptor;

import cn.hutool.json.JSONUtil;
import com.yang.cart.To.UserInfoTo;
import com.yang.constant.AuthServerConstant;
import com.yang.constant.CartConstant;
import com.yang.vo.MemberRespVo;
import org.apache.commons.lang.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.UUID;

//在执行目标方法之前，判断用户的登录状态,并封装

public class CartInterceptor implements HandlerInterceptor {
    //目标方法执行之前拦截
    public static ThreadLocal<UserInfoTo> toThreadLocal=new ThreadLocal<>();
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        UserInfoTo userInfoTo=new UserInfoTo();

        HttpSession session = request.getSession();
        String s = JSONUtil.toJsonStr(session.getAttribute(AuthServerConstant.LOGIN_USER));
        MemberRespVo memberRespVo = JSONUtil.toBean(s, MemberRespVo.class);
        if(memberRespVo!=null)
        {
            //用户已登录
            userInfoTo.setUserId(memberRespVo.getId());

        }
        Cookie[] cookies = request.getCookies();
        if(cookies!=null&&cookies.length>0)
        {
            for (Cookie cookie : cookies) {
                if(CartConstant.TEMP_USER_COOKIE_NAME.equals(cookie.getName()))
                {
                    userInfoTo.setUserKey(cookie.getValue());
                }
            }
        }
        if(StringUtils.isEmpty(userInfoTo.getUserKey()))
        {
            //自定义临时用户
            String uuid= UUID.randomUUID().toString();
            userInfoTo.setUserKey(uuid);
        }
        //目标方法执行之前
        toThreadLocal.set(userInfoTo);
        return true;
    }

    //业务执行之后
    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        UserInfoTo userInfoTo = toThreadLocal.get();
        Cookie cookie = new Cookie(CartConstant.TEMP_USER_COOKIE_NAME, userInfoTo.getUserKey());
        cookie.setDomain("gulimall.com");
        cookie.setMaxAge(CartConstant.TEMP_USER_COOKIE_TIMEOUT);
        response.addCookie(cookie);
        toThreadLocal.remove();
    }
}
