package com.securemarts.domain.logistics.partner;

/**
 * Carrier-agnostic interface for creating and tracking shipments.
 * Implementations: InternalLogisticsPartner (in-house riders), SendstackAdapter, MaxAdapter, etc.
 */
public interface LogisticsPartner {

    String getCarrierCode();

    /**
     * Create shipment with the partner. Returns external ID and tracking URL (optional).
     */
    ShipmentResult createShipment(ShipmentRequest request);

    /**
     * Get current status from partner (e.g. PENDING, IN_TRANSIT, DELIVERED).
     */
    String getStatus(String externalShipmentId);

    /**
     * Cancel shipment if supported.
     */
    void cancelShipment(String externalShipmentId);
}
