package com.wyt.agriculture.controller.admin;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.wyt.agriculture.domain.po.ProducePackage;
import com.wyt.agriculture.domain.po.PackageProduce;
import com.wyt.agriculture.domain.result.Result;
import com.wyt.agriculture.service.IProducePackageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin/package")
@Tag(name = "管理端-套餐管理")
@Slf4j
public class AdminPackageController {

    @Autowired
    private IProducePackageService packageService;

    @GetMapping
    public Result<IPage<ProducePackage>> list(@RequestParam(defaultValue = "1") Integer page,
                                            @RequestParam(defaultValue = "10") Integer size,
                                            @RequestParam(required = false) String name) {
        return Result.success(packageService.adminGetList(page, size, name));
    }

    @GetMapping("/{id}")
    @Operation(summary = "套餐详情（含农产品列表）")
    public Result<Map<String, Object>> detail(@PathVariable Long id) {
        return Result.success(packageService.getPackageDetail(id));
    }

    @PostMapping
    @Operation(summary = "创建套餐")
    public Result<String> create(@RequestBody Map<String, Object> body) {
        ProducePackage pkg = parsePackage(body);
        List<PackageProduce> produces = parseProduces(body);
        packageService.adminCreate(pkg, produces);
        return Result.success("创建成功");
    }

    @PutMapping("/{id}")
    @Operation(summary = "编辑套餐")
    public Result<String> update(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        ProducePackage pkg = parsePackage(body);
        pkg.setId(id);
        List<PackageProduce> produces = parseProduces(body);
        packageService.adminUpdate(pkg, produces);
        return Result.success("更新成功");
    }

    @PutMapping("/{id}/status")
    @Operation(summary = "上架/下架")
    public Result<String> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        packageService.adminUpdateStatus(id, status);
        return Result.success(status == 1 ? "已上架" : "已下架");
    }

    @SuppressWarnings("unchecked")
    private ProducePackage parsePackage(Map<String, Object> body) {
        ProducePackage pkg = new ProducePackage();
        pkg.setPackageName((String) body.get("packageName"));
        pkg.setPackageDesc((String) body.get("packageDesc"));
        pkg.setCoverImg((String) body.get("coverImg"));
        Object price = body.get("packagePrice");
        if (price != null) pkg.setPackagePrice(new java.math.BigDecimal(price.toString()));
        pkg.setIsRecommend(body.get("isRecommend") != null ? (Integer) body.get("isRecommend") : 0);
        pkg.setStatus(1);
        return pkg;
    }

    @SuppressWarnings("unchecked")
    private List<PackageProduce> parseProduces(Map<String, Object> body) {
        List<Map<String, Object>> list = (List<Map<String, Object>>) body.get("produces");
        if (list == null) return List.of();
        return list.stream().map(m -> {
            PackageProduce pb = new PackageProduce();
            pb.setProduceId(Long.valueOf(m.get("produceId").toString()));
            pb.setProduceNum(m.get("produceNum") != null ? (Integer) m.get("produceNum") : 1);
            return pb;
        }).toList();
    }

    @DeleteMapping("/batch")
    @Operation(summary = "批量删除套餐")
    public Result<String> batchDelete(@RequestBody Map<String, Object> body) {
        @SuppressWarnings("unchecked")
        List<Integer> idList = (List<Integer>) body.get("ids");
        for (Integer id : idList) packageService.removeById(id.longValue());
        return Result.success("批量删除完成");
    }

    @PutMapping("/batch-status")
    @Operation(summary = "批量上下架")
    public Result<String> batchStatus(@RequestBody Map<String, Object> body) {
        @SuppressWarnings("unchecked")
        List<Integer> idList = (List<Integer>) body.get("ids");
        Integer status = (Integer) body.get("status");
        for (Integer id : idList) packageService.adminUpdateStatus(id.longValue(), status);
        return Result.success("批量操作完成");
    }
}
