package com.wyt.agriculture.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wyt.agriculture.domain.dto.ChangePasswordDTO;
import com.wyt.agriculture.domain.dto.UserDTO;
import com.wyt.agriculture.domain.po.SysUser;
import com.wyt.agriculture.mapper.SysUserMapper;
import com.wyt.agriculture.service.ISysUserService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Set;

/**
 * <p>
 * 用户信息表 服务实现类
 * </p>
 *
 * @author WYT
 * @since 2025-12-01
 */
@Service
@Slf4j
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements ISysUserService {

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Override
    public SysUser getByUsername(String username) {
        return lambdaQuery()
                .eq(SysUser::getUsername, username)
                .one();
    }

    @Override
    public IPage<SysUser> adminGetUserList(Integer page, Integer size, String username, Integer status, String sortField, String sortOrder) {
        LambdaQueryWrapper<SysUser> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SysUser::getRole, "ROLE_USER");

        // 添加用户名模糊查询条件
        if (username != null && !username.isEmpty()) {
            queryWrapper.like(SysUser::getUsername, username);
        }

        // 添加状态精确查询条件
        if (status != null) {
            queryWrapper.eq(SysUser::getStatus, status);
        }

        // 添加排序
        if (sortField != null && !sortField.isEmpty()) {
            boolean isAsc = !"desc".equalsIgnoreCase(sortOrder);
            if ("username".equals(sortField)) {
                queryWrapper.orderBy(true, isAsc, SysUser::getUsername);
            } else if ("createTime".equals(sortField)) {
                queryWrapper.orderBy(true, isAsc, SysUser::getCreateTime);
            }
        }

        IPage<SysUser> userPage = this.page(new Page<>(page, size), queryWrapper);
        log.info("管理员查询用户列表成功: 总数={}, 页数={}, 搜索条件-用户名={}, 状态={}, 排序={} {}",
                userPage.getTotal(), userPage.getPages(), username, status, sortField, sortOrder);
        return userPage;
    }

    @Override
    public boolean adminUpdateUserStatus(UserDTO userDTO) {
        // 检查用户是否存在且为普通用户
        SysUser user = this.getById(userDTO.getId());
        if (user == null || !user.getRole().equals("ROLE_USER")) {
            log.warn("管理员更新用户状态失败: 用户不存在，userId={}", userDTO.getId());
            throw new RuntimeException("用户不存在");
        }

        // 更新用户状态
        user.setStatus(userDTO.getStatus());
        user.setUpdateTime(LocalDateTime.now());
        boolean result = this.updateById(user);

        // 禁用时清除该用户所有 Redis token
        if (userDTO.getStatus() == 0) {
            Set<String> keys = redisTemplate.keys("token:" + userDTO.getId() + ":*");
            if (keys != null && !keys.isEmpty()) {
                redisTemplate.delete(keys);
                log.info("已清除用户所有登录 token: userId={}, count={}", userDTO.getId(), keys.size());
            }
        }

        log.info("管理员更新用户状态成功: userId={}, username={}, newStatus={}",
                userDTO.getId(), user.getUsername(), userDTO.getStatus());
        return result;
    }

    @Override
    public void adminDeleteUser(Long id) {
        SysUser user = this.getById(id);
        if (user == null) throw new RuntimeException("用户不存在");
        if ("ROLE_ADMIN".equals(user.getRole())) throw new RuntimeException("不能删除管理员");
        this.removeById(id);
        // 清除 token
        Set<String> keys = redisTemplate.keys("token:" + id + ":*");
        if (keys != null && !keys.isEmpty()) redisTemplate.delete(keys);
        log.info("管理员删除用户: userId={}, username={}", id, user.getUsername());
    }

    @Override
    public void adminAddUser(UserDTO userDTO) {
        if (userDTO.getUsername() == null || userDTO.getUsername().isEmpty()) throw new RuntimeException("用户名不能为空");
        if (userDTO.getPassword() == null || userDTO.getPassword().length() < 8) throw new RuntimeException("密码至少8位");
        if (!userDTO.getPassword().matches("^(?=.*[a-zA-Z])(?=.*\\d).{8,20}$")) throw new RuntimeException("密码需包含英文字符和数字");
        if (getByUsername(userDTO.getUsername()) != null) throw new RuntimeException("用户名已存在");

        SysUser user = new SysUser();
        user.setUsername(userDTO.getUsername());
        user.setPassword(passwordEncoder.encode(userDTO.getPassword()));
        user.setEmail(userDTO.getEmail() != null ? userDTO.getEmail() : "");
        user.setRole(userDTO.getRole() != null ? userDTO.getRole() : "ROLE_USER");
        user.setStatus(1);
        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());
        this.save(user);
        log.info("管理员添加用户: username={}, role={}", user.getUsername(), user.getRole());
    }

    @Override
    public boolean changePassword(String username, ChangePasswordDTO changePasswordDTO) {
        // 根据用户名获取用户信息
        SysUser user = this.getByUsername(username);
        if (user == null) {
            log.error("更改密码失败: 用户不存在，username={}", username);
            throw new RuntimeException("用户不存在");
        }
        
        // 验证旧密码是否正确
        if (!passwordEncoder.matches(changePasswordDTO.getOldPassword(), user.getPassword())) {
            log.warn("更改密码失败: 旧密码错误，username={}", username);
            throw new RuntimeException("旧密码错误");
        }
        
        // 验证新密码和确认密码是否一致
        if (!changePasswordDTO.getNewPassword().equals(changePasswordDTO.getConfirmPassword())) {
            log.warn("更改密码失败: 新密码和确认密码不一致，username={}", username);
            throw new RuntimeException("新密码和确认密码不一致");
        }
        
        // 验证新密码格式（英文字符+数字，8-20位）
        if (!changePasswordDTO.getNewPassword().matches("^(?=.*[a-zA-Z])(?=.*\\d).{8,20}$")) {
            log.warn("更改密码失败: 新密码格式不符合要求，username={}", username);
            throw new RuntimeException("密码需包含英文字符和数字，长度8-20位");
        }
        
        // 加密新密码
        String encodedPassword = passwordEncoder.encode(changePasswordDTO.getNewPassword());
        
        // 更新用户密码
        user.setPassword(encodedPassword);
        user.setUpdateTime(LocalDateTime.now());
        boolean result = this.updateById(user);
        
        if (result) {
            log.info("更改密码成功: username={}", username);
        } else {
            log.error("更改密码失败: 数据库更新失败，username={}", username);
            throw new RuntimeException("更改密码失败");
        }
        
        return result;
    }
}
