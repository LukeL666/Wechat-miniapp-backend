package com.example.wx.pay.JSAPI;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.example.wx.user.utility.UserUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static com.example.wx.pay.JSAPI.WeChatPayV3Util.signWithPrivateKey;


@RestController
@RequestMapping("/client/pay")
@Slf4j
public class WeChatPayController {

    @Autowired
    private WeChatPayProperties weChatPayProperties;

    /**
     * 生成 API 请求签名（微信 V3）
     */
    private String generateApiSignature(String method, String url, String timestamp,
                                        String nonceStr, String body) throws Exception {
        String message = String.format("%s\n%s\n%s\n%s\n%s\n",
                method, url, timestamp, nonceStr, body);
        return signWithPrivateKey(message, weChatPayProperties.getPrivateKeyPath());
    }

    /**
     * 生成 JSAPI 支付调起签名
     */
    private String generateJsapiSignature(String appId, String timeStamp, String nonceStr, String prepayId) throws Exception {
        String message = String.format("%s\n%s\n%s\nprepay_id=%s\n",
                appId, timeStamp, nonceStr, prepayId);
        return signWithPrivateKey(message, weChatPayProperties.getPrivateKeyPath());
    }

    /**
     * 拼接 Authorization 头
     */
    private String getAuthorizationHeader(String method, String url, String body) throws Exception {
        String timestamp = String.valueOf(System.currentTimeMillis() / 1000);
        String nonceStr = UUID.randomUUID().toString().replace("-", "");
        String signature = generateApiSignature(method, url, timestamp, nonceStr, body);

        return String.format(
                "WECHATPAY2-SHA256-RSA2048 mchid=\"%s\",nonce_str=\"%s\",timestamp=\"%s\",serial_no=\"%s\",signature=\"%s\"",
                weChatPayProperties.getMchId(), nonceStr, timestamp, weChatPayProperties.getCertSerialNo(), signature
        );
    }

    /**
     * 下单接口（JSAPI）
     */
    @PostMapping("/weChatPay")
    public ReturnPayInfoVO weChatPayV3(@RequestBody PayRequest request) {
        try {
            // 1. 构造下单参数
            JSONObject amount = new JSONObject();
            amount.set("total", request.getTotalFee()); // 单位：分
            amount.set("currency", "CNY");

            JSONObject payer = new JSONObject();
            payer.set("openid", request.getOpenid());

            JSONObject requestBody = new JSONObject();
            requestBody.set("appid", weChatPayProperties.getAppId());
            requestBody.set("mchid", weChatPayProperties.getMchId());
            requestBody.set("description", request.getBody());
            requestBody.set("out_trade_no", request.getOutTradeNo());
            requestBody.set("notify_url", weChatPayProperties.getReturnUrl());
            requestBody.set("amount", amount);
            requestBody.set("payer", payer);
            requestBody.set("attach", request.getAttach());

            String jsonBody = JSONUtil.toJsonStr(requestBody);

            // 2. 请求微信下单 API
            HttpResponse res = HttpRequest.post("https://api.mch.weixin.qq.com/v3/pay/transactions/jsapi")
                    .header("Content-Type", "application/json")
                    .header("Authorization", getAuthorizationHeader("POST", "/v3/pay/transactions/jsapi", jsonBody))
                    .body(jsonBody)
                    .execute();

            if (!res.isOk()) {
                System.out.println(res.body());
                throw new RuntimeException("微信支付请求失败: " + res.getStatus() + " - " + res.body());
            }

            // 3. 解析微信返回的 prepay_id
            JSONObject json = JSONUtil.parseObj(res.body());
            String prepayId = json.getStr("prepay_id");
            if (prepayId == null) {
                throw new RuntimeException("微信返回中没有 prepay_id: " + res.body());
            }

            // 4. 生成 JSAPI 调起支付所需签名
            String nonceStr = UUID.randomUUID().toString().replace("-", "");
            String timeStamp = String.valueOf(System.currentTimeMillis() / 1000);
            String paySign = generateJsapiSignature(weChatPayProperties.getAppId(), timeStamp, nonceStr, prepayId);

            // 5. 返回前端需要的参数
            ReturnPayInfoVO vo = new ReturnPayInfoVO();
            vo.setAppId(weChatPayProperties.getAppId());
            vo.setTimeStamp(timeStamp);
            vo.setNonceStr(nonceStr);
            vo.setPackageValue("prepay_id=" + prepayId);
            vo.setSignType("RSA");
            vo.setPaySign(paySign);
            vo.setPrepayId(prepayId);
            return vo;

        } catch (Exception e) {
            throw new RuntimeException("调用微信支付API异常", e);
        }
    }

    @PostMapping("/weChatPayNotify")
    public ResponseEntity<String> weChatPayNotify(@RequestBody String body) {
        log.info("收到微信支付回调报文: {}", body);

        //https://pay.weixin.qq.com/doc/v3/merchant/4013070368

        // 验签
        try {
            Map<String, Object> notifyData = JSONUtil.toBean(body, Map.class);
            Map<String, Object> resource = (Map<String, Object>) notifyData.get("resource");
            String associatedData = (String) resource.get("associated_data");
            String nonce = (String) resource.get("nonce");
            String ciphertext = (String) resource.get("ciphertext");

            // 解密
            String plainText = WeChatPayV3Util.decrypt(
                    weChatPayProperties.getApiV3key(),
                    associatedData,
                    nonce,
                    ciphertext
            );
            log.info("解密后的支付明文: {}", plainText);

            // 打印交易数据
            Map<String, Object> transaction = JSONUtil.toBean(plainText, Map.class);
            String outTradeNo = (String) transaction.get("out_trade_no");
            String tradeState = (String) transaction.get("trade_state");
            log.info("订单号: {}, 状态: {}", outTradeNo, tradeState);

            return ResponseEntity.ok().build();

        } catch (Exception e) {
            log.error("处理微信支付回调失败", e);

            Map<String, String> resp = new HashMap<>();
            resp.put("code", "FAIL");
            resp.put("message", "失败");

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(JSONUtil.toJsonStr(resp));
        }
    }



    @PostMapping("/closePay")
    public ResponseEntity<String> closePay(@RequestBody Map<String, String> body) {
        try {
            // 参数是否异常
            String outTradeNo = body.get("out_trade_no");
            if (outTradeNo == null || outTradeNo.isEmpty()) {
                return ResponseEntity.badRequest().body("缺少 out_trade_no");
            }

            String url = String.format("/v3/pay/transactions/out-trade-no/%s/close", outTradeNo);

            // String实参Json化
            JSONObject requestJson = new JSONObject();
            requestJson.set("mchid", weChatPayProperties.getMchId());
            String jsonBody = requestJson.toString();

            //验证签名获取
            String authHeader = getAuthorizationHeader("POST", url, jsonBody);

            HttpResponse res = HttpRequest.post("https://api.mch.weixin.qq.com" + url)
                    .header("Content-Type", "application/json")
                    .header("Authorization", authHeader)
                    .body(jsonBody)
                    .execute();

            // 微信关闭订单成功返回 204
            if (res.getStatus() == 204) {
                return ResponseEntity.ok("success");
            } else {
                return ResponseEntity.status(res.getStatus()).body("fail");
            }

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("error");
        }
    }


    @GetMapping("/queryOrder/{outTradeNo}")
    public ResponseEntity<String> queryOrder(@PathVariable("outTradeNo") String outTradeNo) {
        try {
            // 拼接请求 URL（注意带上 mchid 参数）
            String url = String.format("/v3/pay/transactions/out-trade-no/%s?mchid=%s",
                    outTradeNo, weChatPayProperties.getMchId());

            // 生成签名
            String authHeader = getAuthorizationHeader("GET", url, "");

            // 发送请求
            HttpResponse res = HttpRequest.get("https://api.mch.weixin.qq.com" + url)
                    .header("Authorization", authHeader)
                    .header("Accept", "application/json")
                    .execute();

            if (!res.isOk()) {
                log.error("查单失败: {} - {}", res.getStatus(), res.body());
                return ResponseEntity.status(res.getStatus()).body(res.body());
            }

            log.info("查单结果: {}", res.body());

            return ResponseEntity.ok(res.body());

        } catch (Exception e) {
            log.error("调用查单接口异常", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("调用查单接口异常");
        }
    }

    /**
     * 退款接口
     */
    @PostMapping("/refund")
    public ResponseEntity<String> refund(@RequestBody Map<String, Object> body, @RequestHeader String token) {
        try {
            // 获取参数
            String phone  = UserUtils.decodeJWTToken(token);
            String outTradeNo = (String) body.get("out_trade_no");
            String outRefundNo ="REFUND_" + outTradeNo; // 订单号
            Integer refund = (Integer) body.get("refund"); // 退款金额（分）
            Integer total = (Integer) body.get("total");   // 原订单总金额（分）

            if (outTradeNo == null || refund == null || total == null) {
                return ResponseEntity.badRequest().body("参数不完整");
            }

            // 构造请求体
            JSONObject amount = new JSONObject();
            amount.set("refund", refund);
            amount.set("total", total);
            amount.set("currency", "CNY");

            JSONObject requestJson = new JSONObject();
            requestJson.set("out_trade_no", outTradeNo);
            requestJson.set("out_refund_no", outRefundNo);
            requestJson.set("amount", amount);
            requestJson.set("mchid", weChatPayProperties.getMchId());

            String jsonBody = requestJson.toString();

            // 生成签名
            String url = "/v3/refund/domestic/refunds";
            String authHeader = getAuthorizationHeader("POST", url, jsonBody);

            // 请求微信退款 API
            HttpResponse res = HttpRequest.post("https://api.mch.weixin.qq.com" + url)
                    .header("Content-Type", "application/json")
                    .header("Authorization", authHeader)
                    .body(jsonBody)
                    .execute();

            if (res.isOk()) {
                log.info("退款成功: {}", res.body());
                return ResponseEntity.ok(res.body());
            } else {
                log.error("退款失败: {} - {}", res.getStatus(), res.body());
                return ResponseEntity.status(res.getStatus()).body(res.body());
            }

        } catch (Exception e) {
            log.error("调用退款接口异常", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("调用退款接口异常");
        }
    }


    /**
     * 元转分
     */
    private int yuanToFee(BigDecimal bigDecimal) {
        return bigDecimal.multiply(new BigDecimal(100)).intValue();
    }
}
