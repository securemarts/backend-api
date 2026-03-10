package com.securemarts.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Configuration
public class CorsConfig {

    @Value("${app.cors.allowed-origins:http://localhost:3000,http://localhost:5173,http://127.0.0.1:3000,http://127.0.0.1:5173}")
    private String allowedOriginsConfig;

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);
        List<String> origins = parseOrigins(allowedOriginsConfig);
        // Patterns allow any localhost port (e.g. :5174). Always include wildcards so dev works without config.
        List<String> patterns = new java.util.ArrayList<>(List.of("http://localhost:*", "http://127.0.0.1:*"));
        for (String o : origins) {
            if (!patterns.contains(o)) patterns.add(o);
        }
        config.setAllowedOriginPatterns(patterns);
        config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS", "HEAD"));
        config.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "Accept", "X-Requested-With", "Origin", "Cache-Control"));
        config.setExposedHeaders(List.of("Authorization", "Content-Type"));
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    /**
     * Ensures CORS headers are on every response (including 401/403). Runs early so error responses still have CORS.
     */
    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public OncePerRequestFilter corsResponseHeadersFilter() {
        return new OncePerRequestFilter() {
            @Override
            protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
                String origin = request.getHeader("Origin");
                if (origin != null && !origin.isBlank() && isOriginAllowed(origin)) {
                    response.setHeader("Access-Control-Allow-Origin", origin);
                    response.setHeader("Access-Control-Allow-Credentials", "true");
                    response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, PATCH, DELETE, OPTIONS, HEAD");
                    response.setHeader("Access-Control-Allow-Headers", "Authorization, Content-Type, Accept, X-Requested-With, Origin, Cache-Control");
                    response.setHeader("Access-Control-Max-Age", "3600");
                    if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
                        response.setStatus(HttpServletResponse.SC_OK);
                        return;
                    }
                }
                filterChain.doFilter(request, response);
            }

            private boolean isOriginAllowed(String origin) {
                if (origin.startsWith("http://localhost:") || origin.startsWith("http://127.0.0.1:")) return true;
                List<String> allowed = parseOrigins(allowedOriginsConfig);
                return allowed.isEmpty() || allowed.stream().anyMatch(o -> origin.equals(o) || (o.contains("*") && origin.matches(o.replace(".", "\\.").replace("*", ".*")));
            }
        };
    }

    private static List<String> parseOrigins(String value) {
        if (value == null || value.isBlank()) {
            return List.of("http://localhost:3000", "http://localhost:5173", "http://127.0.0.1:3000", "http://127.0.0.1:5173");
        }
        return Arrays.stream(value.split(","))
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .toList();
    }
}
