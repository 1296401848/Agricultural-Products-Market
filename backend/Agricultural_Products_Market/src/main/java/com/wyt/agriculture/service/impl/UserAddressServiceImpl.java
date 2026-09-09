package com.wyt.agriculture.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.wyt.agriculture.domain.dto.AddressDTO;
import com.wyt.agriculture.domain.po.SysUser;
import com.wyt.agriculture.domain.po.UserAddress;
import com.wyt.agriculture.mapper.UserAddressMapper;
import com.wyt.agriculture.service.IUserAddressService;
import com.wyt.agriculture.service.ISysUserService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * <p>
 * 用户地址表 服务实现类
 * </p>
 *
 * @author WYT
 * @since 2025-12-01
 */
@Service
@Slf4j
public class UserAddressServiceImpl extends ServiceImpl<UserAddressMapper, UserAddress> implements IUserAddressService {

    @Autowired
    private ISysUserService sysUserService;

    @Override
    public boolean addAddress(String username, AddressDTO addressDTO) {
        // 根据用户名获取用户ID
        LambdaQueryWrapper<SysUser> userQueryWrapper = new LambdaQueryWrapper<>();
        userQueryWrapper.eq(SysUser::getUsername, username);
        SysUser user = sysUserService.getOne(userQueryWrapper);
        if (user == null) {
            log.error("添加地址失败: 用户不存在，username={}", username);
            throw new RuntimeException("用户不存在");
        }
        Long userId = user.getId();

        // 如果设置为默认地址，先将其他地址设为非默认
        if (addressDTO.getIsDefault() == 1) {
            LambdaQueryWrapper<UserAddress> updateWrapper = new LambdaQueryWrapper<>();
            updateWrapper.eq(UserAddress::getUserId, userId);
            UserAddress updateAddress = new UserAddress();
            updateAddress.setIsDefault(0);
            this.update(updateAddress, updateWrapper);
        }

        // 创建新地址
        UserAddress newAddress = new UserAddress();
        newAddress.setUserId(userId);
        newAddress.setRecipient(addressDTO.getRecipient());
        newAddress.setPhone(addressDTO.getPhone());
        newAddress.setAddress(addressDTO.getAddress());
        newAddress.setIsDefault(addressDTO.getIsDefault());
        newAddress.setCreateTime(LocalDateTime.now());
        boolean result = this.save(newAddress);

        log.info("添加地址成功: addressId={}, username={}", newAddress.getId(), username);
        return result;
    }

    @Override
    public List<UserAddress> getAddressList(String username) {
        // 根据用户名获取用户ID
        LambdaQueryWrapper<SysUser> userQueryWrapper = new LambdaQueryWrapper<>();
        userQueryWrapper.eq(SysUser::getUsername, username);
        SysUser user = sysUserService.getOne(userQueryWrapper);
        if (user == null) {
            log.error("获取地址列表失败: 用户不存在，username={}", username);
            throw new RuntimeException("用户不存在");
        }
        Long userId = user.getId();

        LambdaQueryWrapper<UserAddress> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserAddress::getUserId, userId);
        List<UserAddress> addressList = this.list(queryWrapper);

        log.info("获取地址列表成功: username={}, 数量={}", username, addressList.size());
        return addressList;
    }

    @Override
    public boolean updateAddress(String username, Long addressId, AddressDTO addressDTO) {
        // 根据用户名获取用户ID
        LambdaQueryWrapper<SysUser> userQueryWrapper = new LambdaQueryWrapper<>();
        userQueryWrapper.eq(SysUser::getUsername, username);
        SysUser user = sysUserService.getOne(userQueryWrapper);
        if (user == null) {
            log.error("更新地址失败: 用户不存在，username={}", username);
            throw new RuntimeException("用户不存在");
        }
        Long userId = user.getId();

        // 检查地址是否存在且属于当前用户
        UserAddress existingAddress = this.getById(addressId);
        if (existingAddress == null || !existingAddress.getUserId().equals(userId)) {
            log.warn("更新地址失败: 地址不存在，addressId={}", addressId);
            throw new RuntimeException("地址不存在");
        }

        // 如果设置为默认地址，先将其他地址设为非默认
        if (addressDTO.getIsDefault() == 1) {
            LambdaQueryWrapper<UserAddress> updateWrapper = new LambdaQueryWrapper<>();
            updateWrapper.eq(UserAddress::getUserId, userId)
                    .ne(UserAddress::getId, addressId);
            UserAddress updateAddress = new UserAddress();
            updateAddress.setIsDefault(0);
            this.update(updateAddress, updateWrapper);
        }

        // 更新地址信息
        existingAddress.setRecipient(addressDTO.getRecipient());
        existingAddress.setPhone(addressDTO.getPhone());
        existingAddress.setAddress(addressDTO.getAddress());
        existingAddress.setIsDefault(addressDTO.getIsDefault());
        boolean result = this.updateById(existingAddress);

        log.info("更新地址成功: addressId={}, username={}", addressId, username);
        return result;
    }

    @Override
    public boolean deleteAddress(String username, Long addressId) {
        // 根据用户名获取用户ID
        LambdaQueryWrapper<SysUser> userQueryWrapper = new LambdaQueryWrapper<>();
        userQueryWrapper.eq(SysUser::getUsername, username);
        SysUser user = sysUserService.getOne(userQueryWrapper);
        if (user == null) {
            log.error("删除地址失败: 用户不存在，username={}", username);
            throw new RuntimeException("用户不存在");
        }
        Long userId = user.getId();

        // 检查地址是否存在且属于当前用户
        UserAddress existingAddress = this.getById(addressId);
        if (existingAddress == null || !existingAddress.getUserId().equals(userId)) {
            log.warn("删除地址失败: 地址不存在，addressId={}", addressId);
            throw new RuntimeException("地址不存在");
        }

        // 删除地址
        boolean result = this.removeById(addressId);
        // 如果删除的是默认地址，将剩余地址中第一条设为默认
        if (existingAddress.getIsDefault() != null && existingAddress.getIsDefault() == 1) {
            List<UserAddress> remaining = this.list(new LambdaQueryWrapper<UserAddress>().eq(UserAddress::getUserId, userId));
            if (!remaining.isEmpty()) {
                UserAddress first = remaining.get(0);
                first.setIsDefault(1);
                this.updateById(first);
            }
        }

        log.info("删除地址成功: addressId={}, username={}", addressId, username);
        return result;
    }
}
