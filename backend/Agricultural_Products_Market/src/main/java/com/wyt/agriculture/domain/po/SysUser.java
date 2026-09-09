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
 * 用户信息表
 * </p>
 *
 * @author WYT
 * @since 2025-12-01
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("sys_user")
@Schema(description="用户信息表")
public class SysUser implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "用户ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @Schema(description = "登录用户名")
    private String username;

    @Schema(description = "加密密码（BCrypt）")
    private String password;

    @Schema(description = "角色：ROLE_VISITOR/ROLE_USER/ROLE_ADMIN")
    private String role;

    @Schema(description = "账号状态：1-启用，0-禁用")
    private Integer status;

    @Schema(description = "邮箱")
    private String email;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "更新时间")
    private LocalDateTime updateTime;


}
