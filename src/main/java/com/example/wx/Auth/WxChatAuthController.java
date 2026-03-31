package com.example.wx.Auth;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

import static com.example.wx.user.utility.UserUtils.decodeJWTToken;

@RestController
@RequestMapping("/wx")
@Slf4j
public class WxChatAuthController {

    @Autowired
    private WxChatAuthBean WxChatAuthBean;

    @Autowired
    private WxAuthServices WxAuthServices;

    @GetMapping("/getOpenId")
    public ResponseEntity<?> getOpenId(@RequestParam String code) {
        String url = "https://api.weixin.qq.com/sns/jscode2session?appid={0}&secret={1}&js_code={2}&grant_type=authorization_code";
        String replaceUrl = url.replace("{0}", WxChatAuthBean.getAppId())
                .replace("{1}", WxChatAuthBean.getSecret())
                .replace("{2}", code);

        try {
            String res = HttpUtil.get(replaceUrl);
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

    @GetMapping("/getPhoneNumber")
    public ResponseEntity<?> getPhoneNumber(@RequestParam Map<String, String> map) {
        try {
            String accessToken = WxAuthServices.getAccessToken();
            String code = map.get("code"); // 前端传的 code

            if (accessToken == null || code == null) {
                return ResponseEntity
                        .badRequest()
                        .body("缺少参数 access_token 或 code");
            }

            String url = "https://api.weixin.qq.com/wxa/business/getuserphonenumber?access_token=" + accessToken;

            // 请求体必须带上 code
            JSONObject jsonBody = new JSONObject();
            jsonBody.put("code", code);

            HttpResponse res = HttpRequest.post(url)
                    .body(jsonBody.toJSONString())
                    .execute();

            if (!res.isOk()) {
                return ResponseEntity
                        .status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("请求微信接口失败: " + res.getStatus());
            }

            JSONObject result = JSONObject.parseObject(res.body());
            if (result.getInteger("errcode") != 0) {
                return ResponseEntity
                        .status(HttpStatus.BAD_REQUEST)
                        .body("微信接口错误: " + result.toJSONString());
            }

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("服务器异常: " + e.getMessage());
        }
    }

    @GetMapping("/checkToken")
    public boolean userLoginStatus(@RequestHeader String token) {
        try {
            String phone = decodeJWTToken(token); // 如果 token 无效，这里会抛异常
            return true;
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return false;
        }
    }

}
