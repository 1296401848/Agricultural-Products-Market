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
 * 农产品信息表
 * </p>
 *
 * @author WYT
 * @since 2025-12-01
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("produce_info")
@Schema(description="农产品信息表")
public class ProduceInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "农产品ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @Schema(description = "农产品名称")
    private String produceName;

    @Schema(description = "分类ID")
    private Long categoryId;

    @Schema(description = "生产商")
    private String manufacturer;

    @Schema(description = "农产品价格")
    private BigDecimal price;

    @Schema(description = "库存数量")
    private Integer stock;

    @Schema(description = "农产品简介")
    private String description;

    @Schema(description = "状态：1-上架，0-下架")
    private Integer status;

    @Schema(description = "封面图片URL")
    private String coverUrl;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "更新时间")
    private LocalDateTime updateTime;


}
