package cosplayshop.controller;


import cosplayshop.dto.response.*;
import cosplayshop.repository.UserRepository;
import cosplayshop.service.FavoriteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/favorites")
@RequiredArgsConstructor
public class FavoriteController {

    private final FavoriteService favoriteService;
    private final UserRepository userRepository;

    private Long getUserId(UserDetails u) {
        return userRepository.findByUsername(u.getUsername()).orElseThrow().getId();
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<ProductResponse>>> getFavorites(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size) {
        return ResponseEntity.ok(ApiResponse.success(
                favoriteService.getFavorites(getUserId(userDetails), page, size)));
    }

    @PostMapping("/toggle/{productId}")
    public ResponseEntity<ApiResponse<Boolean>> toggle(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long productId) {
        boolean added = favoriteService.toggleFavorite(getUserId(userDetails), productId);
        return ResponseEntity.ok(ApiResponse.success(
                added ? "Đã thêm vào yêu thích" : "Đã bỏ yêu thích", added));
    }

    @GetMapping("/check/{productId}")
    public ResponseEntity<ApiResponse<Boolean>> check(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long productId) {
        return ResponseEntity.ok(ApiResponse.success(
                favoriteService.isFavorite(getUserId(userDetails), productId)));
    }
}
