package com.wyt.agriculture.service.impl;

import com.wyt.agriculture.domain.po.OrderItem;
import com.wyt.agriculture.mapper.OrderItemMapper;
import com.wyt.agriculture.service.IOrderItemService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 订单项明细表 服务实现类
 * </p>
 *
 * @author WYT
 * @since 2025-12-01
 */
@Service
public class OrderItemServiceImpl extends ServiceImpl<OrderItemMapper, OrderItem> implements IOrderItemService {

}
