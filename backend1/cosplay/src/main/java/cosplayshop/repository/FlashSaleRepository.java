package cosplayshop.repository;

import cosplayshop.entity.FlashSale;      // ← FIX: import đúng
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface FlashSaleRepository extends JpaRepository<FlashSale, Long> {

    @Query("""
        SELECT fs FROM FlashSale fs
        WHERE fs.isActive = true
          AND fs.startTime <= :now
          AND fs.endTime   >= :now
        ORDER BY fs.startTime DESC
    """)
    List<FlashSale> findActiveFlashSales(@Param("now") LocalDateTime now);

    @Query("""
        SELECT fs FROM FlashSale fs
        WHERE fs.endTime >= :now
        ORDER BY fs.startTime ASC
    """)
    List<FlashSale> findUpcoming(@Param("now") LocalDateTime now);
}
