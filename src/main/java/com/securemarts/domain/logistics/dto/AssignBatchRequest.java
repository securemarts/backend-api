package com.securemarts.domain.logistics.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "Assign a batch of PENDING delivery orders to one rider")
public class AssignBatchRequest {

    @NotEmpty
    @Schema(description = "List of delivery order public IDs to assign", example = "[\"del-id-1\",\"del-id-2\"]", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<String> deliveryOrderPublicIds;

    @NotNull
    @Schema(description = "Rider public ID", example = "r1a2b3c4-d5e6-7890-abcd-ef1234567890", requiredMode = Schema.RequiredMode.REQUIRED)
    private String riderPublicId;
}
