package cosplayshop.controller;

import cosplayshop.dto.response.*;
import cosplayshop.repository.UserRepository;
import cosplayshop.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    // ── Danh sách + tìm kiếm ──────────────────────────
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<ProductResponse>>> search(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long seriesId,
            @RequestParam(required = false) String productType,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc")      String sortDir,
            @RequestParam(defaultValue = "0")         int page,
            @RequestParam(defaultValue = "12")        int size) {
        return ResponseEntity.ok(ApiResponse.success(
                productService.searchProducts(keyword, categoryId, seriesId,
                        productType, minPrice, maxPrice, sortBy, sortDir, page, size)));
    }

    // ── Hot & New ─────────────────────────────────────
    @GetMapping("/hot")
    public ResponseEntity<ApiResponse<List<ProductResponse>>> hot(
            @RequestParam(defaultValue = "8") int limit) {
        return ResponseEntity.ok(ApiResponse.success(productService.getHotProducts(limit)));
    }

    @GetMapping("/new")
    public ResponseEntity<ApiResponse<List<ProductResponse>>> newProducts(
            @RequestParam(defaultValue = "8") int limit) {
        return ResponseEntity.ok(ApiResponse.success(productService.getNewProducts(limit)));
    }

    // ── Chi tiết sản phẩm ─────────────────────────────
    @GetMapping("/{slug}")
    public ResponseEntity<ApiResponse<ProductResponse>> detail(@PathVariable String slug) {
        return ResponseEntity.ok(ApiResponse.success(productService.getProductDetail(slug)));
    }

    // ── Kiểm tra khả dụng thuê ────────────────────────
    // FIX: trả về ApiResponse<Map<String,Object>> thay vì ApiResponse<List<VariantDto>>
    @GetMapping("/{productId}/rental-availability")
    public ResponseEntity<ApiResponse<Map<String, Object>>> rentalAvailability(
            @PathVariable Long productId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(ApiResponse.success(
                productService.checkRentalAvailability(productId, startDate, endDate)));
    }
}