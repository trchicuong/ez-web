package trchicuong.ezweb.controllers;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import trchicuong.ezweb.models.Product;
import trchicuong.ezweb.models.User;
import trchicuong.ezweb.services.CartService;
import trchicuong.ezweb.services.OrderService;
import trchicuong.ezweb.services.ProductService;

@Controller
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;
    private final OrderService orderService;
    private final CartService cartService;

    @GetMapping("/products")
    public String list(@RequestParam(defaultValue = "") String category,
                       @RequestParam(required = false) String latest,
                       @RequestParam(required = false) String sale,
                       @RequestParam(required = false) String free,
                       @RequestParam(defaultValue = "0") int page,
                       Model model) {
        PageRequest pageable = PageRequest.of(page, 12);
        Page<Product> products;
        
        // Filter logic
        if (sale != null) {
            products = productService.findOnSale(pageable);
        } else if (free != null) {
            products = productService.findFree(pageable);
        } else if (latest != null) {
            products = productService.findLatest(pageable);
        } else if (!category.isBlank()) {
            products = productService.findByCategory(category, pageable);
        } else {
            products = productService.findActive(pageable);
        }

        model.addAttribute("products", products);
        model.addAttribute("activeCategory", category);
        model.addAttribute("activeSale", sale != null);
        model.addAttribute("activeFree", free != null);
        model.addAttribute("activeLatest", latest != null);
        model.addAttribute("pageTitle", 
            latest != null ? "Sản phẩm mới nhất" :
            sale != null ? "Sản phẩm giảm giá" :
            free != null ? "Sản phẩm miễn phí" :
            category.isBlank() ? "Tất cả source code" : "Danh mục");
        return "pages/products";
    }

    @GetMapping("/product/{slug}")
    public String detail(@PathVariable String slug,
                         HttpSession session,
                         @AuthenticationPrincipal User currentUser,
                         Model model) {
        Product product = productService.findBySlug(slug)
                .orElse(null);
        if (product == null) return "errors/404";

        boolean hasPurchased = false;
        Long purchasedOrderId = null;
        boolean inCart = false;

        if (currentUser != null) {
            hasPurchased = orderService.hasUserPurchased(currentUser.getId(), product.getId());
            if (hasPurchased) {
                purchasedOrderId = orderService
                        .findCompletedOrderByProduct(currentUser.getId(), product.getId())
                        .map(o -> o.getId()).orElse(null);
            }
            inCart = cartService.isInCart(session, product.getId());
        }

        model.addAttribute("product", product);
        model.addAttribute("related", productService.findRelated(product.getCategory().getSlug(), product.getId()));
        model.addAttribute("hasPurchased", hasPurchased);
        model.addAttribute("purchasedOrderId", purchasedOrderId);
        model.addAttribute("inCart", inCart);
        model.addAttribute("pageTitle", product.getTitle());
        return "pages/product-detail";
    }

    @GetMapping("/search")
    public String search(@RequestParam(defaultValue = "") String q,
                         @RequestParam(defaultValue = "0") int page,
                         Model model) {
        Page<Product> results = q.isBlank()
                ? Page.empty()
                : productService.search(q, PageRequest.of(page, 12));

        model.addAttribute("products", results);
        model.addAttribute("q", q);
        model.addAttribute("pageTitle", "Tìm kiếm: " + q);
        return "pages/search";
    }
}
