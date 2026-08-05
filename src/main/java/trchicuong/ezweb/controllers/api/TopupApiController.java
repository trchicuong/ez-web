package trchicuong.ezweb.controllers.api;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import trchicuong.ezweb.models.TopupLog;
import trchicuong.ezweb.models.User;
import trchicuong.ezweb.repository.TopupLogRepository;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/topup")
@RequiredArgsConstructor
public class TopupApiController {

    private final TopupLogRepository topupLogRepository;

    /**
     * Check xem có topup nào được credited sau timestamp 'since' không.
     * Dùng để polling từ client.
     */
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> checkStatus(
            @AuthenticationPrincipal User principal,
            @RequestParam(required = false, defaultValue = "0") long since) {
        
        Map<String, Object> response = new HashMap<>();
        response.put("credited", false);

        if (principal == null || since <= 0) {
            return ResponseEntity.ok(response);
        }

        // dùng cùng timezone với Hibernate jdbc.time_zone
        LocalDateTime sinceTime = java.time.Instant.ofEpochSecond(since)
                .atZone(java.time.ZoneId.of("Asia/Ho_Chi_Minh"))
                .toLocalDateTime();

        // Tìm topup gần nhất của user sau 'sinceTime'
        Optional<TopupLog> recentTopup = topupLogRepository
            .findFirstByUserIdAndStatusAndCreatedAtAfterOrderByCreatedAtDesc(
                principal.getId(), 
                "matched", 
                sinceTime
            );

        if (recentTopup.isPresent()) {
            response.put("credited", true);
            response.put("amount", recentTopup.get().getAmount());
        }

        return ResponseEntity.ok(response);
    }
}
