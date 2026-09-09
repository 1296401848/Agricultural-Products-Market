import request from "@/utils/request.js";

export const login = (loginData) => {
    return request.post('/auth/login', loginData)
}

export const register = (registerData) => {
    return request.post('/auth/register', registerData)
}

export const getCaptcha = () => {
    return request.get('/auth/captcha')
}

export const sendResetCode = (data) => {
    return request.post('/auth/forgot-password', data)
}

export const resetPassword = (data) => {
    return request.post('/auth/reset-password', data)
}

