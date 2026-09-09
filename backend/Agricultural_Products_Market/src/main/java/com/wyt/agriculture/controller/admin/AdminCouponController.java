package com.wyt.agriculture.controller.admin;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.wyt.agriculture.domain.po.Coupon;
import com.wyt.agriculture.domain.result.Result;
import com.wyt.agriculture.service.ICouponService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin/coupons")
@Tag(name = "管理端-优惠券管理")
@Slf4j
public class AdminCouponController {

    @Autowired
    private ICouponService couponService;

    @GetMapping
    @Operation(summary = "优惠券列表")
    public Result<IPage<Coupon>> list(@RequestParam(defaultValue = "1") Integer page,
                                       @RequestParam(defaultValue = "10") Integer size,
                                       @RequestParam(required = false) String couponName) {
        return Result.success(couponService.adminGetCouponList(page, size, couponName));
    }

    @PostMapping
    @Operation(summary = "创建优惠券")
    public Result<String> create(@RequestBody Coupon coupon) {
        couponService.adminCreateCoupon(coupon);
        return Result.success("创建成功");
    }

    @PutMapping("/{id}")
    @Operation(summary = "编辑优惠券")
    public Result<String> update(@PathVariable Long id, @RequestBody Coupon coupon) {
        coupon.setId(id);
        couponService.adminUpdateCoupon(coupon);
        return Result.success("更新成功");
    }

    @PutMapping("/{id}/status")
    @Operation(summary = "上架/下架")
    public Result<String> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        couponService.adminUpdateStatus(id, status);
        return Result.success(status == 1 ? "已上架" : "已下架");
    }

    @PutMapping("/batch-status")
    @Operation(summary = "批量上下架")
    public Result<String> batchStatus(@RequestBody Map<String, Object> body) {
        @SuppressWarnings("unchecked")
        List<Integer> idList = (List<Integer>) body.get("ids");
        Integer status = (Integer) body.get("status");
        for (Integer id : idList) couponService.adminUpdateStatus(id.longValue(), status);
        return Result.success("批量操作完成");
    }

    @DeleteMapping("/batch")
    @Operation(summary = "批量删除")
    public Result<String> batchDelete(@RequestBody Map<String, Object> body) {
        @SuppressWarnings("unchecked")
        List<Integer> idList = (List<Integer>) body.get("ids");
        for (Integer id : idList) couponService.adminUpdateStatus(id.longValue(), 0); // 下架后删除
        return Result.success("批量删除完成");
    }
}
