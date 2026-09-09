package com.wyt.agriculture.task;

import com.wyt.agriculture.service.IUserCouponService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class CouponExpireTask {

    @Autowired
    private IUserCouponService userCouponService;

    /** 每小时扫描一次过期优惠券 */
    @Scheduled(cron = "0 0 * * * ?")
    public void expireCoupons() {
        log.info("开始扫描过期优惠券...");
        try {
            userCouponService.expireOverdueCoupons();
        } catch (Exception e) {
            log.error("扫描过期优惠券异常", e);
        }
    }
}
