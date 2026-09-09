package com.wyt.agriculture.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wyt.agriculture.domain.po.Coupon;
import com.wyt.agriculture.domain.po.UserCoupon;
import com.wyt.agriculture.mapper.CouponMapper;
import com.wyt.agriculture.service.ICouponService;
import com.wyt.agriculture.service.IUserCouponService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class CouponServiceImpl extends ServiceImpl<CouponMapper, Coupon> implements ICouponService {

    @Autowired
    private IUserCouponService userCouponService;

    @Override
    public IPage<Coupon> adminGetCouponList(Integer page, Integer size, String couponName) {
        LambdaQueryWrapper<Coupon> qw = new LambdaQueryWrapper<>();
        if (couponName != null && !couponName.isEmpty()) {
            qw.like(Coupon::getCouponName, couponName);
        }
        qw.orderByDesc(Coupon::getCreateTime);
        return this.page(new Page<>(page, size), qw);
    }

    @Override
    public void adminCreateCoupon(Coupon coupon) {
        coupon.setUsedCount(0);
        coupon.setCreateTime(LocalDateTime.now());
        this.save(coupon);
        log.info("创建优惠券: {}", coupon.getCouponName());
    }

    @Override
    public void adminUpdateCoupon(Coupon coupon) {
        this.updateById(coupon);
        log.info("更新优惠券: id={}", coupon.getId());
    }

    @Override
    public void adminUpdateStatus(Long id, Integer status) {
        Coupon coupon = new Coupon();
        coupon.setId(id);
        coupon.setStatus(status);
        this.updateById(coupon);
        log.info("优惠券状态变更: id={}, status={}", id, status);
    }

    @Override
    public List<Coupon> getAvailableCoupons() {
        LocalDateTime now = LocalDateTime.now();
        LambdaQueryWrapper<Coupon> qw = new LambdaQueryWrapper<>();
        qw.eq(Coupon::getStatus, 1)
          .le(Coupon::getStartTime, now)
          .ge(Coupon::getEndTime, now);
        return this.list(qw);
    }

    @Override
    @Transactional
    public Map<String, Object> receiveCoupon(Long couponId, Long userId) {
        // 1. 校验优惠券
        Coupon coupon = this.getById(couponId);
        if (coupon == null || coupon.getStatus() != 1) {
            throw new RuntimeException("优惠券不存在或已下架");
        }
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(coupon.getStartTime()) || now.isAfter(coupon.getEndTime())) {
            throw new RuntimeException("不在领取时间范围内");
        }
        if (coupon.getUsedCount() >= coupon.getTotalCount()) {
            throw new RuntimeException("优惠券已被领完");
        }

        // 2. 防重复领取
        LambdaQueryWrapper<UserCoupon> checkQw = new LambdaQueryWrapper<>();
        checkQw.eq(UserCoupon::getUserId, userId)
                .eq(UserCoupon::getCouponId, couponId);
        if (userCouponService.count(checkQw) > 0) {
            throw new RuntimeException("您已领取过该优惠券");
        }

        // 3. 创建用户优惠券
        UserCoupon uc = new UserCoupon();
        uc.setUserId(userId);
        uc.setCouponId(couponId);
        uc.setCouponName(coupon.getCouponName());
        uc.setCouponType(coupon.getCouponType());
        uc.setFullMoney(coupon.getFullMoney());
        uc.setReduceMoney(coupon.getReduceMoney());
        uc.setDiscount(coupon.getDiscount());
        uc.setValidStart(now);
        uc.setValidEnd(now.plusDays(coupon.getValidDays()));
        uc.setUseStatus(0);
        uc.setReceiveTime(now);
        userCouponService.save(uc);

        // 4. 更新领取数量
        coupon.setUsedCount(coupon.getUsedCount() + 1);
        this.updateById(coupon);

        log.info("领取优惠券: userId={}, couponId={}, name={}", userId, couponId, coupon.getCouponName());

        Map<String, Object> result = new HashMap<>();
        result.put("userCouponId", uc.getId());
        result.put("couponName", uc.getCouponName());
        return result;
    }
}
