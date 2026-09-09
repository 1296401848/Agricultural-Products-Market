import request from "@/utils/request.js";

// 订单管理
export const getOrderList = (params) => {
    return request.get('/admin/order', { params });
};

export const updateOrderStatus = (data) => {
    return request.put('/admin/order/status', data);
};

export const getOrderDetail = (id) => {
    return request.get(`/admin/order/${id}`);
};

export const batchUpdateOrderStatus = (ids, orderStatus) => {
    return request.put('/admin/order/batch-status', { ids, orderStatus })
}

export const batchDeleteOrders = (ids) => {
    return request.delete('/admin/order/batch', { data: { ids } })
}
