package com.securemarts.domain.places;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.*;

/**
 * Client for Google Places API (New): autocomplete and place details.
 * Uses the same API key as configured for Places; keep it server-side only.
 * @see <a href="https://developers.google.com/maps/documentation/places/web-service/place-autocomplete">Autocomplete (New)</a>
 * @see <a href="https://developers.google.com/maps/documentation/places/web-service/place-details">Place Details (New)</a>
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class GooglePlacesClient {

    private static final String AUTOCOMPLETE_URL = "https://places.googleapis.com/v1/places:autocomplete";
    private static final String DETAILS_BASE_URL = "https://places.googleapis.com/v1/places/";

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${app.google.places-api-key:}")
    private String placesApiKey;

    /**
     * Place Autocomplete (New): POST with JSON body. Returns suggestions with placePrediction (placeId, text).
     *
     * @param input          search text (required)
     * @param sessionToken   optional session token for billing
     * @param includedRegionCodes optional e.g. "ng" for Nigeria (array in JSON: ["ng"])
     * @param languageCode   optional e.g. "en"
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> autocomplete(String input, String sessionToken, String includedRegionCodes, String languageCode) {
        if (placesApiKey == null || placesApiKey.isBlank()) {
            log.warn("Google Places API key not configured");
            return errorResponse("suggestions", Collections.emptyList(), "REQUEST_DENIED", "Places API key not configured");
        }
        if (input == null || input.isBlank()) {
            return errorResponse("suggestions", Collections.emptyList(), "INVALID_REQUEST", "input is required");
        }
        Map<String, Object> body = new HashMap<>();
        body.put("input", input.trim());
        if (sessionToken != null && !sessionToken.isBlank()) {
            body.put("sessionToken", sessionToken);
        }
        if (includedRegionCodes != null && !includedRegionCodes.isBlank()) {
            body.put("includedRegionCodes", List.of(includedRegionCodes.trim().split(",")));
        }
        if (languageCode != null && !languageCode.isBlank()) {
            body.put("languageCode", languageCode.trim());
        }
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-Goog-Api-Key", placesApiKey);
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
        try {
            ResponseEntity<Map> response = restTemplate.exchange(AUTOCOMPLETE_URL, HttpMethod.POST, request, Map.class);
            Map<String, Object> result = response.getBody() != null ? response.getBody() : new HashMap<>();
            if (!result.containsKey("suggestions")) {
                result.put("suggestions", Collections.emptyList());
            }
            return result;
        } catch (Exception e) {
            log.warn("Places autocomplete failed: {}", e.getMessage());
            return errorResponse("suggestions", Collections.emptyList(), "UNKNOWN_ERROR", e.getMessage());
        }
    }

    /**
     * Place Details (New): GET place by ID. Requires X-Goog-FieldMask header.
     *
     * @param placeId  place ID from autocomplete (required), e.g. ChIJ...
     * @param fieldMask comma-separated fields (e.g. id,displayName,formattedAddress,location). If empty, a default set is used.
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> placeDetails(String placeId, String fieldMask) {
        if (placesApiKey == null || placesApiKey.isBlank()) {
            log.warn("Google Places API key not configured");
            return Map.of("error", Map.of("message", "Places API key not configured"));
        }
        if (placeId == null || placeId.isBlank()) {
            return Map.of("error", Map.of("message", "place_id is required"));
        }
        String url = DETAILS_BASE_URL + placeId.trim();
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Goog-Api-Key", placesApiKey);
        String mask = (fieldMask != null && !fieldMask.isBlank())
                ? fieldMask.trim()
                : "id,displayName,formattedAddress,location,addressComponents,nationalPhoneNumber,internationalPhoneNumber";
        headers.set("X-Goog-FieldMask", mask);
        HttpEntity<Void> entity = new HttpEntity<>(headers);
        try {
            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, entity, Map.class);
            return response.getBody() != null ? response.getBody() : Map.of();
        } catch (Exception e) {
            log.warn("Places details failed: {}", e.getMessage());
            return Map.of("error", Map.of("message", e.getMessage()));
        }
    }

    private static Map<String, Object> errorResponse(String listKey, List<?> emptyList, String status, String message) {
        Map<String, Object> m = new HashMap<>();
        m.put(listKey, emptyList);
        m.put("status", status);
        m.put("error_message", message);
        return m;
    }
}
