package com.example.wx.store.service;

import cn.hutool.crypto.SecureUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.example.wx.store.entity.AppStore;
import com.example.wx.store.entity.Product;
import com.example.wx.store.mapper.ProductMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.*;

@Component
public class ProductService {
    @Autowired
    private ProductMapper productMapper;

    public ResponseEntity<?> validation(String username, String password) {
        String encryptedPwd = SecureUtil.sha256(password);
        String pwd = productMapper.getPassword(username);
        if(pwd == null || pwd.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        else if(pwd.equals(encryptedPwd)) {
            return new ResponseEntity<>(HttpStatus.OK);
        }
        else {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public void initStores() {
        List<AppStore> stores = new ArrayList<>();
        JSONArray arr = setProduct();

        for (int i = 0; i < arr.size(); i++) {
            // 遍历所有商店
            List<Product> product_list1 = new ArrayList<>();
            List<Product> product_list2 = new ArrayList<>();
            JSONObject obj = arr.getJSONObject(i);
            String orgTokenStr = obj.getStr("orgToken"); // 原始字符串，包含转义
            String appName = obj.getStr("appName");
            String sn = obj.getStr("sn");
            JSONArray tokens = JSONUtil.parseArray(orgTokenStr);

            // 转成 String[]
//            String[] tokenArr = tokens.toArray(new String[0]); // (/" => ")
            String [] coffee = {
                    "冰美式",
                    "美式",
                    "冰拿铁",
                    "拉花拿铁",
                    "卡布奇诺",
                    "馥芮白",
                    "机器人印花"
            };
            Product p = null;
            int count = 0;
            for (String s : coffee) {
                p = makeProduct(s, new BigDecimal("0.01"), "商品描述", "/icon_coffeeDemo.svg", count + 100, "咖啡豆1");
                product_list1.add(p);
                count++;
            }
            for (String s : coffee) {
                p = makeProduct(s, new BigDecimal("0.01"), "商品描述", "/icon_coffeeDemo.svg", count + 100, "咖啡豆2");
                product_list2.add(p);
                count++;
            }
            JSONObject coffee1 = new JSONObject();
            coffee1.set("title", "咖啡豆1");
            coffee1.set("list", product_list1);
            JSONObject coffee2 = new JSONObject();
            coffee2.set("title", "咖啡豆2");
            coffee2.set("list", product_list2);
            List<JSONObject> coffeeList = new ArrayList<>();
            coffeeList.add(coffee1);
            coffeeList.add(coffee2);
            String productStr = JSONUtil.toJsonStr(coffeeList);
            stores.add(new AppStore(sn, appName, productStr));
        }
        productMapper.batchInsertStores(stores);
    }

    public JSONArray setProduct() {
        HttpResponse response = HttpRequest.get("https://www.lobot.ai:9999/app/findApps").execute();
        JSONObject result = JSONUtil.parseObj(response.body());
        System.out.println(result);
        return result.getJSONArray("data"); // 直接返回data数组
    }

    public void updateProduct(Product product) {}

    public Product makeProduct(String name, BigDecimal price, String context, String img, int id,String category) {
        return new Product(name, price, context, img, id, category);
    }

    public String[] getStores(){
        return productMapper.getStores();
    }

    public JSONObject getStoreByAppName(String appName) {
        JSONObject json = new JSONObject();
        Map<String,String> map = productMapper.getStoreByAppName(appName);
        json.set("appName", map.get("appName"));
        json.set("sn", map.get("sn"));

        json.set("product_list", JSONUtil.toList(JSONUtil.parseArray(map.get("productList")), JSONObject.class));
        return json;
    }

    public JSONObject getStoreByUserName(String userName) {
        JSONObject json = new JSONObject();
        Map<String,String> map = productMapper.getStoreByUserName(userName);
        json.set("appName", map.get("appName"));
        json.set("sn", map.get("sn"));

        json.set("product_list", JSONUtil.toList(JSONUtil.parseArray(map.get("productList")), JSONObject.class));
        return json;
    }

}








