package cosplayshop.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ChangePasswordRequest {
    @NotBlank
    private String oldPassword;

    @NotBlank @jakarta.validation.constraints.Size(min = 6)
    private String newPassword;
}
