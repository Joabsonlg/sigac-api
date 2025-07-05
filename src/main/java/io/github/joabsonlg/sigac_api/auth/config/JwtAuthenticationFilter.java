package io.github.joabsonlg.sigac_api.auth.config;

import io.github.joabsonlg.sigac_api.auth.service.CookieService;
import io.github.joabsonlg.sigac_api.auth.service.JwtService;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
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
    private final CookieService cookieService;
    
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
    
    public JwtAuthenticationFilter(JwtService jwtService, CookieService cookieService) {
        this.jwtService = jwtService;
        this.cookieService = cookieService;
    }
      @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String path = exchange.getRequest().getPath().value();
        String method = exchange.getRequest().getMethod().name();
        
        // Allow OPTIONS requests to pass through without authentication (for CORS preflight)
        if ("OPTIONS".equals(method)) {
            return chain.filter(exchange);
        }
        
        // Skip authentication for public paths
        if (isPublicPath(path)) {
            return chain.filter(exchange);
        }

        String authHeader = exchange.getRequest().getHeaders().getFirst("Authorization");
        String token = null;
        
        // Try to get token from Authorization header first
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7);
        } else {
            // Try to get token from cookies
            token = extractTokenFromCookies(exchange);
        }
        
        // Check if token is present
        if (token == null) {
            return handleUnauthorized(exchange);
        }
        
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
        
        // Set authentication in reactive context (not SecurityContextHolder)
        return chain.filter(exchange)
                .contextWrite(ReactiveSecurityContextHolder.withAuthentication(authentication));
    }
    
    /**
     * Checks if the path is public and doesn't require authentication.
     */
    private boolean isPublicPath(String path) {
        return PUBLIC_PATHS.stream().anyMatch(path::startsWith);
    }
    
    /**
     * Handles unauthorized requests without triggering HTTP Basic Auth popup.
     */
    private Mono<Void> handleUnauthorized(ServerWebExchange exchange) {
        var response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        
        // Set headers to prevent HTTP Basic Auth popup
        response.getHeaders().add("Content-Type", "application/json");
        response.getHeaders().add("Cache-Control", "no-cache, no-store, must-revalidate");
        
        // Return JSON error response instead of triggering Basic Auth
        String jsonError = "{\"error\":\"Unauthorized\",\"message\":\"Authentication required\"}";
        var buffer = response.bufferFactory().wrap(jsonError.getBytes());
        return response.writeWith(Mono.just(buffer));
    }
    
    /**
     * Extracts JWT token from cookies.
     *
     * @param exchange server web exchange containing cookies
     * @return JWT token from access token cookie, or null if not found
     */
    private String extractTokenFromCookies(ServerWebExchange exchange) {
        var cookie = exchange.getRequest().getCookies().getFirst(cookieService.getAccessTokenCookieName());
        return cookie != null ? cookie.getValue() : null;
    }
}
