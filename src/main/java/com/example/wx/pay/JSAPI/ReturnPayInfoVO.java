package com.example.wx.pay.JSAPI;

import lombok.Data;

@Data
public class ReturnPayInfoVO<T> {
    private String appId;         // 公众号/小程序ID
    private String timeStamp;     // 时间戳（秒级）
    private String nonceStr;      // 随机字符串
    private String packageValue;    // 预支付ID（格式：prepay_id=xxx）
    private String signType = "RSA"; // 签名类型（固定RSA）
    private String paySign;       // RSA签名
    private String prepayId;      // 单独的prepay_id
}
