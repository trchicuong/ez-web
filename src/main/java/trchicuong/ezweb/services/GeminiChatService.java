package trchicuong.ezweb.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import trchicuong.ezweb.config.AppProperties;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class GeminiChatService {

    private final WebClient geminiWebClient;
    private final AppProperties appProperties;

    private static final String SYSTEM_PROMPT = """
            Bạn là nhân viên chăm sóc khách hàng của EZWeb — Website bán source code và dịch vụ thiết kế web chuyên nghiệp.
            
            THÔNG TIN VỀ EZWEB:
            - EZWeb là nền tảng cung cấp source code chất lượng cao cho lập trình viên Việt Nam.
            - Dịch vụ: Bán source code (website, app, tool), thiết kế web theo yêu cầu (liên hệ qua Zalo/Messenger).
            - Hỗ trợ: Thứ 2 - Thứ 7, 8:00 - 22:00.
            
            HƯỚNG DẪN MUA HÀNG:
            1. Đăng ký tài khoản tại trang /register.
            2. Nạp tiền vào ví tại trang /topup bằng chuyển khoản ngân hàng với nội dung EZW{ID_tài_khoản}.
            3. Duyệt sản phẩm, thêm vào giỏ hàng.
            4. Thanh toán bằng số dư ví.
            5. Nhận link tải file ZIP qua email ngay sau khi thanh toán.
            
            CHÍNH SÁCH:
            - Bảo hành: Hỗ trợ kỹ thuật 30 ngày sau khi mua.
            - Hoàn tiền: Không hoàn tiền sau khi đã tải file (sản phẩm số).
            - Cập nhật: Source code được cập nhật miễn phí trong 6 tháng.
            
            QUY TẮC TRẢ LỜI:
            - Chỉ trả lời các câu hỏi liên quan đến EZWeb, sản phẩm, chính sách, hướng dẫn mua hàng.
            - Trả lời ngắn gọn, thân thiện, chuyên nghiệp bằng tiếng Việt.
            - Nếu câu hỏi không liên quan đến EZWeb, lịch sự từ chối và hướng dẫn liên hệ.
            - Không trả lời các câu hỏi về chính trị, tôn giáo, hoặc nội dung nhạy cảm.
            """;

    /**
     * Gửi tin nhắn đến Gemini và nhận phản hồi.
     */
    public String chat(String userMessage) {
        String apiKey = appProperties.getGemini().getApiKey();
        if (apiKey == null || apiKey.isBlank()) {
            return "Chatbot chưa được cấu hình. Vui lòng liên hệ Admin.";
        }

        String model = appProperties.getGemini().getModel();
        String url = "/v1beta/models/" + model + ":generateContent?key=" + apiKey;

        try {
            Map<String, Object> requestBody = Map.of(
                    "system_instruction", Map.of(
                            "parts", List.of(Map.of("text", SYSTEM_PROMPT))
                    ),
                    "contents", List.of(
                            Map.of("role", "user",
                                   "parts", List.of(Map.of("text", userMessage)))
                    ),
                    "generationConfig", Map.of(
                            "maxOutputTokens", 512,
                            "temperature", 0.4
                    )
            );

            @SuppressWarnings("unchecked")
            Map<String, Object> response = geminiWebClient.post()
                    .uri(url)
                    .header("Content-Type", "application/json")
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            if (response == null) return "Không nhận được phản hồi từ AI.";

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> candidates = (List<Map<String, Object>>) response.get("candidates");
            if (candidates == null || candidates.isEmpty()) return "AI không có phản hồi.";

            @SuppressWarnings("unchecked")
            Map<String, Object> content = (Map<String, Object>) candidates.get(0).get("content");
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> parts = (List<Map<String, Object>>) content.get("parts");

            return (String) parts.get(0).get("text");
        } catch (Exception e) {
            log.error("Gemini API error: {}", e.getMessage());
            return "Xin lỗi, hiện tại tôi không thể trả lời. Vui lòng thử lại sau.";
        }
    }
}
