package com.wyt.agriculture.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wyt.agriculture.domain.po.ProduceInfo;
import com.wyt.agriculture.domain.po.ProducePackage;
import com.wyt.agriculture.domain.po.PackageProduce;
import com.wyt.agriculture.mapper.ProducePackageMapper;
import com.wyt.agriculture.service.IProduceInfoService;
import com.wyt.agriculture.service.IProducePackageService;
import com.wyt.agriculture.service.IPackageProduceService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ProducePackageServiceImpl extends ServiceImpl<ProducePackageMapper, ProducePackage> implements IProducePackageService {

    @Autowired
    private IPackageProduceService packageProduceService;

    @Autowired
    private IProduceInfoService produceInfoService;

    @Override
    public IPage<ProducePackage> adminGetList(Integer page, Integer size, String name) {
        LambdaQueryWrapper<ProducePackage> qw = new LambdaQueryWrapper<>();
        if (name != null && !name.isEmpty()) qw.like(ProducePackage::getPackageName, name);
        qw.orderByDesc(ProducePackage::getCreateTime);
        return this.page(new Page<>(page, size), qw);
    }

    @Override
    @Transactional
    public void adminCreate(ProducePackage pkg, List<PackageProduce> produces) {
        // 计算原价
        BigDecimal original = BigDecimal.ZERO;
        for (PackageProduce pb : produces) {
            ProduceInfo produce = produceInfoService.getById(pb.getProduceId());
            if (produce != null) original = original.add(produce.getPrice().multiply(new BigDecimal(pb.getProduceNum())));
        }
        pkg.setOriginalPrice(original);
        pkg.setSales(0);
        pkg.setCreateTime(LocalDateTime.now());
        this.save(pkg);

        for (PackageProduce pb : produces) {
            pb.setPackageId(pkg.getId());
        }
        packageProduceService.saveBatch(produces);
        log.info("创建套餐: {}", pkg.getPackageName());
    }

    @Override
    @Transactional
    public void adminUpdate(ProducePackage pkg, List<PackageProduce> produces) {
        ProducePackage old = this.getById(pkg.getId());
        BigDecimal original = BigDecimal.ZERO;
        for (PackageProduce pb : produces) {
            ProduceInfo produce = produceInfoService.getById(pb.getProduceId());
            if (produce != null) original = original.add(produce.getPrice().multiply(new BigDecimal(pb.getProduceNum())));
        }
        pkg.setOriginalPrice(original);
        this.updateById(pkg);

        // 替换关联农产品
        packageProduceService.remove(new LambdaQueryWrapper<PackageProduce>().eq(PackageProduce::getPackageId, pkg.getId()));
        for (PackageProduce pb : produces) {
            pb.setPackageId(pkg.getId());
        }
        packageProduceService.saveBatch(produces);
        log.info("更新套餐: id={}", pkg.getId());
    }

    @Override
    public void adminUpdateStatus(Long id, Integer status) {
        ProducePackage pkg = new ProducePackage();
        pkg.setId(id);
        pkg.setStatus(status);
        this.updateById(pkg);
    }

    @Override
    public List<ProducePackage> getUserPackageList() {
        return this.list(new LambdaQueryWrapper<ProducePackage>().eq(ProducePackage::getStatus, 1).orderByDesc(ProducePackage::getIsRecommend).orderByDesc(ProducePackage::getCreateTime));
    }

    @Override
    public Map<String, Object> getPackageDetail(Long packageId) {
        ProducePackage pkg = this.getById(packageId);
        if (pkg == null || pkg.getStatus() != 1) throw new RuntimeException("套餐不存在或已下架");
        List<Map<String, Object>> produces = getPackageProduces(packageId);
        Map<String, Object> result = new HashMap<>();
        result.put("packageInfo", pkg);
        result.put("produces", produces);
        return result;
    }

    @Override
    public List<Map<String, Object>> getPackageProduces(Long packageId) {
        List<PackageProduce> pbs = packageProduceService.list(new LambdaQueryWrapper<PackageProduce>().eq(PackageProduce::getPackageId, packageId));
        return pbs.stream().map(pb -> {
            ProduceInfo produce = produceInfoService.getById(pb.getProduceId());
            if (produce == null) return null;
            Map<String, Object> m = new HashMap<>();
            m.put("produceId", produce.getId());
            m.put("produceName", produce.getProduceName());
            m.put("price", produce.getPrice());
            m.put("coverUrl", produce.getCoverUrl());
            m.put("stock", produce.getStock());
            m.put("produceNum", pb.getProduceNum());
            return m;
        }).filter(Objects::nonNull).collect(Collectors.toList());
    }
}
