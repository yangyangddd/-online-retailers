package com.yang.gulimall.search.pojo;

/**
 * Copyright 2022 json.cn
 */


import lombok.Data;

/**
 * Auto-generated: 2022-08-15 20:49:53
 *
 * @author json.cn (i@json.cn)
 * @website http://www.json.cn/java2pojo/
 */
@Data
public class bank {

    private int account_number;
    private int balance;
    private String firstname;
    private String lastname;
    private int age;
    private String gender;
    private String address;
    private String employer;
    private String email;
    private String city;
    private String state;
    public void setAccount_number(int account_number) {
        this.account_number = account_number;
    }
}
