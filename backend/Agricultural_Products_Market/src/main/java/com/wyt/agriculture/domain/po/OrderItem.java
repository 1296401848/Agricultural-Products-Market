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
 * 订单项明细表
 * </p>
 *
 * @author WYT
 * @since 2025-12-01
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("order_item")
@Schema(description="订单项明细表")
public class OrderItem implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "订单项ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @Schema(description = "订单ID")
    private Long orderId;

    @Schema(description = "农产品ID")
    private Long produceId;

    @Schema(description = "农产品名称")
    private String produceName;

    @Schema(description = "购买单价")
    private BigDecimal price;

    @Schema(description = "购买数量")
    private Integer quantity;

    @Schema(description = "套餐ID（itemType=2时有值）")
    private Long packageId;

    @Schema(description = "明细类型：1-普通农产品 2-套餐")
    private Integer itemType;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "创建时间")
    private LocalDateTime createTime;


}
