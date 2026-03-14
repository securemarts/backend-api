package com.securemarts.domain.catalog.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "Create a navigation menu with nested items")
public class CreateMenuRequest {

    @NotBlank
    @Size(max = 100)
    @Pattern(regexp = "^[a-z0-9][a-z0-9-]*[a-z0-9]$", message = "Handle must be lowercase alphanumeric with hyphens")
    @Schema(description = "Unique handle for the menu (lowercase alphanumeric with hyphens). "
            + "Common handles: main-menu, footer, sidebar.",
            example = "main-menu", requiredMode = Schema.RequiredMode.REQUIRED)
    private String handle;

    @NotBlank
    @Size(max = 255)
    @Schema(description = "Display title of the menu", example = "Main Menu", requiredMode = Schema.RequiredMode.REQUIRED)
    private String title;

    @Valid
    @Schema(description = "Menu items (nested up to 3 levels deep)")
    private List<MenuItemRequest> items;
}
