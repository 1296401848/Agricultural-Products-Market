import request from "@/utils/request.js";

// 农产品管理
export const getProduceList = (params) => {
    return request.get('/admin/produce', { params });
};

export const addProduce = (produceData) => {
    return request.post('/admin/produce', produceData);
};

export const updateProduce = (id, produceData) => {
    return request.put(`/admin/produce/${id}`, produceData);
};

export const deleteProduce = (id) => {
    return request.delete(`/admin/produce/${id}`);
};

export const updateProduceStatus = (data) => {
    return request.put('/admin/produce/status', data);
};

export const getProduceDetail = (id) => {
    return request.get(`/admin/produce/${id}`)
}

export const batchUpdateProduceStatus = (ids, status) => {
    return request.put('/admin/produce/batch-status', { ids, status })
}

export const batchDeleteProduces = (ids) => {
    return request.delete('/admin/produce/batch', { data: { ids } })
}