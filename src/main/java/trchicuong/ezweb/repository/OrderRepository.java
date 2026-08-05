package trchicuong.ezweb.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import trchicuong.ezweb.models.Order;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {

    Page<Order> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    List<Order> findByUserIdOrderByCreatedAtDesc(Long userId);

    Page<Order> findByStatusOrderByCreatedAtDesc(String status, Pageable pageable);

    @Query("SELECT COUNT(o) FROM Order o WHERE o.status = 'completed'")
    long countCompleted();

    @Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM Order o WHERE o.status = 'completed'")
    long sumRevenue();

    /** Kiểm tra user đã mua sản phẩm này chưa */
    @Query("SELECT COUNT(oi) > 0 FROM OrderItem oi WHERE oi.order.user.id = :userId AND oi.product.id = :productId AND oi.order.status = 'completed'")
    boolean hasUserPurchased(Long userId, Long productId);

    /** Lấy đơn hàng completed đầu tiên có chứa product */
    @Query("SELECT oi.order FROM OrderItem oi WHERE oi.order.user.id = :userId AND oi.product.id = :productId AND oi.order.status = 'completed' ORDER BY oi.order.createdAt DESC")
    List<Order> findCompletedOrdersByProduct(Long userId, Long productId, Pageable pageable);
}
