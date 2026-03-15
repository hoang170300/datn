package cosplayshop.controller;


import cosplayshop.dto.request.ReviewRequest;
import cosplayshop.dto.response.*;
import cosplayshop.repository.UserRepository;
import cosplayshop.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;
    private final UserRepository userRepository;

    private Long getUserId(UserDetails u) {
        return userRepository.findByUsername(u.getUsername()).orElseThrow().getId();
    }

    @GetMapping("/product/{productId}")
    public ResponseEntity<ApiResponse<PageResponse<ReviewService.ReviewDto>>> getByProduct(
            @PathVariable Long productId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(ApiResponse.success(
                reviewService.getProductReviews(productId, page, size)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ReviewService.ReviewDto>> addReview(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody ReviewRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Cảm ơn bạn đã đánh giá!",
                reviewService.addReview(getUserId(userDetails), request)));
    }
}
