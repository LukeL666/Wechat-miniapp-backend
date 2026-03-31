package com.example.wx.pay.JSAPI;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.io.InputStream;
import java.io.IOException;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;

public class WeChatPayV3Util {

    /**
     * 从文件或 classpath 读取私钥
     */
    public static PrivateKey loadPrivateKey(String filePath) throws Exception {
        String pem = readPemContent(filePath);
        pem = pem.replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replaceAll("\\s+", ""); // 去掉换行、空格
        byte[] keyBytes = Base64.getDecoder().decode(pem);
        return KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(keyBytes));
    }

    /**
     * 读取 PEM 文件内容（支持绝对路径和 classpath: 前缀）
     */
    private static String readPemContent(String path) throws IOException {
        if (path.startsWith("classpath:")) {
            String realPath = path.replace("classpath:", "");
            try (InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(realPath)) {
                if (is == null) {
                    throw new IOException("找不到 classpath 下的文件: " + realPath);
                }
                return new String(org.apache.commons.io.IOUtils.toByteArray(is), StandardCharsets.UTF_8);
            }
        } else {
            byte[] bytes = java.nio.file.Files.readAllBytes(java.nio.file.Paths.get(path));
            return new String(bytes, StandardCharsets.UTF_8);
        }
    }

    /**
     * 使用私钥对消息进行 SHA256withRSA 签名
     */
    public static String signWithPrivateKey(String message, String privateKeyPath) throws Exception {
        PrivateKey privateKey = loadPrivateKey(privateKeyPath);
        Signature signature = Signature.getInstance("SHA256withRSA");
        signature.initSign(privateKey);
        signature.update(message.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(signature.sign());
    }

    public static String decrypt(String apiV3Key, String associatedData, String nonce, String ciphertext) throws Exception {
        // key
        byte[] keyBytes = apiV3Key.getBytes(StandardCharsets.UTF_8);
        SecretKeySpec key = new SecretKeySpec(keyBytes, "AES");

        // AES/GCM/NoPadding
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        GCMParameterSpec spec = new GCMParameterSpec(128, nonce.getBytes(StandardCharsets.UTF_8));
        cipher.init(Cipher.DECRYPT_MODE, key, spec);
        cipher.updateAAD(associatedData.getBytes(StandardCharsets.UTF_8));

        // 解密
        byte[] data = cn.hutool.core.codec.Base64.decode(ciphertext);
        byte[] plainText = cipher.doFinal(data);

        return new String(plainText, StandardCharsets.UTF_8);
    }
}
