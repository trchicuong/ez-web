package trchicuong.ezweb.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import trchicuong.ezweb.models.User;
import trchicuong.ezweb.services.GeminiChatService;

import java.util.Map;

@RestController
@RequestMapping("/chat")
@RequiredArgsConstructor
public class ChatController {

    private final GeminiChatService geminiChatService;

    @PostMapping
    public ResponseEntity<Map<String, String>> chat(@RequestBody Map<String, String> body,
                                                     @AuthenticationPrincipal User principal) {
        String message = body.getOrDefault("message", "").trim();
        if (message.isBlank() || message.length() > 500) {
            return ResponseEntity.badRequest().body(Map.of("error", "Tin nhắn không hợp lệ"));
        }
        String reply = geminiChatService.chat(message);
        return ResponseEntity.ok(Map.of("reply", reply));
    }
}
