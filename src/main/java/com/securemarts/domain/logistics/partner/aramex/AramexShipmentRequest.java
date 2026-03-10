package com.securemarts.domain.logistics.partner.aramex;

import com.securemarts.domain.logistics.partner.ShipmentRequest;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class AramexShipmentRequest {

    private String username;
    private String password;
    private String accountNumber;
    private String accountEntity;
    private String accountCountryCode;
    private String reference;
    private String pickupAddress;
    private BigDecimal pickupLat;
    private BigDecimal pickupLng;
    private String deliveryAddress;
    private BigDecimal deliveryLat;
    private BigDecimal deliveryLng;
    private String shipperName;
    private String shipperPhone;
    private String consigneeName;
    private String consigneePhone;
    private BigDecimal weightKg;
    private String parcelDescription;

    public static AramexShipmentRequest from(ShipmentRequest req, String username, String password,
                                             String accountNumber, String accountEntity, String accountCountryCode) {
        return AramexShipmentRequest.builder()
                .username(username)
                .password(password)
                .accountNumber(accountNumber)
                .accountEntity(accountEntity)
                .accountCountryCode(accountCountryCode)
                .reference(req.getOrderPublicId() != null ? req.getOrderPublicId() : (req.getShipmentId() != null ? "S" + req.getShipmentId() : ""))
                .pickupAddress(req.getPickupAddress())
                .pickupLat(req.getPickupLat())
                .pickupLng(req.getPickupLng())
                .deliveryAddress(req.getDeliveryAddress())
                .deliveryLat(req.getDeliveryLat())
                .deliveryLng(req.getDeliveryLng())
                .weightKg(req.getWeightKg() != null ? req.getWeightKg() : BigDecimal.ONE)
                .parcelDescription(req.getParcelDescription() != null ? req.getParcelDescription() : "Shipment")
                .build();
    }
}
