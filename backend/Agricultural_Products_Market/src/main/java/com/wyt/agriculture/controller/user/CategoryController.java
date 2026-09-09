package com.wyt.agriculture.controller.user;

import com.wyt.agriculture.domain.po.Category;
import com.wyt.agriculture.domain.result.Result;
import com.wyt.agriculture.service.ICategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * <p>
 * 农产品分类表 前端控制器
 * </p>
 *
 * @author WYT
 * @since 2025-12-01
 */
@RestController
@RequestMapping("/category")
@Tag(name = "农产品分类管理", description = "农产品分类相关接口")
@Slf4j
public class CategoryController {

    @Autowired
    private ICategoryService categoryService;

    /**
     * 获取所有农产品分类
     * @return 农产品分类列表
     */
    @GetMapping
    @Operation(summary = "获取所有农产品分类", description = "获取系统中所有的农产品分类")
    public Result<List<Category>> getAllCategories() {
        log.info("获取农产品分类列表请求");
        try {
            List<Category> categoryList = categoryService.list();
            log.info("获取农产品分类列表成功: 数量={}", categoryList.size());
            return Result.success(categoryList);
        } catch (Exception e) {
            log.error("获取农产品分类列表失败: error={}", e.getMessage());
            return Result.error("获取农产品分类列表失败");
        }
    }

    /**
     * 获取农产品分类详情
     * @param id 分类ID
     * @return 农产品分类详情
     */
    @GetMapping("/{id}")
    @Operation(summary = "获取农产品分类详情", description = "根据分类ID获取农产品分类详情")
    public Result<Category> getCategoryDetail(@PathVariable Long id) {
        log.info("获取农产品分类详情请求: categoryId={}", id);
        try {
            Category category = categoryService.getById(id);
            if (category == null) {
                log.warn("获取农产品分类详情失败: 分类不存在，categoryId={}", id);
                return Result.error("分类不存在");
            }
            log.info("获取农产品分类详情成功: categoryId={}, categoryName={}", id, category.getCategoryName());
            return Result.success(category);
        } catch (Exception e) {
            log.error("获取农产品分类详情失败: categoryId={}, error={}", id, e.getMessage());
            return Result.error("获取农产品分类详情失败");
        }
    }
}
