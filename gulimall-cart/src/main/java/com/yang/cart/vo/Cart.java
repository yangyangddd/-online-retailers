package com.yang.cart.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class Cart {//整个购物车,需要重写他的get方法
    List<CartItem> items;
    private Integer countNum;//商品数量
    private Integer countType;//商品类型数量
    private BigDecimal totalAmount;//商品总价
    private BigDecimal reduce;//减免价格

    public Integer getCountNum() {
        int count=0;
        if(items!=null&&items.size()>0){
            for (CartItem item : items) {
                count+=item.getCount();
            }
        }
        return count;
    }

    public Integer getCountType() {
        int count=0;
        if(items!=null&&items.size()>0)
        {
            for (CartItem item : items) {
                count+=1;
            }
        }
        return count;
    }

    public BigDecimal getTotalAmount() {
        BigDecimal amount=new BigDecimal(0);
        if(items!=null&&items.size()>0)
        {
            for (CartItem item : items) {
                BigDecimal totalPrice = item.getTotalPrice();
                amount=amount.add(totalPrice);
            }
        }
        //减去优惠总价
        BigDecimal subtract = amount.subtract(getReduce());
        return subtract;
    }
}
