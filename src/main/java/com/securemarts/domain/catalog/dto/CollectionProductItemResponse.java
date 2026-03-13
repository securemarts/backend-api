package com.securemarts.domain.catalog.dto;

import com.securemarts.domain.catalog.entity.CollectionProduct;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Product item in a collection (with position)")
public class CollectionProductItemResponse {

    private String productPublicId;
    private String title;
    private String handle;
    private int position;

    public static CollectionProductItemResponse from(CollectionProduct cp) {
        CollectionProductItemResponse r = new CollectionProductItemResponse();
        r.setProductPublicId(cp.getProduct() != null ? cp.getProduct().getPublicId() : null);
        r.setTitle(cp.getProduct() != null ? cp.getProduct().getTitle() : null);
        r.setHandle(cp.getProduct() != null ? cp.getProduct().getHandle() : null);
        r.setPosition(cp.getPosition());
        return r;
    }
}
