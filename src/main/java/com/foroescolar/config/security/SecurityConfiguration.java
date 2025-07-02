package com.foroescolar.config.security;

import com.foroescolar.config.security.filters.BlacklistTokenFilter;
import com.foroescolar.config.security.filters.JwtAuthenticationFilter;
import com.foroescolar.config.security.filters.RequestLoggingFilter;
import com.foroescolar.config.security.handlers.SecurityExceptionHandler;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Configuration
@EnableWebSecurity
public class SecurityConfiguration {
    private final RequestLoggingFilter requestLoggingFilter;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final BlacklistTokenFilter blacklistTokenFilter;
    private final UserDetailsService userDetailsService;
    private final SecurityExceptionHandler securityExceptionHandler;
    private final MeterRegistry meterRegistry;

    // Constantes para roles
    private static final String ROLE_ADMIN = "ADMINISTRADOR";
    private static final String ROLE_PROFESOR = "PROFESOR";
    private static final String ROLE_TUTOR = "TUTOR";
    private static final String ROLE_ESTUDIANTE = "ESTUDIANTE";

    private final Map<String, Boolean> accessDecisionCache = new ConcurrentHashMap<>();

    private final Map<String, Set<String>> patternRoleMap = new HashMap<>();

    public SecurityConfiguration(
            RequestLoggingFilter requestLoggingFilter,
            JwtAuthenticationFilter jwtAuthenticationFilter,
            BlacklistTokenFilter blacklistTokenFilter,
            UserDetailsService userDetailsService,
            SecurityExceptionHandler securityExceptionHandler,
            MeterRegistry meterRegistry) {
        this.requestLoggingFilter = requestLoggingFilter;
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.blacklistTokenFilter = blacklistTokenFilter;
        this.userDetailsService = userDetailsService;
        this.securityExceptionHandler = securityExceptionHandler;
        this.meterRegistry = meterRegistry;

        initializePatternRoleMap();
    }

    private void initializePatternRoleMap() {
        addPatternRoles("/api/auth/login", new String[]{});
        addPatternRoles("/swagger-ui.html", new String[]{});
        addPatternRoles("/v3/api-docs/**", new String[]{});
        addPatternRoles("/swagger-ui/**", new String[]{});
        addPatternRoles("/api/test/redis", new String[]{});
        addPatternRoles("/api/user/add", new String[]{});

        // Patrones de administrador
        addPatternRoles("/api/user/getAll", new String[]{ROLE_ADMIN});
        addPatternRoles("/api/estudiante/getAll", new String[]{ROLE_ADMIN});
        addPatternRoles("/api/profesor/add", new String[]{ROLE_ADMIN});
        addPatternRoles("/api/estudiante/add", new String[]{ROLE_ADMIN});
        addPatternRoles("/api/tutorlegal/add", new String[]{ROLE_ADMIN});
        addPatternRoles("/api/user/add", new String[]{ROLE_ADMIN});
        addPatternRoles("/api/**/delete/**", new String[]{ROLE_ADMIN});
        addPatternRoles("/api/estudiante/update", new String[]{ROLE_ADMIN});
        addPatternRoles("/api/profesor/update", new String[]{ROLE_ADMIN});
        addPatternRoles("/api/tutorlegal/update", new String[]{ROLE_ADMIN});

        // Patrones para profesores
        addPatternRoles("/api/asistencia/add", new String[]{ROLE_ADMIN, ROLE_PROFESOR});
        addPatternRoles("/api/asistencia/update/**", new String[]{ROLE_ADMIN, ROLE_PROFESOR});
        addPatternRoles("/api/asistencia/**", new String[]{ROLE_ADMIN, ROLE_PROFESOR});
        addPatternRoles("/api/profesor/**", new String[]{ROLE_ADMIN, ROLE_PROFESOR});
        addPatternRoles("/api/tutorlegal/**", new String[]{ROLE_ADMIN, ROLE_PROFESOR});

        // Patrones para tutores
        addPatternRoles("/api/estudiante/{id}", new String[]{ROLE_ADMIN, ROLE_PROFESOR, ROLE_TUTOR});
        addPatternRoles("/api/estudiante/{id}/asistencias", new String[]{ROLE_ADMIN, ROLE_PROFESOR, ROLE_TUTOR});
        addPatternRoles("/api/estudiante/filterGrado", new String[]{ROLE_ADMIN, ROLE_PROFESOR, ROLE_TUTOR});
        addPatternRoles("/api/tutorlegal/asistenciaHijo/{id}", new String[]{ROLE_ADMIN, ROLE_PROFESOR, ROLE_TUTOR});
        addPatternRoles("/api/auth/logout", new String[]{ROLE_ADMIN, ROLE_PROFESOR, ROLE_TUTOR, ROLE_ESTUDIANTE});

        // Self-access
        addPatternRoles("/api/user/{id}", new String[]{ROLE_ADMIN, ROLE_PROFESOR, ROLE_TUTOR, ROLE_ESTUDIANTE});
        addPatternRoles("/api/profesor/{id}", new String[]{ROLE_ADMIN, ROLE_PROFESOR});
        addPatternRoles("/api/tutorlegal/{id}", new String[]{ROLE_ADMIN, ROLE_PROFESOR, ROLE_TUTOR});
    }

    private void addPatternRoles(String pattern, String[] roles) {
        Set<String> roleSet = new HashSet<>(Arrays.asList(roles));
        patternRoleMap.put(pattern, roleSet);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
        Timer.Sample configTimer = Timer.start(meterRegistry);

        SecurityFilterChain chain = httpSecurity
                .csrf(AbstractHttpConfigurer::disable)
                .exceptionHandling(exceptionHandling -> exceptionHandling
                        .authenticationEntryPoint(securityExceptionHandler)
                        .accessDeniedHandler(securityExceptionHandler)
                )
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(getPublicEndpoints()).permitAll()

                        .requestMatchers(getSelfAccessEndpoints()).authenticated()
                        .requestMatchers(getAdministratorEndpoints()).hasRole(ROLE_ADMIN)
                        .requestMatchers(getTeacherEndpoints()).hasAnyRole(ROLE_ADMIN, ROLE_PROFESOR)
                        .requestMatchers(getTutorEndpoints()).hasAnyRole(ROLE_ADMIN, ROLE_PROFESOR, ROLE_TUTOR)

                        .anyRequest().authenticated()
                )
                .addFilterBefore(requestLoggingFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterAfter(jwtAuthenticationFilter, RequestLoggingFilter.class)
                .addFilterAfter(blacklistTokenFilter, JwtAuthenticationFilter.class)
                .userDetailsService(userDetailsService)
                .build();

        configTimer.stop(meterRegistry.timer("security.config.initialization"));

        return chain;
    }

    private RequestMatcher[] getPublicEndpoints() {
        return new RequestMatcher[] {
                new AntPathRequestMatcher("/api/auth/login", HttpMethod.POST.name()),
                new AntPathRequestMatcher("/swagger-ui.html"),
                new AntPathRequestMatcher("/v3/api-docs/**"),
                new AntPathRequestMatcher("/swagger-ui/**"),
                new AntPathRequestMatcher("/api/test/redis", HttpMethod.GET.name()),
                new AntPathRequestMatcher("/api/user/add", HttpMethod.POST.name())
        };
    }

    private RequestMatcher[] getSelfAccessEndpoints() {
        return new RequestMatcher[] {
                new AntPathRequestMatcher("/api/user/{id}", HttpMethod.GET.name()),
                new AntPathRequestMatcher("/api/profesor/{id}", HttpMethod.GET.name()),
                new AntPathRequestMatcher("/api/tutorlegal/{id}", HttpMethod.GET.name())
        };
    }

    private RequestMatcher[] getAdministratorEndpoints() {
        return new RequestMatcher[] {
                new AntPathRequestMatcher("/api/user/getAll", HttpMethod.GET.name()),
                new AntPathRequestMatcher("/api/estudiante/getAll", HttpMethod.GET.name()),
                new AntPathRequestMatcher("/api/profesor/add", HttpMethod.POST.name()),
                new AntPathRequestMatcher("/api/estudiante/add", HttpMethod.POST.name()),
                new AntPathRequestMatcher("/api/tutorlegal/add", HttpMethod.POST.name()),
                new AntPathRequestMatcher("/api/user/add", HttpMethod.POST.name()),
                new AntPathRequestMatcher("/api/**", HttpMethod.DELETE.name()),
                new AntPathRequestMatcher("/api/estudiante/update", HttpMethod.PUT.name()),
                new AntPathRequestMatcher("/api/profesor/update", HttpMethod.PUT.name()),
                new AntPathRequestMatcher("/api/tutorlegal/update", HttpMethod.PUT.name()),
                new AntPathRequestMatcher("/api/asistencia/add", HttpMethod.POST.name()),
                new AntPathRequestMatcher("/api/asistencia/update/**", HttpMethod.PATCH.name()),
                new AntPathRequestMatcher("/api/grado/**", HttpMethod.POST.name()),
                new AntPathRequestMatcher("/api/grado/**", HttpMethod.GET.name()),
                new AntPathRequestMatcher("/api/grado/**", HttpMethod.DELETE.name()),
                new AntPathRequestMatcher("/api/grado/**", HttpMethod.PATCH.name())
        };
    }

    private RequestMatcher[] getTeacherEndpoints() {
        return new RequestMatcher[] {
                new AntPathRequestMatcher("/api/asistencia/add", HttpMethod.POST.name()),
                new AntPathRequestMatcher("/api/asistencia/update/**", HttpMethod.PUT.name()),
                new AntPathRequestMatcher("/api/asistencia/**", HttpMethod.GET.name()),
                new AntPathRequestMatcher("/api/profesor/**", HttpMethod.GET.name()),
                new AntPathRequestMatcher("/api/tutorlegal/**", HttpMethod.GET.name())
        };
    }

    private RequestMatcher[] getTutorEndpoints() {
        return new RequestMatcher[] {
                new AntPathRequestMatcher("/api/estudiante/{id}", HttpMethod.GET.name()),
                new AntPathRequestMatcher("/api/estudiante/{id}/asistencias", HttpMethod.GET.name()),
                new AntPathRequestMatcher("/api/estudiante/filterGrado", HttpMethod.GET.name()),
                new AntPathRequestMatcher("/api/tutorlegal/asistenciaHijo/{id}", HttpMethod.GET.name()),
                new AntPathRequestMatcher("/api/auth/logout", HttpMethod.POST.name())
        };
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    /**
     * Bean para monitoreo del rendimiento de decisiones de seguridad
     */
    @Bean
    public SecurityMetricsService securityMetricsService() {
        return new SecurityMetricsService(meterRegistry);
    }

    /**
     * Servicio para métricas de seguridad
     */
    @Slf4j
    public static class SecurityMetricsService {
        private final MeterRegistry meterRegistry;

        private final Counter accessGrantedCounter;
        private final Counter accessDeniedCounter;

        private final Timer accessDecisionTimer;

        private final Map<String, Counter> endpointAccessMap = new ConcurrentHashMap<>();

        public SecurityMetricsService(MeterRegistry meterRegistry) {
            this.meterRegistry = meterRegistry;

            this.accessGrantedCounter = Counter.builder("security.access.granted")
                    .description("Número de decisiones de acceso concedidas")
                    .register(meterRegistry);

            this.accessDeniedCounter = Counter.builder("security.access.denied")
                    .description("Número de decisiones de acceso denegadas")
                    .register(meterRegistry);

            this.accessDecisionTimer = Timer.builder("security.access.decision")
                    .description("Tiempo para tomar decisiones de acceso")
                    .register(meterRegistry);
        }

        public void recordAccessGranted(String path) {
            accessGrantedCounter.increment();
            getOrCreateEndpointCounter(path, true).increment();
        }

        public void recordAccessDenied(String path) {
            accessDeniedCounter.increment();
            getOrCreateEndpointCounter(path, false).increment();
        }

        public Timer getAccessDecisionTimer() {
            return accessDecisionTimer;
        }

        private Counter getOrCreateEndpointCounter(String path, boolean granted) {
            String status = granted ? "granted" : "denied";
            String name = "security.endpoint." + status;
            String endpointKey = path.replace("/", "_").replace("{", "").replace("}", "");

            return endpointAccessMap.computeIfAbsent(
                    path + ":" + status,
                    key -> Counter.builder(name)
                            .tag("endpoint", endpointKey)
                            .description("Accesos a endpoint: " + path)
                            .register(meterRegistry)
            );
        }
    }
}