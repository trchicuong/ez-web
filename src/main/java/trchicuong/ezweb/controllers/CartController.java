package trchicuong.ezweb.controllers;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import trchicuong.ezweb.services.CartService;
import trchicuong.ezweb.services.ProductService;

import java.util.Map;

@Controller
@RequestMapping("/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;
    private final ProductService productService;

    @GetMapping
    public String viewCart(HttpSession session, Model model) {
        model.addAttribute("cart", cartService.getCart(session));
        model.addAttribute("cartTotal", cartService.getCartTotal(session));
        model.addAttribute("pageTitle", "Giỏ hàng");
        return "pages/cart";
    }

    @PostMapping("/add/{productId}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> addToCart(@PathVariable Long productId,
                                                          HttpSession session) {
        return productService.findById(productId)
                .filter(p -> "active".equals(p.getStatus()))
                .map(product -> {
                    cartService.addToCart(session, product);
                    return ResponseEntity.ok(Map.<String, Object>of(
                            "success", true,
                            "cartCount", cartService.getCartCount(session),
                            "message", "Đã thêm vào giỏ hàng"
                    ));
                })
                .orElse(ResponseEntity.badRequest().body(Map.of("success", false, "message", "Sản phẩm không tồn tại")));
    }

    @PostMapping("/remove/{productId}")
    public String removeFromCart(@PathVariable Long productId,
                                  HttpSession session,
                                  RedirectAttributes redirectAttributes) {
        cartService.removeFromCart(session, productId);
        redirectAttributes.addFlashAttribute("successMessage", "Đã xóa khỏi giỏ hàng");
        return "redirect:/cart";
    }

    @PostMapping("/clear")
    public String clearCart(HttpSession session, RedirectAttributes redirectAttributes) {
        cartService.clearCart(session);
        redirectAttributes.addFlashAttribute("successMessage", "Đã xóa toàn bộ giỏ hàng");
        return "redirect:/cart";
    }
}
