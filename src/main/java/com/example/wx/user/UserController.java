package com.example.wx.user;


import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.example.wx.aes.AESUtils;
import com.example.wx.user.entity.User;

import com.example.wx.user.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static com.example.wx.user.utility.UserUtils.*;

@RestController
@Slf4j
@RequestMapping("/users")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private AESUtils aesUtils;

    // 查
    @GetMapping
    public ResponseEntity<JSONObject> getUser(@RequestHeader String token) throws IOException {
        String phone = decodeJWTToken(token); // 如果 token 无效，会直接抛异常

        User user = userService.getUserByPhone(phone);
        //System.out.println("User: " + user);
        if (user != null) {
            return ResponseEntity.ok(userToJsonForm(user));
        }
        return ResponseEntity.notFound().build();
    }

    // 增
    @PostMapping
    public ResponseEntity<JSONObject> createUser(@RequestBody JSONObject body) throws IOException {
        String phone = body.getStr("phone");
        User user = userService.getUserByPhone(phone);
        if (user != null) {
            String newToken = generateJWTToken(phone, 3600);
            user.setToken(newToken);
            userService.updateUserToken(newToken, phone);
            return ResponseEntity.ok(userToJsonForm(user));
        }

        User newUser = userService.addUser(
                phone,
                body.getStr("openId"),
                generateJWTToken(phone,60 * 60), // 60min
                JSONUtil.toJsonStr(body.get("cart")),
                JSONUtil.toJsonStr(body.get("currentOrderList")),
                JSONUtil.toJsonStr(body.get("historyOrderList")),
                "[]"
        );
        return ResponseEntity
                .status(HttpStatus.CREATED) // 201 Created
                .body(userToJsonForm(newUser));
    }

    // 改
    @PutMapping
    public ResponseEntity<JSONObject> updateUserInfo(@RequestBody JSONObject body, @RequestHeader String token) {
        String phone = decodeJWTToken(token);
        System.out.println("UpdateUserInfo");

        //System.out.println("Body:" + body);
        User user = userService.getUserByPhone(phone);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        String newPhone = body.getStr("phone");
        if(newPhone != null){
            userService.updateUserPhone(newPhone, phone);
            userService.updateUserToken(generateJWTToken(newPhone, 3600), newPhone);
        }

        String userName = body.getStr("userName");

        if (userName != null) {
            userService.updateUserName(userName, phone);
        }
        //System.out.println("userName:" + userName);

        String cart = JSONUtil.toJsonStr(body.get("cart"));
        //System.out.println("cart :" + cart);
        if (cart != null && !cart.isEmpty()) {
            userService.updateUserCart(cart, phone);
        }

        String currentOrder = JSONUtil.toJsonStr(body.get("currentOrder"));
        if (currentOrder != null && !currentOrder.isEmpty()) {
            userService.updateUserCurOrder(currentOrder, phone);
        }
        //System.out.println("Current Order :" + currentOrder);

        String historyOrder = JSONUtil.toJsonStr(body.get("historyOrder"));
        if (historyOrder != null && !historyOrder.isEmpty()) {
            userService.updateUserHisOrder(historyOrder, phone);
        }
        //System.out.println("History Order :" + historyOrder);

        String couponList = JSONUtil.toJsonStr(body.get("couponList"));
        if (couponList != null && !couponList.isEmpty()) {
            userService.updateUserCouponList(couponList, phone);
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(new JSONObject());
    }

    // 删
    @DeleteMapping
    public ResponseEntity<JSONObject> deleteUser(@RequestBody JSONObject body, @RequestHeader String token) {
        String phone = decodeJWTToken(token);

        User user = userService.getUserByPhone(phone);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        userService.deleteUserByPhone(phone);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build(); // 204
    }

    @PostMapping("/uploadAvatar")
    public ResponseEntity<?> uploadAvatar(@RequestParam("avatar") MultipartFile avatar,  @RequestHeader String token) {
        // 检查是否有文件
        String phone = decodeJWTToken(token);

        User user = userService.getUserByPhone(phone);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        if (avatar.isEmpty()) {
            return ResponseEntity.badRequest().body("请上传头像文件");
        }

        // 获取文件原始文件名
        String fileName = user.getUserName() + '_' +System.currentTimeMillis() + ".jpg";
        // 拼接路径
        File destFile = new File("/opt/wx_xcx/user_avatar/" + fileName);

        try {
            String oldAvatar = user.getAvatar();
            if(oldAvatar != null) {
                File file = new File("/opt/wx_xcx/user_avatar/" + oldAvatar);
                if(file.exists()) {
                    boolean isDelete = file.delete();
                    if(!isDelete) {
                        return ResponseEntity.status(500).body("服务器删除头像失败");
                    }
                }
            }
            // 保存文件到指定路径
            avatar.transferTo(destFile);  // Hutool 会自动处理文件保存
            userService.updateUserAvatar(fileName, phone);
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("文件保存失败");
        }

        return ResponseEntity.ok("头像上传成功");
    }

    @PostMapping("/updateOrder")
    public ResponseEntity<String> updateOrder(@RequestBody JSONObject body) {
        try {
            // 1. 基础参数校验
            if (body == null) {
                return ResponseEntity.badRequest().body("请求体为空");
            }
            String qrValue = body.getStr("qr_value");
            if(qrValue == null || qrValue.isEmpty()){
                return ResponseEntity.badRequest().body("二维码值为空");
            }
            String[] fields = qrValue.split("\\|");
            if (fields.length < 4) {
                return ResponseEntity.badRequest().body("参数格式不正确，应包含 id|quantity|orderId|phoneEnc");
            }

            // 打印接收到的参数
            for (String field : fields) {
                System.out.println("接收字段: " + field);
            }

            String id = fields[0];
            String orderId = fields[2];
            int stage = body.getInt("stage");

            // 2. 解密手机号
            String phone;
            try {
                phone = aesUtils.decrypt(fields[3]);
            } catch (Exception e) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("手机号解密失败");
            }

            // 3. 获取用户
            User user = userService.getUserByPhone(phone);
            if (user == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("未找到该用户");
            }

            // 4. 转换 currentOrder
            List<JSONObject> currentOrder;
            try {
                currentOrder = JSONUtil.toList(JSONUtil.parseArray(user.getCurrentOrder()), JSONObject.class);
            } catch (Exception e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("用户订单数据解析失败");
            }

            boolean orderFound = false;
            boolean itemFound = false;

            // 5. 遍历订单
            for (JSONObject orderObj : currentOrder) {
                if (orderId.equals(orderObj.getStr("order_id"))) {
                    orderFound = true;
                    int totalMade = ((Number) orderObj.get("total_quantity_made")).intValue();
                    // 遍历 item_list
                    JSONArray itemList = orderObj.getJSONArray("item_list");
                    for (int i = 0; i < itemList.size(); i++) {
                        JSONObject item = itemList.getJSONObject(i);
                        boolean itemFinished = item.getBool("isComplete");
                        if (!itemFinished && id.equals(item.getStr("id"))) {
                            itemFound = true;
                            int quantityMade = ((Number) item.get("quantity_made")).intValue();
                            int quantity = ((Number) item.get("quantity")).intValue();
                            if(stage == 0){
                                ++quantityMade;
                                item.set("quantity_made", quantityMade);
                                orderObj.set("total_quantity_made", ++totalMade);
                            }
                            else if(stage == 1){
                                System.out.println(orderObj.getInt("time_to_done") - 3);
                                orderObj.set("time_to_done", orderObj.getInt("time_to_done") - 3);
                                if (quantityMade == quantity) {
                                    item.set("isComplete", true);
                                }
                            }
                            else{
                                return ResponseEntity.badRequest().body("阶段参数错误: 开始制作0, 完成订单:1");
                            }
                            break;
                        }
                    }
                    if (stage == 0 && itemFound) {
                        orderObj.set("state", "制作中");
                    }
                    else if (stage == 1 && totalMade == ((Number) orderObj.get("total_quantity")).intValue()) {
                        orderObj.set("state", "已完成");
                    }
                    break;
                }
            }

            if (!orderFound) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("未找到该订单");
            }
            if (!itemFound) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("订单队列不存在该商品或额外扫码，二维码无效");
            }

            // 6. 更新数据库
            userService.updateUserCurOrder(JSONUtil.toJsonStr(currentOrder), phone);
            return ResponseEntity.ok("订单更新成功");

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("服务器异常: " + e.getMessage());
        }
    }

}
