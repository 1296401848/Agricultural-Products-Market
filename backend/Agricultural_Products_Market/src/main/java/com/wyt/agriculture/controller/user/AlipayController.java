package com.wyt.agriculture.controller.user;

import com.wyt.agriculture.domain.result.Result;
import com.wyt.agriculture.service.IAlipayService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

/**
 * 支付宝支付控制器
 */
@Slf4j
@RestController
@RequestMapping("/alipay")
@Tag(name = "支付宝支付", description = "支付宝支付相关接口")
public class AlipayController {

    @Autowired
    private IAlipayService alipayService;

    /**
     * 生成支付宝支付表单
     * @param orderNo 订单编号
     * @param totalAmount 订单金额
     * @param subject 订单主题
     * @param body 订单描述
     * @return 支付表单HTML
     */
    @GetMapping("/pay")
    public Result<String> pay(@RequestParam String orderNo, 
                              @RequestParam double totalAmount, 
                              @RequestParam String subject, 
                              @RequestParam String body) {
        try {
            String payForm = alipayService.createPayForm(orderNo, totalAmount, subject, body);
            return Result.success(payForm, "生成支付表单成功");
        } catch (Exception e) {
            return Result.error("生成支付表单失败: " + e.getMessage());
        }
    }

    /**
     * 处理支付宝异步通知
     * @param params 支付宝通知参数
     * @return 处理结果
     */
    @PostMapping("/notify")
    public String notify(@RequestParam Map<String, String> params) {
        return alipayService.handleNotify(params);
    }

    /**
     * 处理支付宝同步返回（跳转前端订单页）
     */
    @org.springframework.beans.factory.annotation.Value("${app.frontend-url:http://localhost:5173}")
    private String frontendUrl;

    @GetMapping("/return")
    public ModelAndView returnUrl(@RequestParam Map<String, String> params) {
        log.info("支付宝同步返回参数: {}", params);
        return new ModelAndView("redirect:" + frontendUrl + "/orders?paid=1");
    }
}
