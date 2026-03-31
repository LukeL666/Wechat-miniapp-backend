package com.example.wx.Auth;

import lombok.Data;
import org.springframework.stereotype.Component;

@Data
@Component
public class WxChatAuthBean {

    private String appId="wx123456"; // appId for wechat platform
    private String secret="1234556789"; // corresponding secret number
}
