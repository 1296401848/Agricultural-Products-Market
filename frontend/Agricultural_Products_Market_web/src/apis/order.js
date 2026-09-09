import request from "@/utils/request.js";

export const getOrderList = () => {
    return request.get('/order')
}

export const getOrderDetail = (id) => {
    return request.get(`/order/${id}`)
}

export const createOrder = (orderData) => {
    return request.post('/order', orderData)
}

export const payOrder = (payData) => {
    return request.post('/order/pay', payData)
}

export const cancelOrder = (id) => {
    return request.put(`/order/${id}/cancel`)
}

export const confirmOrder = (id) => {
    return request.put(`/order/${id}/confirm`)
}

export const getAdminOrderList = (params) => {
    return request.get('/admin/orders', { params })
}

export const updateOrderStatus = (statusData) => {
    return request.put('/admin/orders/status', statusData)
}