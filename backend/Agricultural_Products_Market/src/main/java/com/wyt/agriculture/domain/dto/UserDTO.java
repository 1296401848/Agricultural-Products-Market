package com.wyt.agriculture.domain.dto;

import lombok.Data;

/**
 * 登录请求DTO
 */
@Data
public class UserDTO {
    private Long id;
    private String username;
    private String password;
    private String email;
    private String role;
    private Integer status;
}
