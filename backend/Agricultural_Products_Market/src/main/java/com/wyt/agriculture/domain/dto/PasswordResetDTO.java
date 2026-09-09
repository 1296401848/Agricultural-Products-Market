package com.wyt.agriculture.domain.dto;

import lombok.Data;

/**
 * 密码重置请求DTO
 */
@Data
public class PasswordResetDTO {

    /** 发送验证码：邮箱 */
    private String email;

    /** 图形验证码 key */
    private String captchaKey;

    /** 图形验证码 */
    private String captchaCode;

    /** 重置密码：邮箱验证码 */
    private String code;

    /** 重置密码：新密码 */
    private String newPassword;
}
