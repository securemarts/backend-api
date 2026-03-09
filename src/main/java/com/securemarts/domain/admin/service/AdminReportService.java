package com.securemarts.domain.admin.service;

import com.securemarts.domain.admin.dto.MerchantAnalyticsResponse;
import com.securemarts.domain.admin.dto.StoreSalesSummaryResponse;
import com.securemarts.domain.admin.dto.SubscriptionAnalyticsResponse;
import com.securemarts.domain.onboarding.entity.Business;
import com.securemarts.domain.onboarding.entity.SubscriptionHistory;
import com.securemarts.domain.onboarding.entity.Store;
import com.securemarts.domain.onboarding.repository.BusinessRepository;
import com.securemarts.domain.onboarding.repository.StoreRepository;
import com.securemarts.domain.onboarding.repository.SubscriptionHistoryRepository;
import com.securemarts.domain.order.repository.OrderRepository;
import com.securemarts.domain.payment.repository.PaymentTransactionRepository;
import com.securemarts.domain.pos.repository.OfflineTransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminReportService {

    private final BusinessRepository businessRepository;
    private final SubscriptionHistoryRepository subscriptionHistoryRepository;
    private final OrderRepository orderRepository;
    private final PaymentTransactionRepository paymentTransactionRepository;
    private final OfflineTransactionRepository offlineTransactionRepository;
    private final StoreRepository storeRepository;

    @Transactional(readOnly = true)
    public MerchantAnalyticsResponse getMerchantAnalytics(String plan, String status, String periodDays) {
        int days = periodDays != null && !periodDays.isBlank() ? parseIntSafe(periodDays, 30) : 30;
        Instant from = Instant.now().atOffset(ZoneOffset.UTC).minusDays(days).toInstant();
        Instant to = Instant.now();

        long total = businessRepository.count();
        long active = businessRepository.findAll().stream()
                .filter(b -> b.getSubscriptionStatus() == Business.SubscriptionStatus.ACTIVE || b.getSubscriptionStatus() == Business.SubscriptionStatus.TRIALING)
                .count();
        long newInPeriod = businessRepository.findAll().stream()
                .filter(b -> b.getCreatedAt() != null && !b.getCreatedAt().isBefore(from) && !b.getCreatedAt().isAfter(to))
                .count();
        double churnRate = active > 0 ? 0.0 : 0.0; // placeholder: would need "cancelled in period" count

        List<MerchantAnalyticsResponse.TrendPoint> trend = buildMerchantTrend(days);
        List<MerchantAnalyticsResponse.DistributionItem> distribution = buildMerchantStatusDistribution();
        List<MerchantAnalyticsResponse.MerchantActivityRow> activity = buildMerchantActivity(plan, status, 10);

        return MerchantAnalyticsResponse.builder()
                .totalMerchants(total)
                .activeMerchants(active)
                .newMerchants(newInPeriod)
                .churnRate(churnRate)
                .activeMerchantsChangePercent(null)
                .trend(trend)
                .statusDistribution(distribution)
                .merchantActivity(activity)
                .build();
    }

    @Transactional(readOnly = true)
    public SubscriptionAnalyticsResponse getSubscriptionAnalytics(String periodDays, String plan, String status) {
        int days = periodDays != null && !periodDays.isBlank() ? parseIntSafe(periodDays, 30) : 30;

        long total = businessRepository.count();
        long paid = businessRepository.findAll().stream()
                .filter(b -> b.getSubscriptionStatus() == Business.SubscriptionStatus.ACTIVE
                        && b.getSubscriptionPlan() != Business.SubscriptionPlan.BASIC)
                .count();
        long freePlan = businessRepository.countBySubscriptionPlan(Business.SubscriptionPlan.BASIC);
        double churnRate = 0.0;

        List<SubscriptionAnalyticsResponse.SubscriptionTrendPoint> trend = buildSubscriptionTrend(days);
        List<SubscriptionAnalyticsResponse.SubscriptionDistributionItem> planDist = buildPlanDistribution();
        List<SubscriptionAnalyticsResponse.SubscriptionActivityRow> activity = buildSubscriptionActivity(10);

        return SubscriptionAnalyticsResponse.builder()
                .totalSubscriptions(total)
                .paidSubscriptions(paid)
                .freePlanCount(freePlan)
                .churnRate(churnRate)
                .paidSubscriptionsChangePercent(null)
                .trend(trend)
                .planDistribution(planDist)
                .subscriptionActivity(activity)
                .build();
    }

    @Transactional(readOnly = true)
    public StoreSalesSummaryResponse getStoreSalesSummary(Long storeId, String periodDaysParam) {
        int periodDays = periodDaysParam != null && !periodDaysParam.isBlank() ? parseIntSafe(periodDaysParam, 30) : 30;
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new com.securemarts.common.exception.ResourceNotFoundException("Store", String.valueOf(storeId)));
        Instant to = Instant.now();
        Instant from = to.atOffset(ZoneOffset.UTC).minusDays(periodDays).toInstant();

        BigDecimal onlineRevenue = paymentTransactionRepository.sumAmountByStoreIdAndStatusSuccessAndCreatedAtBetween(storeId, from, to);
        BigDecimal inStoreRevenue = offlineTransactionRepository.sumAmountByStoreIdAndSyncedAtBetween(storeId, from, to);
        if (onlineRevenue == null) onlineRevenue = BigDecimal.ZERO;
        if (inStoreRevenue == null) inStoreRevenue = BigDecimal.ZERO;
        BigDecimal totalRevenue = onlineRevenue.add(inStoreRevenue);

        List<StoreSalesSummaryResponse.SalesByDay> byDay = new ArrayList<>();
        DateTimeFormatter fmt = DateTimeFormatter.ISO_LOCAL_DATE;
        for (int i = periodDays - 1; i >= 0; i--) {
            LocalDate d = LocalDate.now().minusDays(i);
            Instant dayStart = d.atStartOfDay(ZoneOffset.UTC).toInstant();
            Instant dayEnd = d.plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant();
            BigDecimal dayOnline = paymentTransactionRepository.sumAmountByStoreIdAndStatusSuccessAndCreatedAtBetween(storeId, dayStart, dayEnd);
            BigDecimal dayInStore = offlineTransactionRepository.sumAmountByStoreIdAndSyncedAtBetween(storeId, dayStart, dayEnd);
            if (dayOnline == null) dayOnline = BigDecimal.ZERO;
            if (dayInStore == null) dayInStore = BigDecimal.ZERO;
            byDay.add(new StoreSalesSummaryResponse.SalesByDay(d.format(fmt), dayOnline, dayInStore, dayOnline.add(dayInStore)));
        }

        return StoreSalesSummaryResponse.builder()
                .storePublicId(store.getPublicId())
                .storeName(store.getName())
                .periodDays(periodDays)
                .onlineRevenue(onlineRevenue)
                .inStoreRevenue(inStoreRevenue)
                .totalRevenue(totalRevenue)
                .byDay(byDay)
                .build();
    }

    private int parseIntSafe(String s, int defaultVal) {
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            return defaultVal;
        }
    }

    private List<MerchantAnalyticsResponse.TrendPoint> buildMerchantTrend(int days) {
        List<MerchantAnalyticsResponse.TrendPoint> out = new ArrayList<>();
        DateTimeFormatter fmt = DateTimeFormatter.ISO_LOCAL_DATE;
        for (int i = days - 1; i >= 0; i--) {
            LocalDate d = LocalDate.now().minusDays(i);
            Instant dayStart = d.atStartOfDay(ZoneOffset.UTC).toInstant();
            Instant dayEnd = d.plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant();
            long newCount = businessRepository.findAll().stream()
                    .filter(b -> b.getCreatedAt() != null && !b.getCreatedAt().isBefore(dayStart) && b.getCreatedAt().isBefore(dayEnd))
                    .count();
            long activeCount = businessRepository.findAll().stream()
                    .filter(b -> (b.getSubscriptionStatus() == Business.SubscriptionStatus.ACTIVE || b.getSubscriptionStatus() == Business.SubscriptionStatus.TRIALING)
                            && b.getCreatedAt() != null && !b.getCreatedAt().isAfter(dayEnd))
                    .count();
            out.add(new MerchantAnalyticsResponse.TrendPoint(d.format(fmt), activeCount, newCount));
        }
        return out;
    }

    private List<MerchantAnalyticsResponse.DistributionItem> buildMerchantStatusDistribution() {
        Map<String, Long> byStatus = businessRepository.findAll().stream()
                .collect(Collectors.groupingBy(b -> b.getSubscriptionStatus().name(), Collectors.counting()));
        long total = byStatus.values().stream().mapToLong(Long::longValue).sum();
        return byStatus.entrySet().stream()
                .map(e -> new MerchantAnalyticsResponse.DistributionItem(
                        e.getKey(),
                        e.getValue(),
                        total > 0 ? 100.0 * e.getValue() / total : 0))
                .collect(Collectors.toList());
    }

    private List<MerchantAnalyticsResponse.MerchantActivityRow> buildMerchantActivity(String plan, String status, int limit) {
        List<Business> businesses = businessRepository.findAll(PageRequest.of(0, limit)).getContent();
        return businesses.stream().map(b -> {
            String name = b.getTradeName() != null && !b.getTradeName().isBlank() ? b.getTradeName()
                    : (b.getStores() != null && !b.getStores().isEmpty() ? b.getStores().get(0).getName() : b.getLegalName());
            List<Long> storeIds = b.getStores() != null ? b.getStores().stream().map(s -> s.getId()).toList() : List.of();
            long invoices = storeIds.isEmpty() ? 0 : orderRepository.countByStoreIdIn(storeIds);
            BigDecimal payments = storeIds.isEmpty() ? BigDecimal.ZERO : paymentTransactionRepository.sumAmountByStoreIdInAndStatusSuccess(storeIds);
            BigDecimal offlineSales = storeIds.isEmpty() ? BigDecimal.ZERO : offlineTransactionRepository.sumAmountByStoreIdInAndSyncedNotNull(storeIds);
            String signUp = b.getCreatedAt() != null ? DateTimeFormatter.ISO_LOCAL_DATE.format(b.getCreatedAt().atOffset(ZoneOffset.UTC)) : "";
            return new MerchantAnalyticsResponse.MerchantActivityRow(name, b.getSubscriptionPlan().name(), signUp, null, invoices, payments != null ? payments.toString() : "0", offlineSales != null ? offlineSales.toString() : "0");
        }).collect(Collectors.toList());
    }

    private List<SubscriptionAnalyticsResponse.SubscriptionTrendPoint> buildSubscriptionTrend(int days) {
        List<SubscriptionAnalyticsResponse.SubscriptionTrendPoint> out = new ArrayList<>();
        DateTimeFormatter fmt = DateTimeFormatter.ISO_LOCAL_DATE;
        for (int i = days - 1; i >= 0; i--) {
            LocalDate d = LocalDate.now().minusDays(i);
            out.add(new SubscriptionAnalyticsResponse.SubscriptionTrendPoint(d.format(fmt), 0, 0));
        }
        return out;
    }

    private List<SubscriptionAnalyticsResponse.SubscriptionDistributionItem> buildPlanDistribution() {
        Map<String, Long> byPlan = new HashMap<>();
        for (Business.SubscriptionPlan p : Business.SubscriptionPlan.values()) {
            byPlan.put(p.name(), businessRepository.countBySubscriptionPlan(p));
        }
        long total = byPlan.values().stream().mapToLong(Long::longValue).sum();
        return byPlan.entrySet().stream()
                .map(e -> new SubscriptionAnalyticsResponse.SubscriptionDistributionItem(
                        e.getKey(),
                        e.getValue(),
                        total > 0 ? 100.0 * e.getValue() / total : 0))
                .collect(Collectors.toList());
    }

    private List<SubscriptionAnalyticsResponse.SubscriptionActivityRow> buildSubscriptionActivity(int limit) {
        List<SubscriptionHistory> history = subscriptionHistoryRepository.findAll(PageRequest.of(0, limit * 2))
                .getContent().stream()
                .sorted(Comparator.comparing(SubscriptionHistory::getCreatedAt).reversed())
                .limit(limit)
                .toList();
        Set<Long> businessIds = history.stream().map(SubscriptionHistory::getBusinessId).collect(Collectors.toSet());
        Map<Long, Business> businessMap = businessIds.isEmpty() ? Map.of() : businessRepository.findAllById(businessIds).stream().collect(Collectors.toMap(Business::getId, b -> b));
        return history.stream().map(h -> {
            Business b = businessMap.get(h.getBusinessId());
            String name = b != null ? (b.getTradeName() != null && !b.getTradeName().isBlank() ? b.getTradeName() : (b.getStores() != null && !b.getStores().isEmpty() ? b.getStores().get(0).getName() : b.getLegalName())) : "—";
            String date = h.getCreatedAt() != null ? DateTimeFormatter.ISO_LOCAL_DATE.format(h.getCreatedAt().atOffset(ZoneOffset.UTC)) : "";
            return new SubscriptionAnalyticsResponse.SubscriptionActivityRow(name, h.getPlan(), h.getEventType(), date);
        }).collect(Collectors.toList());
    }
}
