package com.securemarts.domain.favorite.service;

import com.securemarts.common.dto.PageResponse;
import com.securemarts.common.exception.ResourceNotFoundException;
import com.securemarts.domain.auth.repository.UserRepository;
import com.securemarts.domain.catalog.entity.Product;
import com.securemarts.domain.catalog.entity.ProductMedia;
import com.securemarts.domain.catalog.entity.ProductVariant;
import com.securemarts.domain.favorite.dto.FavoriteCheckResponse;
import com.securemarts.domain.favorite.dto.FavoriteResponse;
import com.securemarts.domain.favorite.entity.Favorite;
import com.securemarts.domain.favorite.repository.FavoriteRepository;
import com.securemarts.domain.onboarding.entity.Store;
import com.securemarts.domain.onboarding.repository.StoreRepository;
import com.securemarts.domain.catalog.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FavoriteService {

    private final FavoriteRepository favoriteRepository;
    private final UserRepository userRepository;
    private final StoreRepository storeRepository;
    private final ProductRepository productRepository;

    @Transactional
    public FavoriteResponse add(String userPublicId, String storePublicId, String productPublicId) {
        var user = userRepository.findByPublicId(userPublicId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userPublicId));
        Store store = storeRepository.findByPublicId(storePublicId)
                .orElseThrow(() -> new ResourceNotFoundException("Store", storePublicId));
        Product product = productRepository.findByPublicIdAndStoreId(productPublicId, store.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Product", productPublicId));

        if (favoriteRepository.existsByUserPublicIdAndStorePublicIdAndProductPublicId(userPublicId, storePublicId, productPublicId)) {
            Favorite existing = favoriteRepository.findByUserPublicIdAndStorePublicIdAndProductPublicId(userPublicId, storePublicId, productPublicId).orElseThrow();
            return toResponse(existing);
        }

        Favorite favorite = new Favorite();
        favorite.setUser(user);
        favorite.setStore(store);
        favorite.setProduct(product);
        favorite = favoriteRepository.save(favorite);
        return toResponse(favorite);
    }

    @Transactional(readOnly = true)
    public PageResponse<FavoriteResponse> list(String userPublicId, String storePublicId, Pageable pageable) {
        userRepository.findByPublicId(userPublicId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userPublicId));

        Page<Favorite> page = storePublicId != null && !storePublicId.isBlank()
                ? favoriteRepository.findAllByUserPublicIdAndStorePublicId(userPublicId, storePublicId, pageable)
                : favoriteRepository.findAllByUserPublicId(userPublicId, pageable);

        return PageResponse.of(page.map(this::toResponse));
    }

    @Transactional(readOnly = true)
    public FavoriteCheckResponse check(String userPublicId, String storePublicId, String productPublicId) {
        var opt = favoriteRepository.findByUserPublicIdAndStorePublicIdAndProductPublicId(userPublicId, storePublicId, productPublicId);
        return FavoriteCheckResponse.builder()
                .favorite(opt.isPresent())
                .favoritePublicId(opt.map(Favorite::getPublicId).orElse(null))
                .build();
    }

    @Transactional
    public void remove(String userPublicId, String favoritePublicId) {
        Favorite favorite = favoriteRepository.findByPublicIdAndUserPublicId(favoritePublicId, userPublicId)
                .orElseThrow(() -> new ResourceNotFoundException("Favorite", favoritePublicId));
        favoriteRepository.delete(favorite);
    }

    @Transactional
    public void removeByStoreAndProduct(String userPublicId, String storePublicId, String productPublicId) {
        Favorite favorite = favoriteRepository.findByUserPublicIdAndStorePublicIdAndProductPublicId(userPublicId, storePublicId, productPublicId)
                .orElseThrow(() -> new ResourceNotFoundException("Favorite", "store=" + storePublicId + ", product=" + productPublicId));
        favoriteRepository.delete(favorite);
    }

    private FavoriteResponse toResponse(Favorite f) {
        Store store = f.getStore();
        Product product = f.getProduct();
        String imageUrl = product.getMedia() != null && !product.getMedia().isEmpty()
                ? product.getMedia().stream().findFirst().map(ProductMedia::getUrl).orElse(null)
                : null;
        BigDecimal price = null;
        String currency = null;
        if (product.getVariants() != null && !product.getVariants().isEmpty()) {
            ProductVariant first = product.getVariants().get(0);
            price = first.getPriceAmount();
            currency = first.getCurrency();
        }
        return FavoriteResponse.builder()
                .publicId(f.getPublicId())
                .storePublicId(store.getPublicId())
                .storeName(store.getName())
                .storeSlug(store.getDomainSlug())
                .productPublicId(product.getPublicId())
                .productTitle(product.getTitle())
                .productImageUrl(imageUrl)
                .productPrice(price)
                .productCurrency(currency)
                .createdAt(f.getCreatedAt())
                .build();
    }
}
