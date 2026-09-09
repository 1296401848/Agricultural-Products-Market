package com.wyt.agriculture.controller.admin;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.wyt.agriculture.domain.dto.ProduceDTO;
import com.wyt.agriculture.domain.po.ProduceInfo;
import com.wyt.agriculture.domain.result.Result;
import com.wyt.agriculture.service.IProduceInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import lombok.extern.slf4j.Slf4j;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin/produce")
@Tag(name = "管理员农产品管理", description = "管理员对农产品的管理接口")
@Slf4j
public class AdminProduceController {

    @Autowired
    private IProduceInfoService produceInfoService;

    /**
     * 分页查询所有农产品
     * @param page 页码
     * @param size 每页大小
     * @param produceName 名称（可选，用于模糊查询）
     * @param categoryId 分类ID（可选，用于按分类筛选）
     * @param status 状态（可选，用于按状态筛选，1-上架，0-下架）
     * @return 农产品列表
     */
    @GetMapping
    @Operation(summary = "分页查询所有农产品", description = "分页查询所有农产品，支持按名称模糊搜索、分类筛选和状态筛选")
    public Result<IPage<ProduceInfo>> getProduceList(@RequestParam(defaultValue = "1") Integer page, 
                                             @RequestParam(defaultValue = "10") Integer size,
                                             @RequestParam(required = false) String produceName,
                                             @RequestParam(required = false) Long categoryId,
                                             @RequestParam(required = false) Integer status) {
        log.info("管理员查询农产品列表请求: page={}, size={}, produceName={}, categoryId={}, status={}", page, size, produceName, categoryId, status);
        IPage<ProduceInfo> producePage = produceInfoService.adminGetProduceList(page, size, produceName, categoryId, status);
        return Result.success(producePage);
    }

    /**
     * 添加农产品
     * @param produceDTO 农产品请求
     * @return 操作结果
     */
    @PostMapping
    @Operation(summary = "添加农产品", description = "添加新的农产品信息")
    public Result<String> addProduce(@RequestBody ProduceDTO produceDTO) {
        log.info("管理员添加农产品请求: produceName={}, manufacturer={}", produceDTO.getProduceName(), produceDTO.getManufacturer());
        try {
            boolean result = produceInfoService.adminAddProduce(produceDTO);
            if (result) {
                return Result.success("添加农产品成功");
            } else {
                return Result.error("添加农产品失败");
            }
        } catch (RuntimeException e) {
            log.warn("管理员添加农产品失败: error={}", e.getMessage());
            return Result.error(e.getMessage());
        }
    }

    /**
     * 修改农产品
     * @param id 农产品ID
     * @param produceDTO 农产品请求
     * @return 操作结果
     */
    @PutMapping("/{id}")
    @Operation(summary = "修改农产品", description = "修改指定ID的农产品信息")
    public Result<String> updateProduce(@PathVariable Long id, @RequestBody ProduceDTO produceDTO) {
        log.info("管理员修改农产品请求: produceId={}, produceName={}", id, produceDTO.getProduceName());
        try {
            boolean result = produceInfoService.adminUpdateProduce(id, produceDTO);
            if (result) {
                return Result.success("修改农产品成功");
            } else {
                return Result.error("修改农产品失败");
            }
        } catch (RuntimeException e) {
            log.warn("管理员修改农产品失败: produceId={}, error={}", id, e.getMessage());
            return Result.error(e.getMessage());
        }
    }

    /**
     * 删除农产品
     * @param id 农产品ID
     * @return 操作结果
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除农产品", description = "删除指定ID的农产品")
    public Result<String> deleteProduce(@PathVariable Long id) {
        log.info("管理员删除农产品请求: produceId={}", id);
        try {
            boolean result = produceInfoService.adminDeleteProduce(id);
            if (result) {
                return Result.success("删除农产品成功");
            } else {
                return Result.error("删除农产品失败");
            }
        } catch (RuntimeException e) {
            log.warn("管理员删除农产品失败: produceId={}, error={}", id, e.getMessage());
            return Result.error(e.getMessage());
        }
    }

    /**
     * 管理员获取农产品详情
     * @param id 农产品ID
     * @return 农产品详情，包括下架农产品
     */
    @GetMapping("/{id}")
    @Operation(summary = "管理员获取农产品详情", description = "管理员获取农产品详情，包括下架农产品")
    public Result<ProduceInfo> getProduceDetail(@PathVariable Long id) {
        log.info("管理员获取农产品详情请求: produceId={}", id);
        try {
            ProduceInfo produceInfo = produceInfoService.adminGetProduceDetail(id);
            log.info("管理员获取农产品详情成功: produceId={}, produceName={}", id, produceInfo.getProduceName());
            return Result.success(produceInfo);
        } catch (RuntimeException e) {
            log.warn("管理员获取农产品详情失败: produceId={}, error={}", id, e.getMessage());
            return Result.error(e.getMessage());
        }
    }

    /**
     * 上架/下架农产品
     * @param updateProduceStatusDTO 更新状态请求
     * @return 操作结果
     */
    @PutMapping("/status")
    @Operation(summary = "上架/下架农产品", description = "更新农产品的上架状态")
    public Result<String> updateProduceStatus(@RequestBody ProduceDTO updateProduceStatusDTO) {
        log.info("管理员更新农产品状态请求: produceId={}, newStatus={}", updateProduceStatusDTO.getId(), updateProduceStatusDTO.getStatus());
        try {
            boolean result = produceInfoService.adminUpdateProduceStatus(updateProduceStatusDTO);
            if (result) {
                return Result.success("更新农产品状态成功");
            } else {
                return Result.error("更新农产品状态失败");
            }
        } catch (RuntimeException e) {
            log.warn("管理员更新农产品状态失败: produceId={}, error={}", updateProduceStatusDTO.getId(), e.getMessage());
            return Result.error(e.getMessage());
        }
    }

    @PutMapping("/batch-status")
    @Operation(summary = "批量上下架")
    public Result<String> batchStatus(@RequestBody Map<String, Object> body) {
        @SuppressWarnings("unchecked")
        List<Integer> idList = (List<Integer>) body.get("ids");
        Integer status = (Integer) body.get("status");
        for (Integer id : idList) {
            ProduceDTO dto = new ProduceDTO();
            dto.setId(id.longValue());
            dto.setStatus(status);
            produceInfoService.adminUpdateProduceStatus(dto);
        }
        return Result.success("批量操作完成");
    }

    @DeleteMapping("/batch")
    @Operation(summary = "批量删除")
    public Result<String> batchDelete(@RequestBody Map<String, Object> body) {
        @SuppressWarnings("unchecked")
        List<Integer> idList = (List<Integer>) body.get("ids");
        for (Integer id : idList) produceInfoService.adminDeleteProduce(id.longValue());
        return Result.success("批量删除完成");
    }

}