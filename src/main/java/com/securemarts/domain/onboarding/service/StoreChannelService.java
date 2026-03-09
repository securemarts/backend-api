package com.securemarts.domain.onboarding.service;

import com.securemarts.common.exception.BusinessRuleException;
import com.securemarts.domain.onboarding.entity.Store;
import org.springframework.stereotype.Service;

@Service
public class StoreChannelService {

    /**
     * Throws if the store does not support online (e-commerce) sales.
     * Use before cart, checkout, and order creation.
     */
    public void ensureOnlineEnabled(Store store) {
        if (store == null || !store.isOnlineEnabled()) {
            throw new BusinessRuleException("Store does not support online sales");
        }
    }

    /**
     * Throws if the store does not support physical (retail/POS) sales.
     * Use before POS register, session, and sync operations.
     */
    public void ensureRetailEnabled(Store store) {
        if (store == null || !store.isRetailEnabled()) {
            throw new BusinessRuleException("Store does not support physical retail sales");
        }
    }
}
