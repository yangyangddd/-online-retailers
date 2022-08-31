package com.yang.gulimall.member.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yang.common.utils.PageUtils;
import com.yang.gulimall.member.entity.MemberEntity;
import com.yang.gulimall.member.exception.EmailExistException;
import com.yang.gulimall.member.exception.UsernameExistException;
import com.yang.gulimall.member.vo.MemberLoginVo;
import com.yang.gulimall.member.vo.MemberRegistVo;
import com.yang.gulimall.member.vo.SocialUser;

import java.util.Map;

/**
 * 会员
 *
 * @author yang
 * @email yang@gmail.com
 * @date 2022-06-12 09:36:22
 */
public interface MemberService extends IService<MemberEntity>  {

    PageUtils queryPage(Map<String, Object> params);

    void regis(MemberRegistVo vo);
    void checkEmailUnique(String email) throws EmailExistException;
    void checkUsername(String username) throws UsernameExistException;

    MemberEntity login(MemberLoginVo vo);

    MemberEntity login(SocialUser socialUser);

}

