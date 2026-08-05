package trchicuong.ezweb.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import trchicuong.ezweb.models.DownloadToken;

import java.util.Optional;

public interface DownloadTokenRepository extends JpaRepository<DownloadToken, Long> {
    Optional<DownloadToken> findByToken(String token);
}
