package com.securemarts.domain.catalog.dto;

import com.securemarts.domain.catalog.entity.MenuItem;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.stream.Collectors;

@Data
@Builder
@Schema(description = "Menu item with nested children")
public class MenuItemResponse {

    @Schema(description = "Public ID", example = "a1b2c3d4-e5f6-7890-abcd-ef1234567890")
    private String publicId;

    @Schema(description = "Display text", example = "Summer Collection")
    private String title;

    @Schema(description = "Link type", example = "COLLECTION")
    private String type;

    @Schema(description = "Linked resource public ID", example = "f9ac848c-af80-4b7d-a21f-8ba647ac1566")
    private String resourceId;

    @Schema(description = "Resolved or external URL", example = "/collections/summer")
    private String url;

    @Schema(description = "Position among siblings", example = "0")
    private int position;

    @Schema(description = "Nested child items")
    private List<MenuItemResponse> children;

    public static MenuItemResponse from(MenuItem item) {
        return MenuItemResponse.builder()
                .publicId(item.getPublicId())
                .title(item.getTitle())
                .type(item.getType().name())
                .resourceId(item.getResourceId())
                .url(item.getUrl())
                .position(item.getPosition())
                .children(item.getChildren() != null && !item.getChildren().isEmpty()
                        ? item.getChildren().stream()
                            .map(MenuItemResponse::from)
                            .collect(Collectors.toList())
                        : List.of())
                .build();
    }
}
