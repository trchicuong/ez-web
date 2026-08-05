package trchicuong.ezweb.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import trchicuong.ezweb.services.ProductService;

@Controller
@RequiredArgsConstructor
public class HomeController {

    private final ProductService productService;

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("featured", productService.findFeatured());
        model.addAttribute("onSale", productService.findOnSale());
        model.addAttribute("activeCategory", "");
        return "pages/home";
    }

    @GetMapping("/403")
    public String forbidden() {
        return "errors/403";
    }
}
