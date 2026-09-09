package com.wyt.agriculture.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wyt.agriculture.domain.po.UserCoupon;
import com.wyt.agriculture.mapper.UserCouponMapper;
import com.wyt.agriculture.service.IUserCouponService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
public class UserCouponServiceImpl extends ServiceImpl<UserCouponMapper, UserCoupon> implements IUserCouponService {

    @Override
    public List<UserCoupon> getUserCoupons(Long userId, Integer useStatus) {
        LambdaQueryWrapper<UserCoupon> qw = new LambdaQueryWrapper<>();
        qw.eq(UserCoupon::getUserId, userId);
        if (useStatus != null) {
            qw.eq(UserCoupon::getUseStatus, useStatus);
        }
        qw.orderByDesc(UserCoupon::getReceiveTime);
        return this.list(qw);
    }

    @Override
    public BigDecimal useCoupon(Long userCouponId, Long orderId, BigDecimal totalAmount) {
        UserCoupon uc = this.getById(userCouponId);
        if (uc == null) {
            throw new RuntimeException("优惠券不存在");
        }
        if (uc.getUseStatus() != 0) {
            throw new RuntimeException("优惠券不可用");
        }
        if (uc.getValidEnd().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("优惠券已过期");
        }

        // 计算优惠金额
        BigDecimal discountAmount = calculateDiscount(uc, totalAmount);

        // 标记已使用
        uc.setUseStatus(1);
        uc.setOrderId(orderId);
        this.updateById(uc);

        log.info("使用优惠券: id={}, orderId={}, discountAmount={}", userCouponId, orderId, discountAmount);
        return discountAmount;
    }

    @Override
    public void refundCoupon(Long orderId) {
        LambdaQueryWrapper<UserCoupon> qw = new LambdaQueryWrapper<>();
        qw.eq(UserCoupon::getOrderId, orderId)
          .eq(UserCoupon::getUseStatus, 1);
        UserCoupon uc = this.getOne(qw);
        if (uc != null) {
            uc.setUseStatus(0);
            uc.setOrderId(null);
            this.updateById(uc);
            log.info("退还优惠券: id={}, orderId={}", uc.getId(), orderId);
        }
    }

    @Override
    public void expireOverdueCoupons() {
        LambdaUpdateWrapper<UserCoupon> uw = new LambdaUpdateWrapper<>();
        uw.eq(UserCoupon::getUseStatus, 0)
          .lt(UserCoupon::getValidEnd, LocalDateTime.now())
          .set(UserCoupon::getUseStatus, 2);
        boolean updated = this.update(uw);
        log.info("过期优惠券扫描完成: updated={}", updated);
    }

    /** 计算优惠金额 */
    private BigDecimal calculateDiscount(UserCoupon uc, BigDecimal totalAmount) {
        int type = uc.getCouponType();
        if (type == 1) {
            // 满减券
            if (uc.getFullMoney() != null && totalAmount.compareTo(uc.getFullMoney()) >= 0) {
                return uc.getReduceMoney() != null ? uc.getReduceMoney() : BigDecimal.ZERO;
            }
            throw new RuntimeException("未达到满减门槛（满" + uc.getFullMoney() + "元可用）");
        } else if (type == 2) {
            // 无门槛券
            return uc.getReduceMoney() != null ? uc.getReduceMoney() : BigDecimal.ZERO;
        } else if (type == 3) {
            // 折扣券
            if (uc.getDiscount() != null) {
                return totalAmount.subtract(totalAmount.multiply(uc.getDiscount())).setScale(2, RoundingMode.HALF_UP);
            }
            return BigDecimal.ZERO;
        }
        return BigDecimal.ZERO;
    }
}
