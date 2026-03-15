package cosplayshop.controller;


import cosplayshop.dto.response.ApiResponse;
import cosplayshop.service.SeriesService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class SeriesController {

    private final SeriesService seriesService;

    @GetMapping("/series")
    public ResponseEntity<ApiResponse<List<SeriesService.SeriesDto>>> getAll() {
        return ResponseEntity.ok(ApiResponse.success(seriesService.getAll()));
    }

    @GetMapping("/series/{id}")
    public ResponseEntity<ApiResponse<SeriesService.SeriesDto>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(seriesService.getById(id)));
    }

    @GetMapping("/admin/series")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<SeriesService.SeriesDto>>> getAllForAdmin() {
        return ResponseEntity.ok(ApiResponse.success(seriesService.getAllForAdmin()));
    }

    @PostMapping("/admin/series")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<SeriesService.SeriesDto>> create(
            @RequestBody SeriesService.SeriesRequest req) {
        return ResponseEntity.ok(ApiResponse.success("Đã tạo series", seriesService.create(req)));
    }

    @PutMapping("/admin/series/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<SeriesService.SeriesDto>> update(
            @PathVariable Long id, @RequestBody SeriesService.SeriesRequest req) {
        return ResponseEntity.ok(ApiResponse.success("Đã cập nhật", seriesService.update(id, req)));
    }

    @PatchMapping("/admin/series/{id}/toggle")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> toggle(@PathVariable Long id) {
        seriesService.toggle(id);
        return ResponseEntity.ok(ApiResponse.success("Đã cập nhật trạng thái", null));
    }

    @DeleteMapping("/admin/series/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        seriesService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Đã xóa series", null));
    }
}
