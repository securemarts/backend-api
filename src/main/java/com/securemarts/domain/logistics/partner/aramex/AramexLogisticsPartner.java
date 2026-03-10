package com.securemarts.domain.logistics.partner.aramex;

import com.securemarts.common.exception.BusinessRuleException;
import com.securemarts.domain.logistics.partner.ShipmentRequest;
import com.securemarts.domain.logistics.partner.ShipmentResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

/**
 * Aramex carrier adapter. Uses Aramex Shipments Preparation API (XML) for create and tracking API for status.
 * Configure app.logistics.aramex.* in application.yml or .env.
 * @see <a href="https://www.aramex.com/us/en/developers-solution-center/aramex-apis">Aramex APIs</a>
 */
@Component
@ConditionalOnProperty(name = "app.logistics.aramex.enabled", havingValue = "true")
@RequiredArgsConstructor
@Slf4j
public class AramexLogisticsPartner implements com.securemarts.domain.logistics.partner.LogisticsPartner {

    private static final String CARRIER_CODE = "ARAMEX";

    @Value("${app.logistics.aramex.enabled:false}")
    private boolean enabled;

    @Value("${app.logistics.aramex.base-url:https://ws.aramex.net/ShippingAPI.V2}")
    private String baseUrl;

    @Value("${app.logistics.aramex.username:}")
    private String username;

    @Value("${app.logistics.aramex.password:}")
    private String password;

    @Value("${app.logistics.aramex.account-number:}")
    private String accountNumber;

    @Value("${app.logistics.aramex.account-entity:}")
    private String accountEntity;

    @Value("${app.logistics.aramex.account-country-code:}")
    private String accountCountryCode;

    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    public String getCarrierCode() {
        return CARRIER_CODE;
    }

    @Override
    public ShipmentResult createShipment(ShipmentRequest request) {
        if (!enabled || !isConfigured()) {
            return ShipmentResult.builder()
                    .success(false)
                    .message("Aramex is not configured or enabled")
                    .build();
        }
        try {
            AramexShipmentRequest aramexReq = AramexShipmentRequest.from(request, username, password, accountNumber, accountEntity, accountCountryCode);
            String xmlRequest = AramexXmlBuilder.buildCreateShipmentRequest(aramexReq);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_XML);
            HttpEntity<String> entity = new HttpEntity<>(xmlRequest, headers);
            String url = baseUrl.replaceAll("/$", "");
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
            return AramexResponseParser.parseCreateShipmentResponse(response.getBody());
        } catch (Exception e) {
            log.warn("Aramex createShipment failed: {}", e.getMessage());
            return ShipmentResult.builder()
                    .success(false)
                    .message(e.getMessage())
                    .build();
        }
    }

    @Override
    public String getStatus(String externalShipmentId) {
        if (!enabled || !isConfigured()) {
            throw new BusinessRuleException("Aramex is not configured or enabled");
        }
        try {
            String xmlRequest = AramexXmlBuilder.buildTrackShipmentRequest(externalShipmentId, username, password, accountNumber, accountEntity, accountCountryCode);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_XML);
            HttpEntity<String> entity = new HttpEntity<>(xmlRequest, headers);
            String url = baseUrl.replaceAll("/$", "");
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
            return AramexResponseParser.parseTrackingStatus(response.getBody());
        } catch (Exception e) {
            log.warn("Aramex getStatus failed for {}: {}", externalShipmentId, e.getMessage());
            throw new BusinessRuleException("Aramex tracking failed: " + e.getMessage());
        }
    }

    @Override
    public void cancelShipment(String externalShipmentId) {
        if (!enabled || !isConfigured()) {
            throw new BusinessRuleException("Aramex is not configured or enabled");
        }
        try {
            String xmlRequest = AramexXmlBuilder.buildCancelShipmentRequest(externalShipmentId, username, password, accountNumber, accountEntity, accountCountryCode);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_XML);
            HttpEntity<String> entity = new HttpEntity<>(xmlRequest, headers);
            String url = baseUrl.replaceAll("/$", "");
            restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
        } catch (Exception e) {
            log.warn("Aramex cancelShipment failed for {}: {}", externalShipmentId, e.getMessage());
            throw new BusinessRuleException("Aramex cancel failed: " + e.getMessage());
        }
    }

    private boolean isConfigured() {
        return username != null && !username.isBlank()
                && password != null && !password.isBlank()
                && accountNumber != null && !accountNumber.isBlank()
                && accountEntity != null && !accountEntity.isBlank()
                && accountCountryCode != null && !accountCountryCode.isBlank();
    }
}
