package com.securemarts.domain.logistics.partner.aramex;

import com.securemarts.domain.logistics.partner.ShipmentResult;
import lombok.extern.slf4j.Slf4j;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parses Aramex API XML responses. Adapt to actual response schema from Aramex docs.
 */
@Slf4j
public final class AramexResponseParser {

    private static final Pattern SHIPMENT_NUMBER = Pattern.compile("<(?:arr:)?ShipmentNumber[^>]*>([^<]+)</(?:arr:)?ShipmentNumber>", Pattern.CASE_INSENSITIVE);
    private static final Pattern LABEL_URL = Pattern.compile("<(?:arr:)?LabelURL[^>]*>([^<]+)</(?:arr:)?LabelURL>", Pattern.CASE_INSENSITIVE);
    private static final Pattern TRACKING_STATUS = Pattern.compile("<(?:arr:)?UpdateCode[^>]*>([^<]+)</(?:arr:)?UpdateCode>", Pattern.CASE_INSENSITIVE);
    private static final Pattern HAS_ERROR = Pattern.compile("<(?:arr:)?HasErrors[^>]*>true</(?:arr:)?HasErrors>", Pattern.CASE_INSENSITIVE);

    private AramexResponseParser() {}

    public static ShipmentResult parseCreateShipmentResponse(String xml) {
        if (xml == null || xml.isBlank()) {
            return ShipmentResult.builder().success(false).message("Empty response").build();
        }
        if (HAS_ERROR.matcher(xml).find()) {
            return ShipmentResult.builder().success(false).message("Aramex API returned errors").build();
        }
        String shipmentNumber = extractGroup(xml, SHIPMENT_NUMBER);
        String labelUrl = extractGroup(xml, LABEL_URL);
        if (shipmentNumber == null || shipmentNumber.isBlank()) {
            return ShipmentResult.builder().success(false).message("No shipment number in response").build();
        }
        return ShipmentResult.builder()
                .success(true)
                .externalShipmentId(shipmentNumber.trim())
                .labelUrl(labelUrl != null ? labelUrl.trim() : null)
                .trackingUrl(labelUrl != null ? labelUrl.trim() : null)
                .build();
    }

    public static String parseTrackingStatus(String xml) {
        if (xml == null || xml.isBlank()) return "PENDING";
        String code = extractGroup(xml, TRACKING_STATUS);
        if (code == null || code.isBlank()) return "PENDING";
        String upper = code.trim().toUpperCase();
        if (upper.contains("DELIVERED") || upper.contains("DELIVERY")) return "DELIVERED";
        if (upper.contains("PICKED") || upper.contains("PICKUP")) return "PICKED_UP";
        if (upper.contains("TRANSIT") || upper.contains("IN_TRANSIT")) return "IN_TRANSIT";
        if (upper.contains("FAIL") || upper.contains("CANCEL")) return "FAILED";
        if (upper.contains("RETURN")) return "RETURNED";
        return "IN_TRANSIT";
    }

    private static String extractGroup(String xml, Pattern pattern) {
        Matcher m = pattern.matcher(xml);
        return m.find() ? m.group(1) : null;
    }
}
