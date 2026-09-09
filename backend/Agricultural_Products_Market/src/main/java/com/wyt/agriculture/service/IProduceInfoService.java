package com.wyt.agriculture.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.wyt.agriculture.domain.dto.ProduceDTO;
import com.wyt.agriculture.domain.dto.ProduceDetailDTO;
import com.wyt.agriculture.domain.dto.PageDTO;
import com.wyt.agriculture.domain.po.ProduceInfo;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 * 农产品信息表 服务类
 * </p>
 *
 * @author WYT
 * @since 2025-12-01
 */
public interface IProduceInfoService extends IService<ProduceInfo> {
    /**
     * 获取农产品列表
     * @param produceName 名称（可选）
     * @param categoryIds 分类ID，逗号分隔（可选）
     * @param minPrice 最低价（可选）
     * @param maxPrice 最高价（可选）
     * @param inStock 仅看有货（可选，1=是）
     * @param currentPage 当前页码
     * @param pageSize 每页大小
     * @return 农产品分页列表
     */
    PageDTO<List<ProduceInfo>> getProduceList(String produceName, String categoryIds, java.math.BigDecimal minPrice, java.math.BigDecimal maxPrice, Integer inStock, Integer currentPage, Integer pageSize);

    /**
     * 获取农产品详情
     * @param id 农产品ID
     * @return 农产品详情
     */
    ProduceDetailDTO getProduceDetail(Long id);

    /**
     * 管理员分页查询所有农产品
     * @param page 页码
     * @param size 每页大小
     * @param produceName 名称（可选，用于模糊查询）
     * @param categoryId 分类ID（可选，用于按分类筛选）
     * @param status 状态（可选，用于按状态筛选，1-上架，0-下架）
     * @return 农产品分页列表
     */
    IPage<ProduceInfo> adminGetProduceList(Integer page, Integer size, String produceName, Long categoryId, Integer status);

    /**
     * 管理员添加农产品
     * @param produceDTO 农产品请求
     * @return 添加结果
     */
    boolean adminAddProduce(ProduceDTO produceDTO);

    /**
     * 管理员修改农产品
     * @param id 农产品ID
     * @param produceDTO 农产品请求
     * @return 修改结果
     */
    boolean adminUpdateProduce(Long id, ProduceDTO produceDTO);

    /**
     * 管理员删除农产品
     * @param id 农产品ID
     * @return 删除结果
     */
    boolean adminDeleteProduce(Long id);

    /**
     * 管理员获取农产品详情
     * @param id 农产品ID
     * @return 农产品详情，包括下架农产品
     */
    ProduceInfo adminGetProduceDetail(Long id);

    /**
     * 管理员上架/下架农产品
     * @param produceDTO 更新状态请求
     * @return 更新结果
     */
    boolean adminUpdateProduceStatus(ProduceDTO produceDTO);
}
