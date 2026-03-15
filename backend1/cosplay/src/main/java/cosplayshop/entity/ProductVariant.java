package cosplayshop.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "product_variants")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductVariant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false, length = 20, columnDefinition = "NVARCHAR(20)")
    private String size;

    @Column(length = 50, columnDefinition = "NVARCHAR(50)")
    private String color;

    // SKU nullable - unique chỉ khi NOT NULL (filtered index trên DB)
    @Column(length = 100, unique = false)
    private String sku;

    // ── FIX: @Builder.Default ──────────────────────────
    @Builder.Default
    @Column(name = "stock_quantity")
    private Integer stockQuantity = 0;

    @Builder.Default
    @Column(name = "rental_quantity")
    private Integer rentalQuantity = 0;

    @Builder.Default
    @Column(name = "is_active")
    private Boolean isActive = true;

    // Giá riêng cho variant (nullable = dùng giá của product)
    @Column(precision = 15, scale = 2)
    private BigDecimal price;
}