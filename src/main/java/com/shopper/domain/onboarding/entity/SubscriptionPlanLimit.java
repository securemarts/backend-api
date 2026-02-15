package com.shopper.domain.onboarding.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "subscription_plan_limits", indexes = {
        @Index(name = "idx_subscription_plan_limits_plan", columnList = "plan", unique = true)
})
@Getter
@Setter
public class SubscriptionPlanLimit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 20)
    private String plan;

    @Column(name = "max_stores", nullable = false)
    private int maxStores = 1;

    @Column(name = "max_locations_per_store", nullable = false)
    private int maxLocationsPerStore = 1;

    @Column(name = "max_products", nullable = false)
    private int maxProducts = 50;

    @Column(name = "max_staff", nullable = false)
    private int maxStaff = 1;

    @Column(name = "max_price_rules", nullable = false)
    private int maxPriceRules = 1;

    @Column(name = "max_discount_codes", nullable = false)
    private int maxDiscountCodes = 1;

    @Column(name = "max_pos_registers", nullable = false)
    private int maxPosRegisters = 0;

    @Column(name = "delivery_enabled", nullable = false)
    private boolean deliveryEnabled = false;

    @Column(name = "paystack_plan_code_monthly", length = 100)
    private String paystackPlanCodeMonthly;

    @Column(name = "paystack_plan_code_annual", length = 100)
    private String paystackPlanCodeAnnual;
}
