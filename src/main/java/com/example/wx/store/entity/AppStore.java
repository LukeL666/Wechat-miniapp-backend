package com.example.wx.store.entity;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AppStore {
    private String sn;
    private String appName;
    private String productList;
}