package com.example.wx.aes;

import cn.hutool.crypto.SecureUtil;
import cn.hutool.crypto.symmetric.AES;
import org.springframework.stereotype.Component;

@Component
public class AESUtils {
    private AES aes;
    private final String KEY = "j5pfiv013gcz8zqa";


    public AESUtils() {
        this.aes = SecureUtil.aes(KEY.getBytes());
    }

    public String encrypt(String phone) {
        return aes.encryptBase64(phone);
    }

    public String decrypt(String encryptedPhone) {
        return aes.decryptStr(encryptedPhone);
    }
}
