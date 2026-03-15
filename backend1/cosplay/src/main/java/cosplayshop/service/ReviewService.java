package cosplayshop.service;


import cosplayshop.dto.request.ReviewRequest;
import cosplayshop.dto.response.*;
import cosplayshop.entity.*;
import cosplayshop.exception.ResourceNotFoundException;
import cosplayshop.repository.*;
import lombok.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ReviewService {
    private final ReviewRepository reviewRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    public PageResponse<ReviewDto> getProductReviews(Long productId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return PageResponse.of(
                reviewRepository.findByProductIdAndIsApprovedTrueOrderByCreatedAtDesc(productId, pageable)                        .map(this::toDto));
    }

    @Transactional
    public ReviewDto addReview(Long userId, ReviewRequest request) {
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Sản phẩm không tồn tại"));
        User user = userRepository.findById(userId).orElseThrow();

        if (reviewRepository.existsByUserIdAndProductId(userId, request.getProductId())) {
            throw new IllegalArgumentException("Bạn đã đánh giá sản phẩm này rồi");
        }

        Review review = Review.builder()
                .product(product)
                .user(user)
                .rating(request.getRating())
                .title(request.getTitle())
                .comment(request.getComment())
                .isApproved(true)
                .build();

        review = reviewRepository.save(review);

        // Cập nhật avg rating
        Double avg = reviewRepository.getAvgRatingByProductId(product.getId());
        Long count = reviewRepository.getReviewCountByProductId(product.getId());
        product.setAvgRating(avg != null ? BigDecimal.valueOf(avg) : BigDecimal.ZERO);
        product.setReviewCount(count != null ? count.intValue() : 0);
        productRepository.save(product);

        return toDto(review);
    }

    private ReviewDto toDto(Review r) {
        return ReviewDto.builder()
                .id(r.getId())
                .userId(r.getUser().getId())
                .userFullName(r.getUser().getFullName())
                .rating(r.getRating())
                .title(r.getTitle())
                .comment(r.getComment())
                .createdAt(r.getCreatedAt())
                .build();
    }

    @Data @Builder
    public static class ReviewDto {
        private Long id;
        private Long userId;
        private String userFullName;
        private Integer rating;
        private String title;
        private String comment;
        private LocalDateTime createdAt;
    }
}
