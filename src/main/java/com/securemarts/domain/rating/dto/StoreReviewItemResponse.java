package com.securemarts.domain.rating.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
@Schema(description = "A single review in the public store reviews list")
public class StoreReviewItemResponse {

    @Schema(description = "Rating score 1-5")
    private Integer score;

    @Schema(description = "Optional comment")
    private String comment;

    @Schema(description = "When the rating was submitted/updated")
    private Instant createdAt;

    @Schema(description = "Reviewer display label (e.g. 'Customer')")
    private String reviewerLabel;
}
