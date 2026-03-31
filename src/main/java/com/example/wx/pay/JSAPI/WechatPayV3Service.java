package com.example.wx.pay.JSAPI;
import cn.hutool.http.HttpRequest;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.google.gson.Gson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import lombok.extern.slf4j.Slf4j;

import java.util.Map;

@Service
@Slf4j
public class WechatPayV3Service {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    WeChatPayProperties weChatPayProperties;

    //https://pay.weixin.qq.com/doc/v3/merchant/4012791856
    public ResponseEntity<?> jsapiPay(Map<String, Object>  requestBody, ReturnPayInfoVO vo) {
        String url = "https://api.mch.weixin.qq.com/v3/pay/transactions/jsapi";
        try {
            String Authorization_str = "Authorization: WECHATPAY2-SHA256-RSA2048 mchid=\"{0}\"," +
                    "nonce_str=\"{1}\"," +
                    "signature=\"{2}\"," +
                    "timestamp=\"{3}\","+
                    "serial_no=\"{4}\"";

            String replaceUrl = url.replace("{0}", weChatPayProperties.getMchId())
                    .replace("{1}", vo.getNonceStr())
                    .replace("{2}",vo.getPaySign())
                    .replace("{3}",vo.getTimeStamp())
                    .replace("{4}", weChatPayProperties.getCertSerialNo());

            String jsonBody = new Gson().toJson(requestBody);

            HttpRequest request = HttpRequest.post(replaceUrl)
                    .header("Content-Type", "application/json") // 必须设置Content-Type
                    .header("Authorization", Authorization_str) // 微信支付V3认证头
                    .header("Accept", "application/json") // 声明接受JSON响应
                    .body(jsonBody); // 设置请求体

            // 2. 执行请求并获取响应
            String res = request.execute().body();

            JSONObject result = JSON.parseObject(res);

            // Check if WeChat returned an error (e.g., invalid code)
            if (result.containsKey("errcode")) {
                return ResponseEntity.badRequest().body(result);
            }

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Failed to fetch OpenID: {}", e.getMessage());
            return ResponseEntity.internalServerError().body("Failed to fetch OpenID");
        }
    }
}
