package com.wyt.agriculture.controller.user;

import com.wyt.agriculture.domain.dto.AddressDTO;
import com.wyt.agriculture.domain.po.UserAddress;
import com.wyt.agriculture.domain.result.Result;
import com.wyt.agriculture.service.IUserAddressService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import lombok.extern.slf4j.Slf4j;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;

import java.util.List;

/**
 * <p>
 * 用户地址表 前端控制器
 * </p>
 *
 * @author WYT
 * @since 2025-12-01
 */
@RestController
@RequestMapping("/address")
@Tag(name = "地址管理", description = "用户收货地址相关接口")
@Slf4j
public class UserAddressController {

    @Autowired
    private IUserAddressService userAddressService;

    /**
     * 添加收货地址
     * @param addressDTO 添加地址请求
     * @return 操作结果
     */
    @PostMapping
    @Operation(summary = "添加收货地址", description = "为当前用户添加新的收货地址")
    public Result<String> addAddress(@RequestBody AddressDTO addressDTO) {
        // 获取当前用户名
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        log.info("添加收货地址请求: username={}, recipient={}", username, addressDTO.getRecipient());
        try {
            boolean result = userAddressService.addAddress(username, addressDTO);
            if (result) {
                return Result.success("添加地址成功");
            } else {
                return Result.error("添加地址失败");
            }
        } catch (RuntimeException e) {
            log.warn("添加收货地址失败: username={}, error={}", username, e.getMessage());
            return Result.error(e.getMessage());
        }
    }

    /**
     * 查看地址列表
     * @return 地址列表
     */
    @GetMapping
    @Operation(summary = "查看地址列表", description = "获取当前用户的所有收货地址")
    public Result<List<UserAddress>> getAddressList() {
        // 获取当前用户名
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        log.info("查看收货地址列表请求: username={}", username);
        try {
            List<UserAddress> addressList = userAddressService.getAddressList(username);
            return Result.success(addressList);
        } catch (RuntimeException e) {
            log.warn("查看收货地址列表失败: username={}, error={}", username, e.getMessage());
            return Result.error(e.getMessage());
        }
    }

    /**
     * 修改收货地址
     * @param id 地址ID
     * @param addressDTO 更新地址请求
     * @return 操作结果
     */
    @PutMapping("/{id}")
    @Operation(summary = "修改收货地址", description = "修改当前用户指定ID的收货地址")
    public Result<String> updateAddress(@PathVariable Long id, @RequestBody AddressDTO addressDTO) {
        // 获取当前用户名
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        log.info("修改收货地址请求: username={}, addressId={}", username, id);
        try {
            boolean result = userAddressService.updateAddress(username, id, addressDTO);
            if (result) {
                return Result.success("修改地址成功");
            } else {
                return Result.error("修改地址失败");
            }
        } catch (RuntimeException e) {
            log.warn("修改收货地址失败: username={}, addressId={}, error={}", username, id, e.getMessage());
            return Result.error(e.getMessage());
        }
    }

    /**
     * 删除收货地址
     * @param id 地址ID
     * @return 操作结果
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除收货地址", description = "删除当前用户指定ID的收货地址")
    public Result<String> deleteAddress(@PathVariable Long id) {
        // 获取当前用户名
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        log.info("删除收货地址请求: username={}, addressId={}", username, id);
        try {
            boolean result = userAddressService.deleteAddress(username, id);
            if (result) {
                return Result.success("删除地址成功");
            } else {
                return Result.error("删除地址失败");
            }
        } catch (RuntimeException e) {
            log.warn("删除收货地址失败: username={}, addressId={}, error={}", username, id, e.getMessage());
            return Result.error(e.getMessage());
        }
    }
}
