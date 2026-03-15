package cosplayshop.repository;

import cosplayshop.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    // FIX: @EntityGraph load items + productVariant cùng lúc
    // tránh LazyInitializationException khi toResponse() đọc size/color
    @EntityGraph(attributePaths = {"items", "items.productVariant"})
    Page<Order> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    @EntityGraph(attributePaths = {"items", "items.productVariant"})
    Page<Order> findByStatus(Order.OrderStatus status, Pageable pageable);

    // findAll override với EntityGraph
    @EntityGraph(attributePaths = {"items", "items.productVariant"})
    @Query("SELECT o FROM Order o")
    Page<Order> findAllWithItems(Pageable pageable);

    Optional<Order> findByOrderNumber(String orderNumber);

    List<Order> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);

    Page<Order> findByUserId(Long userId, Pageable pageable);
}
