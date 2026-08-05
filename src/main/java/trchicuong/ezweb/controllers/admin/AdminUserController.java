package trchicuong.ezweb.controllers.admin;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import trchicuong.ezweb.models.User;
import trchicuong.ezweb.services.UserService;

@Controller
@RequestMapping("/admin/users")
@RequiredArgsConstructor
public class AdminUserController {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    @GetMapping
    public String list(@RequestParam(defaultValue = "0") int page, Model model) {
        model.addAttribute("users", userService.findAllPaged(
                PageRequest.of(page, 20, Sort.by("createdAt").descending())));
        model.addAttribute("pageTitle", "Quản lý người dùng");
        return "admin/users";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        User user = userService.findById(id).orElse(null);
        if (user == null) return "redirect:/admin/users";
        model.addAttribute("user", user);
        model.addAttribute("pageTitle", "Chi tiết: " + user.getName());
        return "admin/user-detail";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id,
                           @AuthenticationPrincipal User currentUser,
                           Model model) {
        User user = userService.findById(id).orElse(null);
        if (user == null) return "redirect:/admin/users";
        
        model.addAttribute("user", user);
        model.addAttribute("isOwnAccount", currentUser.getId().equals(id));
        model.addAttribute("pageTitle", "Chỉnh sửa: " + user.getName());
        return "admin/user-edit";
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Long id,
                         @RequestParam String name,
                         @RequestParam String email,
                         @RequestParam(required = false) String role,
                         @RequestParam(required = false, defaultValue = "0") Long balance,
                         @RequestParam(required = false) String newPassword,
                         @AuthenticationPrincipal User currentUser,
                         RedirectAttributes redirectAttributes) {
        User user = userService.findById(id).orElse(null);
        if (user == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy người dùng");
            return "redirect:/admin/users";
        }

        // Update basic info
        user.setName(name);
        user.setEmail(email);

        // Update role (không cho tự sửa quyền mình)
        if (role != null && !currentUser.getId().equals(id)) {
            user.setRole(role.toUpperCase());
        }

        // Update balance
        if (balance != null && balance >= 0) {
            user.setBalance(balance);
        }

        // Update password if provided
        if (newPassword != null && !newPassword.isBlank() && newPassword.length() >= 8) {
            user.setPassword(passwordEncoder.encode(newPassword));
        }

        userService.save(user);
        redirectAttributes.addFlashAttribute("successMessage", "Đã cập nhật người dùng");
        return "redirect:/admin/users/" + id;
    }

    @PostMapping("/{id}/role")
    public String changeRole(@PathVariable Long id,
                              @RequestParam String role,
                              RedirectAttributes redirectAttributes) {
        userService.findById(id).ifPresent(user -> {
            user.setRole(role.toUpperCase());
            userService.save(user);
        });
        redirectAttributes.addFlashAttribute("successMessage", "Đã cập nhật quyền");
        return "redirect:/admin/users";
    }
}
