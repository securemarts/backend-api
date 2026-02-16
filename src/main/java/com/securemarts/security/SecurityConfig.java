package com.securemarts.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;

    private static final String[] PUBLIC = {
            "/auth/register",
            "/auth/login",
            "/auth/refresh",
            "/auth/verify-email",
            "/auth/verify-email/resend",
            "/auth/verify-phone",
            "/auth/reset-password/request",
            "/auth/reset-password/confirm",
            "/rider/auth/register",
            "/rider/auth/login",
            "/rider/auth/refresh",
            "/rider/auth/logout",
            "/rider/auth/verify-email",
            "/rider/auth/verify-email/resend",
            "/api-docs/**",
            "/swagger-ui/**",
            "/swagger-ui.html"
    };

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(e -> e
                        .authenticationEntryPoint((HttpServletRequest request, HttpServletResponse response,
                                org.springframework.security.core.AuthenticationException authException) -> {
                            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                            response.setContentType("application/json");
                            try {
                                response.getWriter().write("{\"error\":\"Unauthorized\",\"message\":\"Authentication required\"}");
                            } catch (IOException ex) {
                                throw new RuntimeException(ex);
                            }
                        }))
                .authorizeHttpRequests(a -> a
                        .requestMatchers(PUBLIC).permitAll()
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/public/**").permitAll()
                        .requestMatchers("/storefront/**").permitAll()
                        .requestMatchers("/discovery/**").permitAll()
                        .requestMatchers("/stores/*/cart/**").permitAll()
                        .requestMatchers("/stores/*/pricing/apply").permitAll()
                        .requestMatchers("/webhooks/**").permitAll()
                        .requestMatchers("/admin/auth/login", "/admin/auth/complete-setup").permitAll()
                        .requestMatchers("/admin/**").hasAnyRole("PLATFORM_ADMIN", "SUPERUSER", "SUPPORT")
                        .requestMatchers("/rider/**").hasRole("RIDER")
                        .requestMatchers("/stores/*/settings/**").hasAnyRole("MERCHANT_OWNER", "MERCHANT_STAFF")
                        .requestMatchers("/onboarding/**").authenticated()
                        .anyRequest().authenticated())
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }
}
