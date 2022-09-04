package com.yang.gulimall.order.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

//订单确认需要用的数据
@Data
public class OrderConfirmVo {
    //收货地址，ums_member_receive_address
    List<MemberAddressVo> address;
    //所有选中的购物项
    List<OrderItemVo> items;
    //发票记录
    //优惠券信息
    //积分信息
    Integer integration;
    Map<Long,Boolean> stocks;//是否有库存
    //订单总额
//     BigDecimal total;
//     BigDecimal payPrice;//应付的价格
    //TODO 避免提交过多订单，需要加个令牌
    String orderToken;
    BigDecimal price;
    Integer count;
    BigDecimal total;
    public BigDecimal getPayPrice() {
        return getTotal();
    }

    public Integer getCount()
    {
        Integer i=0;
        if(items!=null)
        {
            for (OrderItemVo item : items) {
                i+=item.getCount();
            }
        }
        return i;
    }
    public BigDecimal getTotal() {
        BigDecimal num=new BigDecimal(0);
        for (OrderItemVo item : items) {
            num = num.add(item.getPrice());
        }
        return num;
    }
}
