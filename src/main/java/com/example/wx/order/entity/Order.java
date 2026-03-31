package com.example.wx.order.entity;

import lombok.*;


@AllArgsConstructor
@NoArgsConstructor
@Data
public class Order {
    private String orderId;
    private String id;
    private int totalQuantity;
    private int quantityMade;
    private String phone;
}