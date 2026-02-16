package com.securemarts.domain.onboarding.controller;

import com.securemarts.domain.onboarding.dto.SubscriptionPlanPublicResponse;
import com.securemarts.domain.onboarding.repository.SubscriptionPlanLimitRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Comparator;
import java.util.List;
import java.util.stream.StreamSupport;

@RestController
@RequestMapping("/public")
@RequiredArgsConstructor
@Tag(name = "Public - Subscription plans", description = "Unauthenticated; for landing page pricing/features")
public class PublicSubscriptionController {

    private final SubscriptionPlanLimitRepository subscriptionPlanLimitRepository;

    @GetMapping("/subscription-plans")
    @Operation(summary = "List subscription plans", description = "Returns all plans (Basic, Pro, Enterprise) with limits and features for landing page")
    public ResponseEntity<List<SubscriptionPlanPublicResponse>> listPlans() {
        List<SubscriptionPlanPublicResponse> plans = StreamSupport.stream(subscriptionPlanLimitRepository.findAll().spliterator(), false)
                .sorted(Comparator.comparing(l -> {
                    String p = l.getPlan();
                    if ("BASIC".equals(p)) return 0;
                    if ("PRO".equals(p)) return 1;
                    return 2;
                }))
                .map(SubscriptionPlanPublicResponse::from)
                .toList();
        return ResponseEntity.ok(plans);
    }
}
