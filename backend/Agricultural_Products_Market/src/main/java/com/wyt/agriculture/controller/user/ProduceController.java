package com.wyt.agriculture.controller.user;

import com.wyt.agriculture.domain.dto.ProduceDetailDTO;
import com.wyt.agriculture.domain.dto.PageDTO;
import com.wyt.agriculture.domain.po.ProduceInfo;
import com.wyt.agriculture.domain.result.Result;
import com.wyt.agriculture.service.IProduceInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.util.List;

/**
 * <p>
 * 农产品信息表 前端控制器
 * </p>
 *
 * @author WYT
 * @since 2025-12-01
 */
@RestController
@RequestMapping("/produce")
@Tag(name = "农产品管理", description = "农产品浏览和查询接口")
@Slf4j
public class ProduceController {

    @Autowired
    private IProduceInfoService produceInfoService;

    /**
     * 农产品列表（支持筛选和模糊查询）
     */
    @GetMapping
    @Operation(summary = "获取农产品列表", description = "获取上架农产品列表，支持按名称、分类、价格、库存筛选")
    public Result<PageDTO<List<ProduceInfo>>> getProduceList(@RequestParam(required = false) String produceName,
                                              @RequestParam(required = false) String categoryIds,
                                              @RequestParam(required = false) BigDecimal minPrice,
                                              @RequestParam(required = false) BigDecimal maxPrice,
                                              @RequestParam(required = false) Integer inStock,
                                              @RequestParam Integer currentPage,
                                              @RequestParam Integer pageSize) {
        log.info("获取农产品列表请求: produceName={}, categoryIds={}, minPrice={}, maxPrice={}, inStock={}, currentPage={}, pageSize={}",
                produceName, categoryIds, minPrice, maxPrice, inStock, currentPage, pageSize);
        try {
            PageDTO<List<ProduceInfo>> pageDTO = produceInfoService.getProduceList(produceName, categoryIds, minPrice, maxPrice, inStock, currentPage, pageSize);
            log.info("获取农产品列表成功: 总条数={}, 总页数={}", pageDTO.getTotal(), pageDTO.getPages());
            return Result.success(pageDTO);
        } catch (RuntimeException e) {
            log.warn("获取农产品列表失败: error={}", e.getMessage());
            return Result.error(e.getMessage());
        }
    }

    /**
     * 农产品详情
     * @param id 农产品ID
     * @return 农产品详情
     */
    @GetMapping("/{id}")
    @Operation(summary = "获取农产品详情", description = "获取指定ID的农产品详细信息")
    public Result<ProduceDetailDTO> getProduceDetail(@PathVariable Long id) {
        log.info("获取农产品详情请求: id={}", id);
        try {
            ProduceDetailDTO produceDetail = produceInfoService.getProduceDetail(id);
            log.info("获取农产品详情成功: id={}, produceName={}", id, produceDetail.getProduceName());
            return Result.success(produceDetail);
        } catch (RuntimeException e) {
            log.warn("获取农产品详情失败: id={}, error={}", id, e.getMessage());
            return Result.notFound(e.getMessage());
        }
    }
}
