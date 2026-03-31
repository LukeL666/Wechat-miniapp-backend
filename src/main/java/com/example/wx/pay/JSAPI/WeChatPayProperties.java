package com.example.wx.pay.JSAPI;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties("wx.pay")  //映射yml的内容
public class WeChatPayProperties {

    private String appId;
    private String mchId;
    private String apiV3key; // 32位APIv3密钥
    private String privateKeyPath; // apiclient_key.pem路径
    private String certPath; // apiclient_cert.pem路径
    private String certSerialNo; // 证书序列号
    private String returnUrl;
}
