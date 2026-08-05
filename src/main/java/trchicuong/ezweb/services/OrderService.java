package trchicuong.ezweb.services;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import trchicuong.ezweb.config.AppProperties;
import trchicuong.ezweb.models.*;
import trchicuong.ezweb.repository.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final DownloadTokenRepository downloadTokenRepository;
    private final UserService userService;
    private final CartService cartService;
    private final MailService mailService;
    private final AppProperties appProperties;

    /**
     * Tạo đơn hàng từ giỏ hàng, trừ ví, gửi mail.
     * Ném exception nếu không đủ số dư.
     */
    public Order checkout(User user, HttpSession session) {
        List<CartItem> cartItems = cartService.getCart(session);
        if (cartItems.isEmpty()) {
            throw new IllegalStateException("Giỏ hàng trống");
        }

        long total = cartService.getCartTotal(session);
        if (user.getBalance() < total) {
            throw new IllegalStateException("Số dư ví không đủ. Vui lòng nạp thêm tiền.");
        }

        // Trừ ví ngay (atomic via JPQL)
        boolean deducted = userService.deductBalance(user.getId(), total);
        if (!deducted) {
            throw new IllegalStateException("Số dư ví không đủ. Vui lòng nạp thêm tiền.");
        }

        // Tạo Order
        Order order = Order.builder()
                .user(user)
                .totalAmount(total)
                .status("completed")
                .paymentMethod("balance")
                .build();

        for (CartItem ci : cartItems) {
            Product product = productRepository.findById(ci.getProductId())
                    .orElseThrow(() -> new IllegalStateException("Sản phẩm không tồn tại: " + ci.getProductId()));
            OrderItem item = OrderItem.builder()
                    .order(order)
                    .product(product)
                    .priceSnapshot(ci.getPrice())
                    .build();
            order.getItems().add(item);
            productRepository.incrementDownloads(product.getId());
        }

        Order saved = orderRepository.save(order);

        // Xoá giỏ hàng
        cartService.clearCart(session);

        // Tạo download token & gửi mail (async)
        DownloadToken token = createDownloadToken(saved, user);
        mailService.sendOrderSuccessMail(user, saved, token);

        return saved;
    }

    private DownloadToken createDownloadToken(Order order, User user) {
        String tokenValue = UUID.randomUUID().toString().replace("-", "") +
                            UUID.randomUUID().toString().replace("-", "");
        tokenValue = tokenValue.substring(0, 64);

        DownloadToken token = DownloadToken.builder()
                .token(tokenValue)
                .order(order)
                .user(user)
                .expiresAt(LocalDateTime.now().plusHours(appProperties.getDownload().getTokenTtlHours()))
                .build();
        return downloadTokenRepository.save(token);
    }

    @Transactional(readOnly = true)
    public Page<Order> findByUser(Long userId, int page) {
        return orderRepository.findByUserIdOrderByCreatedAtDesc(userId, PageRequest.of(page, 10));
    }

    @Transactional(readOnly = true)
    public Optional<Order> findById(Long id) {
        return orderRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public boolean hasUserPurchased(Long userId, Long productId) {
        return orderRepository.hasUserPurchased(userId, productId);
    }

    @Transactional(readOnly = true)
    public Optional<Order> findCompletedOrderByProduct(Long userId, Long productId) {
        List<Order> orders = orderRepository.findCompletedOrdersByProduct(userId, productId, PageRequest.of(0, 1));
        return orders.isEmpty() ? Optional.empty() : Optional.of(orders.get(0));
    }

    /** Validate download token — no user check, token itself is the credential */
    public Optional<DownloadToken> validateDownloadToken(String tokenValue) {
        return downloadTokenRepository.findByToken(tokenValue)
                .filter(t -> !t.isExpired() && !t.isUsed());
    }

    /** @deprecated use validateDownloadToken(String) */
    public Optional<DownloadToken> validateDownloadToken(String tokenValue, Long userId) {
        return validateDownloadToken(tokenValue)
                .filter(t -> t.getUser().getId().equals(userId));
    }

    /** Đánh dấu token đã dùng */
    public void markTokenUsed(DownloadToken token) {
        token.setUsedAt(LocalDateTime.now());
        downloadTokenRepository.save(token);
    }

    public long countCompleted() {
        return orderRepository.countCompleted();
    }

    public long countAll() {
        return orderRepository.count();
    }

    public long sumRevenue() {
        return orderRepository.sumRevenue();
    }

    @Transactional(readOnly = true)
    public List<Order> findRecent(org.springframework.data.domain.Pageable pageable) {
        return orderRepository.findAll(pageable).getContent();
    }

    @Transactional(readOnly = true)
    public org.springframework.data.domain.Page<Order> findAll(int page) {
        return orderRepository.findAll(
                org.springframework.data.domain.PageRequest.of(page, 20,
                        org.springframework.data.domain.Sort.by("createdAt").descending()));
    }

    @Transactional(readOnly = true)
    public org.springframework.data.domain.Page<Order> findByStatus(String status, int page) {
        return orderRepository.findByStatusOrderByCreatedAtDesc(status,
                org.springframework.data.domain.PageRequest.of(page, 20));
    }

    public void updateStatus(Long orderId, String status) {
        orderRepository.findById(orderId).ifPresent(order -> {
            order.setStatus(status);
            orderRepository.save(order);
        });
    }

    /** Tạo lại download token cho đơn hàng đã completed */
    public DownloadToken regenerateToken(Order order, User user) {
        return createDownloadToken(order, user);
    }
}
