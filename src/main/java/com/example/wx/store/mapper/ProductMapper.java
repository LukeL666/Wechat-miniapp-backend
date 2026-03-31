package com.example.wx.store.mapper;

import com.example.wx.store.entity.AppStore;
import org.apache.ibatis.annotations.*;
import java.util.List;

import java.util.Map;

public interface ProductMapper {

    @Select("SELECT password FROM app_product WHERE username={$userName}")
    String getPassword(@Param("userName") String userName);

    @Insert({
            "<script>",
            "INSERT INTO app_product (sn, app_name, product_list) VALUES ",
            "<foreach collection='list' item='item' separator=','>",
            "(#{item.sn}, #{item.appName}, #{item.productList})",
            "</foreach>",
            "</script>"
    })
    void batchInsertStores(@Param("list") List<AppStore> stores);

    @Insert("INSERT INTO app_product(sn, app_name, product_list) " +
            "VALUES(#{sn}, #{appName}, #{productList})")
    void insertStore(@Param("sn") String sn,
                     @Param("appName") String appName,
                     @Param("productList") String productList);

    @Select("SELECT sn, app_name AS appName, product_list AS productList " +
            "FROM app_product WHERE app_name = #{appName}")
    Map<String, String> getStoreByAppName(@Param("appName") String appName);

    @Select("SELECT sn, app_name AS appName, product_list AS productList " +
            "FROM app_product WHERE username = #{userName}")
    Map<String, String> getStoreByUserName(@Param("userName") String userName);

    @Select("SELECT app_name AS appName FROM app_product")
    String[] getStores();



    @Update("UPDATE app_product SET app_name = #{appName}, product_list = #{productList} " +
            "WHERE sn = #{sn}")
    void updateStore(@Param("sn") String sn,
                     @Param("appName") String appName,
                     @Param("productList") String productList);

    @Delete("DELETE FROM app_product WHERE sn = #{sn}")
    void deleteStore(@Param("sn") String sn);
}
