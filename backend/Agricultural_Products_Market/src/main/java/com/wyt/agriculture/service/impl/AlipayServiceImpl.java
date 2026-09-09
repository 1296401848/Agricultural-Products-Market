package com.wyt.agriculture.service.impl;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.alipay.api.response.AlipayTradePagePayResponse;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.wyt.agriculture.domain.po.OrderInfo;
import com.wyt.agriculture.service.IAlipayService;
import com.wyt.agriculture.service.IOrderInfoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 支付宝支付服务实现类
 */
@Service
@Slf4j
public class AlipayServiceImpl implements IAlipayService {

    @Autowired
    private AlipayClient alipayClient;

    @Autowired
    private AlipayTradePagePayRequest alipayTradePagePayRequest;

    @Autowired
    private IOrderInfoService orderInfoService;

    @Override
    public String createPayForm(String orderNo, double totalAmount, String subject, String body) throws Exception {
        // 设置请求参数
        alipayTradePagePayRequest.setBizContent("{" +
            "\"out_trade_no\":\"" + orderNo + "\"," +
            "\"total_amount\":\"" + totalAmount + "\"," +
            "\"subject\":\"" + subject + "\"," +
            "\"body\":\"" + body + "\"," +
            "\"product_code\":\"FAST_INSTANT_TRADE_PAY\"" +
        "}");

        try {
            // 调用支付宝API，生成支付表单
            AlipayTradePagePayResponse response = alipayClient.pageExecute(alipayTradePagePayRequest);
            if (response.isSuccess()) {
                log.info("生成支付宝支付表单成功: orderNo={}, totalAmount={}", orderNo, totalAmount);
                return response.getBody();
            } else {
                log.error("生成支付宝支付表单失败: orderNo={}, error={}", orderNo, response.getMsg());
                throw new RuntimeException("生成支付表单失败: " + response.getMsg());
            }
        } catch (AlipayApiException e) {
            log.error("调用支付宝API失败: orderNo={}, error={}", orderNo, e.getMessage());
            throw new Exception("调用支付宝API失败", e);
        }
    }

    @Override
    @Transactional
    public String handleNotify(Map<String, String> params) {
        try {
            log.info("收到支付宝异步通知: params={}", params);

            // 获取订单编号和支付状态
            String orderNo = params.get("out_trade_no");
            String tradeStatus = params.get("trade_status");

            // 查询订单
            LambdaQueryWrapper<OrderInfo> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(OrderInfo::getOrderNo, orderNo);
            OrderInfo orderInfo = orderInfoService.getOne(queryWrapper);

            if (orderInfo == null) {
                log.error("支付宝异步通知处理失败: 订单不存在，orderNo={}", orderNo);
                return "failure";
            }

            // 如果订单已支付，直接返回成功
            if (orderInfo.getPayStatus() == 1) {
                log.info("支付宝异步通知处理: 订单已支付，orderNo={}", orderNo);
                return "success";
            }

            // 处理支付成功的情况
            if ("TRADE_SUCCESS".equals(tradeStatus) || "TRADE_FINISHED".equals(tradeStatus)) {
                // 更新订单状态为已支付
                orderInfo.setPayStatus(1);
                orderInfo.setPayTime(LocalDateTime.now());
                orderInfo.setUpdateTime(LocalDateTime.now());
                orderInfoService.updateById(orderInfo);

                log.info("支付宝异步通知处理成功: 订单支付完成，orderNo={}", orderNo);
                return "success";
            } else {
                log.warn("支付宝异步通知处理: 支付状态异常，orderNo={}, tradeStatus={}", orderNo, tradeStatus);
                return "failure";
            }
        } catch (Exception e) {
            log.error("处理支付宝异步通知失败: error={}", e.getMessage(), e);
            return "failure";
        }
    }
}
