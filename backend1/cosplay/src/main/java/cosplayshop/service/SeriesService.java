package cosplayshop.service;


import cosplayshop.entity.Series;
import cosplayshop.exception.ResourceNotFoundException;
import cosplayshop.repository.SeriesRepository;
import lombok.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SeriesService {

    private final SeriesRepository seriesRepository;

    public List<SeriesDto> getAll() {
        return seriesRepository.findByIsActiveTrueOrderByNameAsc()
                .stream().map(this::toDto).collect(Collectors.toList());
    }

    public List<SeriesDto> getAllForAdmin() {
        return seriesRepository.findAll().stream().map(this::toDto).collect(Collectors.toList());
    }

    public SeriesDto getById(Long id) {
        return toDto(seriesRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Series không tồn tại")));
    }

    @Transactional
    public SeriesDto create(SeriesRequest req) {
        if (seriesRepository.existsBySlug(req.getSlug())) {
            throw new IllegalArgumentException("Slug đã tồn tại");
        }
        Series s = Series.builder()
                .name(req.getName()).slug(req.getSlug())
                .description(req.getDescription()).imageUrl(req.getImageUrl())
                .seriesType(req.getSeriesType() != null
                        ? Series.SeriesType.valueOf(req.getSeriesType())
                        : Series.SeriesType.ANIME)
                .isActive(true).build();
        return toDto(seriesRepository.save(s));
    }

    @Transactional
    public SeriesDto update(Long id, SeriesRequest req) {
        Series s = seriesRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Series không tồn tại"));
        s.setName(req.getName());
        s.setDescription(req.getDescription());
        s.setImageUrl(req.getImageUrl());
        if (req.getSeriesType() != null)
            s.setSeriesType(Series.SeriesType.valueOf(req.getSeriesType()));
        if (req.getIsActive() != null) s.setIsActive(req.getIsActive());
        return toDto(seriesRepository.save(s));
    }

    @Transactional
    public void toggle(Long id) {
        Series s = seriesRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Series không tồn tại"));
        s.setIsActive(!s.getIsActive());
        seriesRepository.save(s);
    }

    @Transactional
    public void delete(Long id) {
        seriesRepository.delete(seriesRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Series không tồn tại")));
    }

    private SeriesDto toDto(Series s) {
        return SeriesDto.builder()
                .id(s.getId()).name(s.getName()).slug(s.getSlug())
                .description(s.getDescription()).imageUrl(s.getImageUrl())
                .seriesType(s.getSeriesType() != null ? s.getSeriesType().name() : null)
                .isActive(s.getIsActive()).build();
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class SeriesDto {
        private Long id;
        private String name, slug, description, imageUrl, seriesType;
        private Boolean isActive;
    }

    @Data
    public static class SeriesRequest {
        private String name, slug, description, imageUrl, seriesType;
        private Boolean isActive;
    }
}

