package com.foroescolar.config.security.filters;

import com.foroescolar.exceptions.security.filters.FilterErrorHandler;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Random;
import java.util.UUID;
import java.util.regex.Pattern;

@Component
@Order(1)
@Slf4j
public class RequestLoggingFilter extends BaseSecurityFilter {
    private final Pattern staticResourcePattern = Pattern.compile(".+\\.(css|js|html|png|jpg|jpeg|gif|ico|svg)$");
    private final Random random = new Random();

    @Value("${logging.sample-rate:1.0}")
    private double loggingSampleRate;

    public RequestLoggingFilter(FilterErrorHandler errorHandler) {
        super(errorHandler, "/swagger-ui/**", "/v3/api-docs/**");
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();

        if (staticResourcePattern.matcher(path).matches()) {
            return true;
        }

        return super.shouldNotFilter(request);
    }

    @Override
    protected void doFilterInternal(SecurityFilterContext context) throws ServletException, IOException {
        boolean shouldLog = random.nextDouble() < loggingSampleRate;

        if (shouldLog) {
            String requestId = UUID.randomUUID().toString();
            MDC.put("requestId", requestId);

            String userAgent = context.request().getHeader("User-Agent");
            String method = context.request().getMethod();
            String uri = context.request().getRequestURI();

            if (method.equals("GET") && isLowValueEndpoint(uri)) {
                log.debug("Request: {} {} - ID: {} - UA: {}",
                        method, uri, requestId, abbreviateUserAgent(userAgent));
            } else {
                log.info("Iniciando request: {} {} - RequestId: {} - IP: {}",
                        method, uri, requestId, getClientIp(context.request()));
            }

            long startTime = System.currentTimeMillis();

            try {
                context.filterChain().doFilter(context.request(), context.response());
            } finally {
                long duration = System.currentTimeMillis() - startTime;
                int status = context.response().getStatus();

                if (status >= 400 || duration > 1000) {
                    log.warn("Completado request: {} {} - Status: {} - Duración: {}ms",
                            method, uri, status, duration);
                } else if (log.isDebugEnabled()) {
                    log.debug("Completado request: {} - Duración: {}ms", requestId, duration);
                }

                MDC.clear();
            }
        } else {
            context.filterChain().doFilter(context.request(), context.response());
        }
    }

    private boolean isLowValueEndpoint(String uri) {
        return uri.startsWith("/api/health") ||
                uri.contains("/status") ||
                uri.contains("/metrics");
    }

    private String abbreviateUserAgent(String userAgent) {
        if (userAgent == null || userAgent.length() <= 30) {
            return userAgent;
        }
        return userAgent.substring(0, 27) + "...";
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }
}