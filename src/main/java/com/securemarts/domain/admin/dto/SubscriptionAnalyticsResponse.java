package com.securemarts.domain.admin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
@Schema(description = "Subscription analytics report")
public class SubscriptionAnalyticsResponse {

    @Schema(description = "Total subscriptions (businesses with a plan)")
    private long totalSubscriptions;

    @Schema(description = "Paid subscriptions (ACTIVE status, non-free plan)")
    private long paidSubscriptions;

    @Schema(description = "Free plan count")
    private long freePlanCount;

    @Schema(description = "Churn rate (0-100)")
    private double churnRate;

    @Schema(description = "Change vs previous period")
    private Double paidSubscriptionsChangePercent;

    @Schema(description = "Trend: new subscriptions and renewals over time")
    private List<SubscriptionTrendPoint> trend;

    @Schema(description = "Distribution by plan for donut")
    private List<SubscriptionDistributionItem> planDistribution;

    @Schema(description = "Recent subscription activity")
    private List<SubscriptionActivityRow> subscriptionActivity;

    @Data
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class SubscriptionTrendPoint {
        private String date;
        private long newSubscriptions;
        private long renewals;
    }

    @Data
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class SubscriptionDistributionItem {
        private String plan;
        private long count;
        private double percent;
    }

    @Data
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class SubscriptionActivityRow {
        private String merchantName;
        private String plan;
        private String event;
        private String date;
    }
}
