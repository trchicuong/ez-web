package trchicuong.ezweb.controllers.admin;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import trchicuong.ezweb.models.Category;
import trchicuong.ezweb.repository.ProductRepository;
import trchicuong.ezweb.services.CategoryService;

import java.util.List;

@Controller
@RequestMapping("/admin/categories")
@RequiredArgsConstructor
public class AdminCategoryController {

    private final CategoryService categoryService;
    private final ProductRepository productRepository;

    @GetMapping
    public String list(Model model) {
        List<Category> categories = categoryService.findAll();
        // Set product count cho từng category
        categories.forEach(cat -> cat.setProductCount(productRepository.countByCategory(cat)));
        model.addAttribute("categories", categories);
        model.addAttribute("newCategory", new Category());
        model.addAttribute("pageTitle", "Quản lý danh mục");
        return "admin/categories";
    }

    @PostMapping("/save")
    public String save(@RequestParam(required = false) Long id,
                        @RequestParam String name,
                        @RequestParam String slug,
                        @RequestParam(defaultValue = "ph ph-folder") String icon,
                        @RequestParam(defaultValue = "0") Integer sortOrder,
                        RedirectAttributes redirectAttributes) {
        Category category = id != null
                ? categoryService.findById(id).orElse(new Category())
                : new Category();
        category.setName(name.trim());
        category.setSlug(slug.trim().toLowerCase().replaceAll("[^a-z0-9-]", "-"));
        category.setIcon(icon.trim());
        category.setSortOrder(sortOrder);
        categoryService.save(category);
        redirectAttributes.addFlashAttribute("successMessage", "Lưu danh mục thành công");
        return "redirect:/admin/categories";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        categoryService.deleteById(id);
        redirectAttributes.addFlashAttribute("successMessage", "Đã xóa danh mục");
        return "redirect:/admin/categories";
    }
}
