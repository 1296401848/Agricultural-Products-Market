package com.wyt.agriculture.controller.admin;

import com.wyt.agriculture.domain.dto.UserDTO;
import com.wyt.agriculture.domain.result.Result;
import com.wyt.agriculture.service.IAuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * <p>
 * 管理员认证控制器
 * </p>
 *
 * @author WYT
 * @since 2025-12-08
 */
@RestController
@RequestMapping("/admin/auth")
@Tag(name = "管理员认证", description = "管理员登录相关接口")
@Slf4j
public class AdminAuthController {

    @Autowired
    private IAuthService authService;

    /**
     * 管理员登录
     * @param userDTO 登录信息
     * @return 登录结果，包含token、username、role等信息
     */
    @PostMapping("/login")
    @Operation(summary = "管理员登录", description = "管理员登录接口，返回JWT令牌")
    public Result login(@RequestBody UserDTO userDTO) {
        log.info("管理员登录请求: username={}", userDTO.getUsername());
        try {
            Map<String, String> result = authService.login(userDTO);

            // 非管理员禁止登录管理端
            if (!"ROLE_ADMIN".equals(result.get("role"))) {
                log.warn("非管理员尝试登录管理端: username={}, role={}", result.get("username"), result.get("role"));
                return Result.error("无权限访问管理端");
            }

            log.info("管理员登录成功: username={}, role={}", result.get("username"), result.get("role"));
            return Result.success(result, "管理员登录成功");
        } catch (RuntimeException e) {
            log.warn("管理员登录失败: username={}, error={}", userDTO.getUsername(), e.getMessage());
            return Result.error(e.getMessage());
        }
    }
}
