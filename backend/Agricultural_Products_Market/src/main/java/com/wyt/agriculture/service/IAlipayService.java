package com.wyt.agriculture.service;

/**
 * 支付宝支付服务接口
 */
public interface IAlipayService {
    /**
     * 生成支付宝支付表单
     * @param orderNo 订单编号
     * @param totalAmount 订单金额
     * @param subject 订单主题
     * @param body 订单描述
     * @return 支付宝支付表单HTML
     * @throws Exception 支付生成异常
     */
    String createPayForm(String orderNo, double totalAmount, String subject, String body) throws Exception;

    /**
     * 处理支付宝异步通知
     * @param params 支付宝通知参数
     * @return 处理结果
     */
    String handleNotify(java.util.Map<String, String> params);
}
