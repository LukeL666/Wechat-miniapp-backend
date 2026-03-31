package com.example.wx.store.entity;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;


@Data
@AllArgsConstructor
public class Product {
    private String name;         // 商品名称
    private BigDecimal price;    // 价格
    private String context;      // 商品描述
    private String img;          // 图片地址
    private int id;
    private String category;
}
