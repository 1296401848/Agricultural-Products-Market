import request from "@/utils/request.js";

export const getAmapKey = () => {
    return request.get('/amap/securityCode')
}