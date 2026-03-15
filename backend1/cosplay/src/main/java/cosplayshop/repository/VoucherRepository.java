package cosplayshop.repository;

import cosplayshop.entity.Voucher;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Optional;

@Repository
public interface VoucherRepository extends JpaRepository<Voucher, Long> {

    Optional<Voucher> findByCode(String code);

    @Query("""
        SELECT v FROM Voucher v
        WHERE v.code = :code
          AND v.isActive = true
          AND (v.startDate IS NULL OR v.startDate <= CURRENT_TIMESTAMP)
          AND (v.endDate   IS NULL OR v.endDate   >= CURRENT_TIMESTAMP)
          AND (v.usageLimit IS NULL OR v.usedCount < v.usageLimit)
          AND (v.minOrderValue IS NULL OR v.minOrderValue <= :amount)
    """)
    Optional<Voucher> findValidVoucher(
            @Param("code")   String code,
            @Param("amount") BigDecimal amount
    );
}
