package com.example.wx.user.mapper;

import com.example.wx.user.entity.User;
import org.apache.ibatis.annotations.*;

public interface UserMapper {
    @Select("SELECT token, avatar, open_id AS openId, user_name AS userName, phone_number AS phoneNumber," +
            " cart, current_order AS currentOrder, history_order AS historyOrder, coupon_list AS couponList" +
            " FROM users WHERE phone_number =#{phone}")
    User getUserByPhone(@Param("phone") String phone);

//    @Select("SELECT token, avatar, open_id AS openId, user_name AS userName, phone_number AS phoneNumber," +
//            " cart, current_order AS currentOrder, history_order AS historyOrder, coupon_list AS couponList" +
//            " FROM users WHERE token =#{token}")
//    User getUserByToken(@Param("token") String token);

//    @Select("SELECT token, avatar, open_id AS openId, user_name AS userName, phone_number AS phoneNumber," +
//            " cart, current_order AS currentOrder, history_order AS historyOrder, coupon_list AS couponList" +
//            " FROM users WHERE open_id =#{openId}")
//    User getUserByOpenId(@Param("openId") String openId);

    @Insert("INSERT INTO users(token, open_id, user_name,phone_number, cart, current_order, history_order, coupon_list)" +
            "VALUES(#{token},#{openId}, #{userName}, #{phoneNumber}, #{cart}, #{currentOrder}, #{historyOrder},#{couponList})")
    @Options(keyProperty = "openId")
    void addUser(User user);

    @Update("UPDATE users " +
    "SET user_name = #{userName} " +
    "WHERE phone_number =#{phone}")
    void updateUserName(@Param("userName") String userName,  @Param("phone") String phone);

    @Update("UPDATE users " +
            "SET phone_number = #{newPhone} " +
            "WHERE phone_number =#{oldPhone}")
    void updateUserPhone(@Param("newPhone") String newPhone, @Param("oldPhone") String oldPhone);

    @Update("UPDATE users " +
            "SET token = #{token} " +
            "WHERE phone_number =#{phone}")
    void updateUserToken(@Param("token") String token, @Param("phone") String phone);

    @Update("UPDATE users " +
            "SET cart = #{cart} " +
            "WHERE phone_number =#{phone}")
    void updateUserCart(@Param("cart") String cart,  @Param("phone") String phone);

    @Update("UPDATE users " +
            "SET current_order = #{curOrder} " +
            "WHERE phone_number =#{phone}")
    void updateUserCurOrder(@Param("curOrder") String curOrder,  @Param("phone") String phone);

    @Update("UPDATE users " +
            "SET avatar = #{avatar} " +
            "WHERE phone_number =#{phone}")
    void updateUserAvatar(@Param("avatar") String avatar,  @Param("phone") String phone);

    @Update("UPDATE users " +
            "SET history_order = #{hisOrder} " +
            "WHERE phone_number =#{phone}")
    void updateUserHisOrder(@Param("hisOrder") String hisOrder,  @Param("phone") String phone);

    @Update("UPDATE users " +
            "SET coupon_list = #{couponList} " +
            "WHERE phone_number =#{phone}")
    void updateUserCouponList(@Param("couponList") String couponList,  @Param("phone") String phone);

    @Delete("DELETE FROM users WHERE phone_number = #{phoneNumber}")
    void deleteUserByPhone(String phoneNumber);

}
