package trchicuong.ezweb.controllers.admin;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import trchicuong.ezweb.models.TopupLog;
import trchicuong.ezweb.models.User;
import trchicuong.ezweb.repository.TopupLogRepository;
import trchicuong.ezweb.services.UserService;

@Controller
@RequestMapping("/admin/topups")
@RequiredArgsConstructor
public class AdminTopupController {

    private final TopupLogRepository topupLogRepository;
    private final UserService userService;

    @GetMapping
    public String list(@RequestParam(defaultValue = "") String status,
                       @RequestParam(defaultValue = "") String q,
                       @RequestParam(defaultValue = "0") int page,
                       Model model) {
        PageRequest pageable = PageRequest.of(page, 20, Sort.by("createdAt").descending());
        Page<TopupLog> logs;

        if (!q.isBlank()) {
            if (!status.isBlank()) {
                logs = topupLogRepository.searchByStatusAndKeyword(status, q, pageable);
            } else {
                logs = topupLogRepository.searchByKeyword(q, pageable);
            }
        } else if (!status.isBlank()) {
            logs = topupLogRepository.findByStatus(status, pageable);
        } else {
            logs = topupLogRepository.findAll(pageable);
        }

        long totalCount = topupLogRepository.count();
        long totalRevenue = topupLogRepository.sumAmountByStatus("matched");
        long unmatchedCount = topupLogRepository.countByStatus("unmatched");

        model.addAttribute("logs", logs);
        model.addAttribute("status", status);
        model.addAttribute("search", q);
        model.addAttribute("totalCount", totalCount);
        model.addAttribute("totalRevenue", totalRevenue);
        model.addAttribute("unmatchedCount", unmatchedCount);
        model.addAttribute("pageTitle", "Quản lý nạp tiền");
        return "admin/topups";
    }

    @PostMapping("/{id}/match")
    public String matchManual(@PathVariable Long id,
                              @RequestParam Long userId,
                              RedirectAttributes ra) {
        TopupLog log = topupLogRepository.findById(id).orElse(null);
        if (log == null || !"unmatched".equals(log.getStatus())) {
            ra.addFlashAttribute("errorMessage", "Giao dịch không hợp lệ hoặc đã được khớp.");
            return "redirect:/admin/topups";
        }
        User user = userService.findById(userId).orElse(null);
        if (user == null) {
            ra.addFlashAttribute("errorMessage", "Không tìm thấy người dùng ID " + userId);
            return "redirect:/admin/topups";
        }
        userService.addBalance(userId, log.getAmount());
        log.setUser(user);
        log.setStatus("matched");
        topupLogRepository.save(log);
        ra.addFlashAttribute("successMessage",
            "Đã khớp " + String.format("%,d", log.getAmount()) + "đ vào tài khoản " + user.getEmail());
        return "redirect:/admin/topups";
    }

    @GetMapping("/{id}/raw")
    @ResponseBody
    public String viewRaw(@PathVariable Long id) {
        TopupLog log = topupLogRepository.findById(id).orElse(null);
        if (log == null) return "{}";
        return log.getRawData() != null ? log.getRawData() : "{}";
    }
}
