package com.wyt.agriculture.controller.user;

import com.wyt.agriculture.domain.dto.PasswordResetDTO;
import com.wyt.agriculture.domain.dto.UserDTO;
import com.wyt.agriculture.domain.result.Result;
import com.wyt.agriculture.service.IAuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

@RestController
@RequestMapping("/auth")
@Tag(name = "认证管理", description = "用户登录和注册接口")
@Slf4j
public class AuthController {

    @Autowired
    private IAuthService authService;

    // 登录
    @PostMapping("/login")
    @Operation(summary = "用户登录")
    public Result login(@RequestBody UserDTO userDTO) {
        log.info("用户登录请求: username={}", userDTO.getUsername());
        try {
            Map<String, String> result = authService.login(userDTO);
            log.info("登录成功: username={}, role={}", result.get("username"), result.get("role"));
            return Result.success(result, "登录成功");
        } catch (RuntimeException e) {
            log.warn("登录失败: username={}, error={}", userDTO.getUsername(), e.getMessage());
            return Result.error(e.getMessage());
        }
    }

    // 注册
    @PostMapping("/register")
    @Operation(summary = "用户注册", description = "创建新用户账号")
    public Result<String> register(@RequestBody UserDTO userDTO) {
        log.info("用户注册请求: username={}", userDTO.getUsername());
        try {
            authService.register(userDTO);
            log.info("注册成功: username={}", userDTO.getUsername());
            return Result.success("注册成功");
        } catch (RuntimeException e) {
            log.warn("注册失败: username={}, error={}", userDTO.getUsername(), e.getMessage());
            return Result.error(e.getMessage());
        }
    }

    // 获取图形验证码
    @GetMapping("/captcha")
    @Operation(summary = "获取图形验证码", description = "返回 captchaKey 和 Base64 图片")
    public Result<Map<String, String>> captcha() {
        try {
            Map<String, String> result = authService.generateCaptcha();
            return Result.success(result);
        } catch (RuntimeException e) {
            log.warn("生成验证码失败: {}", e.getMessage());
            return Result.error(e.getMessage());
        }
    }

    // 发送密码重置验证码
    @PostMapping("/forgot-password")
    @Operation(summary = "发送密码重置验证码", description = "向注册邮箱发送6位验证码")
    public Result<String> forgotPassword(@RequestBody PasswordResetDTO dto) {
        log.info("密码重置验证码请求: email={}", dto.getEmail());
        try {
            authService.sendResetCode(dto);
            log.info("验证码发送成功: email={}", dto.getEmail());
            return Result.success("验证码已发送，请查收邮件");
        } catch (RuntimeException e) {
            log.warn("验证码发送失败: email={}, error={}", dto.getEmail(), e.getMessage());
            return Result.error(e.getMessage());
        }
    }

    // 重置密码
    @PostMapping("/reset-password")
    @Operation(summary = "重置密码", description = "验证码校验通过后更新密码")
    public Result<String> resetPassword(@RequestBody PasswordResetDTO dto) {
        log.info("密码重置请求: email={}", dto.getEmail());
        try {
            authService.resetPassword(dto);
            log.info("密码重置成功: email={}", dto.getEmail());
            return Result.success("密码重置成功，请使用新密码登录");
        } catch (RuntimeException e) {
            log.warn("密码重置失败: email={}, error={}", dto.getEmail(), e.getMessage());
            return Result.error(e.getMessage());
        }
    }
}
