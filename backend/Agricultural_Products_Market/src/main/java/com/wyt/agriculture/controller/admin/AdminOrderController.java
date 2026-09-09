package com.wyt.agriculture.controller.admin;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.wyt.agriculture.domain.dto.OrderDTO;
import com.wyt.agriculture.domain.po.OrderInfo;
import com.wyt.agriculture.domain.result.Result;
import com.wyt.agriculture.domain.vo.OrderVO;
import com.wyt.agriculture.service.IOrderInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import lombok.extern.slf4j.Slf4j;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin/order")
@Tag(name = "管理员订单管理", description = "管理员对订单的管理接口")
@Slf4j
public class AdminOrderController {

    @Autowired
    private IOrderInfoService orderInfoService;

    /**
     * 分页展示所有用户订单，支持按“下单日期”“支付状态（已支付/未支付）”“订单状态”查询订单
     * @param page 页码
     * @param size 每页大小
     * @param startDate 开始日期（可选）
     * @param endDate 结束日期（可选）
     * @param payStatus 支付状态（可选，0-未支付，1-已支付）
     * @param orderStatus 订单状态（可选，用于精确查询）
     * @return 订单列表
     */
    @GetMapping
    @Operation(summary = "分页展示所有用户订单", description = "分页查询所有用户订单，支持按下单日期、支付状态和订单状态筛选")
    public Result<IPage<OrderInfo>> getOrderList(@RequestParam(defaultValue = "1") Integer page, 
                                             @RequestParam(defaultValue = "10") Integer size,
                                             @RequestParam(required = false) String startDate,
                                             @RequestParam(required = false) String endDate,
                                             @RequestParam(required = false) Integer payStatus,
                                             @RequestParam(required = false) String orderStatus) {
        log.info("管理员查询订单列表请求: page={}, size={}, startDate={}, endDate={}, payStatus={}, orderStatus={}", 
                page, size, startDate, endDate, payStatus, orderStatus);
        try {
            // 解析日期
            LocalDateTime parsedStartDate = null;
            LocalDateTime parsedEndDate = null;
            if (startDate != null && !startDate.isEmpty()) {
                parsedStartDate = LocalDateTime.parse(startDate);
            }
            if (endDate != null && !endDate.isEmpty()) {
                parsedEndDate = LocalDateTime.parse(endDate);
            }
            IPage<OrderInfo> orderPage = orderInfoService.adminGetOrderList(page, size, parsedStartDate, parsedEndDate, payStatus, orderStatus);
            return Result.success(orderPage);
        } catch (Exception e) {
            log.warn("管理员查询订单列表失败: error={}", e.getMessage());
            return Result.error("查询订单列表失败: " + e.getMessage());
        }
    }

    /**
     * 修改订单状态（如将已支付订单标记为“已发货”）
     * @param orderDTO 更新订单状态请求
     * @return 操作结果
     */
    @PutMapping("/status")
    @Operation(summary = "修改订单状态", description = "更新订单状态，如将已支付订单标记为已发货")
    public Result<String> updateOrderStatus(@RequestBody OrderDTO orderDTO) {
        log.info("管理员更新订单状态请求: orderId={}, newStatus={}", orderDTO.getId(), orderDTO.getOrderStatus());
        try {
            boolean result = orderInfoService.adminUpdateOrderStatus(orderDTO);
            if (result) {
                return Result.success("更新订单状态成功");
            } else {
                return Result.error("更新订单状态失败");
            }
        } catch (RuntimeException e) {
            log.warn("管理员更新订单状态失败: orderId={}, error={}", orderDTO.getId(), e.getMessage());
            return Result.error(e.getMessage());
        }
    }

    /**
     * 查看订单详情
     * @param id 订单ID
     * @return 订单详情及订单项
     */
    @GetMapping("/{id}")
    @Operation(summary = "查看订单详情", description = "管理员查看指定订单的详细信息，包括订单项")
    public Result<OrderVO> getOrderDetail(@PathVariable Long id) {
        log.info("管理员查看订单详情请求: orderId={}", id);
        try {
            OrderVO orderVO = orderInfoService.adminGetOrderDetail(id);
            return Result.success(orderVO);
        } catch (RuntimeException e) {
            log.warn("管理员查看订单详情失败: orderId={}, error={}", id, e.getMessage());
            return Result.error(e.getMessage());
        }
    }

    @PutMapping("/batch-status")
    @Operation(summary = "批量修改订单状态")
    public Result<String> batchStatus(@RequestBody Map<String, Object> body) {
        @SuppressWarnings("unchecked")
        List<Integer> idList = (List<Integer>) body.get("ids");
        String status = (String) body.get("orderStatus");
        for (Integer id : idList) {
            OrderDTO dto = new OrderDTO(); dto.setId(id.longValue()); dto.setOrderStatus(status);
            orderInfoService.adminUpdateOrderStatus(dto);
        }
        return Result.success("批量操作完成");
    }

    @DeleteMapping("/batch")
    @Operation(summary = "批量删除订单")
    public Result<String> batchDelete(@RequestBody Map<String, Object> body) {
        @SuppressWarnings("unchecked")
        List<Integer> idList = (List<Integer>) body.get("ids");
        for (Integer id : idList) orderInfoService.adminDeleteOrder(id.longValue());
        return Result.success("批量删除完成");
    }

}