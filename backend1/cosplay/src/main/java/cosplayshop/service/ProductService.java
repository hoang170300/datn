package cosplayshop.service;

import cosplayshop.dto.request.ProductRequest;
import cosplayshop.dto.response.*;
import cosplayshop.entity.*;
import cosplayshop.exception.ResourceNotFoundException;
import cosplayshop.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductVariantRepository variantRepository;
    private final CategoryRepository categoryRepository;
    private final SeriesRepository seriesRepository;

    // ── Public: Danh sách / Tìm kiếm ─────────────────
    public PageResponse<ProductResponse> searchProducts(
            String keyword, Long categoryId, Long seriesId,
            String productType, BigDecimal minPrice, BigDecimal maxPrice,
            String sortBy, String sortDir, int page, int size) {

        String safeSortBy = List.of("salePrice","viewsCount","avgRating","createdAt","name")
                .contains(sortBy) ? sortBy : "createdAt";
        Sort sort = "asc".equalsIgnoreCase(sortDir)
                ? Sort.by(safeSortBy).ascending()
                : Sort.by(safeSortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Product> result;
        // FIX: tách query có/không có productType để tránh Hibernate 6 null enum bug
        if (productType != null && !productType.isBlank()) {
            try {
                Product.ProductType typeEnum = Product.ProductType.valueOf(productType);
                result = productRepository.searchProductsWithType(
                        typeEnum, keyword, categoryId, seriesId, minPrice, maxPrice, pageable);
            } catch (IllegalArgumentException e) {
                result = productRepository.searchProductsNoType(
                        keyword, categoryId, seriesId, minPrice, maxPrice, pageable);
            }
        } else {
            result = productRepository.searchProductsNoType(
                    keyword, categoryId, seriesId, minPrice, maxPrice, pageable);
        }
        return PageResponse.of(result.map(this::toResponse));
    }

    public List<ProductResponse> getHotProducts(int limit) {
        return productRepository.findHotProducts(PageRequest.of(0, limit))
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    public List<ProductResponse> getNewProducts(int limit) {
        return productRepository.findNewProducts(PageRequest.of(0, limit))
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    public ProductResponse getProductById(Long id) {
        Product p = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sản phẩm không tồn tại: " + id));
        return toDetailResponse(p);
    }

    @Transactional
    public ProductResponse getProductDetail(String slug) {
        Product p = productRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Sản phẩm không tồn tại: " + slug));
        p.setViewsCount(p.getViewsCount() + 1);
        productRepository.save(p);
        return toDetailResponse(p);
    }

    public Map<String, Object> checkRentalAvailability(Long productId,
                                                       LocalDate startDate, LocalDate endDate) {
        List<ProductVariant> available =
                productRepository.findAvailableForRental(productId, startDate, endDate);
        Map<String, Object> result = new HashMap<>();
        result.put("available", !available.isEmpty());
        result.put("availableVariants", available.stream().map(v -> {
            Map<String, Object> m = new HashMap<>();
            m.put("id", v.getId());
            m.put("size", v.getSize());
            m.put("color", v.getColor() != null ? v.getColor() : "");
            m.put("rentalQuantity", v.getRentalQuantity());
            return m;
        }).collect(Collectors.toList()));
        return result;
    }

    // ── Admin CRUD ─────────────────────────────────────
    // Admin search - cũng tách query
    public PageResponse<ProductResponse> searchProductsAdmin(
            String keyword, Long categoryId, String productType, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Product> result;
        if (productType != null && !productType.isBlank()) {
            try {
                Product.ProductType typeEnum = Product.ProductType.valueOf(productType);
                result = productRepository.findAllForAdminWithType(typeEnum, keyword, categoryId, pageable);
            } catch (IllegalArgumentException e) {
                result = productRepository.findAllForAdmin(keyword, categoryId, pageable);
            }
        } else {
            result = productRepository.findAllForAdmin(keyword, categoryId, pageable);
        }
        // Dùng toDetailResponse để trả variants (kèm stockQuantity) cho admin list
        return PageResponse.of(result.map(this::toDetailResponse));
    }

    @Transactional
    public ProductResponse createProduct(ProductRequest req) {
        Category category = categoryRepository.findById(req.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category không tồn tại"));
        Series series = req.getSeriesId() != null
                ? seriesRepository.findById(req.getSeriesId()).orElse(null) : null;

        Product p = Product.builder()
                .name(req.getName()).slug(req.getSlug())
                .description(req.getDescription()).characterName(req.getCharacterName())
                .category(category).series(series)
                .productType(Product.ProductType.valueOf(req.getProductType()))
                .salePrice(req.getSalePrice())
                .rentalPricePerDay(req.getRentalPricePerDay())
                .depositAmount(req.getDepositAmount() != null ? req.getDepositAmount() : BigDecimal.ZERO)
                .primaryImageUrl(req.getImageUrls() != null && !req.getImageUrls().isEmpty()
                        ? req.getImageUrls().get(0) : null)
                .isHot(req.getIsHot() != null ? req.getIsHot() : false)
                .isNew(req.getIsNew() != null ? req.getIsNew() : true)
                .isActive(req.getIsActive() != null ? req.getIsActive() : true)
                .build();
        p = productRepository.save(p);

        if (req.getVariants() != null) {
            for (ProductRequest.VariantRequest vr : req.getVariants()) {
                variantRepository.save(ProductVariant.builder()
                        .product(p).size(vr.getSize()).color(vr.getColor()).sku(vr.getSku() != null && !vr.getSku().isBlank() ? vr.getSku() : null)
                        .stockQuantity(vr.getStockQuantity() != null ? vr.getStockQuantity() : 0)
                        .rentalQuantity(vr.getRentalQuantity() != null ? vr.getRentalQuantity() : 0)
                        .price(vr.getPrice() != null ? vr.getPrice() : (req.getSalePrice() != null ? req.getSalePrice() : java.math.BigDecimal.ZERO)).isActive(true).build());
            }
        }
        return toDetailResponse(productRepository.findById(p.getId()).orElseThrow());
    }

    @Transactional
    public ProductResponse updateProduct(Long id, ProductRequest req) {
        Product p = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sản phẩm không tồn tại"));
        if (req.getCategoryId() != null)
            p.setCategory(categoryRepository.findById(req.getCategoryId()).orElse(p.getCategory()));
        if (req.getSeriesId() != null)
            p.setSeries(seriesRepository.findById(req.getSeriesId()).orElse(null));
        if (req.getName() != null)        p.setName(req.getName());
        if (req.getSlug() != null)        p.setSlug(req.getSlug());
        if (req.getDescription() != null) p.setDescription(req.getDescription());
        if (req.getCharacterName() != null) p.setCharacterName(req.getCharacterName());
        if (req.getProductType() != null)
            p.setProductType(Product.ProductType.valueOf(req.getProductType()));
        if (req.getSalePrice() != null)        p.setSalePrice(req.getSalePrice());
        if (req.getRentalPricePerDay() != null) p.setRentalPricePerDay(req.getRentalPricePerDay());
        if (req.getDepositAmount() != null)    p.setDepositAmount(req.getDepositAmount());
        if (req.getIsHot() != null)    p.setIsHot(req.getIsHot());
        if (req.getIsNew() != null)    p.setIsNew(req.getIsNew());
        if (req.getIsActive() != null) p.setIsActive(req.getIsActive());
        if (req.getImageUrls() != null && !req.getImageUrls().isEmpty())
            p.setPrimaryImageUrl(req.getImageUrls().get(0));
        productRepository.save(p);

        // ── Cập nhật variants: MERGE (không xóa hết rồi tạo lại)
        // → giữ stockQuantity hiện tại nếu không truyền lên
        if (req.getVariants() != null && !req.getVariants().isEmpty()) {
            java.util.List<ProductVariant> existing = variantRepository.findByProductIdAndIsActiveTrue(p.getId());
            // Map id → variant hiện tại để tra cứu nhanh
            java.util.Map<Long, ProductVariant> existingMap = new java.util.HashMap<>();
            for (ProductVariant ev : existing) existingMap.put(ev.getId(), ev);

            for (ProductRequest.VariantRequest vr : req.getVariants()) {
                if (vr.getId() != null && existingMap.containsKey(vr.getId())) {
                    // Cập nhật variant đã có
                    ProductVariant ev = existingMap.get(vr.getId());
                    if (vr.getSize()           != null) ev.setSize(vr.getSize());
                    if (vr.getColor()          != null) ev.setColor(vr.getColor());
                    if (vr.getPrice()          != null) ev.setPrice(vr.getPrice());
                    if (vr.getStockQuantity()  != null) ev.setStockQuantity(vr.getStockQuantity());
                    if (vr.getRentalQuantity() != null) ev.setRentalQuantity(vr.getRentalQuantity());
                    if (vr.getSku() != null && !vr.getSku().isBlank()) ev.setSku(vr.getSku());
                    variantRepository.save(ev);
                    existingMap.remove(vr.getId()); // Đã xử lý
                } else {
                    // Tạo variant mới
                    variantRepository.save(ProductVariant.builder()
                            .product(p)
                            .size(vr.getSize())
                            .color(vr.getColor())
                            .sku(vr.getSku() != null && !vr.getSku().isBlank() ? vr.getSku() : null)
                            .price(vr.getPrice())
                            .stockQuantity(vr.getStockQuantity() != null ? vr.getStockQuantity() : 0)
                            .rentalQuantity(vr.getRentalQuantity() != null ? vr.getRentalQuantity() : 0)
                            .isActive(true)
                            .build());
                }
            }
            // Các variant không có trong request → đánh dấu inactive (không xóa để giữ lịch sử)
            for (ProductVariant gone : existingMap.values()) {
                gone.setIsActive(false);
                variantRepository.save(gone);
            }
        }

        return toDetailResponse(productRepository.findById(p.getId()).orElseThrow());
    }

    @Transactional
    public void toggleStatus(Long id) {
        Product p = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sản phẩm không tồn tại"));
        p.setIsActive(!p.getIsActive());
        productRepository.save(p);
    }

    @Transactional
    public void toggleProductStatus(Long id) { toggleStatus(id); }

    // ── Mappers ───────────────────────────────────────
    public ProductResponse toResponse(Product p) {
        return ProductResponse.builder()
                .id(p.getId()).name(p.getName()).slug(p.getSlug())
                .characterName(p.getCharacterName())
                .categoryId(p.getCategory() != null ? p.getCategory().getId() : null)
                .categoryName(p.getCategory() != null ? p.getCategory().getName() : null)
                .seriesId(p.getSeries() != null ? p.getSeries().getId() : null)
                .seriesName(p.getSeries() != null ? p.getSeries().getName() : null)
                .productType(p.getProductType() != null ? p.getProductType().name() : null)
                .salePrice(p.getSalePrice()).rentalPricePerDay(p.getRentalPricePerDay())
                .depositAmount(p.getDepositAmount())
                .primaryImageUrl(p.getPrimaryImageUrl())
                .isHot(p.getIsHot()).isNew(p.getIsNew()).isActive(p.getIsActive())
                .avgRating(p.getAvgRating()).reviewCount(p.getReviewCount())
                .viewsCount(p.getViewsCount()).build();
    }

    public ProductResponse toDetailResponse(Product p) {
        List<ProductResponse.VariantDto> variantDtos = new ArrayList<>();
        for (ProductVariant v : p.getVariants()) {
            ProductResponse.VariantDto dto = new ProductResponse.VariantDto();
            dto.setId(v.getId()); dto.setSize(v.getSize()); dto.setColor(v.getColor());
            dto.setStockQuantity(v.getStockQuantity()); dto.setRentalQuantity(v.getRentalQuantity());
            dto.setPrice(v.getPrice()); dto.setIsActive(v.getIsActive());
            variantDtos.add(dto);
        }
        List<ProductResponse.ImageDto> imageDtos = new ArrayList<>();
        for (ProductImage img : p.getImages()) {
            ProductResponse.ImageDto dto = new ProductResponse.ImageDto();
            dto.setId(img.getId()); dto.setImageUrl(img.getImageUrl());
            dto.setAltText(img.getAltText()); dto.setIsPrimary(img.getIsPrimary());
            imageDtos.add(dto);
        }
        ProductResponse res = toResponse(p);
        res.setDescription(p.getDescription());
        res.setVariants(variantDtos);
        res.setImages(imageDtos);
        return res;
    }

    public static String generateSlug(String name) {
        if (name == null) return "";
        return name.toLowerCase()
                .replaceAll("[àáạảãâầấậẩẫăằắặẳẵ]","a").replaceAll("[èéẹẻẽêềếệểễ]","e")
                .replaceAll("[ìíịỉĩ]","i").replaceAll("[òóọỏõôồốộổỗơờớợởỡ]","o")
                .replaceAll("[ùúụủũưừứựửữ]","u").replaceAll("[ỳýỵỷỹ]","y").replaceAll("đ","d")
                .replaceAll("[^a-z0-9\\s-]","").trim().replaceAll("\\s+","-").replaceAll("-+","-");
    }
}
