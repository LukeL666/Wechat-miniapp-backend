package com.example.wx.order;

import com.example.wx.order.entity.Order;
import com.example.wx.order.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.List;


@RestController
@Slf4j
@RequestMapping("/orders")
public class OrderController {
    @Autowired
    private OrderService orderService;

    private final AtomicInteger counter = new AtomicInteger(100);

    @PostMapping
    public void addOrder(@RequestBody String data) {
        orderService.addOrder(data);
    }

    @GetMapping
    public List<Order> getOrder(@RequestParam("orderId") String orderId) {
        return orderService.getOrder(orderId);
    }

    @PutMapping
    public void updateOrder(@RequestBody String orderId) {
        orderService.updateOrder(orderId);
    }

    @GetMapping("/getQueueNum")
    public int getQueueNum() {
        return counter.getAndIncrement();
    }
}
