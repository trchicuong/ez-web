package trchicuong.ezweb.services;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import trchicuong.ezweb.models.Product;
import trchicuong.ezweb.repository.ProductRepository;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class ProductService {

    private final ProductRepository productRepository;

    @Transactional(readOnly = true)
    public Page<Product> findActive(Pageable pageable) {
        return productRepository.findByStatus("active", pageable);
    }

    @Transactional(readOnly = true)
    public Page<Product> findByCategory(String categorySlug, Pageable pageable) {
        return productRepository.findByCategorySlugAndStatus(categorySlug, "active", pageable);
    }

    @Transactional(readOnly = true)
    public Page<Product> search(String q, Pageable pageable) {
        return productRepository.search(q, pageable);
    }

    @Transactional(readOnly = true)
    public Optional<Product> findBySlug(String slug) {
        return productRepository.findBySlug(slug);
    }

    @Transactional(readOnly = true)
    public Optional<Product> findById(Long id) {
        return productRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public List<Product> findFeatured() {
        return productRepository.findTop8ByStatusOrderByCreatedAtDesc("active");
    }

    @Transactional(readOnly = true)
    public List<Product> findOnSale() {
        return productRepository.findTop8ByStatusAndSalePriceIsNotNullOrderByCreatedAtDesc("active");
    }

    @Transactional(readOnly = true)
    public Page<Product> findOnSale(Pageable pageable) {
        return productRepository.findByStatusAndSalePriceIsNotNull("active", pageable);
    }

    @Transactional(readOnly = true)
    public Page<Product> findFree(Pageable pageable) {
        return productRepository.findByStatusAndPrice("active", 0L, pageable);
    }

    @Transactional(readOnly = true)
    public Page<Product> findLatest(Pageable pageable) {
        return productRepository.findByStatusOrderByCreatedAtDesc("active", pageable);
    }

    @Transactional(readOnly = true)
    public List<Product> findRelated(String categorySlug, Long excludeId) {
        return productRepository.findTop4ByCategorySlugAndStatusAndIdNot(categorySlug, "active", excludeId);
    }

    public Product save(Product product) {
        return productRepository.save(product);
    }

    public void deleteById(Long id) {
        productRepository.deleteById(id);
    }

    public void incrementDownloads(Long id) {
        productRepository.incrementDownloads(id);
    }

    public long countActive() {
        return productRepository.countByStatus("active");
    }

    public long countAll() {
        return productRepository.count();
    }

    @Transactional(readOnly = true)
    public Page<Product> findAllForAdmin(Pageable pageable) {
        return productRepository.findAll(pageable);
    }
}
