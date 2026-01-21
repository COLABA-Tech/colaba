package com.example.colaba.shared.webmvc.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Slf4j
public class InternalAuthenticationFilter extends OncePerRequestFilter {

    private final String internalApiKey;

    public InternalAuthenticationFilter(String internalApiKey) {
        this.internalApiKey = internalApiKey;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String path = request.getRequestURI();
        log.info("Processing request: {}", path);

        String key = request.getHeader("X-Internal-Key");
        log.info("Internal key: {}", key);

        if (key == null || !key.equals(internalApiKey)) {
            log.warn("Invalid or missing internal key for path: {}", path);
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            return;
        }

        log.info("Internal key validated for path: {}", path);
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(
                        "internal-service",
                        null,
                        List.of(new SimpleGrantedAuthority("ROLE_INTERNAL"))
                );

        SecurityContextHolder.getContext().setAuthentication(auth);

        filterChain.doFilter(request, response);

        SecurityContextHolder.clearContext();
    }
}