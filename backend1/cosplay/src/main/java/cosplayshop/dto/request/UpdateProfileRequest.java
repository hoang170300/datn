package cosplayshop.dto.request;


import lombok.Data;

@Data
public class UpdateProfileRequest {
    private String fullName;
    private String phone;
    private String addressStreet;
    private String addressProvince;
    private String addressDistrict;
    private String addressWard;
}