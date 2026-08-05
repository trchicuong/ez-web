package trchicuong.ezweb.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import trchicuong.ezweb.models.User;
import trchicuong.ezweb.services.OrderService;
import trchicuong.ezweb.services.UserService;

@Controller
@RequestMapping("/account")
@RequiredArgsConstructor
public class AccountController {

    private final UserService userService;
    private final OrderService orderService;

    @GetMapping
    public String profile(@AuthenticationPrincipal User principal, Model model) {
        User user = userService.findById(principal.getId()).orElseThrow();
        model.addAttribute("user", user);
        model.addAttribute("recentOrders", orderService.findByUser(principal.getId(), 0).getContent());
        model.addAttribute("pageTitle", "Tài khoản của tôi");
        return "pages/account";
    }

    @PostMapping("/update")
    public String updateProfile(@AuthenticationPrincipal User principal,
                                 @RequestParam String name,
                                 RedirectAttributes redirectAttributes) {
        User user = userService.findById(principal.getId()).orElseThrow();
        user.setName(name.trim());
        userService.save(user);
        redirectAttributes.addFlashAttribute("successMessage", "Cập nhật thông tin thành công");
        return "redirect:/account";
    }

    @PostMapping("/change-password")
    public String changePassword(@AuthenticationPrincipal User principal,
                                  @RequestParam String currentPassword,
                                  @RequestParam String newPassword,
                                  @RequestParam String confirmPassword,
                                  RedirectAttributes redirectAttributes) {
        if (!newPassword.equals(confirmPassword)) {
            redirectAttributes.addFlashAttribute("errorMessage", "Mật khẩu xác nhận không khớp");
            return "redirect:/account";
        }
        if (newPassword.length() < 6) {
            redirectAttributes.addFlashAttribute("errorMessage", "Mật khẩu mới phải có ít nhất 6 ký tự");
            return "redirect:/account";
        }
        User user = userService.findById(principal.getId()).orElseThrow();
        if (!userService.checkPassword(user, currentPassword)) {
            redirectAttributes.addFlashAttribute("errorMessage", "Mật khẩu hiện tại không đúng");
            return "redirect:/account";
        }
        userService.changePassword(user, newPassword);
        redirectAttributes.addFlashAttribute("successMessage", "Đổi mật khẩu thành công");
        return "redirect:/account";
    }
}
