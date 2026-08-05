package trchicuong.ezweb.controllers.admin;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import trchicuong.ezweb.services.CategoryService;
import trchicuong.ezweb.services.OrderService;
import trchicuong.ezweb.services.ProductService;
import trchicuong.ezweb.services.UserService;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminDashboardController {

    private final UserService userService;
    private final ProductService productService;
    private final OrderService orderService;
    private final CategoryService categoryService;

    @GetMapping({"", "/"})
    public String dashboard(Model model) {
        model.addAttribute("totalUsers", userService.count());
        model.addAttribute("totalProducts", productService.countAll());
        model.addAttribute("totalCategories", categoryService.count());
        model.addAttribute("totalOrders", orderService.countAll());
        model.addAttribute("totalCompleted", orderService.countCompleted());
        model.addAttribute("totalRevenue", orderService.sumRevenue());
        
        // Recent data
        PageRequest recentSort = PageRequest.of(0, 5, Sort.by("createdAt").descending());
        model.addAttribute("recentOrders", orderService.findRecent(recentSort));
        model.addAttribute("recentUsers", userService.findRecent(recentSort));
        
        model.addAttribute("pageTitle", "Dashboard");
        return "admin/dashboard";
    }
}
