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
@Table(name = "categories")
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, unique = true, length = 110)
    private String slug;

    /** Phosphor icon class, e.g. "ph ph-code" */
    @Column(nullable = false, length = 60)
    @Builder.Default
    private String icon = "ph ph-folder";

    @Column(name = "sort_order", nullable = false)
    @Builder.Default
    private Integer sortOrder = 0;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "category", fetch = FetchType.LAZY)
    @Builder.Default
    private List<Product> products = new ArrayList<>();

    /** Số lượng sản phẩm trong category (transient field) */
    @Transient
    private long productCount;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
