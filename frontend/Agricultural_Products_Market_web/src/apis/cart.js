import request from "@/utils/request.js";

export const getCartList = () => {
    return request.get('/cart')
}

export const addToCart = (cartData) => {
    return request.post('/cart', cartData)
}

export const updateCartQuantity = (cartData) => {
    return request.put('/cart', cartData)
}

export const deleteCartItem = (id) => {
    return request.delete(`/cart/${id}`)
}