package trchicuong.ezweb.controllers.admin;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import trchicuong.ezweb.models.Category;
import trchicuong.ezweb.models.Product;
import trchicuong.ezweb.services.CategoryService;
import trchicuong.ezweb.services.ProductService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Controller
@RequestMapping("/admin/products")
@RequiredArgsConstructor
public class AdminProductController {

    private final ProductService productService;
    private final CategoryService categoryService;

    @GetMapping
    public String list(@RequestParam(defaultValue = "0") int page, Model model) {
        model.addAttribute("products", productService.findAllForAdmin(
                PageRequest.of(page, 20, org.springframework.data.domain.Sort.by("createdAt").descending())));
        model.addAttribute("pageTitle", "Quản lý sản phẩm");
        return "admin/products";
    }

    @GetMapping("/new")
    public String newForm(Model model) {
        model.addAttribute("product", new Product());
        model.addAttribute("categories", categoryService.findAll());
        model.addAttribute("pageTitle", "Thêm sản phẩm");
        return "admin/product-form";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        Product product = productService.findById(id).orElse(null);
        if (product == null) return "redirect:/admin/products";
        model.addAttribute("product", product);
        model.addAttribute("categories", categoryService.findAll());
        model.addAttribute("pageTitle", "Sửa sản phẩm");
        return "admin/product-form";
    }

    @PostMapping("/save")
    public String save(@RequestParam(required = false) Long id,
                        @RequestParam Long categoryId,
                        @RequestParam String title,
                        @RequestParam String slug,
                        @RequestParam String summary,
                        @RequestParam String description,
                        @RequestParam Long price,
                        @RequestParam(required = false) Long salePrice,
                        @RequestParam(defaultValue = "") String demoUrl,
                        @RequestParam(defaultValue = "draft") String status,
                        @RequestParam(defaultValue = "") String thumbnail,
                        @RequestParam(defaultValue = "") String tags,
                        @RequestParam(defaultValue = "") String filePath,
                        RedirectAttributes redirectAttributes) {

        Category category = categoryService.findById(categoryId).orElse(null);
        if (category == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Danh mục không hợp lệ");
            return "redirect:/admin/products";
        }

        Product product = id != null
                ? productService.findById(id).orElse(new Product())
                : new Product();

        product.setCategory(category);
        product.setTitle(title.trim());
        product.setSlug(slug.trim().toLowerCase().replaceAll("[^a-z0-9-]", "-"));
        product.setSummary(summary.trim());
        product.setDescription(description);
        product.setPrice(price);
        product.setSalePrice((salePrice != null && salePrice > 0) ? salePrice : null);
        product.setDemoUrl(demoUrl.trim());
        product.setStatus(status);
        product.setThumbnail(thumbnail.trim());
        product.setFilePath(filePath.trim());

        List<String> tagList = new ArrayList<>(Arrays.stream(tags.split(","))
                .map(String::trim).filter(t -> !t.isBlank()).toList());
        product.setTags(tagList);

        productService.save(product);
        redirectAttributes.addFlashAttribute("successMessage", "Lưu sản phẩm thành công");
        return "redirect:/admin/products";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        productService.deleteById(id);
        redirectAttributes.addFlashAttribute("successMessage", "Đã xóa sản phẩm");
        return "redirect:/admin/products";
    }
}
