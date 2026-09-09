package com.wyt.agriculture.controller.user;

import com.wyt.agriculture.domain.dto.ChangePasswordDTO;
import com.wyt.agriculture.domain.result.Result;
import com.wyt.agriculture.service.ISysUserService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RestController;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * <p>
 * 用户信息表 前端控制器
 * </p>
 *
 * @author WYT
 * @since 2025-12-01
 */
@RestController
@RequestMapping("/user")
@Tag(name = "用户管理", description = "用户信息相关接口")
@Slf4j
public class SysUserController {

    @Autowired
    private ISysUserService sysUserService;

    /**
     * 更改用户密码
     * @param changePasswordDTO 更改密码请求
     * @return 更改结果
     */
    @PutMapping("/password")
    @Operation(summary = "更改密码", description = "当前登录用户更改密码")
    public Result<String> changePassword(@RequestBody ChangePasswordDTO changePasswordDTO) {
        // 获取当前用户名
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        log.info("更改密码请求: username={}", username);
        
        try {
            boolean result = sysUserService.changePassword(username, changePasswordDTO);
            if (result) {
                log.info("更改密码成功: username={}", username);
                return Result.success("更改密码成功");
            } else {
                log.warn("更改密码失败: username={}", username);
                return Result.error("更改密码失败");
            }
        } catch (RuntimeException e) {
            log.warn("更改密码失败: username={}, error={}", username, e.getMessage());
            return Result.error(e.getMessage());
        }
    }
}
