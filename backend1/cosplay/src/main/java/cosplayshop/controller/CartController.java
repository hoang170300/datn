package cosplayshop.controller;

import cosplayshop.dto.request.CartItemRequest;
import cosplayshop.dto.response.ApiResponse;
import cosplayshop.repository.UserRepository;
import cosplayshop.service.CartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;
    private final UserRepository userRepository;

    private Long getUserId(UserDetails u) {
        return userRepository.findByUsername(u.getUsername()).orElseThrow().getId();
    }

    @GetMapping
    public ResponseEntity<ApiResponse<CartService.CartDto>> getCart(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(ApiResponse.success(cartService.getCart(getUserId(userDetails))));
    }

    // FIX: addItem nhận CartItemRequest rồi unpack — khớp với CartService.addItem(userId, variantId, qty, type, start, end)
    @PostMapping("/items")
    public ResponseEntity<ApiResponse<CartService.CartDto>> addItem(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody CartItemRequest req) {
        Long userId = getUserId(userDetails);
        CartService.CartDto cart = cartService.addItem(
                userId,
                req.getProductVariantId(),
                req.getQuantity() != null ? req.getQuantity() : 1,
                req.getOrderType(),
                req.getRentalStartDate(),
                req.getRentalEndDate()
        );
        return ResponseEntity.ok(ApiResponse.success("Đã thêm vào giỏ hàng", cart));
    }

    @PatchMapping("/items/{itemId}")
    public ResponseEntity<ApiResponse<CartService.CartDto>> updateQuantity(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long itemId,
            @RequestParam Integer quantity) {
        return ResponseEntity.ok(ApiResponse.success(
                cartService.updateQuantity(getUserId(userDetails), itemId, quantity)));
    }

    @DeleteMapping("/items/{itemId}")
    public ResponseEntity<ApiResponse<CartService.CartDto>> removeItem(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long itemId) {
        return ResponseEntity.ok(ApiResponse.success(
                "Đã xóa sản phẩm",
                cartService.removeItem(getUserId(userDetails), itemId)));
    }

    @DeleteMapping
    public ResponseEntity<ApiResponse<CartService.CartDto>> clearCart(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(ApiResponse.success(
                "Đã xóa giỏ hàng",
                cartService.clearCart(getUserId(userDetails))));
    }
}