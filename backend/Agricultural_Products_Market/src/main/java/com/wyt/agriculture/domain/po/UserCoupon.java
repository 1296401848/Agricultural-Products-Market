package com.wyt.agriculture.domain.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * <p>
 * 用户优惠券关联表
 * </p>
 *
 * @author WYT
 * @since 2025-12-01
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("user_coupon")
@Schema(description = "用户优惠券")
public class UserCoupon implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "主键ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "优惠券模板ID")
    private Long couponId;

    @Schema(description = "优惠券名称")
    private String couponName;

    @Schema(description = "优惠券类型：1-满减券 2-无门槛券 3-折扣券")
    private Integer couponType;

    @Schema(description = "满减门槛金额")
    private BigDecimal fullMoney;

    @Schema(description = "减免金额")
    private BigDecimal reduceMoney;

    @Schema(description = "折扣比例，范围0.01~1.00")
    private BigDecimal discount;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "使用开始时间")
    private LocalDateTime validStart;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "使用截止时间")
    private LocalDateTime validEnd;

    @Schema(description = "使用状态：0-未使用 1-已使用 2-已过期")
    private Integer useStatus;

    @Schema(description = "关联订单ID")
    private Long orderId;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "领取时间")
    private LocalDateTime receiveTime;
}