package com.wyt.agriculture.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wyt.agriculture.domain.dto.ProduceDTO;
import com.wyt.agriculture.domain.dto.ProduceDetailDTO;
import com.wyt.agriculture.domain.dto.PageDTO;
import com.wyt.agriculture.domain.po.Category;
import com.wyt.agriculture.domain.po.ProduceInfo;
import com.wyt.agriculture.domain.po.PackageProduce;
import com.wyt.agriculture.mapper.ProduceInfoMapper;
import com.wyt.agriculture.service.ICategoryService;
import com.wyt.agriculture.service.IProduceInfoService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * 农产品信息表 服务实现类
 * </p>
 *
 * @author WYT
 * @since 2025-12-01
 */
@Service
@Slf4j
public class ProduceInfoServiceImpl extends ServiceImpl<ProduceInfoMapper, ProduceInfo> implements IProduceInfoService {

    @Autowired
    private ICategoryService categoryService;

    @Autowired
    private com.wyt.agriculture.service.IPackageProduceService packageProduceService;

    @Autowired
    private com.wyt.agriculture.service.IProducePackageService producePackageService;

    @Override
    public PageDTO getProduceList(String produceName, String categoryIds, BigDecimal minPrice, BigDecimal maxPrice, Integer inStock, Integer currentPage, Integer pageSize) {
        LambdaQueryWrapper<ProduceInfo> queryWrapper = new LambdaQueryWrapper<>();
        // 只查询上架的农产品
        queryWrapper.eq(ProduceInfo::getStatus, 1);
        // 按名称/生产商模糊查询
        if (produceName != null && !produceName.isEmpty()) {
            queryWrapper.and(w -> w.like(ProduceInfo::getProduceName, produceName)
                    .or().like(ProduceInfo::getManufacturer, produceName));
        }
        // 多分类筛选
        if (categoryIds != null && !categoryIds.isEmpty()) {
            List<Long> idList = Arrays.stream(categoryIds.split(","))
                    .map(Long::parseLong)
                    .collect(Collectors.toList());
            queryWrapper.in(ProduceInfo::getCategoryId, idList);
        }
        // 价格范围筛选
        if (minPrice != null) {
            queryWrapper.ge(ProduceInfo::getPrice, minPrice);
        }
        if (maxPrice != null) {
            queryWrapper.le(ProduceInfo::getPrice, maxPrice);
        }
        // 仅看有货
        if (inStock != null && inStock == 1) {
            queryWrapper.gt(ProduceInfo::getStock, 0);
        }
        Page<ProduceInfo> page = new Page<>(currentPage, pageSize);
        page = this.page(page, queryWrapper);
        List<ProduceInfo> produceList = page.getRecords();
        return PageDTO.of(page, produceList);
    }

    @Override
    public ProduceDetailDTO getProduceDetail(Long id) {
        ProduceInfo produceInfo = this.getById(id);
        // 只允许查看上架的农产品
        if (produceInfo == null || produceInfo.getStatus() == 0) {
            throw new RuntimeException("农产品不存在或已下架");
        }
        ProduceDetailDTO produceDetailDTO = new ProduceDetailDTO();
        BeanUtil.copyProperties(produceInfo, produceDetailDTO);
        // 获取分类名称
        Category category = categoryService.getById(produceInfo.getCategoryId());
        produceDetailDTO.setCategoryName(category.getCategoryName());
        return produceDetailDTO;
    }

    @Override
    public IPage<ProduceInfo> adminGetProduceList(Integer page, Integer size, String produceName, Long categoryId, Integer status) {
        LambdaQueryWrapper<ProduceInfo> queryWrapper = new LambdaQueryWrapper<>();
        // 按名称/生产商模糊查询
        if (produceName != null && !produceName.isEmpty()) {
            queryWrapper.and(w -> w.like(ProduceInfo::getProduceName, produceName)
                    .or().like(ProduceInfo::getManufacturer, produceName));
        }
        // 按分类ID筛选
        if (categoryId != null) {
            queryWrapper.eq(ProduceInfo::getCategoryId, categoryId);
        }
        // 按状态筛选
        if (status != null) {
            queryWrapper.eq(ProduceInfo::getStatus, status);
        }
        IPage<ProduceInfo> producePage = this.page(new Page<>(page, size), queryWrapper);
        log.info("管理员查询农产品列表成功: 总数={}, 页数={}", producePage.getTotal(), producePage.getPages());
        return producePage;
    }

    @Override
    public boolean adminAddProduce(ProduceDTO produceDTO) {
        if (produceDTO.getPrice() == null || produceDTO.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("价格必须大于0");
        }
        if (produceDTO.getStock() == null || produceDTO.getStock() < 0) {
            throw new RuntimeException("库存不能为负数");
        }
        // 创建农产品
        ProduceInfo produceInfo = new ProduceInfo();
        produceInfo.setProduceName(produceDTO.getProduceName());
        produceInfo.setCategoryId(produceDTO.getCategoryId());
        produceInfo.setManufacturer(produceDTO.getManufacturer());
        produceInfo.setPrice(produceDTO.getPrice());
        produceInfo.setStock(produceDTO.getStock());
        produceInfo.setDescription(produceDTO.getDescription());
        produceInfo.setStatus(produceDTO.getStatus());
        produceInfo.setCoverUrl(produceDTO.getCoverUrl());
        produceInfo.setCreateTime(LocalDateTime.now());
        produceInfo.setUpdateTime(LocalDateTime.now());
        boolean result = this.save(produceInfo);

        log.info("管理员添加农产品成功: produceId={}, produceName={}", produceInfo.getId(), produceInfo.getProduceName());
        return result;
    }

    @Override
    public boolean adminUpdateProduce(Long id, ProduceDTO produceDTO) {
        // 检查农产品是否存在
        ProduceInfo produceInfo = this.getById(id);
        if (produceInfo == null) {
            log.warn("管理员修改农产品失败: 农产品不存在，produceId={}", id);
            throw new RuntimeException("农产品不存在");
        }

        if (produceDTO.getPrice() == null || produceDTO.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("价格必须大于0");
        }
        if (produceDTO.getStock() == null || produceDTO.getStock() < 0) {
            throw new RuntimeException("库存不能为负数");
        }
        // 更新农产品信息
        produceInfo.setProduceName(produceDTO.getProduceName());
        produceInfo.setCategoryId(produceDTO.getCategoryId());
        produceInfo.setManufacturer(produceDTO.getManufacturer());
        produceInfo.setPrice(produceDTO.getPrice());
        produceInfo.setStock(produceDTO.getStock());
        produceInfo.setDescription(produceDTO.getDescription());
        produceInfo.setStatus(produceDTO.getStatus());
        produceInfo.setCoverUrl(produceDTO.getCoverUrl());
        produceInfo.setUpdateTime(LocalDateTime.now());
        boolean result = this.updateById(produceInfo);

        log.info("管理员修改农产品成功: produceId={}, produceName={}", id, produceDTO.getProduceName());
        return result;
    }

    @Override
    public boolean adminDeleteProduce(Long id) {
        // 检查农产品是否存在
        ProduceInfo produceInfo = this.getById(id);
        if (produceInfo == null) {
            log.warn("管理员删除农产品失败: 农产品不存在，produceId={}", id);
            throw new RuntimeException("农产品不存在");
        }

        // 删除套餐关联中的该农产品
        packageProduceService.remove(new LambdaQueryWrapper<PackageProduce>().eq(PackageProduce::getProduceId, id));

        // 删除农产品
        boolean result = this.removeById(id);

        log.info("管理员删除农产品成功: produceId={}, produceName={}", id, produceInfo.getProduceName());
        return result;
    }

    @Override
    public ProduceInfo adminGetProduceDetail(Long id) {
        ProduceInfo produceInfo = this.getById(id);
        if (produceInfo == null) {
            throw new RuntimeException("农产品不存在");
        }
        return produceInfo;
    }

    @Override
    public boolean adminUpdateProduceStatus(ProduceDTO produceDTO) {
        // 检查农产品是否存在
        ProduceInfo produceInfo = this.getById(produceDTO.getId());
        if (produceInfo == null) {
            log.warn("管理员更新农产品状态失败: 农产品不存在，produceId={}", produceDTO.getId());
            throw new RuntimeException("农产品不存在");
        }

        // 更新农产品状态
        produceInfo.setStatus(produceDTO.getStatus());
        produceInfo.setUpdateTime(LocalDateTime.now());
        boolean result = this.updateById(produceInfo);

        log.info("管理员更新农产品状态成功: produceId={}, produceName={}, newStatus={}", 
                produceDTO.getId(), produceInfo.getProduceName(), produceDTO.getStatus());
        return result;
    }
}
