package com.shopper.domain.discovery.service;

import com.shopper.common.dto.PageResponse;
import com.shopper.common.exception.ResourceNotFoundException;
import com.shopper.domain.discovery.dto.DeliveryEtaResponse;
import com.shopper.domain.discovery.dto.LocationAvailabilityResponse;
import com.shopper.domain.discovery.dto.LocationSummaryResponse;
import com.shopper.domain.discovery.dto.StoreDiscoveryResponse;
import com.shopper.domain.inventory.entity.InventoryItem;
import com.shopper.domain.inventory.repository.InventoryItemRepository;
import com.shopper.domain.inventory.repository.LocationRepository;
import com.shopper.domain.logistics.repository.ServiceZoneRepository;
import com.shopper.domain.onboarding.entity.Store;
import com.shopper.domain.onboarding.entity.StoreProfile;
import com.shopper.domain.onboarding.repository.StoreRepository;
import com.shopper.domain.catalog.entity.Product;
import com.shopper.domain.catalog.entity.ProductVariant;
import com.shopper.domain.catalog.repository.ProductMediaRepository;
import com.shopper.domain.catalog.repository.ProductVariantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DiscoveryService {

    private static final double EARTH_RADIUS_KM = 6371.0;

    private final StoreRepository storeRepository;
    private final LocationRepository locationRepository;
    private final InventoryItemRepository inventoryItemRepository;
    private final ProductVariantRepository productVariantRepository;
    private final ProductMediaRepository productMediaRepository;
    private final ServiceZoneRepository serviceZoneRepository;

    @Transactional(readOnly = true)
    public PageResponse<StoreDiscoveryResponse> searchStores(String q, String state, String city,
                                                             BigDecimal lat, BigDecimal lng, BigDecimal radiusKm,
                                                             boolean includeLocations,
                                                             int page, int size) {
        List<Store> stores;
        if (q != null && !q.isBlank()) {
            stores = storeRepository.findActiveByNameOrTradeName(q.trim());
        } else if (state != null && !state.isBlank() && city != null && !city.isBlank()) {
            stores = storeRepository.findActiveByProfileStateAndCity(state.trim(), city.trim());
        } else if (lat != null && lng != null) {
            stores = storeRepository.findActiveWithGeo();
        } else {
            stores = storeRepository.findByActiveTrue();
        }

        List<StoreDiscoveryResponse> list = stores.stream()
                .map(s -> toDiscoveryResponse(s, includeLocations, lat, lng))
                .filter(r -> r != null)
                .collect(Collectors.toList());

        if (lat != null && lng != null) {
            list.sort(Comparator.comparing(r -> r.getDistanceKm() != null ? r.getDistanceKm() : Double.MAX_VALUE));
            if (radiusKm != null && radiusKm.doubleValue() > 0) {
                list = list.stream().filter(r -> r.getDistanceKm() != null && r.getDistanceKm() <= radiusKm.doubleValue()).collect(Collectors.toList());
            }
        }

        int total = list.size();
        int from = Math.min(page * size, total);
        int to = Math.min(from + size, total);
        List<StoreDiscoveryResponse> pageContent = from < to ? list.subList(from, to) : Collections.emptyList();
        return PageResponse.<StoreDiscoveryResponse>builder()
                .content(pageContent)
                .page(page)
                .size(size)
                .totalElements(total)
                .totalPages((total + size - 1) / size)
                .first(page == 0)
                .last(from + size >= total)
                .build();
    }

    @Transactional(readOnly = true)
    public List<LocationSummaryResponse> listLocationsForStore(String storePublicId) {
        Store store = storeRepository.findByPublicId(storePublicId)
                .orElseThrow(() -> new ResourceNotFoundException("Store", storePublicId));
        return locationRepository.findByStoreId(store.getId()).stream()
                .map(LocationSummaryResponse::from)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public LocationAvailabilityResponse getLocationAvailability(String storePublicId, String locationPublicId, List<String> variantPublicIds) {
        Store store = storeRepository.findByPublicId(storePublicId)
                .orElseThrow(() -> new ResourceNotFoundException("Store", storePublicId));
        var location = locationRepository.findByPublicIdAndStoreId(locationPublicId, store.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Location", locationPublicId));
        List<Long> variantIds = new ArrayList<>();
        if (variantPublicIds != null && !variantPublicIds.isEmpty()) {
            variantIds = productVariantRepository.findByPublicIdIn(variantPublicIds).stream()
                    .map(ProductVariant::getId)
                    .collect(Collectors.toList());
        }
        List<InventoryItem> items;
        if (variantIds.isEmpty()) {
            items = inventoryItemRepository.findByLocationIdWithVariantAndProduct(location.getId());
        } else {
            items = inventoryItemRepository.findByLocationIdAndVariantIdInWithVariantAndProduct(location.getId(), variantIds);
        }
        List<Long> productIds = items.stream()
                .map(ii -> ii.getProductVariant().getProduct().getId())
                .distinct()
                .collect(Collectors.toList());
        Map<Long, String> productIdToFirstImageUrl = new HashMap<>();
        if (!productIds.isEmpty()) {
            for (var pm : productMediaRepository.findByProduct_IdInOrderByPositionAsc(productIds)) {
                productIdToFirstImageUrl.putIfAbsent(pm.getProduct().getId(), pm.getUrl());
            }
        }
        Map<Long, String> finalImageMap = productIdToFirstImageUrl;
        List<LocationAvailabilityResponse.VariantAvailabilityDto> variants = items.stream()
                .map(ii -> {
                    ProductVariant v = ii.getProductVariant();
                    Product p = v.getProduct();
                    String imageUrl = finalImageMap.get(p.getId());
                    return LocationAvailabilityResponse.VariantAvailabilityDto.builder()
                            .variantPublicId(v.getPublicId())
                            .quantityAvailable(ii.getQuantityAvailable())
                            .productTitle(p.getTitle())
                            .variantTitle(v.getTitle())
                            .sku(v.getSku())
                            .priceAmount(v.getPriceAmount())
                            .currency(v.getCurrency())
                            .imageUrl(imageUrl)
                            .build();
                })
                .collect(Collectors.toList());
        return LocationAvailabilityResponse.builder()
                .locationPublicId(location.getPublicId())
                .locationName(location.getName())
                .storePublicId(store.getPublicId())
                .variants(variants)
                .build();
    }

    @Transactional(readOnly = true)
    public DeliveryEtaResponse getDeliveryEta(String storePublicId, BigDecimal lat, BigDecimal lng, String city, String state) {
        storeRepository.findByPublicId(storePublicId)
                .orElseThrow(() -> new ResourceNotFoundException("Store", storePublicId));
        // Resolve service zone by city or nearest zone center by lat/lng
        var zones = serviceZoneRepository.findByActiveTrue(org.springframework.data.domain.Pageable.unpaged()).getContent();
        if (city != null && !city.isBlank()) {
            var match = zones.stream().filter(z -> city.equalsIgnoreCase(z.getCity())).findFirst();
            if (match.isPresent()) {
                return DeliveryEtaResponse.builder()
                        .estimatedHours(BigDecimal.valueOf(24))
                        .zoneOrHubName(match.get().getName())
                        .build();
            }
        }
        if (lat != null && lng != null && !zones.isEmpty()) {
            var nearest = zones.stream()
                    .filter(z -> z.getCenterLat() != null && z.getCenterLng() != null)
                    .min(Comparator.comparingDouble(z -> haversineKm(lat.doubleValue(), lng.doubleValue(), z.getCenterLat().doubleValue(), z.getCenterLng().doubleValue())));
            if (nearest.isPresent()) {
                return DeliveryEtaResponse.builder()
                        .estimatedHours(BigDecimal.valueOf(48))
                        .zoneOrHubName(nearest.get().getName())
                        .build();
            }
        }
        return DeliveryEtaResponse.builder()
                .estimatedHours(BigDecimal.valueOf(72))
                .zoneOrHubName(null)
                .build();
    }

    private StoreDiscoveryResponse toDiscoveryResponse(Store s, boolean includeLocations, BigDecimal queryLat, BigDecimal queryLng) {
        StoreProfile p = s.getProfile();
        if (p == null) return null;
        String brandName = s.getBusiness() != null ? s.getBusiness().getTradeName() : null;
        StoreDiscoveryResponse.StoreDiscoveryResponseBuilder b = StoreDiscoveryResponse.builder()
                .publicId(s.getPublicId())
                .name(s.getName())
                .domainSlug(s.getDomainSlug())
                .brandName(brandName)
                .logoUrl(p.getLogoUrl())
                .description(p.getDescription())
                .contactEmail(p.getContactEmail())
                .contactPhone(p.getContactPhone());
        if (queryLat != null && queryLng != null && p.getLatitude() != null && p.getLongitude() != null) {
            b.distanceKm(haversineKm(queryLat.doubleValue(), queryLng.doubleValue(), p.getLatitude().doubleValue(), p.getLongitude().doubleValue()));
        }
        if (includeLocations) {
            b.locations(locationRepository.findByStoreId(s.getId()).stream()
                    .map(LocationSummaryResponse::from)
                    .collect(Collectors.toList()));
        }
        return b.build();
    }

    private static double haversineKm(double lat1, double lng1, double lat2, double lng2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLng / 2) * Math.sin(dLng / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return EARTH_RADIUS_KM * c;
    }
}
