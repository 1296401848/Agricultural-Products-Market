package com.wyt.agriculture.domain.dto;

import lombok.Data;

/**
 * 更新订单状态请求DTO
 */
@Data
public class OrderDTO {

    private Long id;

    private Long addressId;

    /** 用户选择的优惠券ID */
    private Long userCouponId;

    private String orderStatus;

}