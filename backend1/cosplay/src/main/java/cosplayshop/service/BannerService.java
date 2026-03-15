package cosplayshop.service;


import cosplayshop.entity.Banner;
import cosplayshop.exception.ResourceNotFoundException;
import cosplayshop.repository.BannerRepository;
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
public class BannerService {

    private final BannerRepository bannerRepository;

    public List<BannerDto> getActiveBanners() {
        return bannerRepository.findActiveBanners().stream().map(this::toDto).collect(Collectors.toList());
    }

    public Page<BannerDto> getAllForAdmin(int page, int size) {
        return bannerRepository.findAll(
                PageRequest.of(page, size, Sort.by("displayOrder").ascending())
        ).map(this::toDto);
    }

    @Transactional
    public BannerDto create(BannerRequest req) {
        Banner b = Banner.builder()
                .title(req.getTitle()).description(req.getDescription())
                .imageUrl(req.getImageUrl()).linkUrl(req.getLinkUrl())
                .displayOrder(req.getDisplayOrder() != null ? req.getDisplayOrder() : 0)
                .isActive(true)
                .startDate(req.getStartDate()).endDate(req.getEndDate())
                .build();
        return toDto(bannerRepository.save(b));
    }

    @Transactional
    public BannerDto update(Long id, BannerRequest req) {
        Banner b = bannerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Banner không tồn tại"));
        b.setTitle(req.getTitle()); b.setDescription(req.getDescription());
        b.setImageUrl(req.getImageUrl()); b.setLinkUrl(req.getLinkUrl());
        if (req.getDisplayOrder() != null) b.setDisplayOrder(req.getDisplayOrder());
        if (req.getIsActive() != null) b.setIsActive(req.getIsActive());
        b.setStartDate(req.getStartDate()); b.setEndDate(req.getEndDate());
        return toDto(bannerRepository.save(b));
    }

    @Transactional
    public void toggle(Long id) {
        Banner b = bannerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Banner không tồn tại"));
        b.setIsActive(!b.getIsActive());
        bannerRepository.save(b);
    }

    @Transactional
    public void delete(Long id) {
        bannerRepository.delete(bannerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Banner không tồn tại")));
    }

    private BannerDto toDto(Banner b) {
        return BannerDto.builder()
                .id(b.getId()).title(b.getTitle()).description(b.getDescription())
                .imageUrl(b.getImageUrl()).linkUrl(b.getLinkUrl())
                .displayOrder(b.getDisplayOrder()).isActive(b.getIsActive())
                .startDate(b.getStartDate()).endDate(b.getEndDate())
                .build();
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class BannerDto {
        private Long id;
        private String title, description, imageUrl, linkUrl;
        private Integer displayOrder;
        private Boolean isActive;
        private LocalDateTime startDate, endDate;
    }

    @Data
    public static class BannerRequest {
        private String title, description, imageUrl, linkUrl;
        private Integer displayOrder;
        private Boolean isActive;
        private LocalDateTime startDate, endDate;
    }
}