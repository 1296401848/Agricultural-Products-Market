package com.wyt.agriculture.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.wyt.agriculture.domain.po.Coupon;

import java.util.List;
import java.util.Map;

public interface ICouponService extends IService<Coupon> {

    IPage<Coupon> adminGetCouponList(Integer page, Integer size, String couponName);

    void adminCreateCoupon(Coupon coupon);

    void adminUpdateCoupon(Coupon coupon);

    void adminUpdateStatus(Long id, Integer status);

    /** 用户端：获取可领取的优惠券 */
    List<Coupon> getAvailableCoupons();

    /** 领取优惠券，返回 UserCoupon JSON */
    Map<String, Object> receiveCoupon(Long couponId, Long userId);
}
