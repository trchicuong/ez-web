package trchicuong.ezweb.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import trchicuong.ezweb.models.Category;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    Optional<Category> findBySlug(String slug);
    List<Category> findAllByOrderBySortOrderAsc();
}
