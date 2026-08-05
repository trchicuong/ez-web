package trchicuong.ezweb.models;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "topup_logs")
public class TopupLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(nullable = false)
    @Builder.Default
    private Long amount = 0L;

    @Column(name = "transfer_code", length = 100)
    @Builder.Default
    private String transferCode = "";

    @Column(nullable = false, length = 30)
    @Builder.Default
    private String provider = "sepay";

    @Column(name = "provider_id", length = 120)
    @Builder.Default
    private String providerId = "";

    /** matched | unmatched */
    @Column(nullable = false, length = 20)
    @Builder.Default
    private String status = "unmatched";

    @Column(name = "raw_data", columnDefinition = "TEXT")
    private String rawData;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) createdAt = LocalDateTime.now();
    }
}
