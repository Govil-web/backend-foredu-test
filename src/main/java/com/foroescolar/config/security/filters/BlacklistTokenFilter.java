package com.foroescolar.config.security.filters;

import com.foroescolar.exceptions.security.filters.token.TokenInvalidatedException;
import com.foroescolar.exceptions.security.filters.FilterErrorHandler;
import com.foroescolar.services.TokenService;
import com.foroescolar.utils.token.TokenExtractor;
import jakarta.servlet.ServletException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Order(3)
@Slf4j
public class BlacklistTokenFilter extends BaseSecurityFilter {
    private final TokenService tokenService;

    private final Set<String> blacklistCache = ConcurrentHashMap.newKeySet();

    private volatile long lastSyncTime = 0;

    private static final long SYNC_INTERVAL = 300000;

    public BlacklistTokenFilter(
            FilterErrorHandler errorHandler,
            TokenService tokenService) {
        super(errorHandler, "/api/auth/login", "/swagger-ui.html", "/v3/api-docs/**", "/swagger-ui/**");
        this.tokenService = tokenService;

        syncBlacklist();
    }

    @Override
    protected void doFilterInternal(SecurityFilterContext context) throws ServletException, IOException {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastSyncTime > SYNC_INTERVAL) {
            syncBlacklist();
        }

        Optional<String> token = TokenExtractor.extractFromRequest(context.request());

        if (token.isPresent()) {
            String tokenValue = token.get();

            if (blacklistCache.contains(tokenValue)) {
                throw new TokenInvalidatedException("Token ha sido invalidado");
            }

            if (currentTime - lastSyncTime > 60000) { // 1 minuto
                if (tokenService.isTokenInBlacklist(tokenValue)) {
                    blacklistCache.add(tokenValue);
                    throw new TokenInvalidatedException("Token ha sido invalidado");
                }
            }

            if (log.isDebugEnabled()) {
                log.debug("Token verificado contra lista negra: OK");
            }
        }

        context.filterChain().doFilter(context.request(), context.response());
    }

    @Scheduled(fixedRate = 300000)
    public void syncBlacklist() {
        try {
            Set<String> persistentBlacklist = tokenService.getAllBlacklistedTokens();

            blacklistCache.clear();
            blacklistCache.addAll(persistentBlacklist);

            lastSyncTime = System.currentTimeMillis();
            log.debug("Lista negra sincronizada, {} tokens en caché", blacklistCache.size());
        } catch (Exception e) {
            log.error("Error sincronizando lista negra de tokens", e);
        }
    }

    public void addToBlacklist(String token) {
        blacklistCache.add(token);
    }
}