package com.securemarts.domain.catalog.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "Product option (e.g. Size, Color) with values (e.g. S, M, L)")
public class ProductOptionRequest {

    @Schema(description = "Option name", example = "Size")
    private String name;

    @Schema(description = "Option values", example = "[\"S\", \"M\", \"L\"]")
    private List<String> values;
}
