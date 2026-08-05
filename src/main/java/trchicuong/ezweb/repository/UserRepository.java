package trchicuong.ezweb.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import trchicuong.ezweb.models.User;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);

    @Modifying
    @Query("UPDATE User u SET u.balance = u.balance + :amount WHERE u.id = :id")
    int addBalance(Long id, Long amount);

    @Modifying
    @Query("UPDATE User u SET u.balance = u.balance - :amount WHERE u.id = :id AND u.balance >= :amount")
    int deductBalance(Long id, Long amount);
}
