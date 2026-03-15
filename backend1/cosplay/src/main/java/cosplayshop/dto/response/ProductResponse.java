package cosplayshop.dto.response;

import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductResponse {

    private Long id;
    private String name;
    private String slug;
    private String description;
    private String characterName;

    private Long categoryId;
    private String categoryName;
    private Long seriesId;
    private String seriesName;

    private String productType;
    private BigDecimal salePrice;
    private BigDecimal rentalPricePerDay;
    private BigDecimal depositAmount;
    private String primaryImageUrl;

    private Boolean isHot;
    private Boolean isNew;
    private Boolean isActive;
    private BigDecimal avgRating;
    private Integer reviewCount;
    private Integer viewsCount;

    // Chỉ có trong detail response
    private List<VariantDto> variants;
    private List<ImageDto> images;

    // ── FIX: dùng @Data @NoArgsConstructor @AllArgsConstructor thay vì @Builder
    //    để tránh lỗi "incompatible types: inference variable T" khi dùng .stream().map() ──
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class VariantDto {
        private Long id;
        private String size;
        private String color;
        private Integer stockQuantity;
        private Integer rentalQuantity;
        private BigDecimal price;
        private Boolean isActive;
        // Không có sku — đã bỏ khỏi entity ProductVariant
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ImageDto {
        private Long id;
        private String imageUrl;
        private String altText;
        private Boolean isPrimary;
    }
}