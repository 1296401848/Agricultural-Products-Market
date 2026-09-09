package com.wyt.agriculture.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.wyt.agriculture.domain.dto.CartDTO;
import com.wyt.agriculture.domain.po.ProduceInfo;
import com.wyt.agriculture.domain.po.PackageProduce;
import com.wyt.agriculture.domain.po.ShoppingCart;
import com.wyt.agriculture.domain.po.SysUser;
import com.wyt.agriculture.mapper.ShoppingCartMapper;
import com.wyt.agriculture.service.IProduceInfoService;
import com.wyt.agriculture.service.IPackageProduceService;
import com.wyt.agriculture.service.IShoppingCartService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wyt.agriculture.service.ISysUserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * <p>
 * 购物车表 服务实现类
 * </p>
 *
 * @author WYT
 * @since 2025-12-01
 */
@Slf4j
@Service
public class ShoppingCartServiceImpl extends ServiceImpl<ShoppingCartMapper, ShoppingCart> implements IShoppingCartService {

    @Autowired
    private IProduceInfoService produceInfoService;
    @Autowired
    private ISysUserService sysUserService;

    @Autowired
    private IPackageProduceService packageProduceService;

    @Override
    public void addToCart(String username, CartDTO cartDTO) {
        // 根据用户名获取用户ID
        LambdaQueryWrapper<SysUser> userQueryWrapper = new LambdaQueryWrapper<>();
        userQueryWrapper.eq(SysUser::getUsername, username);
        SysUser user = sysUserService.getOne(userQueryWrapper);
        if (user == null) {
            log.error("添加购物车失败: 用户不存在，username={}", username);
            throw new RuntimeException("用户不存在");
        }

        Long userId = user.getId();
        int type = cartDTO.getCartType() != null ? cartDTO.getCartType() : 1;

        ShoppingCart newCartItem = new ShoppingCart();
        newCartItem.setUserId(userId);
        newCartItem.setQuantity(cartDTO.getQuantity() != null ? cartDTO.getQuantity() : 1);
        newCartItem.setCartType(type);

        if (type == 1) {
            // 普通农产品
            ProduceInfo produceInfo = produceInfoService.getById(cartDTO.getProduceId());
            if (produceInfo == null || produceInfo.getStatus() == 0) throw new RuntimeException("农产品不存在或已下架");
            if (cartDTO.getQuantity() > produceInfo.getStock()) throw new RuntimeException("库存不足");
            newCartItem.setProduceId(cartDTO.getProduceId());

            // 检查是否已存在
            LambdaQueryWrapper<ShoppingCart> qw = new LambdaQueryWrapper<>();
            qw.eq(ShoppingCart::getUserId, userId).eq(ShoppingCart::getProduceId, cartDTO.getProduceId()).eq(ShoppingCart::getCartType, 1);
            ShoppingCart exist = this.getOne(qw);
            if (exist != null) {
                exist.setQuantity(exist.getQuantity() + cartDTO.getQuantity());
                exist.setUpdateTime(LocalDateTime.now());
                this.updateById(exist);
                return;
            }
        } else {
            // 套餐
            newCartItem.setPackageId(cartDTO.getPackageId());
            if (cartDTO.getPackageId() == null) throw new RuntimeException("套餐ID不能为空");

            // 检查库存
            int qty = cartDTO.getQuantity() != null ? cartDTO.getQuantity() : 1;
            List<PackageProduce> pkgProduces = packageProduceService.list(
                    new LambdaQueryWrapper<PackageProduce>().eq(PackageProduce::getPackageId, cartDTO.getPackageId()));
            int minStock = Integer.MAX_VALUE;
            for (PackageProduce pb : pkgProduces) {
                ProduceInfo bi = produceInfoService.getById(pb.getProduceId());
                if (bi == null || bi.getStatus() != 1) throw new RuntimeException("套餐内农产品《" + (bi != null ? bi.getProduceName() : "") + "》已下架");
                int maxQty = bi.getStock() / pb.getProduceNum();
                if (maxQty < minStock) minStock = maxQty;
            }
            if (qty > minStock) throw new RuntimeException("套餐库存不足，最大可购 " + minStock + " 套");

            // 检查是否已存在同套餐
            LambdaQueryWrapper<ShoppingCart> qw = new LambdaQueryWrapper<>();
            qw.eq(ShoppingCart::getUserId, userId).eq(ShoppingCart::getPackageId, cartDTO.getPackageId()).eq(ShoppingCart::getCartType, 2);
            ShoppingCart exist = this.getOne(qw);
            if (exist != null) {
                int newQty = exist.getQuantity() + qty;
                if (newQty > minStock) throw new RuntimeException("套餐库存不足，购物车已有 " + exist.getQuantity() + " 套，最多可购 " + minStock + " 套");
                exist.setQuantity(newQty);
                exist.setUpdateTime(LocalDateTime.now());
                this.updateById(exist);
                return;
            }
        }

        newCartItem.setCreateTime(LocalDateTime.now());
        newCartItem.setUpdateTime(LocalDateTime.now());
        this.save(newCartItem);
    }

    @Override
    public List<ShoppingCart> getCartList(String username) {
        // 根据用户名获取用户ID
        LambdaQueryWrapper<SysUser> userQueryWrapper = new LambdaQueryWrapper<>();
        userQueryWrapper.eq(SysUser::getUsername, username);
        SysUser user = sysUserService.getOne(userQueryWrapper);
        if (user == null) {
            log.error("获取购物车列表失败: 用户不存在，username={}", username);
            throw new RuntimeException("用户不存在");
        }

        Long userId = user.getId();

        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId, userId);
        return this.list(queryWrapper);
    }

    @Override
    public void updateCartQuantity(String username, CartDTO cartDTO) {
        // 根据用户名获取用户ID
        LambdaQueryWrapper<SysUser> userQueryWrapper = new LambdaQueryWrapper<>();
        userQueryWrapper.eq(SysUser::getUsername, username);
        SysUser user = sysUserService.getOne(userQueryWrapper);
        if (user == null) {
            log.error("修改购物车数量失败: 用户不存在，username={}", username);
            throw new RuntimeException("用户不存在");
        }

        Long userId = user.getId();
        // 检查购物车项是否存在且属于当前用户
        ShoppingCart cartItem = this.getById(cartDTO.getId());
        if (cartItem == null || !cartItem.getUserId().equals(userId)) {
            throw new RuntimeException("购物车项不存在");
        }

        // 检查库存
        if (cartItem.getCartType() == null || cartItem.getCartType() == 1) {
            // 普通农产品
            ProduceInfo produceInfo = produceInfoService.getById(cartItem.getProduceId());
            if (produceInfo == null || cartDTO.getQuantity() > produceInfo.getStock()) {
                throw new RuntimeException("库存不足");
            }
        } else if (cartItem.getCartType() == 2) {
            // 套餐：检查套餐内所有农产品库存
            List<PackageProduce> pkgProduces = packageProduceService.list(
                    new LambdaQueryWrapper<PackageProduce>().eq(PackageProduce::getPackageId, cartItem.getPackageId()));
            for (PackageProduce pb : pkgProduces) {
                ProduceInfo bi = produceInfoService.getById(pb.getProduceId());
                if (bi == null || bi.getStock() < pb.getProduceNum() * cartDTO.getQuantity()) {
                    throw new RuntimeException("套餐内农产品《" + (bi != null ? bi.getProduceName() : "") + "》库存不足");
                }
            }
        }

        // 更新数量
        cartItem.setQuantity(cartDTO.getQuantity());
        cartItem.setUpdateTime(LocalDateTime.now());
        this.updateById(cartItem);
    }

    @Override
    public void deleteCartItem(String username, Long id) {
        // 根据用户名获取用户ID
        LambdaQueryWrapper<SysUser> userQueryWrapper = new LambdaQueryWrapper<>();
        userQueryWrapper.eq(SysUser::getUsername, username);
        SysUser user = sysUserService.getOne(userQueryWrapper);
        if (user == null) {
            log.error("删除购物车项失败: 用户不存在，username={}", username);
            throw new RuntimeException("用户不存在");
        }

        Long userId = user.getId();
        // 检查购物车项是否存在且属于当前用户
        ShoppingCart cartItem = this.getById(id);
        if (cartItem == null || !cartItem.getUserId().equals(userId)) {
            throw new RuntimeException("购物车项不存在");
        }

        // 删除购物车项
        this.removeById(id);
    }
}
