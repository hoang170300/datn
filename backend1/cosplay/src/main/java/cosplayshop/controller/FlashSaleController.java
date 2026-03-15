package cosplayshop.controller;


import cosplayshop.dto.response.ApiResponse;
import cosplayshop.service.FlashSaleService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class FlashSaleController {

    private final FlashSaleService flashSaleService;

    @GetMapping("/flash-sales/active")
    public ResponseEntity<ApiResponse<List<FlashSaleService.FlashSaleDto>>> getActive() {
        return ResponseEntity.ok(ApiResponse.success(flashSaleService.getActive()));
    }

    @GetMapping("/admin/flash-sales")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Page<FlashSaleService.FlashSaleDto>>> getAllAdmin(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(ApiResponse.success(flashSaleService.getAllForAdmin(page, size)));
    }

    @PostMapping("/admin/flash-sales")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<FlashSaleService.FlashSaleDto>> create(
            @RequestBody FlashSaleService.FlashSaleRequest req) {
        return ResponseEntity.ok(ApiResponse.success("Đã tạo flash sale", flashSaleService.create(req)));
    }

    @PutMapping("/admin/flash-sales/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<FlashSaleService.FlashSaleDto>> update(
            @PathVariable Long id, @RequestBody FlashSaleService.FlashSaleRequest req) {
        return ResponseEntity.ok(ApiResponse.success("Đã cập nhật", flashSaleService.update(id, req)));
    }

    @PatchMapping("/admin/flash-sales/{id}/toggle")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> toggle(@PathVariable Long id) {
        flashSaleService.toggle(id);
        return ResponseEntity.ok(ApiResponse.success("Đã cập nhật trạng thái", null));
    }

    @DeleteMapping("/admin/flash-sales/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        flashSaleService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Đã xóa flash sale", null));
    }
}
