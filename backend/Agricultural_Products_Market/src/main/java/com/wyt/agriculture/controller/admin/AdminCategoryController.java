package com.wyt.agriculture.controller.admin;

import com.wyt.agriculture.domain.dto.CategoryDTO;
import com.wyt.agriculture.domain.po.Category;
import com.wyt.agriculture.domain.result.Result;
import com.wyt.agriculture.service.ICategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 管理员农产品分类管理
 * </p>
 *
 * @author WYT
 * @since 2025-12-08
 */
@RestController
@RequestMapping("/admin/category")
@Tag(name = "管理员分类管理", description = "管理员分类相关接口")
@Slf4j
public class AdminCategoryController {

    @Autowired
    private ICategoryService categoryService;

    /**
     * 管理员添加分类
     * @param categoryDTO 分类信息
     * @return 添加结果
     */
    @PostMapping
    @Operation(summary = "添加分类", description = "管理员添加农产品分类")
    public Result<String> addCategory(@RequestBody CategoryDTO categoryDTO) {
        log.info("管理员添加分类请求: categoryName={}", categoryDTO.getCategoryName());
        
        try {
            boolean result = categoryService.adminAddCategory(categoryDTO);
            if (result) {
                log.info("管理员添加分类成功: categoryName={}", categoryDTO.getCategoryName());
                return Result.success("添加分类成功");
            } else {
                log.warn("管理员添加分类失败: categoryName={}", categoryDTO.getCategoryName());
                return Result.error("添加分类失败");
            }
        } catch (RuntimeException e) {
            log.warn("管理员添加分类失败: categoryName={}, error={}", categoryDTO.getCategoryName(), e.getMessage());
            return Result.error(e.getMessage());
        }
    }

    /**
     * 管理员修改分类
     * @param id 分类ID
     * @param categoryDTO 分类信息
     * @return 修改结果
     */
    @PutMapping("/{id}")
    @Operation(summary = "修改分类", description = "管理员修改农产品分类")
    public Result<String> updateCategory(@PathVariable Long id, @RequestBody CategoryDTO categoryDTO) {
        log.info("管理员修改分类请求: categoryId={}, categoryName={}", id, categoryDTO.getCategoryName());
        
        try {
            boolean result = categoryService.adminUpdateCategory(id, categoryDTO);
            if (result) {
                log.info("管理员修改分类成功: categoryId={}, categoryName={}", id, categoryDTO.getCategoryName());
                return Result.success("修改分类成功");
            } else {
                log.warn("管理员修改分类失败: categoryId={}", id);
                return Result.error("修改分类失败");
            }
        } catch (RuntimeException e) {
            log.warn("管理员修改分类失败: categoryId={}, error={}", id, e.getMessage());
            return Result.error(e.getMessage());
        }
    }

    /**
     * 管理员删除分类
     * @param id 分类ID
     * @return 删除结果
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除分类", description = "管理员删除农产品分类")
    public Result<String> deleteCategory(@PathVariable Long id) {
        log.info("管理员删除分类请求: categoryId={}", id);
        
        try {
            boolean result = categoryService.adminDeleteCategory(id);
            if (result) {
                log.info("管理员删除分类成功: categoryId={}", id);
                return Result.success("删除分类成功");
            } else {
                log.warn("管理员删除分类失败: categoryId={}", id);
                return Result.error("删除分类失败");
            }
        } catch (RuntimeException e) {
            log.warn("管理员删除分类失败: categoryId={}, error={}", id, e.getMessage());
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 获取分类列表
     * @return 分类列表
     */
    @GetMapping
    @Operation(summary = "获取分类列表", description = "获取所有农产品分类")
    public Result getCategoryList() {
        log.info("管理员获取分类列表请求");
        
        try {
            List<Category> categoryList = categoryService.list();
            log.info("管理员获取分类列表成功: 数量={}", categoryList.size());
            return Result.success(categoryList, "获取分类列表成功");
        } catch (RuntimeException e) {
            log.warn("管理员获取分类列表失败: error={}", e.getMessage());
            return Result.error(e.getMessage());
        }
    }

    @DeleteMapping("/batch")
    @Operation(summary = "批量删除分类")
    public Result<String> batchDelete(@RequestBody Map<String, Object> body) {
        @SuppressWarnings("unchecked")
        List<Integer> idList = (List<Integer>) body.get("ids");
        for (Integer id : idList) categoryService.adminDeleteCategory(id.longValue());
        return Result.success("批量删除完成");
    }
}
