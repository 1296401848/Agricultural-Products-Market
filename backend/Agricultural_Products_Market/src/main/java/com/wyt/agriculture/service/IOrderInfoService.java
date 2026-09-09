package com.wyt.agriculture.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.wyt.agriculture.domain.dto.PayOrderDTO;
import com.wyt.agriculture.domain.dto.OrderDTO;
import com.wyt.agriculture.domain.po.OrderInfo;
import com.baomidou.mybatisplus.extension.service.IService;
import com.wyt.agriculture.domain.vo.OrderVO;

import java.time.LocalDateTime;
import java.util.List;

/**
 * <p>
 * 订单表 服务类
 * </p>
 *
 * @author WYT
 * @since 2025-12-01
 */
public interface IOrderInfoService extends IService<OrderInfo> {
    /**
     * 创建订单
     * @param username 用户名
     * @param orderDTO 创建订单请求
     * @return 订单信息
     */
    OrderInfo createOrder(String username, OrderDTO orderDTO);

    /**
     * 支付订单
     * @param username 用户名
     * @param payOrderDTO 支付订单请求
     * @return 支付结果
     */
    boolean payOrder(String username, PayOrderDTO payOrderDTO);

    /**
     * 获取用户订单列表
     * @param username 用户名
     * @return 订单列表
     */
    List<OrderInfo> getOrderList(String username);

    /**
     * 获取订单详情
     * @param username 用户名
     * @param orderId 订单ID
     * @return 订单信息及订单项
     */
    OrderVO getOrderDetail(String username, Long orderId);

    /**
     * 管理员分页查询订单
     * @param page 页码
     * @param size 每页大小
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @param payStatus 支付状态
     * @param orderStatus 订单状态
     * @return 订单分页列表
     */
    IPage<OrderInfo> adminGetOrderList(Integer page, Integer size, LocalDateTime startDate, LocalDateTime endDate, Integer payStatus, String orderStatus);

    /**
     * 管理员更新订单状态
     * @param orderDTO 更新订单状态请求
     * @return 更新结果
     */
    boolean adminUpdateOrderStatus(OrderDTO orderDTO);

    /**
     * 管理员获取订单详情
     * @param orderId 订单ID
     * @return 订单信息及订单项
     */
    OrderVO adminGetOrderDetail(Long orderId);

    /**
     * 确认收货
     */
    void confirmOrder(String username, Long orderId);

    /**
     * 取消订单（用户取消 + 系统自动取消）
     * @param username 用户名（系统调用时为 null）
     * @param orderId 订单ID
     */
    void cancelOrder(String username, Long orderId);

    /**
     * 定时任务：自动取消超时未支付订单
     */
    void autoCancelExpiredOrders();

    /**
     * 管理员删除订单
     */
    void adminDeleteOrder(Long id);
}
