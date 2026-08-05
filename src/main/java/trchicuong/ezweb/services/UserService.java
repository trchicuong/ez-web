package trchicuong.ezweb.services;

import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import trchicuong.ezweb.models.User;
import trchicuong.ezweb.repository.UserRepository;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, @Lazy PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /** Spring Security: load by email */
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy tài khoản: " + email));
    }

    public User register(User user) {
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new IllegalArgumentException("Email đã được sử dụng");
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRole("USER");
        return userRepository.save(user);
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    public User save(User user) {
        return userRepository.save(user);
    }

    public List<User> findAll() {
        return userRepository.findAll();
    }

    public org.springframework.data.domain.Page<User> findAllPaged(org.springframework.data.domain.Pageable pageable) {
        return userRepository.findAll(pageable);
    }

    public long count() {
        return userRepository.count();
    }

    @Transactional(readOnly = true)
    public List<User> findRecent(org.springframework.data.domain.Pageable pageable) {
        return userRepository.findAll(pageable).getContent();
    }

    public void changePassword(User user, String rawNewPassword) {
        user.setPassword(passwordEncoder.encode(rawNewPassword));
        userRepository.save(user);
    }

    public boolean checkPassword(User user, String rawPassword) {
        return passwordEncoder.matches(rawPassword, user.getPassword());
    }

    /** Cộng số dư ví (gọi trong transaction) */
    @Transactional
    public void addBalance(Long userId, Long amount) {
        int updated = userRepository.addBalance(userId, amount);
        if (updated == 0) throw new IllegalStateException("Không thể cập nhật số dư");
    }

    /** Trừ số dư ví — trả false nếu không đủ tiền */
    @Transactional
    public boolean deductBalance(Long userId, Long amount) {
        return userRepository.deductBalance(userId, amount) > 0;
    }
}
