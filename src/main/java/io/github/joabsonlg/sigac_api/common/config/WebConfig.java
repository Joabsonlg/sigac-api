package io.github.joabsonlg.sigac_api.common.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;
import org.springframework.web.reactive.config.EnableWebFlux;
import org.springframework.web.reactive.config.WebFluxConfigurer;

import java.util.Arrays;
import java.util.List;

/**
 * Configuration class for WebFlux setup.
 * Configures CORS, content negotiation, and other web-related settings.
 */
@Configuration
@EnableWebFlux
public class WebConfig implements WebFluxConfigurer {

    @Value("${sigac.security.cors.allowed-origins}")
    private String allowedOrigins;
    
    @Value("${sigac.security.cors.allowed-methods}")
    private String allowedMethods;
    
    @Value("${sigac.security.cors.allowed-headers}")
    private String allowedHeaders;
    
    @Value("${sigac.security.cors.allow-credentials}")
    private boolean allowCredentials;

    /**
     * Configures CORS settings for the application.
     * Uses properties from application.properties for configuration.
     *
     * @return configured CorsWebFilter
     */
    @Bean
    public CorsWebFilter corsWebFilter() {
        CorsConfiguration corsConfig = new CorsConfiguration();
        
        // Set allowed origins from properties - be explicit about origins
        List<String> origins = Arrays.asList(allowedOrigins.split(","));
        corsConfig.setAllowedOriginPatterns(Arrays.asList("*")); // Allow all patterns for development
        corsConfig.setAllowedOrigins(origins);
        
        // Set allowed methods from properties
        List<String> methods = Arrays.asList(allowedMethods.split(","));
        corsConfig.setAllowedMethods(methods);
        corsConfig.addAllowedMethod("OPTIONS"); // Explicitly allow OPTIONS
        
        // Set allowed headers - be more permissive for development
        corsConfig.setAllowedHeaders(Arrays.asList("*"));
        
        // CRITICAL: Allow credentials for cookie-based auth
        corsConfig.setAllowCredentials(true);
        
        // Expose useful headers for debugging
        corsConfig.addExposedHeader("Authorization");
        corsConfig.addExposedHeader("Set-Cookie");
        corsConfig.addExposedHeader("Access-Control-Allow-Credentials");
        
        // Set max age for preflight requests
        corsConfig.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfig);

        return new CorsWebFilter(source);
    }
}
