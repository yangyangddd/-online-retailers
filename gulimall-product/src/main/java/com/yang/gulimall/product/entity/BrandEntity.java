package com.yang.gulimall.product.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.yang.valid.ListValue;
import com.yang.valid.addGroup;
import com.yang.valid.updateGroup;
import com.yang.valid.updateShowStatusGroup;
import lombok.Data;
import org.hibernate.validator.constraints.URL;

import javax.validation.constraints.*;
import java.io.Serializable;

/**
 * 品牌
 * 
 * @author yang
 * @email yang@gmail.com
 * @date 2022-06-11 19:48:15
 */
@Data
@TableName("pms_brand")
public class BrandEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * 品牌id
	 */
	@NotNull(message = "修改必须指定品牌id",groups = {updateGroup.class})
	@Null(message = "新增不能指定id",groups = {addGroup.class})
	@TableId
	private Long brandId;
	/**
	 * 品牌名
	 */
	@NotBlank(message = "品牌名必须提交",groups = {updateGroup.class,addGroup.class})
	private String name;
	/**
	 * 品牌logo地址
	 */
	@NotEmpty(groups = {addGroup.class})
	@URL(message = "logo必须是一个合法的url地址",groups = {addGroup.class,updateGroup.class})
	private String logo;
	/**
	 * 介绍
	 */
	private String descript;
	/**
	 * 显示状态[0-不显示；1-显示]
	 */
	@NotNull(groups = { updateShowStatusGroup.class})
	@ListValue(value={0,1},groups = {addGroup.class, updateShowStatusGroup.class})
	private Integer showStatus;
	/**
	 * 检索首字母
	 */
	@NotEmpty(groups = {addGroup.class})
	@Pattern(regexp = "^[a-zA-Z]$",message = "检索首字母必须是一个字母",groups = {addGroup.class,updateGroup.class})
	private String firstLetter;
	/**
	 * 排序
	 */
	@NotNull(groups = {addGroup.class})
	@Min(value = 0,message = "排序必须大于等于0",groups = {addGroup.class,updateGroup.class})
	private Integer sort;

}
