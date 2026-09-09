import request from "@/utils/request.js";

// 用户管理
export const getUserList = (params) => {
    return request.get('/admin/user', { params });
};

// 更改用户状态
export const updateUserStatus = (data) => {
    return request.put('/admin/user/status', data);
};

// 添加用户
export const addUser = (data) => {
    return request.post('/admin/user', data);
};

// 删除用户
export const deleteUser = (id) => {
    return request.delete(`/admin/user/${id}`);
};

// 批量操作
export const batchUpdateUserStatus = (ids, status) => {
    return request.put('/admin/user/batch-status', { ids, status })
}

export const batchDeleteUsers = (ids) => {
    return request.delete('/admin/user/batch', { data: { ids } })
}
