package com.wyt.agriculture.domain.dto;

import lombok.Data;

/**
 * 修改购物车请求DTO
 */
@Data
public class CartDTO {
    private Long id;
    private Long produceId;
    private Integer quantity;
    private Long packageId;
    private Integer cartType;

}
