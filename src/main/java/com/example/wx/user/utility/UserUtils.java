package com.example.wx.user.utility;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import cn.hutool.jwt.JWT;
import cn.hutool.jwt.JWTUtil;
import com.example.wx.user.entity.User;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class UserUtils {

    private static final String SECRET = "my_secret_key_12345";

    public static JSONObject userToJsonForm(User user) throws IOException {
        JSONObject jsonForm = new JSONObject();
        jsonForm.set("id", user.getId());
        jsonForm.set("token", user.getToken());
        jsonForm.set("openId", user.getOpenId());
        if(user.getAvatar()!=null){
            jsonForm.set("avatar", user.getAvatar());
        }

        //System.out.println("UserName is: " + user.getUserName());
        jsonForm.set("userName", user.getUserName());
        //System.out.println("Phone is: " + user.getPhoneNumber());
        jsonForm.set("phone", user.getPhoneNumber());
        List<JSONObject> cart = JSONUtil.toList(JSONUtil.parseArray(user.getCart()),JSONObject.class);
        jsonForm.set("cart", cart);
        List<JSONObject> currentOrder = JSONUtil.toList(JSONUtil.parseArray(user.getCurrentOrder()),JSONObject.class);
        jsonForm.set("currentOrder", currentOrder);
        List<JSONObject> historyOrder = JSONUtil.toList(JSONUtil.parseArray(user.getHistoryOrder()),JSONObject.class);
        jsonForm.set("historyOrder", historyOrder);
        List<JSONObject> couponList = JSONUtil.toList(JSONUtil.parseArray(user.getCouponList()),JSONObject.class);
        jsonForm.set("couponList", couponList);
        //System.out.println("The returned jsonForm is: " + jsonForm);
        return jsonForm;
    }

    public static String generateJWTToken(String phone, long expireSeconds) {
        long now = System.currentTimeMillis() / 1000;

        Map<String, Object> payload = new HashMap<>();
        payload.put("sub", phone);
        payload.put("iat", now);               // 确保是 long
        payload.put("exp", now + expireSeconds); // 确保是 long

        return JWTUtil.createToken(payload, SECRET.getBytes());
    }

    public static String decodeJWTToken(String token) {
        if (token == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Token is required");
        }

        if (!JWTUtil.verify(token, SECRET.getBytes())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unverified Token");
        }

        JWT jwt = JWTUtil.parseToken(token);
        long now = System.currentTimeMillis() / 1000;

        Object iatObj = jwt.getPayload("iat");
        Object expObj = jwt.getPayload("exp");

        long iat = (iatObj instanceof Number) ? ((Number) iatObj).longValue() : Long.parseLong(iatObj.toString());
        long exp = (expObj instanceof Number) ? ((Number) expObj).longValue() : Long.parseLong(expObj.toString());

//        System.out.println("iat: " + iat + " exp: " + exp);
//        System.out.println("now: " + now);
        // 自己校验，不依赖 hutool 的 validate
        if (now < iat || now > exp) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Expired Token");
        }

        return (String) jwt.getPayload("sub");
    }

    public static byte[] readImageWithBufferedStream(File imageFile) throws IOException {
        BufferedInputStream bis = null;
        byte[] imageBytes = new byte[(int) imageFile.length()];

        try {
            bis = new BufferedInputStream(Files.newInputStream(imageFile.toPath()));
            bis.read(imageBytes);
        } finally {
            if (bis != null) {
                bis.close();
            }
        }

        return imageBytes;
    }
}
