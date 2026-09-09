package com.wyt.agriculture.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.wyt.agriculture.domain.po.ProducePackage;
import com.wyt.agriculture.domain.po.PackageProduce;

import java.util.List;
import java.util.Map;

public interface IProducePackageService extends IService<ProducePackage> {

    IPage<ProducePackage> adminGetList(Integer page, Integer size, String name);

    void adminCreate(ProducePackage pkg, List<PackageProduce> produces);

    void adminUpdate(ProducePackage pkg, List<PackageProduce> produces);

    void adminUpdateStatus(Long id, Integer status);

    /** 用户端：上架套餐列表 */
    List<ProducePackage> getUserPackageList();

    /** 用户端：套餐详情（含农产品列表） */
    Map<String, Object> getPackageDetail(Long packageId);

    /** 获取套餐内农产品（含 produceNum 和实际 stock） */
    List<Map<String, Object>> getPackageProduces(Long packageId);
}
