package cosplayshop.repository;

import cosplayshop.entity.FlashSaleItem;   // ← FIX: import đúng
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FlashSaleItemRepository extends JpaRepository<FlashSaleItem, Long> {

    List<FlashSaleItem> findByFlashSaleId(Long flashSaleId);

    @Query("""
        SELECT fsi FROM FlashSaleItem fsi
        WHERE fsi.flashSale.id = :flashSaleId
          AND fsi.soldQuantity < fsi.quantity
          AND fsi.isActive = true
    """)
    List<FlashSaleItem> findAvailableItems(@Param("flashSaleId") Long flashSaleId);
}