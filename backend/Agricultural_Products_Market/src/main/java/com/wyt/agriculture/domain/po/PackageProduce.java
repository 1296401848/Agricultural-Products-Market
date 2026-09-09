package com.wyt.agriculture.domain.po;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

@Data
@TableName("package_book")
@Schema(description = "套餐-农产品关联")
public class PackageProduce implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @Schema(description = "套餐ID")
    private Long packageId;

    @Schema(description = "农产品ID")
    private Long produceId;

    @Schema(description = "套餐内该农产品数量")
    private Integer produceNum;
}
