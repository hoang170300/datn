package cosplayshop.controller;

import cosplayshop.dto.response.ApiResponse;
import cosplayshop.dto.response.PageResponse;
import cosplayshop.service.PosService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin/pos")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class PosController {

    private final PosService posService;

    // ── Tìm sản phẩm nhanh ──────────────────────────
    @GetMapping("/search-products")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> searchProducts(
            @RequestParam String keyword) {
        return ResponseEntity.ok(ApiResponse.success(posService.searchProducts(keyword)));
    }

    // ── Tạo đơn offline ─────────────────────────────
    @PostMapping("/orders")
    public ResponseEntity<ApiResponse<Map<String, Object>>> createOfflineOrder(
            @RequestBody PosService.PosOrderRequest req) {
        return ResponseEntity.ok(ApiResponse.success("Tạo đơn thành công",
                posService.createOfflineOrder(req)));
    }

    // ── Danh sách đơn offline ───────────────────────
    @GetMapping("/orders")
    public ResponseEntity<ApiResponse<PageResponse<Map<String, Object>>>> getOfflineOrders(
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String status) {
        return ResponseEntity.ok(ApiResponse.success(
                posService.getOfflineOrders(page, size, status)));
    }

    // ── Chi tiết đơn offline ─────────────────────────
    @GetMapping("/orders/{id}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getOrder(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(posService.getOrderDetail(id)));
    }

    // ── Xác nhận thanh toán offline ─────────────────
    @PatchMapping("/orders/{id}/pay")
    public ResponseEntity<ApiResponse<Void>> confirmPayment(
            @PathVariable Long id,
            @RequestParam String method) { // CASH | BANK_TRANSFER
        posService.confirmPayment(id, method);
        return ResponseEntity.ok(ApiResponse.success("Đã xác nhận thanh toán", null));
    }

    // ── Xác nhận trả đồ thuê ────────────────────────
    @PatchMapping("/orders/{id}/return")
    public ResponseEntity<ApiResponse<Void>> confirmReturn(
            @PathVariable Long id,
            @RequestParam(defaultValue = "false") boolean damaged,
            @RequestParam(required = false) String note) {
        posService.confirmReturn(id, damaged, note);
        return ResponseEntity.ok(ApiResponse.success("Đã xác nhận trả đồ", null));
    }

    // ── Các đơn thuê đang hoạt động / sắp hết hạn ──
    @GetMapping("/rentals/active")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getActiveRentals() {
        return ResponseEntity.ok(ApiResponse.success(posService.getActiveRentals()));
    }

    @GetMapping("/rentals/overdue")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getOverdueRentals() {
        return ResponseEntity.ok(ApiResponse.success(posService.getOverdueRentals()));
    }

    // ── Báo cáo doanh thu ───────────────────────────
    @GetMapping("/reports/today")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getTodayReport() {
        return ResponseEntity.ok(ApiResponse.success(posService.getTodayReport()));
    }

    @GetMapping("/reports/monthly")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getMonthlyReport(
            @RequestParam(defaultValue = "0") int month,  // 0 = tháng hiện tại
            @RequestParam(defaultValue = "0") int year) {
        return ResponseEntity.ok(ApiResponse.success(posService.getMonthlyReport(month, year)));
    }
}
