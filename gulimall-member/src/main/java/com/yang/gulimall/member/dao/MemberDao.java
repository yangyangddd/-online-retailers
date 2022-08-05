package com.yang.gulimall.member.dao;

import com.yang.gulimall.member.entity.MemberEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 会员
 * 
 * @author yang
 * @email yang@gmail.com
 * @date 2022-06-12 09:36:22
 */
@Mapper
public interface MemberDao extends BaseMapper<MemberEntity> {
	
}
