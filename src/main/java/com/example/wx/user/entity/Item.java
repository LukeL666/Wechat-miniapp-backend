package com.example.wx.user.entity;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class Item {
    private String name;         // 商品名称
    private BigDecimal price;    // 价格
    private String context;      // 商品描述
    private String img;          // 图片地址
    private String temp;   // 温度选项
    private String size;   // 尺寸选项
    private int quantity;
}
