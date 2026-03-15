package cosplayshop.controller;


import cosplayshop.dto.response.ApiResponse;
import cosplayshop.service.VoucherService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequiredArgsConstructor
public class VoucherController {

    private final VoucherService voucherService;

    // ── Public: kiểm tra mã giảm giá ──────────────────
    @GetMapping("/vouchers/validate")
    public ResponseEntity<ApiResponse<VoucherService.VoucherDto>> validate(
            @RequestParam String code,
            @RequestParam BigDecimal amount) {
        return ResponseEntity.ok(ApiResponse.success(voucherService.validateVoucher(code, amount)));
    }

    // ── Admin CRUD ─────────────────────────────────────
    @GetMapping("/admin/vouchers")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Page<VoucherService.VoucherDto>>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.success(voucherService.getAll(page, size)));
    }

    @GetMapping("/admin/vouchers/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<VoucherService.VoucherDto>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(voucherService.getById(id)));
    }

    @PostMapping("/admin/vouchers")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<VoucherService.VoucherDto>> create(
            @RequestBody VoucherService.VoucherRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Tạo voucher thành công", voucherService.create(request)));
    }

    @PutMapping("/admin/vouchers/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<VoucherService.VoucherDto>> update(
            @PathVariable Long id,
            @RequestBody VoucherService.VoucherRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Cập nhật thành công", voucherService.update(id, request)));
    }

    @PatchMapping("/admin/vouchers/{id}/toggle")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> toggle(@PathVariable Long id) {
        voucherService.toggle(id);
        return ResponseEntity.ok(ApiResponse.success("Đã cập nhật trạng thái", null));
    }

    @DeleteMapping("/admin/vouchers/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        voucherService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Đã xóa voucher", null));
    }
}
