package com.wyt.agriculture.service;

import com.wyt.agriculture.domain.dto.AddressDTO;
import com.wyt.agriculture.domain.po.UserAddress;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 * 用户地址表 服务类
 * </p>
 *
 * @author WYT
 * @since 2025-12-01
 */
public interface IUserAddressService extends IService<UserAddress> {
    /**
     * 添加收货地址
     * @param username 用户名
     * @param addressDTO 添加地址请求
     * @return 添加结果
     */
    boolean addAddress(String username, AddressDTO addressDTO);

    /**
     * 获取用户地址列表
     * @param username 用户名
     * @return 地址列表
     */
    List<UserAddress> getAddressList(String username);

    /**
     * 更新收货地址
     * @param username 用户名
     * @param addressId 地址ID
     * @param addressDTO 更新地址请求
     * @return 更新结果
     */
    boolean updateAddress(String username, Long addressId, AddressDTO addressDTO);

    /**
     * 删除收货地址
     * @param username 用户名
     * @param addressId 地址ID
     * @return 删除结果
     */
    boolean deleteAddress(String username, Long addressId);
}
