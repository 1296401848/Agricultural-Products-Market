package com.wyt.agriculture.service;

import com.wyt.agriculture.domain.dto.CategoryDTO;
import com.wyt.agriculture.domain.po.Category;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 农产品分类表 服务类
 * </p>
 *
 * @author WYT
 * @since 2025-12-01
 */
public interface ICategoryService extends IService<Category> {
    
    /**
     * 管理员添加分类
     * @param categoryDTO 分类信息
     * @return 添加结果
     */
    boolean adminAddCategory(CategoryDTO categoryDTO);
    
    /**
     * 管理员修改分类
     * @param id 分类ID
     * @param categoryDTO 分类信息
     * @return 修改结果
     */
    boolean adminUpdateCategory(Long id, CategoryDTO categoryDTO);
    
    /**
     * 管理员删除分类
     * @param id 分类ID
     * @return 删除结果
     */
    boolean adminDeleteCategory(Long id);
}
