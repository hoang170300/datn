package cosplayshop.controller;

import cosplayshop.dto.response.ApiResponse;
import cosplayshop.service.BannerService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class BannerController {

    private final BannerService bannerService;

    @GetMapping("/banners")
    public ResponseEntity<ApiResponse<List<BannerService.BannerDto>>> getActive() {
        return ResponseEntity.ok(ApiResponse.success(bannerService.getActiveBanners()));
    }

    @GetMapping("/admin/banners")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Page<BannerService.BannerDto>>> getAllAdmin(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.success(bannerService.getAllForAdmin(page, size)));
    }

    @PostMapping("/admin/banners")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<BannerService.BannerDto>> create(
            @RequestBody BannerService.BannerRequest req) {
        return ResponseEntity.ok(ApiResponse.success("Đã tạo banner", bannerService.create(req)));
    }

    @PutMapping("/admin/banners/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<BannerService.BannerDto>> update(
            @PathVariable Long id, @RequestBody BannerService.BannerRequest req) {
        return ResponseEntity.ok(ApiResponse.success("Đã cập nhật", bannerService.update(id, req)));
    }

    @PatchMapping("/admin/banners/{id}/toggle")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> toggle(@PathVariable Long id) {
        bannerService.toggle(id);
        return ResponseEntity.ok(ApiResponse.success("Đã cập nhật trạng thái", null));
    }

    @DeleteMapping("/admin/banners/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        bannerService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Đã xóa banner", null));
    }
}
