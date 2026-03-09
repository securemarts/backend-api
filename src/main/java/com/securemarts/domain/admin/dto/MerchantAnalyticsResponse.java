package com.securemarts.domain.admin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
@Schema(description = "Merchant analytics report")
public class MerchantAnalyticsResponse {

    @Schema(description = "Total merchants count")
    private long totalMerchants;

    @Schema(description = "Active merchants (ACTIVE or TRIALING)")
    private long activeMerchants;

    @Schema(description = "New merchants in period")
    private long newMerchants;

    @Schema(description = "Churn rate (0-100)")
    private double churnRate;

    @Schema(description = "Change vs previous period (e.g. 4.63)")
    private Double activeMerchantsChangePercent;

    @Schema(description = "Trend: daily or weekly counts for chart")
    private List<TrendPoint> trend;

    @Schema(description = "Distribution by status for donut")
    private List<DistributionItem> statusDistribution;

    @Schema(description = "Recent merchant activity rows")
    private List<MerchantActivityRow> merchantActivity;

    @Data
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class TrendPoint {
        private String date;
        private long activeCount;
        private long newCount;
    }

    @Data
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class DistributionItem {
        private String label;
        private long count;
        private double percent;
    }

    @Data
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class MerchantActivityRow {
        private String merchantName;
        private String plan;
        private String signUpDate;
        private String lastActive;
        private long totalInvoices;
        @Schema(description = "Online (e-commerce) payment total")
        private String payments;
        @Schema(description = "Physical/retail (POS) sales total")
        private String offlineSales;
    }
}
