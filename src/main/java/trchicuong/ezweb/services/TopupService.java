package trchicuong.ezweb.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import trchicuong.ezweb.models.TopupLog;
import trchicuong.ezweb.models.User;
import trchicuong.ezweb.repository.TopupLogRepository;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class TopupService {

    private final TopupLogRepository topupLogRepository;
    private final UserService userService;

    /**
     * Xử lý giao dịch nạp tiền từ webhook SePay.
     * @return số giao dịch đã xử lý thành công
     */
    public int processTransaction(String provider, String providerId, long amount,
                                   String transferContent, String rawJson) {
        // Idempotency: bỏ qua nếu đã xử lý
        if (topupLogRepository.existsByProviderAndProviderId(provider, providerId)) {
            log.warn("Duplicate topup transaction: provider={}, id={}", provider, providerId);
            return 0;
        }

        if (amount <= 0) return 0;

        // Tìm user từ mã chuyển khoản (EZW{userId})
        Long userId = extractUserId(transferContent);
        User user = null;
        String status = "unmatched";

        if (userId != null) {
            user = userService.findById(userId).orElse(null);
        }

        if (user != null) {
            userService.addBalance(userId, amount);
            status = "matched";
            log.info("Topup matched: userId={}, amount={}", userId, amount);
        } else {
            log.warn("Topup unmatched: content='{}', amount={}", transferContent, amount);
        }

        TopupLog logEntry = TopupLog.builder()
                .user(user)
                .amount(amount)
                .transferCode(transferContent)
                .provider(provider)
                .providerId(providerId)
                .status(status)
                .rawData(rawJson)
                .build();
        topupLogRepository.save(logEntry);

        return "matched".equals(status) ? 1 : 0;
    }

    @Transactional(readOnly = true)
    public Page<TopupLog> findByUser(Long userId, int page) {
        return topupLogRepository.findByUserIdOrderByCreatedAtDesc(userId, PageRequest.of(page, 20));
    }

    /** Trích xuất userId từ nội dung chuyển khoản dạng "...EZW123..." */
    private Long extractUserId(String content) {
        if (content == null || content.isBlank()) return null;
        Pattern pattern = Pattern.compile("EZW(\\d{1,10})", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(content.toUpperCase());
        if (matcher.find()) {
            try {
                return Long.parseLong(matcher.group(1));
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }
}
