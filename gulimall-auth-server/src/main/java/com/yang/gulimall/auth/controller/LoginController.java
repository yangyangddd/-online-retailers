package com.yang.gulimall.auth.controller;

import cn.hutool.core.lang.TypeReference;
import com.yang.common.utils.R;
import com.yang.constant.AuthServerConstant;
import com.yang.exception.BizCodeEnum;
import com.yang.gulimall.auth.feign.MemberFeign;
import com.yang.gulimall.auth.mail.MailUtils;
import com.yang.gulimall.auth.vo.UserLoginVo;
import com.yang.gulimall.auth.vo.UserRegistVo;
import com.yang.vo.MemberRespVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;

@Controller
public class LoginController {

    @Autowired
    private MailUtils mailUtils;
    @Autowired
    private MemberFeign memberFeign;


    @ResponseBody
    @GetMapping("/sms/sendcode")
    public R sendCode(@RequestParam("mail") String mail)
    {    //TODO 1.接口防刷
         // 2.验证码的再次校验
        if(mailUtils.sendCode(mail)) {
            return R.ok();
        }
        return R.error(BizCodeEnum.SMS_CODE_EXCEPTION.getCode(), BizCodeEnum.SMS_CODE_EXCEPTION.getMsg());
    }

    //TODO 重定向携带数据，利用session原理。将数据放在session中，只要跳到下个页面取出数据以后，session里面的数据就会删掉
    //TODO 分布式session问题
    @PostMapping("/regist")
    public String regist(@Valid UserRegistVo vo, BindingResult result, RedirectAttributes redirectAttributes)
    {
        if(result.hasErrors())
        {
            Map<String,String> errors=new HashMap<>();
            result.getFieldErrors().forEach(e->
            {
                String filed=e.getField();
                String defaultMessage = e.getDefaultMessage();
                errors.put(filed,defaultMessage);
            });
            redirectAttributes.addFlashAttribute("errors",errors);
            return "redirect:http://auth.gulimall.com/reg.html";
        }
        String code=vo.getCode();
        if(mailUtils.checkCode(vo.getEmail(),code))
        {
            R r = memberFeign.regist(vo);
            if(r.getCode()!=0)
            {
                Map<String,String> errors=new HashMap<>();
                errors.put("msg", (String) r.get("msg"));
                redirectAttributes.addFlashAttribute("errors",errors);
                //有错误
                return "redirect:http://auth.gulimall.com/reg.html";
            }
            else {
                //验证码正确
                return "redirect:http://auth.gulimall.com/login.html";
            }
        }
        else {
            Map<String,String> errors=new HashMap<>();
            errors.put("code","验证码错误");
            redirectAttributes.addFlashAttribute("errors",errors);
            return "redirect:http://auth.gulimall.com/reg.html";
        }

    }

    @GetMapping("/login.html")
    public String loginPage(HttpSession session)
    {
        Object attribute = session.getAttribute(AuthServerConstant.LOGIN_USER);
        if(attribute==null)
        {
            return "login";
        }
        else {
            return "redirect:http://gulimall.com";
        }
    }

    @PostMapping("/login")
    public String login(UserLoginVo vo, RedirectAttributes redirectAttributes, HttpSession session)
    {
        R login = memberFeign.login(vo);
        if(login.getCode()!=0)
        {
            Map<String,String> errors=new HashMap<>();
            errors.put("msg", (String) login.get("msg"));
            redirectAttributes.addFlashAttribute("errors",errors);
            return "redirect:http://auth.gulimall.com/login.html";
        }
        MemberRespVo data = login.getData(new TypeReference<MemberRespVo>() {
        });
        session.setAttribute(AuthServerConstant.LOGIN_USER,data);
        return "redirect:http://gulimall.com";
    }
}
