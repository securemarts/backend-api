package com.shopper.security;

/**
 * Thread-local holder for current tenant (store/business) context.
 */
public final class CurrentTenant {

    private static final ThreadLocal<Long> STORE_ID = new ThreadLocal<>();
    private static final ThreadLocal<Long> BUSINESS_ID = new ThreadLocal<>();

    public static void setStoreId(Long storeId) {
        STORE_ID.set(storeId);
    }

    public static Long getStoreId() {
        return STORE_ID.get();
    }

    public static void setBusinessId(Long businessId) {
        BUSINESS_ID.set(businessId);
    }

    public static Long getBusinessId() {
        return BUSINESS_ID.get();
    }

    public static void clear() {
        STORE_ID.remove();
        BUSINESS_ID.remove();
    }
}
