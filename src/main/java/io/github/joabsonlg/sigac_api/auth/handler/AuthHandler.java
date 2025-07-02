package io.github.joabsonlg.sigac_api.auth.handler;

import io.github.joabsonlg.sigac_api.auth.dto.LoginRequestDTO;
import io.github.joabsonlg.sigac_api.auth.dto.LoginResponseDTO;
import io.github.joabsonlg.sigac_api.auth.dto.LoginResponseWithCookie;
import io.github.joabsonlg.sigac_api.auth.dto.UserInfoDTO;
import io.github.joabsonlg.sigac_api.auth.exception.AuthenticationException;
import io.github.joabsonlg.sigac_api.auth.repository.AuthRepository;
import io.github.joabsonlg.sigac_api.auth.service.CookieService;
import io.github.joabsonlg.sigac_api.auth.service.JwtService;
import io.github.joabsonlg.sigac_api.auth.service.PasswordService;
import io.github.joabsonlg.sigac_api.common.exception.ResourceNotFoundException;
import io.github.joabsonlg.sigac_api.common.validator.CommonValidator;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Duration;

/**
 * Handler for authentication business logic.
 * Handles login, token refresh, and authentication operations.
 */
@Service
public class AuthHandler {
    
    private final AuthRepository authRepository;
    private final JwtService jwtService;
    private final PasswordService passwordService;
    private final CookieService cookieService;
    private final CommonValidator validator;
    
    public AuthHandler(AuthRepository authRepository, 
                      JwtService jwtService, 
                      PasswordService passwordService,
                      CookieService cookieService,
                      CommonValidator validator) {
        this.authRepository = authRepository;
        this.jwtService = jwtService;
        this.passwordService = passwordService;
        this.cookieService = cookieService;
        this.validator = validator;
    }
    
    /**
     * Authenticates user and generates tokens with cookie.
     *
     * @param loginRequest login credentials
     * @return login response with refresh token cookie
     */
    public Mono<LoginResponseWithCookie> login(LoginRequestDTO loginRequest) {
        // Validate input
        validateLoginRequest(loginRequest);
        
        return authRepository.findUserWithRoleByCpf(loginRequest.cpf())
                .switchIfEmpty(Mono.error(new AuthenticationException("Invalid credentials", "INVALID_CREDENTIALS")))
                .flatMap(userWithRole -> {
                    // Verify password
                    if (!passwordService.matches(loginRequest.password(), userWithRole.user().getPassword())) {
                        return Mono.error(new AuthenticationException("Invalid credentials", "INVALID_CREDENTIALS"));
                    }
                    
                    // Generate tokens
                    String accessToken = jwtService.generateAccessToken(userWithRole.user().getCpf(), userWithRole.role());
                    String refreshToken = jwtService.generateRefreshToken(userWithRole.user().getCpf());
                    Long expiresIn = jwtService.getExpirationTime("access");
                    
                    // Create user info
                    UserInfoDTO userInfo = new UserInfoDTO(
                            userWithRole.user().getCpf(),
                            userWithRole.user().getName(),
                            userWithRole.user().getEmail(),
                            userWithRole.role()
                    );
                    
                    // Create response
                    LoginResponseDTO response = new LoginResponseDTO(
                            accessToken,
                            "Bearer",
                            expiresIn,
                            userInfo
                    );
                    
                    // Create refresh token cookie
                    ResponseCookie refreshCookie = cookieService.createRefreshTokenCookie(
                            refreshToken, 
                            Duration.ofSeconds(jwtService.getExpirationTime("refresh"))
                    );
                    
                    return Mono.just(new LoginResponseWithCookie(response, refreshCookie));
                });
    }
    
    /**
     * Refreshes access token using refresh token from cookie.
     *
     * @param refreshToken refresh token from cookie
     * @return new login response with fresh tokens
     */
    public Mono<LoginResponseWithCookie> refreshToken(String refreshToken) {
        // Validate refresh token
        if (refreshToken == null || refreshToken.trim().isEmpty()) {
            return Mono.error(new AuthenticationException("Refresh token is required", "MISSING_REFRESH_TOKEN"));
        }
        
        // Validate token format and expiration
        if (!jwtService.validateToken(refreshToken)) {
            return Mono.error(new AuthenticationException("Invalid refresh token", "INVALID_REFRESH_TOKEN"));
        }
        
        // Check if it's actually a refresh token
        if (!"refresh".equals(jwtService.extractTokenType(refreshToken))) {
            return Mono.error(new AuthenticationException("Invalid token type", "INVALID_TOKEN_TYPE"));
        }
        
        String cpf = jwtService.extractCpf(refreshToken);
        
        return authRepository.findUserWithRoleByCpf(cpf)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("User", cpf)))
                .map(userWithRole -> {
                    // Generate new tokens
                    String newAccessToken = jwtService.generateAccessToken(userWithRole.user().getCpf(), userWithRole.role());
                    String newRefreshToken = jwtService.generateRefreshToken(userWithRole.user().getCpf());
                    Long expiresIn = jwtService.getExpirationTime("access");
                    
                    // Create user info
                    UserInfoDTO userInfo = new UserInfoDTO(
                            userWithRole.user().getCpf(),
                            userWithRole.user().getName(),
                            userWithRole.user().getEmail(),
                            userWithRole.role()
                    );
                    
                    // Create response
                    LoginResponseDTO response = new LoginResponseDTO(
                            newAccessToken,
                            "Bearer",
                            expiresIn,
                            userInfo
                    );
                    
                    // Create new refresh token cookie
                    ResponseCookie refreshCookie = cookieService.createRefreshTokenCookie(
                            newRefreshToken, 
                            Duration.ofSeconds(jwtService.getExpirationTime("refresh"))
                    );
                    
                    return new LoginResponseWithCookie(response, refreshCookie);
                });
    }
    
    /**
     * Validates user token and returns user information.
     *
     * @param token JWT token
     * @return user information
     */
    public Mono<UserInfoDTO> validateAndGetUserInfo(String token) {
        if (!jwtService.validateToken(token)) {
            return Mono.error(new AuthenticationException("Invalid token", "INVALID_TOKEN"));
        }
        
        // Check if it's an access token
        if (!"access".equals(jwtService.extractTokenType(token))) {
            return Mono.error(new AuthenticationException("Invalid token type", "INVALID_TOKEN_TYPE"));
        }
        
        String cpf = jwtService.extractCpf(token);
        String role = jwtService.extractRole(token);
        
        return authRepository.findUserWithRoleByCpf(cpf)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("User", cpf)))
                .map(userWithRole -> new UserInfoDTO(
                        userWithRole.user().getCpf(),
                        userWithRole.user().getName(),
                        userWithRole.user().getEmail(),
                        role != null ? role : userWithRole.role()
                ));
    }
    
    /**
     * Logs out user by clearing refresh token cookie.
     *
     * @return cookie to clear refresh token
     */
    public ResponseCookie logout() {
        return cookieService.clearRefreshTokenCookie();
    }
    
    /**
     * Validates login request input.
     */
    private void validateLoginRequest(LoginRequestDTO request) {
        validator.validateRequired(request.cpf(), "cpf");
        validator.validateRequired(request.password(), "password");
        validator.validateCpf(request.cpf(), "cpf");
    }
}
