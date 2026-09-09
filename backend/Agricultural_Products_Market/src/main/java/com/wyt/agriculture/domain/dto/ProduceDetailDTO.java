package com.wyt.agriculture.domain.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 农产品请求DTO，用于添加和修改农产品
 */
@Data
public class ProduceDetailDTO {

    private Long id;

    private String produceName;

    private Long categoryId;

    private String categoryName;

    private String manufacturer;

    private BigDecimal price;

    private Integer stock;

    private String description;

    private Integer status;

    private String coverUrl;

}