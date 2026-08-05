package trchicuong.ezweb.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import trchicuong.ezweb.models.TopupLog;

import java.time.LocalDateTime;
import java.util.Optional;

public interface TopupLogRepository extends JpaRepository<TopupLog, Long> {

    Page<TopupLog> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    Optional<TopupLog> findByProviderAndProviderId(String provider, String providerId);

    boolean existsByProviderAndProviderId(String provider, String providerId);

    Optional<TopupLog> findFirstByUserIdAndStatusAndCreatedAtAfterOrderByCreatedAtDesc(
        Long userId, String status, LocalDateTime createdAt);

    Page<TopupLog> findByStatus(String status, Pageable pageable);

    long countByStatus(String status);

    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM TopupLog t WHERE t.status = :status")
    long sumAmountByStatus(String status);

    @Query("SELECT t FROM TopupLog t WHERE " +
           "LOWER(t.user.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(t.user.email) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(t.transferCode) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<TopupLog> searchByKeyword(String keyword, Pageable pageable);

    @Query("SELECT t FROM TopupLog t WHERE t.status = :status AND (" +
           "LOWER(t.user.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(t.user.email) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(t.transferCode) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<TopupLog> searchByStatusAndKeyword(String status, String keyword, Pageable pageable);
}
