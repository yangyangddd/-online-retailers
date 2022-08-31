package com.yang.gulimall.member.service.impl;

import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yang.common.utils.PageUtils;
import com.yang.common.utils.Query;
import com.yang.gulimall.member.dao.MemberDao;
import com.yang.gulimall.member.entity.MemberEntity;
import com.yang.gulimall.member.entity.MemberLevelEntity;
import com.yang.gulimall.member.exception.EmailExistException;
import com.yang.gulimall.member.exception.UsernameExistException;
import com.yang.gulimall.member.service.MemberLevelService;
import com.yang.gulimall.member.service.MemberService;
import com.yang.gulimall.member.vo.MemberLoginVo;
import com.yang.gulimall.member.vo.MemberRegistVo;
import com.yang.gulimall.member.vo.SocialUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Map;
import java.util.UUID;


@Service("memberService")
public class MemberServiceImpl extends ServiceImpl<MemberDao, MemberEntity> implements MemberService {

    @Autowired
    MemberLevelService memberLevelService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<MemberEntity> page = this.page(
                new Query<MemberEntity>().getPage(params),
                new QueryWrapper<MemberEntity>()
        );

        return new PageUtils(page);
    }
    @Override
    public void regis(MemberRegistVo vo)  {
        MemberDao baseMapper = this.baseMapper;
        MemberEntity entity =new MemberEntity();
        //设置默认等级
        MemberLevelEntity one = memberLevelService.
                getOne(new QueryWrapper<MemberLevelEntity>().
                        eq("default_status",1));
        entity.setLevelId(one.getId());

        checkEmailUnique(vo.getEmail());
        entity.setEmail(vo.getEmail());

        checkUsername(vo.getUsername());
        entity.setUsername(vo.getUsername());

        BCryptPasswordEncoder passwordEncoder=new BCryptPasswordEncoder();
        String encode = passwordEncoder.encode(vo.getPassword());

        entity.setPassword(encode);
        //密码需要加密存储
        baseMapper.insert(entity);
    }

    @Override
    public void checkEmailUnique(String email) throws EmailExistException {
        long email1 = this.count(new QueryWrapper<MemberEntity>().eq("email", email));
        if(email1>0)
        throw new EmailExistException();
    }

    @Override
    public void checkUsername(String username) throws UsernameExistException{
        long username1 = this.count(new QueryWrapper<MemberEntity>().eq("username", username));
        if(username1>0)
        throw new UsernameExistException();
    }

    @Override
    public MemberEntity login(MemberLoginVo vo) {
        String loginacct = vo.getLoginacct();
        String password = vo.getPassword();
        MemberEntity one = this.getOne(new QueryWrapper<MemberEntity>().
                eq("username", loginacct).or().
                eq("email", loginacct));
        if (one != null) {
            BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
            boolean matches = passwordEncoder.matches(password, one.getPassword());
            if (matches) {
                return one;
            }
        }
            return null;
    }

    @Override
    public MemberEntity login(SocialUser socialUser) {
        //获取所有的用户信息
        String url = "https://gitee.com/api/v5/user?access_token=" + socialUser.getAccess_token();
        String s = HttpUtil.get(url);
        JSONObject jsonObject = JSONUtil.parseObj(s);
        System.out.println("JsonObect"+jsonObject);
        String id = jsonObject.get("id").toString();
        //判断当前社交用户是否已经登录过系统
        MemberEntity entity = this.getOne(new QueryWrapper<MemberEntity>().
                eq("social_uid", id));
        if(entity!=null)
        {

           return entity;
            //用户已经注册了，则直接登录
        }
        else {
            MemberEntity memberEntity = new MemberEntity();
            memberEntity.setSocialUid(id);
            //邮箱为空就让用户绑定邮箱
            if(JSONUtil.isNull(jsonObject.get("email")))
            {
                //
            }
            //邮箱有重复就抛异常
           else
            {
                checkEmailUnique((String) jsonObject.get("email"));
                memberEntity.setEmail((String) jsonObject.get("email"));
            }

            boolean name = userIsExist((String) jsonObject.get("name"));
            if(name)//获取的用户名已存在
            {
                int i=1;
                while (true) {
                    String s1 = UUID.randomUUID().toString();
                    String substring =s1.substring(0, i);
                    if(userIsExist(jsonObject.get("name")+substring))
                    {
                        i++;
                    }
                    else
                    {
                        memberEntity.setUsername(s1);
                        break;
                    }
                    if(i>31)
                    {
                        throw new RuntimeException();
                    }
                }
                //设置随机名字
            }else
            {
                memberEntity.setUsername((String) jsonObject.get("name"));
            }

            memberEntity.setAccessToken(socialUser.getAccess_token());
            memberEntity.setExpiresIn(socialUser.getExpires_in());
            memberEntity.setNickname(UUID.randomUUID().toString().substring(0,10));
            memberEntity.setCreateTime(new Date());
            //设置默认等级
            MemberLevelEntity one = memberLevelService.
                    getOne(new QueryWrapper<MemberLevelEntity>().
                            eq("default_status",1));
            memberEntity.setLevelId(one.getId());
            //未注册过，则向数据库写入获取的信息
            this.baseMapper.insert(memberEntity);
            return memberEntity;
        }

    }
    public boolean userIsExist(String username)//判断用户是否存在,存在返回true，否则返回false
    {
        try {
            checkUsername(username);
        } catch (UsernameExistException e) {
            return true;
        }
        return false;
    }
}