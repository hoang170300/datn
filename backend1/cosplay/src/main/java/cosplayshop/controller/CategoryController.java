package cosplayshop.controller;

import cosplayshop.dto.response.ApiResponse;
import cosplayshop.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping("/categories")
    public ResponseEntity<ApiResponse<List<CategoryService.CategoryDto>>> getAll() {
        return ResponseEntity.ok(ApiResponse.success(categoryService.getAll()));
    }

    @GetMapping("/categories/{id}")
    public ResponseEntity<ApiResponse<CategoryService.CategoryDto>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(categoryService.getById(id)));
    }

    @GetMapping("/admin/categories")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<CategoryService.CategoryDto>>> getAllForAdmin() {
        return ResponseEntity.ok(ApiResponse.success(categoryService.getAllForAdmin()));
    }

    @PostMapping("/admin/categories")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<CategoryService.CategoryDto>> create(
            @RequestBody CategoryService.CategoryRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Đã tạo danh mục", categoryService.create(request)));
    }

    @PutMapping("/admin/categories/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<CategoryService.CategoryDto>> update(
            @PathVariable Long id,
            @RequestBody CategoryService.CategoryRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Đã cập nhật", categoryService.update(id, request)));
    }

    @PatchMapping("/admin/categories/{id}/toggle")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> toggle(@PathVariable Long id) {
        categoryService.toggle(id);
        return ResponseEntity.ok(ApiResponse.success("Đã cập nhật trạng thái", null));
    }

    @DeleteMapping("/admin/categories/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        categoryService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Đã xóa danh mục", null));
    }
}
