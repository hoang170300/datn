package cosplayshop.dto.request;


import jakarta.validation.constraints.*;
import lombok.Data;
import java.time.LocalDate;

@Data
public class CartItemRequest {
    @NotNull
    private Long productVariantId;

    @Min(1)
    private Integer quantity = 1;

    private String orderType = "SALE"; // SALE or RENTAL

    private LocalDate rentalStartDate;
    private LocalDate rentalEndDate;
}