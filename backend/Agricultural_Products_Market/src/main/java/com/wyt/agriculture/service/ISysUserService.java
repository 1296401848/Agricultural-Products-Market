package com.wyt.agriculture.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.wyt.agriculture.domain.dto.ChangePasswordDTO;
import com.wyt.agriculture.domain.dto.UserDTO;
import com.wyt.agriculture.domain.po.SysUser;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 用户信息表 服务类
 * </p>
 *
 * @author WYT
 * @since 2025-12-01
 */
public interface ISysUserService extends IService<SysUser> {

    /**
     * 根据用户名获取用户信息
     * @param username 用户名
     * @return 用户信息
     */
    SysUser getByUsername(String username);

    /**
     * 管理员分页查询普通用户
     * @param page 页码
     * @param size 每页大小
     * @param username 用户名（可选，用于模糊查询）
     * @param status 用户状态（可选，用于精确查询）
     * @param sortField 排序字段（可选）
     * @param sortOrder 排序方式 asc/desc（可选）
     * @return 用户分页列表
     */
    IPage<SysUser> adminGetUserList(Integer page, Integer size, String username, Integer status, String sortField, String sortOrder);

    /**
     * 管理员更新用户状态
     * @param userDTO 更新用户状态请求
     * @return 更新结果
     */
    boolean adminUpdateUserStatus(UserDTO userDTO);
    
    /**
     * 管理员添加用户
     */
    void adminAddUser(UserDTO userDTO);

    /**
     * 管理员删除用户
     */
    void adminDeleteUser(Long id);

    /**
     * 更改用户密码
     * @param username 用户名
     * @param changePasswordDTO 更改密码请求
     * @return 更改结果
     */
    boolean changePassword(String username, ChangePasswordDTO changePasswordDTO);
}
