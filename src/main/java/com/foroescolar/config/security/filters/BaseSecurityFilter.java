package com.foroescolar.config.security.filters;

import com.foroescolar.exceptions.security.filters.FilterErrorHandler;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public abstract class BaseSecurityFilter extends OncePerRequestFilter {
    private final FilterErrorHandler errorHandler;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();
    private final List<String> publicPaths;

    protected BaseSecurityFilter(FilterErrorHandler errorHandler, String... publicPaths) {
        this.errorHandler = errorHandler;
        this.publicPaths = Arrays.asList(publicPaths);
    }

    protected FilterErrorHandler getErrorHandler() {
        return errorHandler;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String requestPath = request.getRequestURI();
        return publicPaths.stream()
                .anyMatch(pattern -> pathMatcher.match(pattern, requestPath));
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws IOException {

        try {
            doFilterInternal(new SecurityFilterContext(request, response, filterChain));
        } catch (IllegalArgumentException e) {
            errorHandler.handleException(e, response, "Argumento inválido: " + e.getMessage());
        } catch (ServletException e) {
            errorHandler.handleException(e, response, "Error de servlet: " + e.getMessage());
        } catch (Exception e) {
            errorHandler.handleException(e, response, "Error inesperado: " + e.getMessage());
        }
    }

    protected abstract void doFilterInternal(SecurityFilterContext context)
            throws ServletException, IOException;
}