package cosplayshop.service;


import cosplayshop.entity.Category;
import cosplayshop.exception.ResourceNotFoundException;
import cosplayshop.repository.CategoryRepository;
import lombok.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public List<CategoryDto> getAll() {
        return categoryRepository.findByIsActiveTrueOrderByNameAsc()
                .stream().map(this::toDto).collect(Collectors.toList());
    }

    public List<CategoryDto> getAllForAdmin() {
        return categoryRepository.findAll().stream().map(this::toDto).collect(Collectors.toList());
    }

    public CategoryDto getById(Long id) {
        return toDto(categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category không tồn tại")));
    }

    @Transactional
    public CategoryDto create(CategoryRequest req) {
        if (categoryRepository.existsBySlug(req.getSlug())) {
            throw new IllegalArgumentException("Slug đã tồn tại");
        }
        Category c = Category.builder()
                .name(req.getName())
                .slug(req.getSlug())
                .description(req.getDescription())
                .imageUrl(req.getImageUrl())
                .isActive(true)
                .build();
        return toDto(categoryRepository.save(c));
    }

    @Transactional
    public CategoryDto update(Long id, CategoryRequest req) {
        Category c = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category không tồn tại"));
        c.setName(req.getName());
        c.setDescription(req.getDescription());
        c.setImageUrl(req.getImageUrl());
        if (req.getIsActive() != null) c.setIsActive(req.getIsActive());
        return toDto(categoryRepository.save(c));
    }

    @Transactional
    public void toggle(Long id) {
        Category c = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category không tồn tại"));
        c.setIsActive(!c.getIsActive());
        categoryRepository.save(c);
    }

    @Transactional
    public void delete(Long id) {
        Category c = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category không tồn tại"));
        categoryRepository.delete(c);
    }

    private CategoryDto toDto(Category c) {
        return CategoryDto.builder()
                .id(c.getId()).name(c.getName()).slug(c.getSlug())
                .description(c.getDescription()).imageUrl(c.getImageUrl())
                .isActive(c.getIsActive()).build();
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class CategoryDto {
        private Long id;
        private String name, slug, description, imageUrl;
        private Boolean isActive;
    }

    @Data
    public static class CategoryRequest {
        private String name, slug, description, imageUrl;
        private Boolean isActive;
    }
}
