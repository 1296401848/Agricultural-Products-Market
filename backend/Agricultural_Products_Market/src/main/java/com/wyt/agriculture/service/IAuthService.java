package com.wyt.agriculture.service;

import com.wyt.agriculture.domain.dto.PasswordResetDTO;
import com.wyt.agriculture.domain.dto.UserDTO;

import java.util.Map;

/**
 * 认证服务接口
 */
public interface IAuthService {
    /**
     * 用户登录
     * @param userDTO 登录参数
     * @return 登录结果，包含token等信息
     */
    Map<String, String> login(UserDTO userDTO);

    /**
     * 用户注册
     * @param userDTO 注册参数
     * @return 注册结果
     */
    void register(UserDTO userDTO);

    /**
     * 生成图形验证码
     * @return captchaKey + Base64 图片
     */
    Map<String, String> generateCaptcha();

    /**
     * 发送密码重置验证码
     * @param dto 包含邮箱、图形验证码
     */
    void sendResetCode(PasswordResetDTO dto);

    /**
     * 重置密码
     * @param dto 包含邮箱、验证码、新密码
     */
    void resetPassword(PasswordResetDTO dto);
}
