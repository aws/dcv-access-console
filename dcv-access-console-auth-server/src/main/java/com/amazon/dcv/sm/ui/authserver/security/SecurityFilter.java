package com.amazon.dcv.sm.ui.authserver.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@AllArgsConstructor
public class SecurityFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        response.setHeader(
                "Content-Security-Policy",
                "frame-ancestors 'none';" + "form-action 'self';"
                        + "block-all-mixed-content;"
                        + "upgrade-insecure-requests;"
                        + "base-uri 'self';"
                        + "object-src 'none';"
                        + "font-src 'self' data:;"
                        + "img-src 'self' blob: data:;"
                        + "style-src 'self' 'unsafe-inline';");
        filterChain.doFilter(request, response);
    }
}
