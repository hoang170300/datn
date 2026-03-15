package cosplayshop.repository;

import cosplayshop.entity.Product;
import cosplayshop.entity.ProductVariant;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    Optional<Product> findBySlug(String slug);
    boolean existsBySlug(String slug);

    // FIX: tách 2 query - có type và không có type - tránh Hibernate 6 null enum issue
    @Query("""
        SELECT p FROM Product p
        WHERE p.isActive = true
          AND (:keyword IS NULL
               OR LOWER(p.name) LIKE LOWER(CONCAT('%',:keyword,'%'))
               OR LOWER(p.characterName) LIKE LOWER(CONCAT('%',:keyword,'%')))
          AND (:categoryId IS NULL OR p.category.id = :categoryId)
          AND (:seriesId   IS NULL OR p.series.id   = :seriesId)
          AND (:minPrice   IS NULL OR p.salePrice  >= :minPrice)
          AND (:maxPrice   IS NULL OR p.salePrice  <= :maxPrice)
    """)
    Page<Product> searchProductsNoType(
            @Param("keyword") String keyword,
            @Param("categoryId") Long categoryId,
            @Param("seriesId") Long seriesId,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice,
            Pageable pageable);

    @Query("""
        SELECT p FROM Product p
        WHERE p.isActive = true
          AND p.productType = :productType
          AND (:keyword IS NULL
               OR LOWER(p.name) LIKE LOWER(CONCAT('%',:keyword,'%'))
               OR LOWER(p.characterName) LIKE LOWER(CONCAT('%',:keyword,'%')))
          AND (:categoryId IS NULL OR p.category.id = :categoryId)
          AND (:seriesId   IS NULL OR p.series.id   = :seriesId)
          AND (:minPrice   IS NULL OR p.salePrice  >= :minPrice)
          AND (:maxPrice   IS NULL OR p.salePrice  <= :maxPrice)
    """)
    Page<Product> searchProductsWithType(
            @Param("productType") Product.ProductType productType,
            @Param("keyword") String keyword,
            @Param("categoryId") Long categoryId,
            @Param("seriesId") Long seriesId,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice,
            Pageable pageable);

    @Query("SELECT p FROM Product p WHERE p.isHot = true AND p.isActive = true ORDER BY p.viewsCount DESC")
    List<Product> findHotProducts(Pageable pageable);

    @Query("SELECT p FROM Product p WHERE p.isNew = true AND p.isActive = true ORDER BY p.createdAt DESC")
    List<Product> findNewProducts(Pageable pageable);

    @Query(value = """
        SELECT pv.* FROM product_variants pv
        WHERE pv.product_id = :productId AND pv.is_active = 1 AND pv.rental_quantity > 0
          AND pv.id NOT IN (
              SELECT oi.product_variant_id FROM order_items oi
              WHERE oi.item_type = 'RENTAL' AND oi.rental_status <> 'RETURNED'
                AND oi.rental_start_date <= :endDate AND oi.rental_end_date >= :startDate)
    """, nativeQuery = true)
    List<ProductVariant> findAvailableForRental(
            @Param("productId") Long productId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    @Query("""
        SELECT p FROM Product p
        WHERE (:keyword IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%',:keyword,'%')))
          AND (:categoryId IS NULL OR p.category.id = :categoryId)
    """)
    Page<Product> findAllForAdmin(
            @Param("keyword") String keyword,
            @Param("categoryId") Long categoryId,
            Pageable pageable);

    @Query("""
        SELECT p FROM Product p
        WHERE p.productType = :productType
          AND (:keyword IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%',:keyword,'%')))
          AND (:categoryId IS NULL OR p.category.id = :categoryId)
    """)
    Page<Product> findAllForAdminWithType(
            @Param("productType") Product.ProductType productType,
            @Param("keyword") String keyword,
            @Param("categoryId") Long categoryId,
            Pageable pageable);
}
