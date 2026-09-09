package com.wyt.agriculture.config;

import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.request.AlipayTradePagePayRequest;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 支付宝配置类
 */
@Configuration
@ConfigurationProperties(prefix = "alipay")
@Data
public class AlipayConfig {

    private String appId;
    private String gatewayUrl;
    private String privateKey;
    private String alipayPublicKey;
    private String notifyUrl;
    private String returnUrl;

    /**
     * 初始化支付宝客户端
     * @return 支付宝客户端
     */
    @Bean
    public AlipayClient alipayClient() {
        return new DefaultAlipayClient(
                gatewayUrl,
                appId,
                privateKey,
                "JSON",
                "UTF-8",
                alipayPublicKey,
                "RSA2"
        );
    }

    /**
     * 创建支付宝支付请求
     * @return 支付宝支付请求
     */
    @Bean
    public AlipayTradePagePayRequest alipayTradePagePayRequest() {
        AlipayTradePagePayRequest request = new AlipayTradePagePayRequest();
        request.setNotifyUrl(notifyUrl);
        request.setReturnUrl(returnUrl);
        return request;
    }
}
