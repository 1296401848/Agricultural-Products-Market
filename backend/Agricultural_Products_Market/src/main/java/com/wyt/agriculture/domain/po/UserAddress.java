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

/**
 * <p>
 * 用户地址表
 * </p>
 *
 * @author WYT
 * @since 2025-12-01
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("user_address")
@Schema(description="用户地址")
public class UserAddress implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "地址ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "收件人姓名")
    private String recipient;

    @Schema(description = "联系电话")
    private String phone;

    @Schema(description = "详细地址")
    private String address;

    @Schema(description = "是否默认地址：1-是，0-否")
    private Integer isDefault;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "创建时间")
    private LocalDateTime createTime;

}
