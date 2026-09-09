import request from "@/utils/request.js";

export const getAddressList = () => {
    return request.get('/address')
}

export const addAddress = (addressData) => {
    return request.post('/address', addressData)
}

export const updateAddress = (id, addressData) => {
    return request.put(`/address/${id}`, addressData)
}

export const deleteAddress = (id) => {
    return request.delete(`/address/${id}`)
}