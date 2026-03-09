package com.securemarts.domain.rating.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
@Schema(description = "A store rating (my rating or single rating response)")
public class StoreRatingResponse {

    @Schema(description = "Rating record public ID")
    private String publicId;

    @Schema(description = "Store public ID")
    private String storePublicId;

    @Schema(description = "Score 1-5")
    private Integer score;

    @Schema(description = "Optional comment")
    private String comment;

    @Schema(description = "When the rating was created")
    private Instant createdAt;

    @Schema(description = "When the rating was last updated")
    private Instant updatedAt;
}
