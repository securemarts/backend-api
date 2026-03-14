package com.securemarts.domain.catalog.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "Menu item (supports nested children up to 3 levels deep)")
public class MenuItemRequest {

    @NotBlank
    @Size(max = 255)
    @Schema(description = "Display text for the menu link", example = "Summer Collection", requiredMode = Schema.RequiredMode.REQUIRED)
    private String title;

    @NotNull
    @Schema(description = "Type of link: COLLECTION, PRODUCT, HTTP, or FRONTPAGE",
            example = "COLLECTION", requiredMode = Schema.RequiredMode.REQUIRED)
    private String type;

    @Size(max = 36)
    @Schema(description = "Public ID of the linked resource (collection or product). Required for COLLECTION and PRODUCT types.",
            example = "f9ac848c-af80-4b7d-a21f-8ba647ac1566")
    private String resourceId;

    @Size(max = 1000)
    @Schema(description = "URL for HTTP type links. Ignored for COLLECTION/PRODUCT/FRONTPAGE types.",
            example = "https://example.com/promo")
    private String url;

    @Schema(description = "Position among siblings (0-based). Items are ordered by position ascending.", example = "0")
    private Integer position;

    @Valid
    @Schema(description = "Nested child menu items (max 3 levels deep)")
    private List<MenuItemRequest> children;
}
