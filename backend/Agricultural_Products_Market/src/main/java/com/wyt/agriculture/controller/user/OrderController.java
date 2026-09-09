package com.wyt.agriculture.controller.user;

import com.wyt.agriculture.domain.dto.OrderDTO;
import com.wyt.agriculture.domain.dto.PayOrderDTO;
import com.wyt.agriculture.domain.po.OrderInfo;
import com.wyt.agriculture.domain.result.Result;
import com.wyt.agriculture.domain.vo.OrderVO;
import com.wyt.agriculture.service.IAlipayService;
import com.wyt.agriculture.service.IOrderInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import io.swagger.v3.oas.annotations.Operation;

import java.util.List;

/**
 * <p>
 * 订单表 前端控制器
 * </p>
 *
 * @author WYT
 * @since 2025-12-01
 */
@RestController
@RequestMapping("/order")
@Tag(name = "订单管理", description = "用户订单相关接口")
@Slf4j
public class OrderController {

    @Autowired
    private IOrderInfoService orderInfoService;
    
    @Autowired
    private IAlipayService alipayService;

    /**
     * 创建订单
     * @param orderDTO 创建订单请求
     * @return 订单信息
     */
    @PostMapping
    @Operation(summary = "创建订单", description = "根据购物车内容创建新订单")
    public Result<OrderInfo> createOrder(@RequestBody OrderDTO orderDTO) {
        // 获取当前用户名
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        log.info("创建订单请求: username={}, addressId={}", username, orderDTO.getAddressId());
        try {
            OrderInfo orderInfo = orderInfoService.createOrder(username, orderDTO);
            return Result.success(orderInfo, "创建订单成功");
        } catch (RuntimeException e) {
            log.warn("创建订单失败: username={}, error={}", username, e.getMessage());
            return Result.error(e.getMessage());
        }
    }

    /**
     * 支付订单
     * @param payOrderDTO 支付订单请求
     * @return 支付表单HTML
     */
    @PostMapping("/pay")
    @Operation(summary = "支付订单", description = "生成支付宝支付表单")
    public Result<String> payOrder(@RequestBody PayOrderDTO payOrderDTO) {
        // 获取当前用户名
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        log.info("支付订单请求: username={}, orderId={}", username, payOrderDTO.getOrderId());
        try {
            // 根据订单ID查询订单信息
            OrderInfo orderInfo = orderInfoService.getById(payOrderDTO.getOrderId());
            if (orderInfo == null) {
                throw new RuntimeException("订单不存在");
            }

            // 实付金额
            double payAmount = orderInfo.getPayAmount() != null ? orderInfo.getPayAmount().doubleValue() : orderInfo.getTotalAmount().doubleValue();
            // 调用支付宝支付服务生成支付表单
            String payForm = alipayService.createPayForm(
                    orderInfo.getOrderNo(),
                    payAmount,
                    "农产品订单支付",
                    "订单号: " + orderInfo.getOrderNo()
            );
            
            return Result.success(payForm, "生成支付表单成功");
        } catch (RuntimeException e) {
            log.warn("支付订单失败: username={}, orderId={}, error={}", username, payOrderDTO.getOrderId(), e.getMessage());
            return Result.error(e.getMessage());
        } catch (Exception e) {
            log.error("生成支付表单失败: username={}, orderId={}, error={}", username, payOrderDTO.getOrderId(), e.getMessage());
            return Result.error("生成支付表单失败: " + e.getMessage());
        }
    }

    /**
     * 查看订单列表
     * @return 订单列表
     */
    @GetMapping
    @Operation(summary = "查看订单列表", description = "获取当前用户的所有订单")
    public Result<List<OrderInfo>> getOrderList() {
        // 获取当前用户名
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        log.info("查看订单列表请求: username={}", username);
        try {
            List<OrderInfo> orderList = orderInfoService.getOrderList(username);
            return Result.success(orderList);
        } catch (RuntimeException e) {
            log.warn("查看订单列表失败: username={}, error={}", username, e.getMessage());
            return Result.error(e.getMessage());
        }
    }

    /**
     * 查看订单详情
     * @param id 订单ID
     * @return 订单详情及订单项
     */
    @GetMapping("/{id}")
    @Operation(summary = "查看订单详情", description = "获取指定订单的详细信息，包括订单项")
    public Result<OrderVO> getOrderDetail(@PathVariable Long id) {
        // 获取当前用户名
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        log.info("查看订单详情请求: username={}, orderId={}", username, id);
        try {
            OrderVO orderVO = orderInfoService.getOrderDetail(username, id);
            return Result.success(orderVO);
        } catch (RuntimeException e) {
            log.warn("查看订单详情失败: username={}, orderId={}, error={}", username, id, e.getMessage());
            return Result.error(e.getMessage());
        }
    }

    /**
     * 确认收货
     */
    @PutMapping("/{id}/confirm")
    @Operation(summary = "确认收货")
    public Result<String> confirmOrder(@PathVariable Long id) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        log.info("确认收货请求: username={}, orderId={}", username, id);
        try {
            orderInfoService.confirmOrder(username, id);
            return Result.success("已确认收货");
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 取消订单
     */
    @PutMapping("/{id}/cancel")
    @Operation(summary = "取消订单", description = "未支付订单可取消，恢复库存")
    public Result<String> cancelOrder(@PathVariable Long id) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        log.info("取消订单请求: username={}, orderId={}", username, id);
        try {
            orderInfoService.cancelOrder(username, id);
            return Result.success("订单已取消");
        } catch (RuntimeException e) {
            log.warn("取消订单失败: username={}, error={}", username, e.getMessage());
            return Result.error(e.getMessage());
        }
    }
}
