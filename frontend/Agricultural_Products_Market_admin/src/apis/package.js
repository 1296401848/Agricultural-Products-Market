import request from "@/utils/request.js";

export const getPackageList = (params) => request.get('/admin/package', { params })
export const createPackage = (data) => request.post('/admin/package', data)
export const updatePackage = (id, data) => request.put(`/admin/package/${id}`, data)
export const updatePackageStatus = (id, status) => request.put(`/admin/package/${id}/status`, null, { params: { status } })

export const batchUpdatePackageStatus = (ids, status) => request.put('/admin/package/batch-status', { ids, status })
export const batchDeletePackages = (ids) => request.delete('/admin/package/batch', { data: { ids } })
