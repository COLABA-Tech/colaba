package com.example.colaba.file;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class InternalApiKeyFilter extends OncePerRequestFilter {

    @Value("${internal.api-key}")
    private String expectedKey;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String uri = request.getRequestURI();


        if (uri.startsWith("/internal")) {
            String providedKey = request.getHeader("X-Internal-Key");

            if (providedKey == null || !providedKey.equals(expectedKey)) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("text/plain");
                response.getWriter().write("Invalid or missing internal API key");
                return;
            }
        }

        filterChain.doFilter(request, response);
    }
}