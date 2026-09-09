package com.wyt.agriculture.controller.admin;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.wyt.agriculture.domain.dto.UserDTO;
import com.wyt.agriculture.domain.po.SysUser;
import com.wyt.agriculture.domain.result.Result;
import com.wyt.agriculture.service.ISysUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import lombok.extern.slf4j.Slf4j;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin/user")
@Tag(name = "管理员用户管理", description = "管理员对用户的管理接口")
@Slf4j
public class AdminUserController {

    @Autowired
    private ISysUserService sysUserService;

    /**
     * 查看所有普通用户列表
     * @param page 页码
     * @param size 每页大小
     * @param username 用户名（可选，用于模糊查询）
     * @param status 用户状态（可选，用于精确查询）
     * @return 用户列表
     */
    @GetMapping
    @Operation(summary = "查看所有普通用户列表", description = "分页查询所有普通用户，支持用户名模糊搜索、状态精确搜索和排序")
    public Result<IPage<SysUser>> getUserList(@RequestParam(defaultValue = "1") Integer page,
                                             @RequestParam(defaultValue = "10") Integer size,
                                             @RequestParam(required = false) String username,
                                             @RequestParam(required = false) Integer status,
                                             @RequestParam(required = false) String sortField,
                                             @RequestParam(required = false) String sortOrder) {
        log.info("管理员查询用户列表请求: page={}, size={}, username={}, status={}, sortField={}, sortOrder={}",
                page, size, username, status, sortField, sortOrder);
        IPage<SysUser> userPage = sysUserService.adminGetUserList(page, size, username, status, sortField, sortOrder);
        return Result.success(userPage);
    }

    /**
     * 管理员添加用户
     */
    @PostMapping
    @Operation(summary = "添加用户", description = "管理员创建普通用户或管理员")
    public Result<String> addUser(@RequestBody UserDTO userDTO) {
        log.info("管理员添加用户: username={}, role={}", userDTO.getUsername(), userDTO.getRole());
        try {
            sysUserService.adminAddUser(userDTO);
            return Result.success("添加成功");
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 管理员删除用户
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除用户")
    public Result<String> deleteUser(@PathVariable Long id) {
        log.info("管理员删除用户: userId={}", id);
        try {
            sysUserService.adminDeleteUser(id);
            return Result.success("删除成功");
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 禁用/启用用户账号
     * @param userDTO 更新状态请求
     * @return 操作结果
     */
    @PutMapping("/status")
    @Operation(summary = "禁用/启用用户账号", description = "更新用户账号状态")
    public Result<String> updateUserStatus(@RequestBody UserDTO userDTO) {
        log.info("管理员更新用户状态请求: userId={}, newStatus={}", userDTO.getId(), userDTO.getStatus());
        try {
            boolean result = sysUserService.adminUpdateUserStatus(userDTO);
            if (result) {
                return Result.success("更新用户状态成功");
            } else {
                return Result.error("更新用户状态失败");
            }
        } catch (RuntimeException e) {
            log.warn("管理员更新用户状态失败: userId={}, error={}", userDTO.getId(), e.getMessage());
            return Result.error(e.getMessage());
        }
    }

    @PutMapping("/batch-status")
    @Operation(summary = "批量禁用/启用")
    public Result<String> batchStatus(@RequestBody Map<String, Object> body) {
        @SuppressWarnings("unchecked")
        List<Integer> idList = (List<Integer>) body.get("ids");
        Integer status = (Integer) body.get("status");
        for (Integer id : idList) {
            UserDTO dto = new UserDTO();
            dto.setId(id.longValue());
            dto.setStatus(status);
            sysUserService.adminUpdateUserStatus(dto);
        }
        return Result.success("批量操作完成");
    }

    @DeleteMapping("/batch")
    @Operation(summary = "批量删除")
    public Result<String> batchDelete(@RequestBody Map<String, Object> body) {
        @SuppressWarnings("unchecked")
        List<Integer> idList = (List<Integer>) body.get("ids");
        for (Integer id : idList) sysUserService.adminDeleteUser(id.longValue());
        return Result.success("批量删除完成");
    }
}