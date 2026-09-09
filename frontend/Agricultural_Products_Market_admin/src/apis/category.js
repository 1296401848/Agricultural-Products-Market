import request from "@/utils/request.js";

// 分类管理
export const getCategoryList = () => {
    return request.get('/admin/category');
};

export const addCategory = (categoryData) => {
    return request.post('/admin/category', categoryData);
};

export const updateCategory = (id, categoryData) => {
    return request.put(`/admin/category/${id}`, categoryData);
};

export const deleteCategory = (id) => {
    return request.delete(`/admin/category/${id}`);
};
