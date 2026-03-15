package cosplayshop.dto.response;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponse {

    private Long id;
    private String orderNumber;
    private Long userId;
    private String userName;
    private String phone;

    private String status;
    private String paymentStatus;
    private String paymentMethod;

    private BigDecimal subtotalPrice;
    private BigDecimal discountAmount;
    private BigDecimal shippingFee;
    private BigDecimal finalPrice;

    private String shippingAddressStreet;
    private String shippingAddressWard;
    private String shippingAddressDistrict;
    private String shippingAddressProvince;

    private String note;
    private List<OrderItemDto> items;
    private LocalDateTime createdAt;

    // ── FIX: dùng @Data @NoArgsConstructor để tránh lỗi inference khi dùng stream ──
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderItemDto {
        private Long id;
        private String productName;
        // ── FIX: bỏ productSku — thay bằng size + color ──
        private String size;
        private String color;
        private String itemType;
        private Integer quantity;
        private BigDecimal unitPrice;
        private BigDecimal totalPrice;
        private LocalDate rentalStartDate;
        private LocalDate rentalEndDate;
        private Integer rentalDays;
    }
}