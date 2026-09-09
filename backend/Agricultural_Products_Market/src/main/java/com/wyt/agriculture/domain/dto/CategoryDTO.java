package com.wyt.agriculture.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * <p>
 * 农产品分类DTO
 * </p>
 *
 * @author WYT
 * @since 2025-12-08
 */
@Data
@Schema(description = "农产品分类DTO")
public class CategoryDTO {

    @Schema(description = "分类名称")
    private String categoryName;

}
