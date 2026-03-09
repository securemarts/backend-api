package com.securemarts.domain.rating.service;

import com.securemarts.common.dto.PageResponse;
import com.securemarts.common.exception.ResourceNotFoundException;
import com.securemarts.domain.auth.repository.UserRepository;
import com.securemarts.domain.onboarding.repository.StoreRepository;
import com.securemarts.domain.rating.dto.StoreRatingResponse;
import com.securemarts.domain.rating.dto.StoreReviewItemResponse;
import com.securemarts.domain.rating.dto.SubmitRatingRequest;
import com.securemarts.domain.rating.entity.StoreRating;
import com.securemarts.domain.rating.repository.StoreRatingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class StoreRatingService {

    private static final String REVIEWER_LABEL = "Customer";

    private final StoreRatingRepository storeRatingRepository;
    private final UserRepository userRepository;
    private final StoreRepository storeRepository;

    @Transactional
    public StoreRatingResponse upsert(String userPublicId, String storePublicId, SubmitRatingRequest request) {
        var user = userRepository.findByPublicId(userPublicId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userPublicId));
        var store = storeRepository.findByPublicId(storePublicId)
                .orElseThrow(() -> new ResourceNotFoundException("Store", storePublicId));

        StoreRating rating = storeRatingRepository.findByUserPublicIdAndStorePublicId(userPublicId, storePublicId)
                .orElseGet(() -> {
                    StoreRating r = new StoreRating();
                    r.setUser(user);
                    r.setStore(store);
                    return r;
                });
        rating.setScore(request.getScore().shortValue());
        rating.setComment(request.getComment());
        rating = storeRatingRepository.save(rating);
        return toRatingResponse(rating);
    }

    @Transactional(readOnly = true)
    public Optional<StoreRatingResponse> getMyRating(String userPublicId, String storePublicId) {
        return storeRatingRepository.findByUserPublicIdAndStorePublicId(userPublicId, storePublicId)
                .map(this::toRatingResponse);
    }

    @Transactional(readOnly = true)
    public double getAverageRating(Long storeId) {
        Double avg = storeRatingRepository.getAverageScoreByStoreId(storeId);
        return avg != null ? avg : 0.0;
    }

    @Transactional(readOnly = true)
    public long getRatingCount(Long storeId) {
        return storeRatingRepository.countByStoreId(storeId);
    }

    @Transactional(readOnly = true)
    public PageResponse<StoreReviewItemResponse> listStoreReviews(String storePublicId, Pageable pageable) {
        var store = storeRepository.findByPublicId(storePublicId)
                .orElseThrow(() -> new ResourceNotFoundException("Store", storePublicId));
        Page<StoreRating> page = storeRatingRepository.findAllByStoreId(store.getId(), pageable);
        return PageResponse.of(page.map(this::toReviewItemResponse));
    }

    private StoreRatingResponse toRatingResponse(StoreRating r) {
        return StoreRatingResponse.builder()
                .publicId(r.getPublicId())
                .storePublicId(r.getStore().getPublicId())
                .score(r.getScore().intValue())
                .comment(r.getComment())
                .createdAt(r.getCreatedAt())
                .updatedAt(r.getUpdatedAt())
                .build();
    }

    private StoreReviewItemResponse toReviewItemResponse(StoreRating r) {
        return StoreReviewItemResponse.builder()
                .score(r.getScore().intValue())
                .comment(r.getComment())
                .createdAt(r.getCreatedAt())
                .reviewerLabel(REVIEWER_LABEL)
                .build();
    }
}
