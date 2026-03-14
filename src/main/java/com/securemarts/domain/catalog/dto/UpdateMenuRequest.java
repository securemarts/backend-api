package com.securemarts.domain.catalog.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "Update a navigation menu. Providing items replaces all existing items (full replace).")
public class UpdateMenuRequest {

    @Size(max = 255)
    @Schema(description = "New display title for the menu", example = "Main Navigation")
    private String title;

    @Valid
    @Schema(description = "Complete list of menu items. Replaces all existing items. Nested up to 3 levels deep.")
    private List<MenuItemRequest> items;
}
