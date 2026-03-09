package com.securemarts.domain.rating.controller;

import com.securemarts.domain.rating.dto.StoreRatingResponse;
import com.securemarts.domain.rating.dto.SubmitRatingRequest;
import com.securemarts.domain.rating.service.StoreRatingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/me/stores")
@RequiredArgsConstructor
@Tag(name = "My store ratings", description = "Submit or update my rating for a store; get my rating")
@SecurityRequirement(name = "bearerAuth")
public class MeStoreRatingController {

    private final StoreRatingService storeRatingService;

    @PutMapping("/{storePublicId}/rating")
    @Operation(summary = "Submit or update my rating", description = "Upsert rating for the store (score 1-5, optional comment). One rating per user per store.")
    public ResponseEntity<StoreRatingResponse> upsertRating(
            @AuthenticationPrincipal String userPublicId,
            @PathVariable String storePublicId,
            @Valid @RequestBody SubmitRatingRequest request) {
        StoreRatingResponse response = storeRatingService.upsert(userPublicId, storePublicId, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{storePublicId}/rating")
    @Operation(summary = "Get my rating for a store", description = "Returns the current user's rating for the store, or 404 if not rated.")
    public ResponseEntity<StoreRatingResponse> getMyRating(
            @AuthenticationPrincipal String userPublicId,
            @PathVariable String storePublicId) {
        Optional<StoreRatingResponse> opt = storeRatingService.getMyRating(userPublicId, storePublicId);
        return opt.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }
}
