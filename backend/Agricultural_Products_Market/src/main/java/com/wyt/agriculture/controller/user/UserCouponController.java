package com.wyt.agriculture.controller.user;

import com.wyt.agriculture.domain.po.SysUser;
import com.wyt.agriculture.domain.po.UserCoupon;
import com.wyt.agriculture.domain.result.Result;
import com.wyt.agriculture.service.ISysUserService;
import com.wyt.agriculture.service.IUserCouponService;
import com.wyt.agriculture.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@RestController
@RequestMapping("/user/coupons")
@Tag(name = "用户端-我的优惠券")
@Slf4j
public class UserCouponController {

    @Autowired
    private IUserCouponService userCouponService;

    @Autowired
    private ISysUserService sysUserService;

    @Autowired
    private JwtUtil jwtUtil;

    @GetMapping
    @Operation(summary = "我的优惠券列表")
    public Result<List<UserCoupon>> list(@RequestHeader("Authorization") String token,
                                          @RequestParam(required = false) Integer useStatus) {
        String username = jwtUtil.getUsernameFromToken(token.replace("Bearer ", ""));
        SysUser user = sysUserService.getByUsername(username);
        return Result.success(userCouponService.getUserCoupons(user.getId(), useStatus));
    }
}
