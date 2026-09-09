package com.wyt.agriculture.controller.user;

import com.wyt.agriculture.domain.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "高德地图", description = "高德地图相关接口")
@RestController
@RequestMapping("/amap")
public class AmapController {

    @Value("${amap.securityJsCode}")
    private String securityJsCode;

    @Operation(summary = "获取安全密钥")
    @GetMapping("/securityCode")
    public Result getAmapKey() {
        return Result.success(securityJsCode);
    }
}