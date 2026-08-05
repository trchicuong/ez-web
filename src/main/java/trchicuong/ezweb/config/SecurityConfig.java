package trchicuong.ezweb.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import trchicuong.ezweb.services.UserService;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final UserService userService;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(csrf -> csrf
                        .ignoringRequestMatchers("/api/webhook/**")
                )
                .authorizeHttpRequests(auth -> auth
                        // Static resources
                        .requestMatchers("/css/**", "/js/**", "/images/**", "/favicon**", "/site.webmanifest").permitAll()
                        // Public pages
                        .requestMatchers("/", "/products", "/product/**", "/search", "/login", "/register").permitAll()
                        .requestMatchers("/error", "/403").permitAll()
                        // Webhook (no CSRF)
                        .requestMatchers("/api/webhook/**").permitAll()
                        // Download by token is public — token itself is the credential
                        .requestMatchers("/download/**").permitAll()
                        // Admin area
                        .requestMatchers("/admin/**").hasAuthority("ADMIN")
                        // Authenticated user area
                        .requestMatchers("/cart/**", "/orders/**", "/account/**", "/topup/**", "/chat/**").hasAnyAuthority("ADMIN", "USER")
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .loginProcessingUrl("/login")
                        .defaultSuccessUrl("/", true)
                        .failureUrl("/login?error")
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout")
                        .deleteCookies("JSESSIONID")
                        .invalidateHttpSession(true)
                        .permitAll()
                )
                .rememberMe(remember -> remember
                        .key("ezweb-secret-key")
                        .rememberMeCookieName("ezweb-remember")
                        .tokenValiditySeconds(7 * 24 * 60 * 60)
                        .userDetailsService(userService)
                )
                .exceptionHandling(ex -> ex
                        .accessDeniedPage("/403")
                )
                .sessionManagement(session -> session
                        .maximumSessions(3)
                        .expiredUrl("/login?expired")
                )
                .build();
    }
}
