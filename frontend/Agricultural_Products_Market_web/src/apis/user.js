import request from "@/utils/request.js";

export const updatePassword = (passwordData) => {
    return request.put('/user/password', passwordData)
}