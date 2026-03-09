package com.securemarts.domain.rating.controller;

import com.securemarts.common.dto.PageResponse;
import com.securemarts.domain.rating.dto.StoreReviewItemResponse;
import com.securemarts.domain.rating.service.StoreRatingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/stores")
@RequiredArgsConstructor
@Tag(name = "Store ratings (public)", description = "Public list of reviews for a store")
public class StoreRatingsPublicController {

    private final StoreRatingService storeRatingService;

    @GetMapping("/{storePublicId}/ratings")
    @Operation(summary = "List store reviews", description = "Paginated list of ratings/reviews for the store (score, comment, createdAt, reviewer label). No auth required.")
    public ResponseEntity<PageResponse<StoreReviewItemResponse>> listStoreReviews(
            @PathVariable String storePublicId,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(storeRatingService.listStoreReviews(storePublicId, pageable));
    }
}
