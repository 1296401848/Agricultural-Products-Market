package com.wyt.agriculture.domain.po;

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
import org.springframework.format.annotation.DateTimeFormat;

/**
 * <p>
 * 农产品分类表
 * </p>
 *
 * @author WYT
 * @since 2025-12-01
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("category")
@Schema(description="农产品分类表")
public class Category implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "分类ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @Schema(description = "分类名称")
    private String categoryName;


    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "创建时间")
    private LocalDateTime createTime;


}
