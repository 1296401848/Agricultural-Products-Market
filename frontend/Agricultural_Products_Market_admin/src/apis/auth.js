import request from "@/utils/request.js";

export const login = (loginData) => {
    return request.post('/auth/login', loginData)
}

