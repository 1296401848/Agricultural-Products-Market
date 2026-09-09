package com.wyt.agriculture.controller.user;

import com.wyt.agriculture.domain.po.Coupon;
import com.wyt.agriculture.domain.po.SysUser;
import com.wyt.agriculture.domain.result.Result;
import com.wyt.agriculture.service.ICouponService;
import com.wyt.agriculture.service.ISysUserService;
import com.wyt.agriculture.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/coupons")
@Tag(name = "用户端-优惠券")
@Slf4j
public class CouponController {

    @Autowired
    private ICouponService couponService;

    @Autowired
    private ISysUserService sysUserService;

    @Autowired
    private JwtUtil jwtUtil;

    @GetMapping("/available")
    @Operation(summary = "可领取优惠券列表")
    public Result<List<Coupon>> available() {
        return Result.success(couponService.getAvailableCoupons());
    }

    @PostMapping("/{couponId}/receive")
    @Operation(summary = "领取优惠券")
    public Result<Map<String, Object>> receive(@PathVariable Long couponId,
                                                @RequestHeader("Authorization") String token) {
        String username = jwtUtil.getUsernameFromToken(token.replace("Bearer ", ""));
        SysUser user = sysUserService.getByUsername(username);
        Map<String, Object> result = couponService.receiveCoupon(couponId, user.getId());
        return Result.success(result, "领取成功");
    }
}
