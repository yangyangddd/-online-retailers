package com.yang.gulimall.order.dao;

import com.yang.gulimall.order.entity.OrderEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 订单
 * 
 * @author yang
 * @email yang@gmail.com
 * @date 2022-06-12 09:43:35
 */
@Mapper
public interface OrderDao extends BaseMapper<OrderEntity> {
	
}
