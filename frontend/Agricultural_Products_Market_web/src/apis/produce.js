import request from "@/utils/request.js";

export const getProduceList = (params) => {
    return request.get('/produce', { params })
}

export const getProduceDetail = (id) => {
    return request.get(`/produce/${id}`)
}
