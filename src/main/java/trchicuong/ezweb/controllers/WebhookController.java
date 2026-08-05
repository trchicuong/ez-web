package trchicuong.ezweb.controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import trchicuong.ezweb.config.AppProperties;
import trchicuong.ezweb.services.TopupService;

import java.util.Map;

/**
 * Nhận webhook từ SePay khi có giao dịch chuyển khoản đến.
 * Đăng ký URL: https://yourdomain.com/api/webhook/topup
 * Header: Authorization: Apikey {TOPUP_SECRET}
 */
@RestController
@RequestMapping("/api/webhook")
@RequiredArgsConstructor
@Slf4j
public class WebhookController {

    private final TopupService topupService;
    private final AppProperties appProperties;
    private final ObjectMapper objectMapper;

    @PostMapping("/topup")
    public ResponseEntity<Map<String, Object>> handleTopup(
            @RequestBody String rawBody) {

        int processed = 0;
        try {
            JsonNode root = objectMapper.readTree(rawBody);
            String provider = appProperties.getTopup().getProvider();

            if ("sepay".equalsIgnoreCase(provider)) {
                processed = parseSePay(root, rawBody, provider);
            } else {
                processed = parseCasso(root, rawBody, provider);
            }
        } catch (Exception e) {
            log.error("Webhook parse error: {}", e.getMessage());
        }

        return ResponseEntity.ok(Map.of("success", true, "processed", processed));
    }

    private int parseSePay(JsonNode root, String rawBody, String provider) {
        // SePay gửi 1 transaction tại một thời điểm
        // Format: { "id": "...", "content": "...", "transferAmount": 100000, ... }
        String providerId = root.path("id").asText("");
        String content = root.path("content").asText("");
        long amount = root.path("transferAmount").asLong(0);
        if (providerId.isBlank() || amount <= 0) return 0;
        return topupService.processTransaction(provider, providerId, amount, content, rawBody);
    }

    private int parseCasso(JsonNode root, String rawBody, String provider) {
        // Casso gửi mảng: { "data": [{ "id": ..., "description": "...", "amount": 100000, ... }] }
        JsonNode data = root.path("data");
        if (!data.isArray()) return 0;
        int count = 0;
        for (JsonNode tx : data) {
            String providerId = tx.path("id").asText("");
            String content = tx.path("description").asText("");
            long amount = tx.path("amount").asLong(0);
            if (!providerId.isBlank() && amount > 0) {
                count += topupService.processTransaction(provider, providerId, amount, content, rawBody);
            }
        }
        return count;
    }
}
