package com.wyt.agriculture.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.wyt.agriculture.domain.po.UserCoupon;

import java.math.BigDecimal;
import java.util.List;

public interface IUserCouponService extends IService<UserCoupon> {

    List<UserCoupon> getUserCoupons(Long userId, Integer useStatus);

    /** 使用优惠券，返回优惠金额 */
    BigDecimal useCoupon(Long userCouponId, Long orderId, BigDecimal totalAmount);

    /** 退还优惠券 */
    void refundCoupon(Long orderId);

    /** 定时任务：标记过期 */
    void expireOverdueCoupons();
}
