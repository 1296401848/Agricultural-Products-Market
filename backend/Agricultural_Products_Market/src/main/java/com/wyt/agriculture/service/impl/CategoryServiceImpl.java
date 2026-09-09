package com.wyt.agriculture.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.wyt.agriculture.domain.dto.CategoryDTO;
import com.wyt.agriculture.domain.po.Category;
import com.wyt.agriculture.domain.po.ProduceInfo;
import com.wyt.agriculture.mapper.CategoryMapper;
import com.wyt.agriculture.service.ICategoryService;
import com.wyt.agriculture.service.IProduceInfoService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * <p>
 * 农产品分类表 服务实现类
 * </p>
 *
 * @author WYT
 * @since 2025-12-01
 */
@Slf4j
@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements ICategoryService {

    @Autowired
    private IProduceInfoService produceInfoService;

    @Override
    public boolean adminAddCategory(CategoryDTO categoryDTO) {
        // 创建分类对象
        Category category = new Category();
        category.setCategoryName(categoryDTO.getCategoryName());
        category.setCreateTime(LocalDateTime.now());
        
        // 保存分类
        boolean result = this.save(category);
        
        if (result) {
            log.info("管理员添加分类成功: categoryName={}", categoryDTO.getCategoryName());
        } else {
            log.error("管理员添加分类失败: categoryName={}", categoryDTO.getCategoryName());
            throw new RuntimeException("添加分类失败");
        }
        
        return result;
    }

    @Override
    public boolean adminUpdateCategory(Long id, CategoryDTO categoryDTO) {
        // 检查分类是否存在
        Category category = this.getById(id);
        if (category == null) {
            log.warn("管理员修改分类失败: 分类不存在，categoryId={}", id);
            throw new RuntimeException("分类不存在");
        }
        
        // 更新分类信息
        category.setCategoryName(categoryDTO.getCategoryName());
        
        // 保存更新
        boolean result = this.updateById(category);
        
        if (result) {
            log.info("管理员修改分类成功: categoryId={}, categoryName={}", id, categoryDTO.getCategoryName());
        } else {
            log.error("管理员修改分类失败: categoryId={}", id);
            throw new RuntimeException("修改分类失败");
        }
        
        return result;
    }

    @Override
    public boolean adminDeleteCategory(Long id) {
        // 检查分类是否存在
        Category category = this.getById(id);
        if (category == null) {
            log.warn("管理员删除分类失败: 分类不存在，categoryId={}", id);
            throw new RuntimeException("分类不存在");
        }
        
        // 将该分类下所有农产品移到「未分类」（categoryId=0）
        LambdaQueryWrapper<ProduceInfo> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ProduceInfo::getCategoryId, id);
        long count = produceInfoService.count(queryWrapper);
        if (count > 0) {
            ProduceInfo updateInfo = new ProduceInfo();
            updateInfo.setCategoryId(0L);
            produceInfoService.update(updateInfo, queryWrapper);
            log.info("已将{}件农产品移入未分类: categoryId={}", count, id);
        }

        // 删除分类
        boolean result = this.removeById(id);
        
        if (result) {
            log.info("管理员删除分类成功: categoryId={}, categoryName={}", id, category.getCategoryName());
        } else {
            log.error("管理员删除分类失败: categoryId={}", id);
            throw new RuntimeException("删除分类失败");
        }
        
        return result;
    }
}
