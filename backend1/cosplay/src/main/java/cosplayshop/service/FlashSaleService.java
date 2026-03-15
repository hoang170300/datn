package cosplayshop.service;

import cosplayshop.entity.FlashSale;          // ← FIX: import đúng
import cosplayshop.exception.ResourceNotFoundException;
import cosplayshop.repository.FlashSaleRepository;
import lombok.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FlashSaleService {

    private final FlashSaleRepository flashSaleRepository;

    public List<FlashSaleDto> getActive() {
        return flashSaleRepository.findActiveFlashSales(LocalDateTime.now())
                .stream().map(this::toDto).collect(Collectors.toList());
    }

    public Page<FlashSaleDto> getAllForAdmin(int page, int size) {
        return flashSaleRepository.findAll(
                PageRequest.of(page, size, Sort.by("startTime").descending())
        ).map(this::toDto);
    }

    @Transactional
    public FlashSaleDto create(FlashSaleRequest req) {
        FlashSale fs = FlashSale.builder()
                .name(req.getName())
                .startTime(req.getStartTime())
                .endTime(req.getEndTime())
                .isActive(true)
                .build();
        return toDto(flashSaleRepository.save(fs));
    }

    @Transactional
    public FlashSaleDto update(Long id, FlashSaleRequest req) {
        FlashSale fs = flashSaleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Flash sale không tồn tại"));
        fs.setName(req.getName());
        fs.setStartTime(req.getStartTime());
        fs.setEndTime(req.getEndTime());
        if (req.getIsActive() != null) fs.setIsActive(req.getIsActive());
        return toDto(flashSaleRepository.save(fs));
    }

    @Transactional
    public void toggle(Long id) {
        FlashSale fs = flashSaleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Flash sale không tồn tại"));
        fs.setIsActive(!fs.getIsActive());
        flashSaleRepository.save(fs);
    }

    @Transactional
    public void delete(Long id) {
        flashSaleRepository.delete(flashSaleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Flash sale không tồn tại")));
    }

    private FlashSaleDto toDto(FlashSale fs) {
        return FlashSaleDto.builder()
                .id(fs.getId())
                .name(fs.getName())
                .startTime(fs.getStartTime())
                .endTime(fs.getEndTime())
                .isActive(fs.getIsActive())
                .build();
    }

    // ── DTOs ──────────────────────────────────────────
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FlashSaleDto {
        private Long id;
        private String name;
        private LocalDateTime startTime;
        private LocalDateTime endTime;
        private Boolean isActive;
    }

    @Data
    public static class FlashSaleRequest {
        private String name;
        private LocalDateTime startTime;
        private LocalDateTime endTime;
        private Boolean isActive;
    }
}