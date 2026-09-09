package com.wyt.agriculture.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wyt.agriculture.domain.po.PackageProduce;
import com.wyt.agriculture.mapper.PackageProduceMapper;
import com.wyt.agriculture.service.IPackageProduceService;
import org.springframework.stereotype.Service;

@Service
public class PackageProduceServiceImpl extends ServiceImpl<PackageProduceMapper, PackageProduce> implements IPackageProduceService {
}
