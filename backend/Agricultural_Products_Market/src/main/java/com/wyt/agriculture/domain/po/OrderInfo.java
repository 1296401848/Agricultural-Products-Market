package com.wyt.agriculture.domain.po;

import java.math.BigDecimal;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.time.LocalDateTime;
import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 订单表
 * </p>
 *
 * @author WYT
 * @since 2025-12-01
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("order_info")
@Schema(description="订单表")
public class OrderInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "订单ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @Schema(description = "订单编号")
    private String orderNo;

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "地址ID")
    private Long addressId;

    @Schema(description = "订单总金额")
    private BigDecimal totalAmount;

    @Schema(description = "支付状态：0-未支付，1-已支付")
    private Integer payStatus;

    @Schema(description = "订单状态：待发货/已发货/已完成/已取消")
    private String orderStatus;

    @Schema(description = "支付时间")
    private LocalDateTime payTime;

    @Schema(description = "优惠券抵扣金额")
    private BigDecimal couponDiscount;

    @Schema(description = "实付金额")
    private BigDecimal payAmount;

    @Schema(description = "使用的用户优惠券ID")
    private Long userCouponId;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "更新时间")
    private LocalDateTime updateTime;


}
