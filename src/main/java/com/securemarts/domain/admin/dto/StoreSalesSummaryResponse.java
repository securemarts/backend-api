package com.securemarts.domain.admin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@Schema(description = "Store sales summary by channel (online vs in-store)")
public class StoreSalesSummaryResponse {

    @Schema(description = "Store public ID")
    private String storePublicId;

    @Schema(description = "Store name")
    private String storeName;

    @Schema(description = "Period length in days")
    private int periodDays;

    @Schema(description = "Total online (e-commerce) revenue in period")
    private BigDecimal onlineRevenue;

    @Schema(description = "Total in-store (POS/retail) revenue in period")
    private BigDecimal inStoreRevenue;

    @Schema(description = "Total revenue across channels")
    private BigDecimal totalRevenue;

    @Schema(description = "Daily breakdown by channel for charts")
    private List<SalesByDay> byDay;

    @Data
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class SalesByDay {
        private String date;
        private BigDecimal online;
        private BigDecimal inStore;
        private BigDecimal total;
    }
}
