package com.wyt.agriculture.domain.po;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("produce_package")
@Schema(description = "农产品套餐")
public class ProducePackage implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @Schema(description = "套餐名称")
    private String packageName;

    @Schema(description = "套餐简介")
    private String packageDesc;

    @Schema(description = "封面图")
    private String coverImg;

    @Schema(description = "单买总价")
    private BigDecimal originalPrice;

    @Schema(description = "套餐售卖价")
    private BigDecimal packagePrice;

    @Schema(description = "销量")
    private Integer sales;

    @Schema(description = "状态：0-下架 1-上架")
    private Integer status;

    @Schema(description = "是否推荐")
    private Integer isRecommend;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
}
