package cosplayshop.repository;


import cosplayshop.entity.*;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.*;

// =========================================
// CategoryRepository
// =========================================
public interface CategoryRepository extends JpaRepository<Category, Long> {

    List<Category> findByIsActiveTrueOrderByNameAsc();

    boolean existsBySlug(String slug);

    Optional<Category> findBySlug(String slug);
}
