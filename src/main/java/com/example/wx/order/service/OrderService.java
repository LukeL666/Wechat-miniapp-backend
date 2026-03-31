package com.example.wx.order.service;

import com.example.wx.aes.AESUtils;
import com.example.wx.order.mapper.OrderMapper;
import com.example.wx.order.entity.Order;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class OrderService {

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private AESUtils aes;

    public void addOrder(String data) {

        String[] fields = data.split("\\|");
//        items.getJSONObject(i).get("id") + "|" +
//        items.getJSONObject(i).get("quantity") + "|" +
//                orderId + "|" + encryptPhone;
        for(String field : fields){
            System.out.println(field);
        }
        String id = fields[0];
        int quantity = Integer.parseInt(fields[1]);
        String orderId = fields[2];
        String phone = aes.decrypt(fields[3]);
        orderMapper.addOrder(phone, orderId, id, quantity, 0);
    }

    public List<Order> getOrder(String orderId) {
        return orderMapper.getOrder(orderId);
    }

    public void updateOrder(String orderId) {
        orderMapper.updateOrder(orderId);
    }

    public boolean isComplete(String orderId) {
        Map<String, Integer> qL = orderMapper.getQuantity(orderId);
        if(qL!=null && !qL.isEmpty()){
            return qL.get("quantityMade").equals(qL.get("totalQuantity"));
        }
        return false;
    }
}
