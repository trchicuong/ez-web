package trchicuong.ezweb.controllers;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import trchicuong.ezweb.config.AppProperties;
import trchicuong.ezweb.models.DownloadToken;
import trchicuong.ezweb.models.Order;
import trchicuong.ezweb.models.User;
import trchicuong.ezweb.services.OrderService;
import trchicuong.ezweb.services.UserService;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

@Controller
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final UserService userService;
    private final AppProperties appProperties;

    /** POST /orders/checkout — thanh toán giỏ hàng bằng ví */
    @PostMapping("/orders/checkout")
    public String checkout(HttpSession session,
                            @AuthenticationPrincipal User principal,
                            RedirectAttributes redirectAttributes) {
        User user = userService.findById(principal.getId())
                .orElseThrow(() -> new IllegalStateException("User not found"));
        try {
            Order order = orderService.checkout(user, session);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Thanh toán thành công! Link tải đã được gửi vào email của bạn.");
            return "redirect:/orders/" + order.getId();
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/cart";
        }
    }

    /** GET /orders — lịch sử đơn hàng */
    @GetMapping("/orders")
    public String orderHistory(@RequestParam(defaultValue = "0") int page,
                                @AuthenticationPrincipal User principal,
                                Model model) {
        Page<Order> orders = orderService.findByUser(principal.getId(), page);
        model.addAttribute("orders", orders);
        model.addAttribute("pageTitle", "Lịch sử mua hàng");
        return "pages/orders";
    }

    /** GET /orders/{id} — chi tiết đơn hàng */
    @GetMapping("/orders/{id}")
    public String orderDetail(@PathVariable Long id,
                               @AuthenticationPrincipal User principal,
                               Model model) {
        Order order = orderService.findById(id).orElse(null);
        if (order == null || !order.getUser().getId().equals(principal.getId())) {
            return "errors/404";
        }
        model.addAttribute("order", order);
        model.addAttribute("pageTitle", "Đơn hàng #" + id);
        return "pages/order-detail";
    }

    /** GET /orders/{id}/download — tạo mới token rồi tải về */
    @GetMapping("/orders/{id}/download")
    public String generateDownload(@PathVariable Long id,
                                    @AuthenticationPrincipal User principal,
                                    RedirectAttributes redirectAttributes) {
        Order order = orderService.findById(id).orElse(null);
        if (order == null || !order.getUser().getId().equals(principal.getId())
                || !"completed".equals(order.getStatus())) {
            redirectAttributes.addFlashAttribute("errorMessage", "Đơn hàng không hợp lệ");
            return "redirect:/orders";
        }
        User user = userService.findById(principal.getId()).orElseThrow();
        DownloadToken token = orderService.regenerateToken(order, user);
        return "redirect:/download/" + token.getToken();
    }

    /** GET /download/{token} — download file zip */
    @GetMapping("/download/{token}")
    public ResponseEntity<Resource> download(@PathVariable String token) {
        DownloadToken dlToken = orderService.validateDownloadToken(token)
                .orElse(null);
        if (dlToken == null) {
            return ResponseEntity.notFound().build();
        }

        // Lấy file path từ các sản phẩm trong đơn hàng
        // Đơn hàng thường có 1+ sản phẩm; tải file đầu tiên
        String filePath = dlToken.getOrder().getItems().stream()
                .findFirst()
                .map(item -> item.getProduct().getFilePath())
                .orElse("");

        if (filePath.isBlank()) {
            return ResponseEntity.notFound().build();
        }

        // Đánh dấu token đã dùng trước khi serve (tránh race condition)
        orderService.markTokenUsed(dlToken);

        // Nếu filePath là URL thì redirect thẳng
        if (filePath.startsWith("http://") || filePath.startsWith("https://")) {
            return ResponseEntity.status(302)
                    .header(HttpHeaders.LOCATION, filePath)
                    .build();
        }

        Path storagePath = Paths.get(appProperties.getStorage().getPath()).toAbsolutePath().normalize();
        Path resolvedPath = storagePath.resolve(filePath).normalize();
        // Path traversal guard
        if (!resolvedPath.startsWith(storagePath)) {
            return ResponseEntity.notFound().build();
        }
        File file = resolvedPath.toFile();
        if (!file.exists() || !file.isFile()) {
            return ResponseEntity.notFound().build();
        }

        Resource resource = new FileSystemResource(file);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getName() + "\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
    }
}
