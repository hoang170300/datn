package cosplayshop.service;


import cosplayshop.entity.Voucher;
import cosplayshop.exception.ResourceNotFoundException;
import cosplayshop.repository.VoucherRepository;
import lombok.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VoucherService {

    private final VoucherRepository voucherRepository;

    // ── Public ───────────────────────────────────────
    public VoucherDto validateVoucher(String code, BigDecimal orderAmount) {
        Voucher v = voucherRepository.findValidVoucher(code, orderAmount)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Mã giảm giá không hợp lệ hoặc đã hết hạn"));

        BigDecimal discountAmount = calcDiscount(v, orderAmount);
        return toDto(v, discountAmount);
    }

    // ── Admin ─────────────────────────────────────────
    public Page<VoucherDto> getAll(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return voucherRepository.findAll(pageable).map(v -> toDto(v, null));
    }

    public VoucherDto getById(Long id) {
        Voucher v = voucherRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Voucher không tồn tại"));
        return toDto(v, null);
    }

    @Transactional
    public VoucherDto create(VoucherRequest request) {
        if (voucherRepository.findByCode(request.getCode()).isPresent()) {
            throw new IllegalArgumentException("Mã voucher đã tồn tại");
        }
        Voucher v = Voucher.builder()
                .code(request.getCode().toUpperCase())
                .description(request.getDescription())
                .discountType(Voucher.DiscountType.valueOf(request.getDiscountType()))
                .discountValue(request.getDiscountValue())
                .minOrderValue(request.getMinOrderValue() != null ? request.getMinOrderValue() : BigDecimal.ZERO)
                .maxDiscountAmount(request.getMaxDiscountAmount())
                .usageLimit(request.getUsageLimit())
                .usedCount(0)
                .startDate(request.getStartDate() != null ? request.getStartDate() : LocalDateTime.now())
                .endDate(request.getEndDate())
                .isActive(true)
                .build();
        return toDto(voucherRepository.save(v), null);
    }

    @Transactional
    public VoucherDto update(Long id, VoucherRequest request) {
        Voucher v = voucherRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Voucher không tồn tại"));
        v.setDescription(request.getDescription());
        v.setDiscountType(Voucher.DiscountType.valueOf(request.getDiscountType()));
        v.setDiscountValue(request.getDiscountValue());
        v.setMinOrderValue(request.getMinOrderValue() != null ? request.getMinOrderValue() : BigDecimal.ZERO);
        v.setMaxDiscountAmount(request.getMaxDiscountAmount());
        v.setUsageLimit(request.getUsageLimit());
        v.setStartDate(request.getStartDate());
        v.setEndDate(request.getEndDate());
        return toDto(voucherRepository.save(v), null);
    }

    @Transactional
    public void toggle(Long id) {
        Voucher v = voucherRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Voucher không tồn tại"));
        v.setIsActive(!v.getIsActive());
        voucherRepository.save(v);
    }

    @Transactional
    public void delete(Long id) {
        Voucher v = voucherRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Voucher không tồn tại"));
        voucherRepository.delete(v);
    }

    // ── Helpers ───────────────────────────────────────
    private BigDecimal calcDiscount(Voucher v, BigDecimal orderAmount) {
        if (v.getDiscountType() == Voucher.DiscountType.PERCENTAGE) {
            BigDecimal d = orderAmount.multiply(v.getDiscountValue()).divide(BigDecimal.valueOf(100));
            if (v.getMaxDiscountAmount() != null) d = d.min(v.getMaxDiscountAmount());
            return d;
        }
        return v.getDiscountValue();
    }

    private VoucherDto toDto(Voucher v, BigDecimal discountAmount) {
        return VoucherDto.builder()
                .id(v.getId())
                .code(v.getCode())
                .description(v.getDescription())
                .discountType(v.getDiscountType() != null ? v.getDiscountType().name() : null)
                .discountValue(v.getDiscountValue())
                .minOrderValue(v.getMinOrderValue())
                .maxDiscountAmount(v.getMaxDiscountAmount())
                .usageLimit(v.getUsageLimit())
                .usedCount(v.getUsedCount())
                .startDate(v.getStartDate())
                .endDate(v.getEndDate())
                .isActive(v.getIsActive())
                .discountAmount(discountAmount)
                .build();
    }

    // ── Inner DTOs ────────────────────────────────────
    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class VoucherDto {
        private Long id;
        private String code;
        private String description;
        private String discountType;
        private BigDecimal discountValue;
        private BigDecimal minOrderValue;
        private BigDecimal maxDiscountAmount;
        private Integer usageLimit;
        private Integer usedCount;
        private LocalDateTime startDate;
        private LocalDateTime endDate;
        private Boolean isActive;
        private BigDecimal discountAmount; // chỉ dùng khi validate
    }

    @Data
    public static class VoucherRequest {
        private String code;
        private String description;
        private String discountType;
        private BigDecimal discountValue;
        private BigDecimal minOrderValue;
        private BigDecimal maxDiscountAmount;
        private Integer usageLimit;
        private LocalDateTime startDate;
        private LocalDateTime endDate;
    }
}