package com.wyt.agriculture.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wyt.agriculture.domain.dto.PayOrderDTO;
import com.wyt.agriculture.domain.dto.OrderDTO;
import com.wyt.agriculture.domain.po.ProduceInfo;
import com.wyt.agriculture.domain.po.OrderInfo;
import com.wyt.agriculture.domain.po.OrderItem;
import com.wyt.agriculture.domain.po.ShoppingCart;
import com.wyt.agriculture.domain.po.SysUser;
import com.wyt.agriculture.domain.po.ProducePackage;
import com.wyt.agriculture.domain.po.PackageProduce;
import com.wyt.agriculture.domain.po.UserAddress;
import com.wyt.agriculture.domain.po.UserCoupon;
import com.wyt.agriculture.domain.vo.OrderVO;
import com.wyt.agriculture.mapper.OrderInfoMapper;
import com.wyt.agriculture.service.IProduceInfoService;
import com.wyt.agriculture.service.IOrderInfoService;
import com.wyt.agriculture.service.IOrderItemService;
import com.wyt.agriculture.service.IShoppingCartService;
import com.wyt.agriculture.service.ISysUserService;
import com.wyt.agriculture.service.IProducePackageService;
import com.wyt.agriculture.service.IPackageProduceService;
import com.wyt.agriculture.service.IUserAddressService;
import com.wyt.agriculture.service.IUserCouponService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * <p>
 * 订单表 服务实现类
 * </p>
 *
 * @author WYT
 * @since 2025-12-01
 */
@Service
@Slf4j
public class OrderInfoServiceImpl extends ServiceImpl<OrderInfoMapper, OrderInfo> implements IOrderInfoService {

    @Autowired
    private ISysUserService sysUserService;

    @Autowired
    private IShoppingCartService shoppingCartService;

    @Autowired
    private IUserAddressService userAddressService;

    @Autowired
    private IProduceInfoService produceInfoService;

    @Autowired
    private IOrderItemService orderItemService;

    @Autowired
    private IUserCouponService userCouponService;

    @Autowired
    private IProducePackageService producePackageService;

    @Autowired
    private IPackageProduceService packageProduceService;

    @Override
    @Transactional
    public OrderInfo createOrder(String username, OrderDTO orderDTO) {
        // 根据用户名获取用户ID
        LambdaQueryWrapper<SysUser> userQueryWrapper = new LambdaQueryWrapper<>();
        userQueryWrapper.eq(SysUser::getUsername, username);
        SysUser user = sysUserService.getOne(userQueryWrapper);
        if (user == null) {
            log.error("创建订单失败: 用户不存在，username={}", username);
            throw new RuntimeException("用户不存在");
        }
        Long userId = user.getId();

        // 检查用户购物车是否为空
        LambdaQueryWrapper<ShoppingCart> cartQueryWrapper = new LambdaQueryWrapper<>();
        cartQueryWrapper.eq(ShoppingCart::getUserId, userId);
        List<ShoppingCart> cartList = shoppingCartService.list(cartQueryWrapper);
        if (cartList.isEmpty()) {
            log.warn("创建订单失败: 购物车为空，userId={}", userId);
            throw new RuntimeException("购物车为空");
        }

        // 检查地址是否存在且属于当前用户
        LambdaQueryWrapper<UserAddress> addressQueryWrapper = new LambdaQueryWrapper<>();
        addressQueryWrapper.eq(UserAddress::getId, orderDTO.getAddressId())
                          .eq(UserAddress::getUserId, userId);
        UserAddress userAddress = userAddressService.getOne(addressQueryWrapper);
        if (userAddress == null) {
            log.warn("创建订单失败: 地址不存在或不属于当前用户，userId={}, addressId={}", userId, orderDTO.getAddressId());
            throw new RuntimeException("地址不存在或不属于当前用户");
        }

        // 计算订单总金额并检查库存
        BigDecimal totalAmount = BigDecimal.ZERO;
        List<OrderItem> orderItems = new ArrayList<>();
        for (ShoppingCart cartItem : cartList) {
            int ctype = cartItem.getCartType() != null ? cartItem.getCartType() : 1;
            if (ctype == 2) {
                // --- 套餐 ---
                ProducePackage pkg = producePackageService.getById(cartItem.getPackageId());
                if (pkg == null || pkg.getStatus() != 1) throw new RuntimeException("套餐不存在或已下架");
                List<PackageProduce> pkgProduces = packageProduceService.list(
                        new LambdaQueryWrapper<PackageProduce>().eq(PackageProduce::getPackageId, cartItem.getPackageId()));
                // 逐本检查库存
                for (PackageProduce pb : pkgProduces) {
                    ProduceInfo bi = produceInfoService.getById(pb.getProduceId());
                    if (bi == null || bi.getStatus() != 1) throw new RuntimeException("套餐内农产品「" + (bi != null ? bi.getProduceName() : pb.getProduceId()) + "」已下架");
                    if (bi.getStock() < pb.getProduceNum() * cartItem.getQuantity())
                        throw new RuntimeException("套餐内农产品「" + bi.getProduceName() + "」库存不足");
                }
                // 价格 = 套餐价 × 数量
                totalAmount = totalAmount.add(pkg.getPackagePrice().multiply(new BigDecimal(cartItem.getQuantity())));
                // 扣减库存 + 创建订单项
                for (PackageProduce pb : pkgProduces) {
                    ProduceInfo bi = produceInfoService.getById(pb.getProduceId());
                    bi.setStock(bi.getStock() - pb.getProduceNum() * cartItem.getQuantity());
                    produceInfoService.updateById(bi);
                    OrderItem oi = new OrderItem();
                    oi.setProduceId(bi.getId());
                    oi.setProduceName(bi.getProduceName());
                    oi.setPrice(bi.getPrice());
                    oi.setQuantity(pb.getProduceNum() * cartItem.getQuantity());
                    oi.setPackageId(pkg.getId());
                    oi.setItemType(2);
                    oi.setCreateTime(LocalDateTime.now());
                    orderItems.add(oi);
                }
                // 套餐销量
                pkg.setSales((pkg.getSales() != null ? pkg.getSales() : 0) + cartItem.getQuantity());
                producePackageService.updateById(pkg);
            } else {
                // --- 普通农产品 ---
                ProduceInfo produceInfo = produceInfoService.getById(cartItem.getProduceId());
                if (produceInfo == null) throw new RuntimeException("农产品不存在");
                if (produceInfo.getStatus() != 1) throw new RuntimeException("农产品已下架");
                if (produceInfo.getStock() < cartItem.getQuantity()) throw new RuntimeException("库存不足");
                BigDecimal itemAmount = produceInfo.getPrice().multiply(new BigDecimal(cartItem.getQuantity()));
                totalAmount = totalAmount.add(itemAmount);
                produceInfo.setStock(produceInfo.getStock() - cartItem.getQuantity());
                produceInfoService.updateById(produceInfo);
                OrderItem orderItem = new OrderItem();
                orderItem.setProduceId(produceInfo.getId());
                orderItem.setProduceName(produceInfo.getProduceName());
                orderItem.setPrice(produceInfo.getPrice());
                orderItem.setQuantity(cartItem.getQuantity());
                orderItem.setItemType(1);
                orderItem.setCreateTime(LocalDateTime.now());
                orderItems.add(orderItem);
            }
        }

        // 优惠券抵扣
        BigDecimal originalTotal = totalAmount;
        BigDecimal discountAmount = BigDecimal.ZERO;
        if (orderDTO.getUserCouponId() != null) {
            discountAmount = userCouponService.useCoupon(orderDTO.getUserCouponId(), null, totalAmount);
            BigDecimal payAmount = totalAmount.subtract(discountAmount);
            if (payAmount.compareTo(BigDecimal.ZERO) < 0) payAmount = BigDecimal.ZERO;
            totalAmount = payAmount;
            log.info("优惠券抵扣: userCouponId={}, discountAmount={}, afterDiscount={}",
                    orderDTO.getUserCouponId(), discountAmount, totalAmount);
        }

        // 生成订单编号
        String orderNo = UUID.randomUUID().toString().replace("-", "");

        // 创建订单
        OrderInfo orderInfo = new OrderInfo();
        orderInfo.setOrderNo(orderNo);
        orderInfo.setUserId(userId);
        orderInfo.setAddressId(orderDTO.getAddressId());
        orderInfo.setTotalAmount(originalTotal);
        orderInfo.setCouponDiscount(discountAmount);
        orderInfo.setPayAmount(totalAmount);
        orderInfo.setPayStatus(0); // 未支付
        orderInfo.setOrderStatus("待发货");
        orderInfo.setCreateTime(LocalDateTime.now());
        orderInfo.setUpdateTime(LocalDateTime.now());
        this.save(orderInfo);

        // 优惠券关联订单ID
        if (orderDTO.getUserCouponId() != null) {
            UserCoupon uc = userCouponService.getById(orderDTO.getUserCouponId());
            if (uc != null && uc.getUseStatus() == 1) {
                uc.setOrderId(orderInfo.getId());
                userCouponService.updateById(uc);
            }
        }

        // 设置订单项的订单ID并保存
        for (OrderItem orderItem : orderItems) {
            orderItem.setOrderId(orderInfo.getId());
        }
        orderItemService.saveBatch(orderItems);

        // 清空购物车
        shoppingCartService.remove(cartQueryWrapper);

        log.info("创建订单成功: orderNo={}, username={}, totalAmount={}, itemCount={}", 
                orderInfo.getOrderNo(), username, totalAmount, orderItems.size());
        return orderInfo;
    }

    @Override
    @Transactional
    public boolean payOrder(String username, PayOrderDTO payOrderDTO) {
        // 检查是否为匿名用户
        if (username.equals("anonymousUser")) {
            log.error("支付订单失败: 请先登录，username={}", username);
            throw new RuntimeException("请先登录");
        }
        
        // 根据用户名获取用户ID
        LambdaQueryWrapper<SysUser> userQueryWrapper = new LambdaQueryWrapper<>();
        userQueryWrapper.eq(SysUser::getUsername, username);
        SysUser user = sysUserService.getOne(userQueryWrapper);
        if (user == null) {
            log.error("支付订单失败: 用户不存在，username={}", username);
            throw new RuntimeException("用户不存在");
        }
        Long userId = user.getId();

        // 检查订单是否存在且属于当前用户
        OrderInfo orderInfo = this.getById(payOrderDTO.getOrderId());
        if (orderInfo == null || !orderInfo.getUserId().equals(userId)) {
            log.warn("支付订单失败: 订单不存在，username={}, orderId={}", username, payOrderDTO.getOrderId());
            throw new RuntimeException("订单不存在");
        }

        // 检查订单是否已支付
        if (orderInfo.getPayStatus() == 1) {
            log.warn("支付订单失败: 订单已支付，orderId={}", payOrderDTO.getOrderId());
            throw new RuntimeException("订单已支付");
        }

        // 模拟支付成功（库存已在创建订单时扣减）
        orderInfo.setPayStatus(1); // 已支付
        orderInfo.setPayTime(LocalDateTime.now());
        orderInfo.setUpdateTime(LocalDateTime.now());
        this.updateById(orderInfo);

        log.info("支付订单成功: orderId={}, orderNo={}", payOrderDTO.getOrderId(), orderInfo.getOrderNo());
        return true;
    }

    @Override
    public List<OrderInfo> getOrderList(String username) {
        // 根据用户名获取用户ID
        LambdaQueryWrapper<SysUser> userQueryWrapper = new LambdaQueryWrapper<>();
        userQueryWrapper.eq(SysUser::getUsername, username);
        SysUser user = sysUserService.getOne(userQueryWrapper);
        if (user == null) {
            log.error("获取订单列表失败: 用户不存在，username={}", username);
            throw new RuntimeException("用户不存在");
        }
        Long userId = user.getId();

        LambdaQueryWrapper<OrderInfo> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(OrderInfo::getUserId, userId);
        queryWrapper.orderByDesc(OrderInfo::getCreateTime);
        List<OrderInfo> orderList = this.list(queryWrapper);

        log.info("获取订单列表成功: username={}, 数量={}", username, orderList.size());
        return orderList;
    }

    @Override
    public OrderVO getOrderDetail(String username, Long orderId) {
        // 根据用户名获取用户ID
        LambdaQueryWrapper<SysUser> userQueryWrapper = new LambdaQueryWrapper<>();
        userQueryWrapper.eq(SysUser::getUsername, username);
        SysUser user = sysUserService.getOne(userQueryWrapper);
        if (user == null) {
            log.error("获取订单详情失败: 用户不存在，username={}", username);
            throw new RuntimeException("用户不存在");
        }
        Long userId = user.getId();

        // 获取订单信息
        OrderInfo orderInfo = this.getById(orderId);
        if (orderInfo == null || !orderInfo.getUserId().equals(userId)) {
            log.warn("获取订单详情失败: 订单不存在，username={}, orderId={}", username, orderId);
            throw new RuntimeException("订单不存在");
        }

        // 获取订单对应的订单项
        LambdaQueryWrapper<OrderItem> orderItemQueryWrapper = new LambdaQueryWrapper<>();
        orderItemQueryWrapper.eq(OrderItem::getOrderId, orderId);
        List<OrderItem> orderItems = orderItemService.list(orderItemQueryWrapper);

        // 获取订单对应的地址信息
        UserAddress userAddress = userAddressService.getById(orderInfo.getAddressId());

        // 封装到OrderVO
        OrderVO orderVO = new OrderVO();
        orderVO.setOrderInfo(orderInfo);
        orderVO.setOrderItems(orderItems);
        if (userAddress != null) {
            orderVO.setRecipient(userAddress.getRecipient());
            orderVO.setPhone(userAddress.getPhone());
            orderVO.setAddress(userAddress.getAddress());
        }

        log.info("获取订单详情成功: orderId={}, orderNo={}, 订单项数量={}", orderId, orderInfo.getOrderNo(), orderItems.size());
        return orderVO;
    }

    @Override
    public IPage<OrderInfo> adminGetOrderList(Integer page, Integer size, LocalDateTime startDate, LocalDateTime endDate, Integer payStatus, String orderStatus) {
        LambdaQueryWrapper<OrderInfo> queryWrapper = new LambdaQueryWrapper<>();
        
        // 按下单日期查询
        if (startDate != null) {
            queryWrapper.ge(OrderInfo::getCreateTime, startDate);
        }
        if (endDate != null) {
            queryWrapper.le(OrderInfo::getCreateTime, endDate);
        }
        
        // 按支付状态查询
        if (payStatus != null) {
            queryWrapper.eq(OrderInfo::getPayStatus, payStatus);
        }
        
        // 按订单状态查询
        if (orderStatus != null && !orderStatus.isEmpty()) {
            queryWrapper.eq(OrderInfo::getOrderStatus, orderStatus);
        }
        
        // 按创建时间倒序排序
        queryWrapper.orderByDesc(OrderInfo::getCreateTime);
        
        IPage<OrderInfo> orderPage = this.page(new Page<>(page, size), queryWrapper);
        log.info("管理员查询订单列表成功: 总数={}, 页数={}, 搜索条件-订单状态={}", orderPage.getTotal(), orderPage.getPages(), orderStatus);
        return orderPage;
    }

    @Override
    public boolean adminUpdateOrderStatus(OrderDTO orderDTO) {
        // 检查订单是否存在
        OrderInfo orderInfo = this.getById(orderDTO.getId());
        if (orderInfo == null) {
            log.warn("管理员更新订单状态失败: 订单不存在，orderId={}", orderDTO.getId());
            throw new RuntimeException("订单不存在");
        }

        // 更新订单状态
        orderInfo.setOrderStatus(orderDTO.getOrderStatus());
        orderInfo.setUpdateTime(LocalDateTime.now());
        boolean result = this.updateById(orderInfo);

        log.info("管理员更新订单状态成功: orderId={}, orderNo={}, newStatus={}", 
                orderDTO.getId(), orderInfo.getOrderNo(), orderDTO.getOrderStatus());
        return result;
    }

    @Override
    public OrderVO adminGetOrderDetail(Long orderId) {
        // 获取订单信息
        OrderInfo orderInfo = this.getById(orderId);
        if (orderInfo == null) {
            log.warn("管理员获取订单详情失败: 订单不存在，orderId={}", orderId);
            throw new RuntimeException("订单不存在");
        }

        // 获取订单对应的订单项
        LambdaQueryWrapper<OrderItem> orderItemQueryWrapper = new LambdaQueryWrapper<>();
        orderItemQueryWrapper.eq(OrderItem::getOrderId, orderId);
        List<OrderItem> orderItems = orderItemService.list(orderItemQueryWrapper);

        // 获取订单对应的地址信息
        UserAddress userAddress = userAddressService.getById(orderInfo.getAddressId());

        // 封装到OrderVO
        OrderVO orderVO = new OrderVO();
        orderVO.setOrderInfo(orderInfo);
        orderVO.setOrderItems(orderItems);
        if (userAddress != null) {
            orderVO.setRecipient(userAddress.getRecipient());
            orderVO.setPhone(userAddress.getPhone());
            orderVO.setAddress(userAddress.getAddress());
        }

        log.info("管理员获取订单详情成功: orderId={}, orderNo={}, 订单项数量={}", orderId, orderInfo.getOrderNo(), orderItems.size());
        return orderVO;
    }

    @Override
    @Transactional
    public void confirmOrder(String username, Long orderId) {
        OrderInfo order = this.getById(orderId);
        if (order == null) throw new RuntimeException("订单不存在");
        if (order.getPayStatus() != 1) throw new RuntimeException("订单未支付");
        if (!"已发货".equals(order.getOrderStatus())) throw new RuntimeException("订单未发货，无法确认收货");

        SysUser user = sysUserService.getByUsername(username);
        if (user == null || !user.getId().equals(order.getUserId())) throw new RuntimeException("无权操作");

        order.setOrderStatus("已完成");
        order.setUpdateTime(LocalDateTime.now());
        this.updateById(order);
        log.info("确认收货: orderId={}, username={}", orderId, username);
    }

    @Override
    @Transactional
    public void cancelOrder(String username, Long orderId) {
        OrderInfo order = this.getById(orderId);
        if (order == null) throw new RuntimeException("订单不存在");
        if (order.getPayStatus() == 1) throw new RuntimeException("已支付订单无法取消");
        if ("已取消".equals(order.getOrderStatus())) throw new RuntimeException("订单已取消");

        // 如果是用户操作，校验归属
        if (username != null) {
            SysUser user = sysUserService.getByUsername(username);
            if (user == null || !user.getId().equals(order.getUserId()))
                throw new RuntimeException("无权操作此订单");
        }

        // 恢复库存
        List<OrderItem> items = orderItemService.list(
                new LambdaQueryWrapper<OrderItem>().eq(OrderItem::getOrderId, orderId));
        for (OrderItem item : items) {
            ProduceInfo produce = produceInfoService.getById(item.getProduceId());
            if (produce != null) {
                produce.setStock(produce.getStock() + item.getQuantity());
                produceInfoService.updateById(produce);
            }
        }

        // 退还优惠券
        if (order.getUserCouponId() != null) {
            userCouponService.refundCoupon(orderId);
        }

        order.setOrderStatus("已取消");
        order.setUpdateTime(LocalDateTime.now());
        this.updateById(order);

        log.info("取消订单成功: orderId={}, orderNo={}, username={}", orderId, order.getOrderNo(), username);
    }

    @Override
    public void adminDeleteOrder(Long id) {
        // 先清理订单项
        orderItemService.remove(new LambdaQueryWrapper<OrderItem>().eq(OrderItem::getOrderId, id));
        this.removeById(id);
        log.info("管理员删除订单: orderId={}", id);
    }

    @Override
    public void autoCancelExpiredOrders() {
        LocalDateTime deadline = LocalDateTime.now().minusMinutes(30);
        List<OrderInfo> expiredOrders = this.list(new LambdaQueryWrapper<OrderInfo>()
                .eq(OrderInfo::getPayStatus, 0)
                .lt(OrderInfo::getCreateTime, deadline));

        for (OrderInfo order : expiredOrders) {
            try {
                cancelOrder(null, order.getId());
                log.info("系统自动取消订单: orderId={}, orderNo={}", order.getId(), order.getOrderNo());
            } catch (Exception e) {
                log.error("自动取消订单失败: orderId={}, error={}", order.getId(), e.getMessage());
            }
        }
        if (!expiredOrders.isEmpty()) log.info("自动取消超时订单完成: 数量={}", expiredOrders.size());
    }
}
