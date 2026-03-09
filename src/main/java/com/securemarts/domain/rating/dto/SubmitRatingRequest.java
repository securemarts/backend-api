package com.securemarts.domain.rating.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "Request to submit or update a store rating")
public class SubmitRatingRequest {

    @NotNull
    @Min(1)
    @Max(5)
    @Schema(description = "Rating score 1-5", requiredMode = Schema.RequiredMode.REQUIRED, minimum = "1", maximum = "5")
    private Integer score;

    @Schema(description = "Optional review comment")
    private String comment;
}
