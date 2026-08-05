package trchicuong.ezweb.controllers;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import trchicuong.ezweb.models.User;
import trchicuong.ezweb.services.CartService;
import trchicuong.ezweb.services.CategoryService;
import trchicuong.ezweb.services.UserService;

/**
 * Tiêm các biến dùng chung vào mọi Model: categories, cartCount, currentUser.
 */
@ControllerAdvice
@RequiredArgsConstructor
public class GlobalControllerAdvice {

    private final CategoryService categoryService;
    private final CartService cartService;
    private final UserService userService;

    @ModelAttribute
    public void addGlobalAttributes(Model model,
                                    HttpSession session,
                                    @AuthenticationPrincipal User principal) {
        model.addAttribute("categories", categoryService.findAll());
        model.addAttribute("cartCount", cartService.getCartCount(session));
        
        // Load fresh user from DB (để có balance, name mới nhất)
        if (principal != null) {
            User currentUser = userService.findById(principal.getId()).orElse(principal);
            model.addAttribute("currentUser", currentUser);
        } else {
            model.addAttribute("currentUser", null);
        }
    }
}
