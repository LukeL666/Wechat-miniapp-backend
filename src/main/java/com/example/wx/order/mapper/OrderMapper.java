package com.example.wx.order.mapper;


import com.example.wx.order.entity.Order;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Map;

public interface OrderMapper {
    @Insert("INSERT INTO orders (phone, order_id, id, total_quantity, quantity_made)" +
            "VALUES ($phone, ${orderId},${id} ${totalQuantity}, ${quantityMade})")
    void addOrder(@Param("phone") String phone, @Param("orderId") String orderId, @Param("id") String id,
                  @Param("total_quantity") int totalQuantity, @Param("quantityMade") int quantityMade
                  );
    @Select("SELECT id, order_id AS orderId, total_quantity AS totalQuantity, quantity_made AS quantityMade, phone" +
            " FROM orders WHERE order_id = #{orderId}")
    List<Order> getOrder(String orderId);

    @Update("UPDATE orders SET quantity_made = quantity_made + 1 WHERE order_id = ${orderId}")
    void updateOrder(String orderId);

    @Select("SELECT quantity_made AS quantityMade, total_quantity AS totalQuantity FROM orders WHERE order_id = ${orderId}")
    Map<String, Integer> getQuantity(String orderId);
}
