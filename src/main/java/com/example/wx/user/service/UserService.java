package com.example.wx.user.service;

import com.example.wx.user.entity.User;
import com.example.wx.user.mapper.UserMapper;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class UserService {
    @Resource
    private UserMapper userMapper;

    public User getUserByPhone(String phone){
        return userMapper.getUserByPhone(phone);
    }

//    public User getUserByToken(String token){
//        return userMapper.getUserByToken(token);
//    }

    // 不安全
//    public User getUserByOpenId(String openId){
//        return userMapper.getUserByOpenId(openId);
//    }

    public User addUser(String phone, String openId, String token, String cart, String currentOrder, String historyOrder, String couponList){
        User user = new User();
        user.setOpenId(openId);
        user.setToken(token);
        user.setUserName("用户_" + phone);
        user.setPhoneNumber(phone);
        user.setCart(cart);
        user.setCurrentOrder(currentOrder);
        user.setHistoryOrder(historyOrder);
        user.setCouponList(couponList);
        System.out.println(user);
        userMapper.addUser(user);
        return getUserByPhone(phone);
    }



    public void updateUserName(String userName, String phone){
        userMapper.updateUserName(userName, phone);
    }

    public void updateUserToken(String token, String phone){
        userMapper.updateUserToken(token, phone);
    }

    public void updateUserPhone(String newPhone, String oldPhone){
        userMapper.updateUserPhone(newPhone, oldPhone);
    }

    public void updateUserCart(String cart, String phone){
        userMapper.updateUserCart(cart, phone);
    }

    public void updateUserCurOrder(String curOrder, String phone){
        userMapper.updateUserCurOrder(curOrder, phone);
    }

    public void updateUserHisOrder(String hisOrder, String phone){
        userMapper.updateUserHisOrder(hisOrder, phone);
    }

    public void updateUserAvatar(String avatar, String phone){
        userMapper.updateUserAvatar(avatar, phone);
    }

    public void updateUserCouponList(String couponList, String phone){
        userMapper.updateUserCouponList(couponList, phone);
    }

    public void deleteUserByPhone(String phone){
        userMapper.deleteUserByPhone(phone);
    }


}
