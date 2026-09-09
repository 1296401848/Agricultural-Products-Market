import request from "@/utils/request.js";

export const getCouponList = (params) => {
    return request.get('/admin/coupons', { params })
}

export const createCoupon = (data) => {
    return request.post('/admin/coupons', data)
}

export const updateCoupon = (id, data) => {
    return request.put(`/admin/coupons/${id}`, data)
}

export const updateCouponStatus = (id, status) => {
    return request.put(`/admin/coupons/${id}/status`, null, { params: { status } })
}

export const batchUpdateCouponStatus = (ids, status) => {
    return request.put('/admin/coupons/batch-status', { ids, status })
}

export const batchDeleteCoupons = (ids) => {
    return request.delete('/admin/coupons/batch', { data: { ids } })
}
