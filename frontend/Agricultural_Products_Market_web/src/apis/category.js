import request from "@/utils/request.js";

export const getCategoryList = () => {
    return request.get('/category')
}

export const getCategoryById = (id) => {
    return request.get(`/category/${id}`)
}