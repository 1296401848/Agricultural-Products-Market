package com.wyt.agriculture.domain.dto;

import lombok.Data;

/**
 * 更新地址请求DTO
 */
@Data
public class AddressDTO {
    private String recipient;
    private String phone;
    private String address;
    private Integer isDefault;
}