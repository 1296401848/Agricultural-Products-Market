import request from "@/utils/request.js";

export const getPackageList = () => request.get('/package')
export const getPackageDetail = (id) => request.get(`/package/${id}`)
