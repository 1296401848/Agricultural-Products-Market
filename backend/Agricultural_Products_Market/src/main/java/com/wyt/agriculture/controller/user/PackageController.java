package com.wyt.agriculture.controller.user;

import com.wyt.agriculture.domain.po.ProducePackage;
import com.wyt.agriculture.domain.result.Result;
import com.wyt.agriculture.service.IProducePackageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/package")
@Tag(name = "用户端-套餐专区")
public class PackageController {

    @Autowired
    private IProducePackageService packageService;

    @GetMapping
    @Operation(summary = "套餐列表")
    public Result<List<ProducePackage>> list() {
        return Result.success(packageService.getUserPackageList());
    }

    @GetMapping("/{id}")
    @Operation(summary = "套餐详情")
    public Result<Map<String, Object>> detail(@PathVariable Long id) {
        return Result.success(packageService.getPackageDetail(id));
    }
}
