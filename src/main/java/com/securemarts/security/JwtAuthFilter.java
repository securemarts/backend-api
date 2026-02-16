package com.securemarts.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private static final String HEADER = "Authorization";
    private static final String PREFIX = "Bearer ";

    private final JwtService jwtService;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {
        try {
            String token = extractToken(request);
            if (StringUtils.hasText(token) && jwtService.validateAccessToken(token)) {
                JwtService.JwtClaims claims = jwtService.parseAccessToken(token);
                var authorities = claims.getRoles().stream()
                        .map(r -> new SimpleGrantedAuthority("ROLE_" + r))
                        .collect(Collectors.toList());
                claims.getScopes().stream()
                        .map(s -> new SimpleGrantedAuthority("SCOPE_" + s))
                        .forEach(authorities::add);
                var auth = new UsernamePasswordAuthenticationToken(
                        claims.getSubject(),
                        null,
                        authorities);
                auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(auth);
                if (claims.getStoreId() != null) {
                    CurrentTenant.setStoreId(claims.getStoreId());
                }
            }
            filterChain.doFilter(request, response);
        } finally {
            CurrentTenant.clear();
        }
    }

    private String extractToken(HttpServletRequest request) {
        String bearer = request.getHeader(HEADER);
        if (StringUtils.hasText(bearer) && bearer.startsWith(PREFIX)) {
            return bearer.substring(PREFIX.length());
        }
        return null;
    }
}
