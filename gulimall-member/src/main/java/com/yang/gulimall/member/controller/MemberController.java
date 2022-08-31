package com.yang.gulimall.member.controller;

import com.yang.common.utils.PageUtils;
import com.yang.common.utils.R;
import com.yang.exception.BizCodeEnum;
import com.yang.gulimall.member.entity.MemberEntity;
import com.yang.gulimall.member.exception.EmailExistException;
import com.yang.gulimall.member.exception.UsernameExistException;
import com.yang.gulimall.member.service.MemberService;
import com.yang.gulimall.member.vo.MemberLoginVo;
import com.yang.gulimall.member.vo.MemberRegistVo;
import com.yang.gulimall.member.vo.SocialUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.Map;



/**
 * 会员
 *
 * @author yang
 * @email yang@gmail.com
 * @date 2022-06-12 09:36:22
 */
@RestController
@RequestMapping("member/member")
public class MemberController {
    @Autowired
    private MemberService memberService;

    @PostMapping("/oauth/login")
    public R oauthLogin(@RequestBody SocialUser socialUser)
    {
      MemberEntity memberEntity=  memberService.login(socialUser);

      return R.ok().setData(memberEntity);
    }
    @PostMapping("/regist")
    public R regist(@RequestBody MemberRegistVo vo)
    {
        try {
            memberService.regis(vo);
        } catch (EmailExistException e) {
            return R.error(BizCodeEnum.EMAIL_EXIST_EXCEPTION.getCode(),BizCodeEnum.EMAIL_EXIST_EXCEPTION.getMsg());
        }catch (UsernameExistException e){
            return R.error(BizCodeEnum.USER_EXIST_EXCEPTION.getCode(),BizCodeEnum.USER_EXIST_EXCEPTION.getMsg());
        }
        return R.ok();
    }
    @PostMapping("/login")
    public R login(@RequestBody MemberLoginVo vo)
    {
        MemberEntity memberEntity=memberService.login(vo);
        if(memberEntity!=null) {
            return R.ok().setData(memberEntity);
        }
        else {
            return R.error(BizCodeEnum.LOGINACTT_PASSWORD_INVALID_EXCEPTION.getCode(), BizCodeEnum.LOGINACTT_PASSWORD_INVALID_EXCEPTION.getMsg());
        }
    }
    /**
     * 列表
     */
    @RequestMapping("/list")
    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = memberService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    public R info(@PathVariable("id") Long id){
		MemberEntity member = memberService.getById(id);

        return R.ok().put("member", member);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    public R save(@RequestBody MemberEntity member){
		memberService.save(member);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    public R update(@RequestBody MemberEntity member){
		memberService.updateById(member);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    public R delete(@RequestBody Long[] ids){
		memberService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

}
