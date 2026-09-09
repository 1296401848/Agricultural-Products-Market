package com.wyt.agriculture.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * <p>
 * 更改密码请求
 * </p>
 *
 * @author WYT
 * @since 2025-12-08
 */
@Data
@Schema(description = "更改密码请求")
public class ChangePasswordDTO {

    @Schema(description = "旧密码")
    private String oldPassword;

    @Schema(description = "新密码")
    private String newPassword;

    @Schema(description = "确认新密码")
    private String confirmPassword;

}
