package com.wyt.agriculture.domain.vo;

import com.wyt.agriculture.domain.po.OrderItem;
import com.wyt.agriculture.domain.po.OrderInfo;
import lombok.Data;

import java.util.List;

/**
 * 订单VO
 */
@Data
public class OrderVO {

    private OrderInfo orderInfo;
    private List<OrderItem> orderItems;
    private String recipient;
    private String phone;
    private String address;
}