package trchicuong.ezweb.controllers.admin;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import trchicuong.ezweb.models.Order;
import trchicuong.ezweb.services.OrderService;

@Controller
@RequestMapping("/admin/orders")
@RequiredArgsConstructor
public class AdminOrderController {

    private final OrderService orderService;

    @GetMapping
    public String list(@RequestParam(defaultValue = "0") int page,
                       @RequestParam(defaultValue = "") String status,
                       Model model) {
        Page<Order> orders = status.isBlank()
                ? orderService.findAll(page)
                : orderService.findByStatus(status, page);
        model.addAttribute("orders", orders);
        model.addAttribute("status", status);
        model.addAttribute("pageTitle", "Quản lý đơn hàng");
        return "admin/orders";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        Order order = orderService.findById(id).orElse(null);
        if (order == null) return "redirect:/admin/orders";
        model.addAttribute("order", order);
        model.addAttribute("pageTitle", "Đơn hàng #" + id);
        return "admin/order-detail";
    }

    @PostMapping("/{id}/status")
    public String changeStatus(@PathVariable Long id,
                               @RequestParam String status,
                               RedirectAttributes redirectAttributes) {
        orderService.updateStatus(id, status);
        redirectAttributes.addFlashAttribute("successMessage", "Đã cập nhật trạng thái đơn hàng");
        return "redirect:/admin/orders";
    }
}
