package com.wyt.agriculture.service;

import com.wyt.agriculture.domain.dto.CartDTO;
import com.wyt.agriculture.domain.po.ShoppingCart;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 * 购物车表 服务类
 * </p>
 *
 * @author WYT
 * @since 2025-12-01
 */
public interface IShoppingCartService extends IService<ShoppingCart> {
    /**
     * 添加农产品到购物车
     * @param username 用户名
     * @param cartDTO 添加购物车请求
     */
    void addToCart(String username, CartDTO cartDTO);

    /**
     * 获取购物车列表
     * @param username 用户名
     * @return 购物车列表
     */
    List<ShoppingCart> getCartList(String username);

    /**
     * 修改购物车数量
     * @param username 用户名
     * @param cartDTO 修改购物车请求
     */
    void updateCartQuantity(String username, CartDTO cartDTO);

    /**
     * 删除购物车项
     * @param username 用户名
     * @param id 购物车项ID
     */
    void deleteCartItem(String username, Long id);
}
