package com.shopper.domain.catalog.dto;

import com.shopper.domain.catalog.entity.Collection;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Collection")
public class CollectionResponse {

    @Schema(description = "Public ID")
    private String publicId;

    @Schema(description = "Title")
    private String title;

    @Schema(description = "Handle")
    private String handle;

    @Schema(description = "Description")
    private String description;

    public static CollectionResponse from(Collection c) {
        CollectionResponse r = new CollectionResponse();
        r.setPublicId(c.getPublicId());
        r.setTitle(c.getTitle());
        r.setHandle(c.getHandle());
        r.setDescription(c.getDescription());
        return r;
    }
}
