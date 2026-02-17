package com.securemarts.domain.places.controller;

import com.securemarts.domain.places.GooglePlacesClient;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Proxies Google Places API (New): autocomplete and place details.
 * API key is kept server-side. Use for address/location search in the customer app.
 */
@RestController
@RequestMapping("/places")
@RequiredArgsConstructor
@Tag(name = "Places", description = "Google Places API (New): autocomplete and place details (server-proxy)")
public class PlacesController {

    private final GooglePlacesClient googlePlacesClient;

    @GetMapping("/autocomplete")
    @Operation(summary = "Place autocomplete", description = "Places API (New). Returns suggestions[].placePrediction with placeId and text. Optional: sessionToken (billing), includedRegionCodes (e.g. ng or ng,us), languageCode (e.g. en).")
    public ResponseEntity<Map<String, Object>> autocomplete(
            @RequestParam String input,
            @RequestParam(required = false) String sessionToken,
            @RequestParam(required = false) String includedRegionCodes,
            @RequestParam(required = false) String languageCode) {
        Map<String, Object> body = googlePlacesClient.autocomplete(input, sessionToken, includedRegionCodes, languageCode);
        return ResponseEntity.ok(body);
    }

    @GetMapping("/details")
    @Operation(summary = "Place details", description = "Places API (New). Get place by place_id (from autocomplete). Optional fieldMask: comma-separated (e.g. id,displayName,formattedAddress,location).")
    public ResponseEntity<Map<String, Object>> placeDetails(
            @RequestParam(name = "place_id") String placeId,
            @RequestParam(required = false) String fieldMask) {
        Map<String, Object> body = googlePlacesClient.placeDetails(placeId, fieldMask);
        return ResponseEntity.ok(body);
    }
}
