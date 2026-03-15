package cosplayshop.repository;

import cosplayshop.entity.Series;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SeriesRepository extends JpaRepository<Series, Long> {
    List<Series> findByIsActiveTrueOrderByNameAsc();
    boolean existsBySlug(String slug);
    Optional<Series> findBySlug(String slug);
}
