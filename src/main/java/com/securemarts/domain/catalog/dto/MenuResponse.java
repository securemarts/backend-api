package com.securemarts.domain.catalog.dto;

import com.securemarts.domain.catalog.entity.Menu;
import com.securemarts.domain.catalog.entity.MenuItem;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Data
@Builder
@Schema(description = "Navigation menu with nested items")
public class MenuResponse {

    @Schema(description = "Public ID", example = "e7f3ff7c-8f11-4a6a-8f47-3e5b8d9e12ab")
    private String publicId;

    @Schema(description = "Menu handle", example = "main-menu")
    private String handle;

    @Schema(description = "Display title", example = "Main Menu")
    private String title;

    @Schema(description = "Number of top-level items", example = "5")
    private int itemsCount;

    @Schema(description = "Top-level menu items (with nested children)")
    private List<MenuItemResponse> items;

    private Instant createdAt;
    private Instant updatedAt;

    public static MenuResponse from(Menu menu) {
        List<MenuItem> topLevel = menu.getItems() != null
                ? menu.getItems().stream()
                    .filter(i -> i.getParent() == null)
                    .toList()
                : List.of();

        return MenuResponse.builder()
                .publicId(menu.getPublicId())
                .handle(menu.getHandle())
                .title(menu.getTitle())
                .itemsCount(topLevel.size())
                .items(topLevel.stream()
                        .map(MenuItemResponse::from)
                        .collect(Collectors.toList()))
                .createdAt(menu.getCreatedAt())
                .updatedAt(menu.getUpdatedAt())
                .build();
    }
}
