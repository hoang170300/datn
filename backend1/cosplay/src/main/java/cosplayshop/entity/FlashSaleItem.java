package cosplayshop.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "flash_sale_items")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FlashSaleItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "flash_sale_id", nullable = false)
    private FlashSale flashSale;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "sale_price", nullable = false)
    private BigDecimal salePrice;

    @Column(name = "original_price")
    private BigDecimal originalPrice;

    @Builder.Default
    @Column(name = "quantity")
    private Integer quantity = 0;

    @Builder.Default
    @Column(name = "sold_quantity")
    private Integer soldQuantity = 0;

    @Builder.Default
    @Column(name = "is_active")
    private Boolean isActive = true;
}
