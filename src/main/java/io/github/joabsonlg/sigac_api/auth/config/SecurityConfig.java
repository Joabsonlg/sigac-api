package io.github.joabsonlg.sigac_api.auth.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.context.NoOpServerSecurityContextRepository;

/**
 * Security configuration for the reactive application.
 * Configures JWT-based authentication and authorization.
 */
@Configuration
@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
public class SecurityConfig {
    
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    
    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }
    
    /**
     * Configures the security filter chain.
     */
    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http
                // Disable CSRF for REST APIs
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                
                // Disable form login
                .formLogin(ServerHttpSecurity.FormLoginSpec::disable)
                
                // Disable HTTP Basic authentication
                .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)
                
                // Configure stateless authentication
                .securityContextRepository(NoOpServerSecurityContextRepository.getInstance())
                
                // Configure authorization rules
                .authorizeExchange(exchanges -> exchanges
                        // Public endpoints - Authentication
                        .pathMatchers(HttpMethod.POST, "/auth/login", "/auth/refresh", "/auth/logout").permitAll()
                        .pathMatchers(HttpMethod.GET, "/auth/health").permitAll()
                        
                        // Swagger/OpenAPI endpoints - Public access
                        .pathMatchers("/swagger-ui/**").permitAll()
                        .pathMatchers("/swagger-ui.html").permitAll()
                        .pathMatchers("/v3/api-docs/**").permitAll()
                        .pathMatchers("/v3/api-docs.yaml").permitAll()
                        .pathMatchers("/webjars/**").permitAll()
                        .pathMatchers("/swagger-resources/**").permitAll()
                        .pathMatchers("/configuration/**").permitAll()
                        .pathMatchers("/actuator/**").permitAll()
                        
                        // Authenticated endpoints - require valid JWT
                        .pathMatchers("/auth/me").authenticated()
                        
                        // All other requests require authentication (for future endpoints)
                        .anyExchange().permitAll() // Temporariamente permitir tudo para testar Swagger
                )
                
                // Add JWT authentication filter
                .addFilterBefore(jwtAuthenticationFilter, SecurityWebFiltersOrder.AUTHENTICATION)
                
                .build();
    }
}
