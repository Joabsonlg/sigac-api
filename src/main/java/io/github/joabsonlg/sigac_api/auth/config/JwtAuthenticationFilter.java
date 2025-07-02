package io.github.joabsonlg.sigac_api.auth.config;

import io.github.joabsonlg.sigac_api.auth.service.JwtService;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * JWT Authentication Filter for WebFlux.
 * Intercepts requests and validates JWT tokens.
 */
@Component
public class JwtAuthenticationFilter implements WebFilter {
    
    private final JwtService jwtService;
    
    // Public endpoints that don't require authentication
    private static final List<String> PUBLIC_PATHS = List.of(
            "/auth/login",
            "/auth/refresh",
            "/auth/logout",
            "/auth/health",
            "/swagger-ui",
            "/v3/api-docs",
            "/webjars",
            "/swagger-resources",
            "/configuration",
            "/actuator",
            "/favicon.ico"
    );
    
    public JwtAuthenticationFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }
    
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String path = exchange.getRequest().getPath().value();
        
        // Skip authentication for public paths
        if (isPublicPath(path)) {
            return chain.filter(exchange);
        }
        
        String authHeader = exchange.getRequest().getHeaders().getFirst("Authorization");
        
        // Check if Authorization header is present
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return handleUnauthorized(exchange);
        }
        
        String token = authHeader.substring(7);
        
        // Validate token
        if (!jwtService.validateToken(token)) {
            return handleUnauthorized(exchange);
        }
        
        // Check if it's an access token
        if (!"access".equals(jwtService.extractTokenType(token))) {
            return handleUnauthorized(exchange);
        }
        
        // Extract user information from token
        String cpf = jwtService.extractCpf(token);
        String role = jwtService.extractRole(token);
        
        // Create authentication object
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                cpf,
                null,
                List.of(new SimpleGrantedAuthority("ROLE_" + role))
        );
        
        // Set authentication in security context
        SecurityContextHolder.getContext().setAuthentication(authentication);
        
        // Continue with the filter chain
        return chain.filter(exchange);
    }
    
    /**
     * Checks if the path is public and doesn't require authentication.
     */
    private boolean isPublicPath(String path) {
        return PUBLIC_PATHS.stream().anyMatch(path::startsWith);
    }
    
    /**
     * Handles unauthorized requests.
     */
    private Mono<Void> handleUnauthorized(ServerWebExchange exchange) {
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        return exchange.getResponse().setComplete();
    }
}
