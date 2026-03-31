package com.example.wx.pay.JSAPI;


import lombok.Data;

@Data
public class PayRequest {

    private String openid; // 用户openid

    private String outTradeNo; // 商户订单号

    private int totalFee; // 单位：元

    private String body; // 商品描述

    private String attach; // 附加数据(可选)

}
