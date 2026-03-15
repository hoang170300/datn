package cosplayshop.repository;

import cosplayshop.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    // FIX: dùng nativeQuery=true vì Hibernate 6 không parse enum trong JPQL
    @Query(value = """
        SELECT oi.* FROM order_items oi
        JOIN orders o ON oi.order_id = o.id
        WHERE oi.item_type = 'RENTAL'
          AND oi.rental_status = 'ACTIVE'
    """, nativeQuery = true)
    List<OrderItem> findActiveRentals();

    @Query(value = """
        SELECT oi.* FROM order_items oi
        JOIN orders o ON oi.order_id = o.id
        WHERE oi.item_type = 'RENTAL'
          AND oi.rental_status = 'ACTIVE'
          AND oi.rental_end_date < :today
    """, nativeQuery = true)
    List<OrderItem> findOverdueRentals(@Param("today") LocalDate today);
}
