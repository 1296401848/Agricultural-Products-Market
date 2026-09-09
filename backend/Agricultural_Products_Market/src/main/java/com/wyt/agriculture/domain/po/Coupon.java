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
 * 优惠券模板表
 * </p>
 *
 * @author WYT
 * @since 2025-12-01
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("coupon")
@Schema(description = "优惠券模板")
public class Coupon implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "优惠券ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

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

    @Schema(description = "总发放数量")
    private Integer totalCount;

    @Schema(description = "已领取数量")
    private Integer usedCount;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "领取开始时间")
    private LocalDateTime startTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "领取结束时间")
    private LocalDateTime endTime;

    @Schema(description = "领取后有效天数")
    private Integer validDays;

    @Schema(description = "状态：0-下架 1-正常")
    private Integer status;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}