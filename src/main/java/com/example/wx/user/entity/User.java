package com.example.wx.user.entity;

import lombok.Data;


@Data
public class User {
    private long id;
    private String token;
    private String avatar;
    private String userName;
    private String openId;
    private String phoneNumber;
    private String cart;
    private String currentOrder;
    private String historyOrder;
    private String couponList;
}
