package com.foroescolar.services.impl;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;

import com.foroescolar.exceptions.security.ErrorCode;
import com.foroescolar.exceptions.security.filters.token.*;
import com.foroescolar.model.User;
import com.foroescolar.services.TokenService;

import io.lettuce.core.RedisConnectionException;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;
import io.micrometer.core.instrument.Timer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@Service
public class TokenServiceImpl implements TokenService {

    @Value("${api.security.secret}")
    private String apiSecret;

    @Value("${api.jwt.verifier.pool-size:4}")
    private int verifierPoolSize;

    private final RedisTemplate<String, String> redisTemplate;
    private final MeterRegistry meterRegistry;

    // Métricas
    private Timer tokenGenerationTimer;
    private Timer tokenValidationTimer;
    private Counter tokenGenerationCounter;
    private Counter tokenValidationCounter;
    private Counter tokenValidationFailedCounter;
    private Counter tokenBlacklistCounter;
    private final AtomicLong cacheHitRatio = new AtomicLong(0);
    private final AtomicLong cacheMissRatio = new AtomicLong(0);

    // Pool de verificadores para mejor concurrencia
    private JWTVerifier[] verifierPool;

    // Cache para tokens decodificados (evitar decodificación repetida)
    private final Map<String, DecodedJWT> tokenCache = new ConcurrentHashMap<>();

    // Cache para tokens en lista negra (memoria local)
    private final Set<String> blacklistCache = ConcurrentHashMap.newKeySet();

    // Timestamp de última sincronización con Redis
    private volatile long lastBlacklistSync = 0;

    private static final String TOKEN_BLACKLIST_PREFIX = "blacklist:";
    private static final long TOKEN_BLACKLIST_DURATION = 24;
    private static final String FORO_ESCOLAR = "Foro Escolar";
    private static final long BLACKLIST_SYNC_INTERVAL = 60000; // 1 minuto
    private static final long CACHE_CLEANUP_INTERVAL = 300000; // 5 minutos
    private static final int MAX_CACHE_SIZE = 10000;

    // Ejecutor para tareas asíncronas
    private final ExecutorService asyncTaskExecutor = Executors.newSingleThreadExecutor();


    public TokenServiceImpl(RedisTemplate<String, String> redisTemplate, MeterRegistry meterRegistry) {
        this.redisTemplate = redisTemplate;
        this.meterRegistry = meterRegistry;
        initializeMetrics();
    }

    @EventListener(ContextRefreshedEvent.class)
    public void initialize() {
        initializeVerifierPool();
        syncBlacklistFromRedis();
        scheduleCacheCleanup();
    }

    private void initializeMetrics() {
        this.tokenGenerationTimer = Timer.builder("security.token.generation")
                .description("Tiempo de generación de tokens")
                .register(meterRegistry);

        this.tokenValidationTimer = Timer.builder("security.token.validation")
                .description("Tiempo de validación de tokens")
                .register(meterRegistry);

        this.tokenGenerationCounter = Counter.builder("security.token.generation.count")
                .description("Número de tokens generados")
                .register(meterRegistry);

        this.tokenValidationCounter = Counter.builder("security.token.validation.count")
                .description("Número de validaciones de tokens")
                .register(meterRegistry);

        this.tokenValidationFailedCounter = Counter.builder("security.token.validation.failed")
                .description("Número de validaciones de tokens fallidas")
                .register(meterRegistry);

        this.tokenBlacklistCounter = Counter.builder("security.token.blacklist.count")
                .description("Número de tokens añadidos a la lista negra")
                .register(meterRegistry);

        // Gauge para el ratio de aciertos de caché
        meterRegistry.gauge("security.token.cache.hit.ratio", cacheHitRatio);
        meterRegistry.gauge("security.token.cache.miss.ratio", cacheMissRatio);

        // Gauge para tamaño de la caché
        meterRegistry.gaugeMapSize("security.token.cache.size", Tags.empty(), tokenCache);
        meterRegistry.gaugeCollectionSize("security.token.blacklist.size", Tags.empty(), blacklistCache);
    }

    private void initializeVerifierPool() {
        log.info("Inicializando pool de verificadores JWT con tamaño {}", verifierPoolSize);
        verifierPool = new JWTVerifier[verifierPoolSize];
        Algorithm algorithm = Algorithm.HMAC256(apiSecret);

        for (int i = 0; i < verifierPoolSize; i++) {
            verifierPool[i] = JWT.require(algorithm)
                    .withIssuer(FORO_ESCOLAR)
                    .build();
        }
    }


    private void scheduleCacheCleanup() {
        asyncTaskExecutor.submit(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    Thread.sleep(CACHE_CLEANUP_INTERVAL);
                    cleanupCaches();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                } catch (Exception e) {
                    log.error("Error en la limpieza programada de caché", e);
                }
            }
        });
    }

    private void cleanupCaches() {
        if (tokenCache.size() > MAX_CACHE_SIZE) {
            log.debug("Limpiando caché de tokens, tamaño actual: {}", tokenCache.size());

            // Eliminar tokens expirados
            long now = System.currentTimeMillis();
            tokenCache.entrySet().removeIf(entry -> {
                try {
                    Date expiresAt = entry.getValue().getExpiresAt();
                    return expiresAt != null && expiresAt.getTime() < now;
                } catch (Exception e) {
                    return true; // Si hay error, eliminar entrada
                }
            });

            // Si aún es demasiado grande, eliminar el 25% más antiguo
            if (tokenCache.size() > MAX_CACHE_SIZE) {
                int toRemove = tokenCache.size() / 4;
                tokenCache.keySet().stream()
                        .limit(toRemove)
                        .forEach(tokenCache::remove);
            }
        }
    }

    @Override
    public String generateToken(User user) {
        return tokenGenerationTimer.record(() -> {
            try {
                Algorithm algorithm = Algorithm.HMAC256(apiSecret);
                String token = JWT.create()
                        .withIssuer(FORO_ESCOLAR)
                        .withSubject(user.getEmail())
                        .withClaim("id", user.getId())
                        .withClaim("role", user.getRol().getAuthority())
                        .withClaim("nombre", user.getNombre())
                        .withJWTId(UUID.randomUUID().toString())
                        .withIssuedAt(new Date())
                        .withExpiresAt(Date.from(generateExpirationDate()))
                        .sign(algorithm);

                tokenGenerationCounter.increment();
                return token;
            } catch (JWTCreationException e) {
                log.error("Error al generar token JWT: {}", e.getMessage());
                throw new TokenMalformedException("Error al generar el token: " + e.getMessage());
            }
        });
    }

    @Override
    public String getUsernameFromToken(String token) {
        return tokenValidationTimer.record(() -> {
            tokenValidationCounter.increment();
            validateTokenNotEmpty(token);

            try {
                // 1. Primero validar estructura (sin verificar firma)
                DecodedJWT preDecoded = preValidateTokenStructure(token);

                // 2. Verificar si ya está en lista negra (evitar verificación innecesaria)
                if (isTokenBlacklisted(token)) {
                    tokenValidationFailedCounter.increment();
                    throw new TokenInvalidatedException("Token está en lista negra");
                }

                // 3. Verificar expiración (antes de verificar firma completa)
                if (isTokenExpired(preDecoded)) {
                    tokenValidationFailedCounter.increment();
                    throw new com.foroescolar.exceptions.security.filters.token.TokenExpiredException(
                            "El token ha expirado", preDecoded.getExpiresAt());
                }

                // 4. Verificar firma completa
                DecodedJWT decodedJWT = verifyToken(token.trim());

                // 5. Almacenar en caché para futuras validaciones
                tokenCache.put(token, decodedJWT);

                // 6. Extraer subject
                return extractSubject(decodedJWT);
            } catch (com.foroescolar.exceptions.security.filters.token.TokenExpiredException e) {
                throw e;
            } catch (JWTVerificationException e) {
                tokenValidationFailedCounter.increment();
                log.debug("Error al verificar token: {}", e.getMessage());
                throw new com.foroescolar.exceptions.security.filters.token.TokenExpiredException(
                        "El token ha expirado", getExpirationDateFromToken(token));
            } catch (Exception e) {
                tokenValidationFailedCounter.increment();
                log.debug("Error inesperado al procesar token: {}", e.getMessage());
                throw new TokenException("Error al procesar el token", ErrorCode.TOKEN_INVALID);
            }
        });
    }

    private DecodedJWT preValidateTokenStructure(String token) {
        try {
            // Solo decodificar sin verificar firma
            return JWT.decode(token.trim());
        } catch (Exception e) {
            throw new TokenMalformedException("Estructura de token inválida: " + e.getMessage());
        }
    }

    private boolean isTokenExpired(DecodedJWT jwt) {
        Date expiresAt = jwt.getExpiresAt();
        return expiresAt != null && expiresAt.before(new Date());
    }

    private void validateTokenNotEmpty(String token) {
        if (token == null || token.trim().isEmpty()) {
            throw new TokenException("Token vacío o nulo", ErrorCode.TOKEN_INVALID);
        }
    }

    private DecodedJWT verifyToken(String token) {
        // Verificar si está en caché primero
        DecodedJWT cachedJwt = tokenCache.get(token);
        if (cachedJwt != null) {
            cacheHitRatio.incrementAndGet();
            return cachedJwt;
        }

        cacheMissRatio.incrementAndGet();

        // Usar un verificador del pool (round-robin simple)
        int verifierIndex = Math.abs(token.hashCode() % verifierPoolSize);
        return verifierPool[verifierIndex].verify(token);
    }

    private String extractSubject(DecodedJWT jwt) {
        String subject = jwt.getSubject();
        if (subject == null) {
            throw new TokenException("Token no contiene subject", ErrorCode.TOKEN_INVALID);
        }
        return subject;
    }

    @Override
    public boolean validateToken(String token, UserDetails userDetails) {
        tokenValidationCounter.increment();

        if (isTokenBlacklisted(token)) {
            return false;
        }

        try {
            // Verificar primero si está en caché
            DecodedJWT cachedJwt = tokenCache.get(token);

            if (cachedJwt != null) {
                cacheHitRatio.incrementAndGet();

                // Verificar expiración desde caché
                if (isTokenExpired(cachedJwt)) {
                    tokenCache.remove(token); // Eliminar de caché si expirado
                    return false;
                }

                // Verificar username desde caché
                String username = cachedJwt.getSubject();
                return username != null && username.equals(userDetails.getUsername());
            }

            cacheMissRatio.incrementAndGet();

            // Validación completa
            final String username = getUsernameFromToken(token);
            return username.equals(userDetails.getUsername());
        } catch (Exception e) {
            tokenValidationFailedCounter.increment();
            return false;
        }
    }

    @Override
    public void invalidateToken(String token) {
        log.info("Iniciando proceso de invalidación de token");
        Timer.Sample sample = Timer.start(meterRegistry);

        try {
            // Solo hacer validación básica de estructura para tokens a invalidar
            preValidateTokenStructure(token);

            // Añadir a caché local inmediatamente
            blacklistCache.add(token);

            // Remover de caché de tokens válidos
            tokenCache.remove(token);

            // Persistir en Redis asíncronamente
            asyncTaskExecutor.submit(() -> addTokenToBlacklist(token));

            tokenBlacklistCounter.increment();
            log.info("Token invalidado exitosamente");
        } catch (Exception e) {
            log.error("Error al invalidar token: {}", e.getMessage());
            throw new TokenOperationException("Error al invalidar el token", e);
        } finally {
            sample.stop(meterRegistry.timer("security.token.invalidation"));
        }
    }

    private void addTokenToBlacklist(String token) {
        try {
            redisTemplate.opsForValue().set(
                    TOKEN_BLACKLIST_PREFIX + token,
                    "true",
                    TOKEN_BLACKLIST_DURATION,
                    TimeUnit.HOURS
            );
        } catch (RedisConnectionException e) {
            log.error("Error de conexión con Redis al invalidar token: {}", e.getMessage());
        } catch (Exception e) {
            log.error("Error al persistir token en lista negra: {}", e.getMessage());
        }
    }

    @Override
    public boolean isTokenInBlacklist(String token) {
        // Verificar primero en caché local
        if (blacklistCache.contains(token)) {
            return true;
        }

        // Verificar si es hora de sincronizar con Redis
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastBlacklistSync > BLACKLIST_SYNC_INTERVAL) {
            syncBlacklistFromRedis();
        }

        // Verificar nuevamente después de posible sincronización
        if (blacklistCache.contains(token)) {
            return true;
        }

        // Si no está en caché, verificar en Redis directamente
        try {
            Boolean result = redisTemplate.hasKey(TOKEN_BLACKLIST_PREFIX + token);
            if (Boolean.TRUE.equals(result)) {
                // Añadir al caché local si se encontró en Redis
                blacklistCache.add(token);
                return true;
            }
            return false;
        } catch (Exception e) {
            log.warn("Error al verificar token en Redis, usando caché local: {}", e.getMessage());
            return blacklistCache.contains(token);
        }
    }

    private void syncBlacklistFromRedis() {
        try {
            Set<String> redisKeys = redisTemplate.keys(TOKEN_BLACKLIST_PREFIX + "*");
            if (!redisKeys.isEmpty()) {
                redisKeys.forEach(key -> {
                    String token = key.replace(TOKEN_BLACKLIST_PREFIX, "");
                    blacklistCache.add(token);
                });
            }
            lastBlacklistSync = System.currentTimeMillis();
            log.debug("Lista negra sincronizada desde Redis, {} tokens en caché", blacklistCache.size());
        } catch (Exception e) {
            log.error("Error al sincronizar lista negra desde Redis", e);
        }
    }

    @Override
    public Set<String> getAllBlacklistedTokens() {
        // Sincronizar con Redis primero
        syncBlacklistFromRedis();

        // Devolver copia de la caché local
        return new HashSet<>(blacklistCache);
    }

    @Override
    public boolean isTokenBlacklisted(String token) {
        return isTokenInBlacklist(token);
    }

    private Date getExpirationDateFromToken(String token) {
        // Verificar primero en caché
        DecodedJWT cachedJwt = tokenCache.get(token);

        if (cachedJwt != null) {
            return cachedJwt.getExpiresAt();
        }

        // Si no está en caché, decodificar (sin verificar firma)
        try {
            DecodedJWT jwt = JWT.decode(token.trim());
            return jwt.getExpiresAt();
        } catch (Exception e) {
            throw new TokenMalformedException("Error al decodificar token: " + e.getMessage());
        }
    }

    private Instant generateExpirationDate() {
        return LocalDateTime.now()
                .plusHours(24)
                .toInstant(ZoneOffset.of("-05:00"));
    }

    // Método para limpieza de recursos en shutdown
    public void shutdown() {
        asyncTaskExecutor.shutdown();
        try {
            if (!asyncTaskExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                asyncTaskExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            asyncTaskExecutor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}