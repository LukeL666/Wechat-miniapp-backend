package com.example.wx.store;

import cn.hutool.extra.qrcode.QrCodeUtil;
import cn.hutool.extra.qrcode.QrConfig;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.example.wx.aes.AESUtils;
import com.example.wx.store.service.ProductService;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayOutputStream;
import java.util.Base64;

import static com.example.wx.user.utility.UserUtils.decodeJWTToken;

@RestController
@Slf4j
@RequestMapping("/store")
public class StoreController {



    @Autowired
    private ProductService productService;

    @Autowired
    private AESUtils aes;

    @GetMapping("/initData")
    public void initData() {
        productService.initStores();
    }

    @PostMapping("/addProducts")
    public ResponseEntity<?> addProducts
            (@RequestBody JSONArray data,
             @RequestHeader("userName") String userName,
             @RequestHeader("password") String password) {
        ResponseEntity<?> isValid = productService.validation(userName,password);
        // 没找到
        if(isValid.getStatusCode()== HttpStatus.BAD_REQUEST){
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        // 其它问题
        else if(isValid.getStatusCode()== HttpStatus.INTERNAL_SERVER_ERROR){
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        // 找到
        JSONObject json = new JSONObject();

        return new ResponseEntity<>(HttpStatus.OK);
    }


    @GetMapping("/getStores")
    public String[] getStores() {
        return productService.getStores();
    }

    @GetMapping("/getStoreByName")
    public JSONObject getStoreByName(@Param("appName") String appName) {
        return productService.getStoreByAppName(appName);
    }

    @GetMapping("/generateQRCode/{orderId}")
    public ResponseEntity<String> generateQRCodeBase64(@PathVariable String orderId) {

        QrConfig qrConfig = new QrConfig(150, 150);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        QrCodeUtil.generate(orderId, qrConfig, "png", out);
        System.out.println("OrderId: " + orderId);
        String base64 = Base64.getEncoder().encodeToString(out.toByteArray());
        // 返回 data URL
        return ResponseEntity.ok(base64);

    }


    @PostMapping("/generateQRCode2")
    public JSONObject generateQRCode2(@RequestBody JSONObject body) {
        QrConfig qrConfig = new QrConfig(150, 150);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        JSONArray items = body.getJSONArray("items");
        String orderId = body.getStr("orderId");
        String sn =  body.getStr("sn");
        String phone = decodeJWTToken(body.getStr("token"));
        String encryptPhone = aes.encrypt(phone);
        String[] qr_list = new String[items.size()];
        String[] qr_value = new String[items.size()];
        for (int i = 0; i < items.size(); i++) {

            String qrStr =  items.getJSONObject(i).get("id") + "|" +
                            items.getJSONObject(i).get("quantity") + "|" +
                            orderId + "|" + encryptPhone;
            System.out.println(qrStr);
            qr_value[i] = qrStr;
            QrCodeUtil.generate(qrStr, qrConfig, "png", out);
            String base64 = Base64.getEncoder().encodeToString(out.toByteArray());
            qr_list[i] = base64;
            out.reset();
        }
        JSONObject json = new JSONObject();
        json.set("qr_value", qr_value);
        json.set("sn", sn);
        //System.out.println(JSONUtil.toJsonStr(json));
        HttpResponse res = HttpRequest.post("https://www.lobot.ai:9999/nfc/addNfcForWechat").body(JSONUtil.toJsonStr(json)).execute();
        JSONObject result = new JSONObject();
        //System.out.println(res.body());
        result.set("qr_list", qr_list);
        return result;
    }

}
