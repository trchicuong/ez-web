package trchicuong.ezweb.models;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "products")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, unique = true, length = 220)
    private String slug;

    @Column(nullable = false, length = 300)
    @Builder.Default
    private String summary = "";

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(length = 500)
    @Builder.Default
    private String thumbnail = "";

    /** Giá gốc (VND) */
    @Column(nullable = false)
    @Builder.Default
    private Long price = 0L;

    /** Giá khuyến mãi (null = không giảm giá) */
    @Column(name = "sale_price")
    private Long salePrice;

    @Column(name = "demo_url", length = 300)
    @Builder.Default
    private String demoUrl = "";

    /** Đường dẫn file ZIP để download sau khi mua */
    @Column(name = "file_path", length = 300)
    @Builder.Default
    private String filePath = "";

    @Column(nullable = false)
    @Builder.Default
    private Integer downloads = 0;

    /** active | draft | inactive */
    @Column(nullable = false, length = 20)
    @Builder.Default
    private String status = "draft";

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "product_tags", joinColumns = @JoinColumn(name = "product_id"))
    @Column(name = "name")
    @Builder.Default
    private List<String> tags = new ArrayList<>();

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    /** Trả về giá hiệu lực (ưu tiên salePrice) */
    public long getEffectivePrice() {
        return (salePrice != null && salePrice > 0) ? salePrice : price;
    }

    public boolean isOnSale() {
        return salePrice != null && salePrice > 0 && salePrice < price;
    }
}
