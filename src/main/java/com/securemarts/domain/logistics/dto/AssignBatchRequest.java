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
    @Schema(description = "List of delivery order public IDs to assign", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<String> deliveryOrderPublicIds;

    @NotNull
    @Schema(description = "Rider public ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private String riderPublicId;
}
