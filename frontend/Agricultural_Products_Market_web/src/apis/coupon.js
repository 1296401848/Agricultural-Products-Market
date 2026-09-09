import request from "@/utils/request.js";

export const getAvailableCoupons = () => {
    return request.get('/coupons/available')
}

export const receiveCoupon = (couponId) => {
    return request.post(`/coupons/${couponId}/receive`)
}

export const getUserCoupons = (useStatus) => {
    return request.get('/user/coupons', { params: { useStatus } })
}
