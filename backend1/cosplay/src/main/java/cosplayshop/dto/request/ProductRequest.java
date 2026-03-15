package cosplayshop.dto.request;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class ProductRequest {

    private String name;
    private String slug;          // ← phải có
    private String description;
    private String characterName;

    private Long categoryId;
    private Long seriesId;
    private String productType;   // SALE | RENTAL | BOTH

    private BigDecimal salePrice;
    private BigDecimal rentalPricePerDay;
    private BigDecimal depositAmount;

    // FIX: Lombok @Data tạo getter isHot() cho Boolean isHot
    // nhưng với field tên "isXxx" đôi khi cần dùng getter "getIsHot()"
    // → Đổi tên field thành hot/newProduct/active để tránh nhầm lẫn
    // HOẶC giữ nguyên nhưng service gọi đúng getter
    private Boolean isHot;
    private Boolean isNew;
    private Boolean isActive;

    private List<VariantRequest> variants;
    private List<String> imageUrls;  // ← phải có

    @Data
    public static class VariantRequest {
        private String size;
        private String color;
        private String sku;
        private Integer stockQuantity;
        private Integer rentalQuantity;
        private BigDecimal price;
        private Boolean isActive;
    }

    // Helper getters để tránh xung đột tên isXxx với Lombok
    public String getSlug() { return slug; }
    public List<String> getImageUrls() { return imageUrls; }
    public Boolean getIsNew() { return isNew; }
    public Boolean getIsActive() { return isActive; }
    public Boolean getIsHot() { return isHot; }
}