package com.wyt.agriculture.task;

import com.wyt.agriculture.service.IOrderInfoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class OrderExpireTask {

    @Autowired
    private IOrderInfoService orderInfoService;

    /** 每5分钟扫描一次超时未支付订单 */
    @Scheduled(cron = "0 */5 * * * ?")
    public void autoCancel() {
        log.info("开始扫描超时未支付订单...");
        try {
            orderInfoService.autoCancelExpiredOrders();
        } catch (Exception e) {
            log.error("扫描超时订单异常", e);
        }
    }
}
