package com.example.wx.user.entity;

import java.math.BigDecimal;
import java.util.List;

public class Order {
    private String store;         // 门店
    private String state;         // 订单状态
    private int numItem;          // 商品种类数
     private List<Item> itemList;  // 商品列表，先不加

    private BigDecimal totalPrice;   // 总价
    private int totalQuantity;       // 总数量
    private String timeToDone;       // 预计完成时间
    private int queueId;             // 排队号
    private String date;             // 日期
    private int orderInFront;        // 前面有多少订单
    private int cupInFront;          // 前面有多少杯
    private String orderId;          // 订单ID
}
