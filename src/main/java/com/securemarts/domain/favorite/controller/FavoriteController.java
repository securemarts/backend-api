package com.securemarts.domain.favorite.controller;

import com.securemarts.common.dto.PageResponse;
import com.securemarts.domain.favorite.dto.AddFavoriteRequest;
import com.securemarts.domain.favorite.dto.FavoriteCheckResponse;
import com.securemarts.domain.favorite.dto.FavoriteResponse;
import com.securemarts.domain.favorite.service.FavoriteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/me/favorites")
@RequiredArgsConstructor
@Tag(name = "Favorites", description = "Customer favorites (wishlist): add, list, check, remove")
@SecurityRequirement(name = "bearerAuth")
public class FavoriteController {

    private final FavoriteService favoriteService;

    @PostMapping
    @Operation(summary = "Add to favorites", description = "Add a product (from a store) to the current user's favorites. Idempotent: returns existing if already added.")
    public ResponseEntity<FavoriteResponse> add(
            @AuthenticationPrincipal String userPublicId,
            @Valid @RequestBody AddFavoriteRequest request) {
        FavoriteResponse response = favoriteService.add(
                userPublicId,
                request.getStorePublicId(),
                request.getProductPublicId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @Operation(summary = "List my favorites", description = "Paginated list. Optionally filter by storePublicId.")
    public ResponseEntity<PageResponse<FavoriteResponse>> list(
            @AuthenticationPrincipal String userPublicId,
            @RequestParam(required = false) String storePublicId,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(favoriteService.list(userPublicId, storePublicId, pageable));
    }

    @GetMapping("/check")
    @Operation(summary = "Check if product is favorite", description = "Returns whether the given store+product is in the user's favorites and the favoritePublicId if so.")
    public ResponseEntity<FavoriteCheckResponse> check(
            @AuthenticationPrincipal String userPublicId,
            @RequestParam String storePublicId,
            @RequestParam String productPublicId) {
        return ResponseEntity.ok(favoriteService.check(userPublicId, storePublicId, productPublicId));
    }

    @DeleteMapping("/{favoritePublicId}")
    @Operation(summary = "Remove from favorites", description = "Remove a favorite by its public ID.")
    public ResponseEntity<Void> remove(
            @AuthenticationPrincipal String userPublicId,
            @PathVariable String favoritePublicId) {
        favoriteService.remove(userPublicId, favoritePublicId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping
    @Operation(summary = "Remove by store and product", description = "Remove the favorite for the given store+product.")
    public ResponseEntity<Void> removeByStoreAndProduct(
            @AuthenticationPrincipal String userPublicId,
            @RequestParam String storePublicId,
            @RequestParam String productPublicId) {
        favoriteService.removeByStoreAndProduct(userPublicId, storePublicId, productPublicId);
        return ResponseEntity.noContent().build();
    }
}
