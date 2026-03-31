package com.example.wx.Auth;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import com.alibaba.fastjson2.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class WxAuthServices {

    @Autowired
    private WxChatAuthBean wxChatAuthBean;

    /**
     * 获取全局 access_token
     * @return access_token
     */
    public String getAccessToken() {
        try {
            String url = String.format(
                    "https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid=%s&secret=%s",
                    wxChatAuthBean.getAppId(),
                    wxChatAuthBean.getSecret()
            );

            HttpResponse res = HttpRequest.get(url).execute();

            if (!res.isOk()) {
                throw new RuntimeException("调用微信接口失败，状态码：" + res.getStatus());
            }

            JSONObject result = JSONObject.parseObject(res.body());

            if (result.getInteger("errcode") != null && result.getInteger("errcode") != 0) {
                throw new RuntimeException("微信接口错误: " + result.toJSONString());
            }

            return result.getString("access_token");

        } catch (Exception e) {
            throw new RuntimeException("获取 access_token 失败：" + e.getMessage(), e);
        }
    }
}

