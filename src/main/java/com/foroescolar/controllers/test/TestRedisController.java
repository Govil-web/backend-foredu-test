package com.foroescolar.controllers.test;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/test")
@RequiredArgsConstructor
@Slf4j
public class TestRedisController {

    private final RedisTemplate<String, String> redisTemplate;

    @GetMapping("/redis")
    public ResponseEntity<String> testRedis() {
        try {
            String testKey = "test:connection";
            String testValue = "Redis está funcionando correctamente - " + LocalDateTime.now();

            redisTemplate.opsForValue().set(testKey, testValue);
            String retrievedValue = redisTemplate.opsForValue().get(testKey);

            log.info("Valor almacenado en Redis: {}", retrievedValue);

            return ResponseEntity.ok(retrievedValue);
        } catch (Exception e) {
            log.error("Error al probar Redis: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al conectar con Redis: " + e.getMessage());
        }
    }
}
