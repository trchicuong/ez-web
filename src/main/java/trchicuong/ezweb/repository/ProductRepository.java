package trchicuong.ezweb.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import trchicuong.ezweb.models.Category;
import trchicuong.ezweb.models.Product;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {

    Optional<Product> findBySlug(String slug);

    Page<Product> findByStatus(String status, Pageable pageable);

    Page<Product> findByCategorySlugAndStatus(String categorySlug, String status, Pageable pageable);

    @Query("SELECT p FROM Product p WHERE p.status = 'active' AND " +
           "(LOWER(p.title) LIKE LOWER(CONCAT('%', :q, '%')) OR " +
           "LOWER(p.summary) LIKE LOWER(CONCAT('%', :q, '%')))")
    Page<Product> search(String q, Pageable pageable);

    List<Product> findTop8ByStatusOrderByCreatedAtDesc(String status);

    List<Product> findTop8ByStatusAndSalePriceIsNotNullOrderByCreatedAtDesc(String status);

    List<Product> findTop4ByCategorySlugAndStatusAndIdNot(String categorySlug, String status, Long id);

    Page<Product> findByStatusAndSalePriceIsNotNull(String status, Pageable pageable);

    Page<Product> findByStatusAndPrice(String status, Long price, Pageable pageable);

    Page<Product> findByStatusOrderByCreatedAtDesc(String status, Pageable pageable);

    @Modifying
    @Query("UPDATE Product p SET p.downloads = p.downloads + 1 WHERE p.id = :id")
    void incrementDownloads(Long id);

    long countByCategory(Category category);

    long countByStatus(String status);
}
