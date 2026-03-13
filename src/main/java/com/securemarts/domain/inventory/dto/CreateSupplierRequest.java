package com.securemarts.domain.inventory.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "Create a new supplier")
public class CreateSupplierRequest {

    @NotBlank
    @Size(max = 255)
    @Schema(description = "Supplier name", requiredMode = Schema.RequiredMode.REQUIRED, example = "Acme Wholesale Ltd")
    private String name;

    @Schema(description = "Contact email", example = "orders@acmewholesale.com")
    private String email;

    @Schema(description = "Contact phone", example = "+234 801 234 5678")
    private String phone;

    @Schema(description = "Company name", example = "Acme Wholesale Ltd")
    private String company;

    @Schema(description = "Primary address line", example = "15 Industrial Avenue")
    private String address1;

    @Schema(description = "Secondary address line", example = "Suite 200")
    private String address2;

    @Schema(description = "City", example = "Lagos")
    private String city;

    @Schema(description = "State / province", example = "Lagos")
    private String state;

    @Schema(description = "Country", example = "Nigeria")
    private String country;

    @Schema(description = "Postal / ZIP code", example = "100001")
    private String postalCode;

    @Schema(description = "Internal notes about this supplier", example = "Preferred vendor for electronics")
    private String notes;
}
