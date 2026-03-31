package com.example.wx.pay.JSAPI;


import com.github.binarywang.wxpay.config.WxPayConfig;
import com.github.binarywang.wxpay.service.WxPayService;
import com.github.binarywang.wxpay.service.impl.WxPayServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;


@Component
@Configuration  //配置类
public class WeChatPayConfig {
    @Autowired
    private WeChatPayProperties properties;

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
    @Bean
    @ConditionalOnMissingBean
    public WxPayConfig payConfig() {
        WxPayConfig payConfig = new WxPayConfig();


        payConfig.setAppId(properties.getAppId());
        payConfig.setMchId(properties.getMchId());
        payConfig.setApiV3Key(properties.getApiV3key()); // 注意是apiV3Key不是mchKey
        payConfig.setPrivateKeyPath(properties.getPrivateKeyPath());
        payConfig.setPrivateCertPath(properties.getCertPath()); // 证书路径（.pem）
        payConfig.setCertSerialNo(properties.getCertSerialNo()); // 证书序列号
        payConfig.setNotifyUrl(properties.getReturnUrl());

        // 禁用V2（避免混淆）
        payConfig.setUseSandboxEnv(false);
        payConfig.setTradeType(null); // V3不需要trade_type

//        System.out.println("Appid: " + properties.getAppId());
//        System.out.println("Mchid:" + properties.getMchId());
//        System.out.println("Apiv3key: " + properties.getPrivateKeyPath());
//        System.out.println("CertserialNo: " + properties.getCertSerialNo());


        return payConfig;
    }

    @Bean
    public WxPayService wxPayService(WxPayConfig payConfig) {
        WxPayService wxPayService = new WxPayServiceImpl();
        wxPayService.setConfig(payConfig);
        return wxPayService;
    }
}
