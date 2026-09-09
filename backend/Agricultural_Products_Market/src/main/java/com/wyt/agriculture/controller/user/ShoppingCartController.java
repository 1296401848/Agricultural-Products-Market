package com.wyt.agriculture.controller.user;

import com.wyt.agriculture.domain.dto.CartDTO;
import com.wyt.agriculture.domain.po.ShoppingCart;
import com.wyt.agriculture.domain.result.Result;
import com.wyt.agriculture.service.IShoppingCartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import lombok.extern.slf4j.Slf4j;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;

import java.util.List;

/**
 * <p>
 * 购物车表 前端控制器
 * </p>
 *
 * @author WYT
 * @since 2025-12-01
 */
@RestController
@RequestMapping("/cart")
@Tag(name = "购物车管理", description = "用户购物车相关接口")
@Slf4j
public class ShoppingCartController {

    @Autowired
    private IShoppingCartService shoppingCartService;

    /**
     * 添加农产品到购物车
     * @param cartDTO 添加购物车请求
     * @return 操作结果
     */
    @PostMapping
    @Operation(summary = "添加农产品到购物车", description = "将农产品添加到当前用户的购物车")
    public Result<String> addToCart(@RequestBody CartDTO cartDTO) {
        // 获取当前用户名
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        log.info("添加农产品到购物车请求: username={}, produceId={}, quantity={}", username, cartDTO.getProduceId(), cartDTO.getQuantity());

        try {
            shoppingCartService.addToCart(username, cartDTO);
            log.info("添加购物车成功: username={}, produceId={}", username, cartDTO.getProduceId());
            return Result.success("添加购物车成功");
        } catch (RuntimeException e) {
            log.warn("添加购物车失败: username={}, error={}", username, e.getMessage());
            return Result.error(e.getMessage());
        }
    }

    /**
     * 查看购物车列表
     * @return 购物车列表
     */
    @GetMapping
    @Operation(summary = "查看购物车列表", description = "获取当前用户的购物车列表")
    public Result<List<ShoppingCart>> getCartList() {
        // 获取当前用户名
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        log.info("查看购物车列表请求: username={}", username);

        try {
            List<ShoppingCart> cartList = shoppingCartService.getCartList(username);
            log.info("查看购物车列表成功: username={}, 数量={}", username, cartList.size());
            return Result.success(cartList);
        } catch (RuntimeException e) {
            log.warn("查看购物车列表失败: username={}, error={}", username, e.getMessage());
            return Result.error(e.getMessage());
        }
    }

    /**
     * 修改购物车数量
     * @param cartDTO 修改购物车请求
     * @return 操作结果
     */
    @PutMapping
    @Operation(summary = "修改购物车数量", description = "更新购物车中商品的数量")
    public Result<String> updateCartQuantity(@RequestBody CartDTO cartDTO) {
        // 获取当前用户名
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        log.info("修改购物车数量请求: username={}, cartItemId={}, newQuantity={}", username, cartDTO.getId(), cartDTO.getQuantity());

        try {
            shoppingCartService.updateCartQuantity(username, cartDTO);
            log.info("修改购物车数量成功: cartItemId={}, newQuantity={}", cartDTO.getId(), cartDTO.getQuantity());
            return Result.success("修改购物车成功");
        } catch (RuntimeException e) {
            log.warn("修改购物车数量失败: username={}, error={}", username, e.getMessage());
            return Result.error(e.getMessage());
        }
    }

    /**
     * 删除购物车项
     * @param id 购物车项ID
     * @return 操作结果
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除购物车项", description = "删除购物车中指定ID的商品")
    public Result<String> deleteCartItem(@PathVariable Long id) {
        // 获取当前用户名
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        log.info("删除购物车项请求: username={}, cartItemId={}", username, id);

        try {
            shoppingCartService.deleteCartItem(username, id);
            log.info("删除购物车项成功: cartItemId={}", id);
            return Result.success("删除购物车项成功");
        } catch (RuntimeException e) {
            log.warn("删除购物车项失败: username={}, error={}", username, e.getMessage());
            return Result.error(e.getMessage());
        }
    }

}
