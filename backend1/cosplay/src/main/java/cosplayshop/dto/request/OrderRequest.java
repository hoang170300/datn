package cosplayshop.dto.request;


import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class OrderRequest {
    @NotBlank
    private String paymentMethod; // COD, BANK_TRANSFER, E_WALLET

    private String voucherCode;

    @NotBlank
    private String shippingAddressStreet;
    @NotBlank
    private String shippingAddressProvince;
    @NotBlank
    private String shippingAddressDistrict;
    @NotBlank
    private String shippingAddressWard;

    @NotBlank
    private String phone;

    private String note;
}
