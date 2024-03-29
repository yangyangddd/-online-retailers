package com.yang.gulimall.order.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * spu信息
 * 
 * @author yang
 * @email yang@gmail.com
 * @date 2022-06-11 19:48:15
 */
@Data
public class SpuInfoVo {

	/**
	 * 商品id
	 */
	private Long id;
	/**
	 * 商品名称
	 */
	private String spuName;
	/**
	 * 商品描述
	 */
	private String spuDescription;
	/**
	 * 所属分类id
	 */
	private Long catalogId;
	/**
	 * 品牌id
	 */
	private Long brandId;
	/**
	 * 
	 */
	private BigDecimal weight;
	/**
	 * 上架状态[0 - 下架，1 - 上架]
	 */
	private Integer publishStatus;
	/**
	 * 
	 */
	private Date createTime;
	/**
	 * 
	 */
	private Date updateTime;

}
