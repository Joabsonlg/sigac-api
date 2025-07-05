package io.github.joabsonlg.sigac_api.auth.controller;

import io.github.joabsonlg.sigac_api.auth.dto.CookieLoginResponseDTO;
import io.github.joabsonlg.sigac_api.auth.dto.LoginRequestDTO;
import io.github.joabsonlg.sigac_api.auth.dto.LoginResponseDTO;
import io.github.joabsonlg.sigac_api.auth.dto.UserInfoDTO;
import io.github.joabsonlg.sigac_api.auth.exception.AuthenticationException;
import io.github.joabsonlg.sigac_api.auth.handler.AuthHandler;
import io.github.joabsonlg.sigac_api.auth.service.CookieService;
import io.github.joabsonlg.sigac_api.common.base.BaseController;
import io.github.joabsonlg.sigac_api.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * Controller for authentication endpoints.
 * Handles login, token refresh, and user validation operations.
 */
@RestController
@RequestMapping("/auth")
@Tag(name = "Authentication", description = "Endpoints para autenticação e autorização")
public class AuthController extends BaseController<LoginResponseDTO, String> {

    private final AuthHandler authHandler;
    private final CookieService cookieService;

    public AuthController(AuthHandler authHandler, CookieService cookieService) {
        this.authHandler = authHandler;
        this.cookieService = cookieService;
    }

    /**
     * Authenticates user and returns JWT tokens.
     * Refresh token is sent via secure HTTP-only cookie.
     *
     * @param loginRequest login credentials
     * @param exchange     server web exchange for setting cookies
     * @return authentication response with access token
     */
    @PostMapping("/login")
    @Operation(
            summary = "Autenticar usuário",
            description = """
                    Autentica o usuário com CPF e senha, retornando:
                    - Informações do usuário autenticado (no response body)
                    - Access Token JWT (em cookie HTTP-only seguro)
                    - Refresh Token (em cookie HTTP-only seguro)
                    
                    **Autenticação 100% via cookies:** Não é necessário enviar tokens manualmente.
                    """,
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Credenciais de login",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = LoginRequestDTO.class),
                            examples = @ExampleObject(
                                    name = "Exemplo Admin",
                                    value = """
                                            {
                                                "cpf": "36900271014",
                                                "password": "admin123"
                                            }
                                            """
                            )
                    )
            )
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201",
                    description = "Login realizado com sucesso",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class),
                            examples = @ExampleObject(
                                    name = "Sucesso",
                                    value = """
                                            {
                                                "success": true,
                                                "message": "Request successful",
                                                "data": {
                                                    "message": "Login successful",
                                                    "user": {
                                                        "cpf": "36900271014",
                                                        "name": "João Silva",
                                                        "email": "admin@sigac.com",
                                                        "role": "ADMIN"
                                                    }
                                                },
                                                "timestamp": "2025-07-02T15:30:00"
                                            }
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Credenciais inválidas"
            )
    })
    public Mono<ResponseEntity<ApiResponse<CookieLoginResponseDTO>>> login(
            @Parameter(description = "Dados de login do usuário")
            @RequestBody LoginRequestDTO loginRequest,
            ServerWebExchange exchange) {
        return authHandler.login(loginRequest)
                .map(loginResponseWithCookie -> {
                    // Add both access and refresh token cookies to response
                    exchange.getResponse().addCookie(loginResponseWithCookie.accessTokenCookie());
                    exchange.getResponse().addCookie(loginResponseWithCookie.refreshTokenCookie());

                    return created(Mono.just(loginResponseWithCookie.response()));
                })
                .flatMap(response -> response);
    }

    /**
     * Refreshes access token using refresh token from cookie.
     * Returns new access token and sets new refresh token cookie.
     *
     * @param exchange server web exchange for reading and setting cookies
     * @return new authentication response with fresh access token
     */
    @PostMapping("/refresh")
    @Operation(
            summary = "Renovar token de acesso",
            description = """
                    Renova o access token usando o refresh token armazenado em cookie.
                    
                    **Requisitos:**
                    - Cookie 'refreshToken' válido e não expirado
                    
                    **Retorna:**
                    - Novo access token
                    - Novo refresh token (em cookie)
                    - Informações atualizadas do usuário
                    """,
            security = @SecurityRequirement(name = "cookieAuth")
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Token renovado com sucesso"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Refresh token inválido ou expirado"
            )
    })
    public Mono<ResponseEntity<ApiResponse<CookieLoginResponseDTO>>> refresh(ServerWebExchange exchange) {
        // Extract refresh token from cookie
        String refreshToken = exchange.getRequest().getCookies()
                .getFirst(cookieService.getRefreshTokenCookieName())
                .getValue();

        return authHandler.refreshToken(refreshToken)
                .map(loginResponseWithCookie -> {
                    // Add both new access and refresh token cookies to response
                    exchange.getResponse().addCookie(loginResponseWithCookie.accessTokenCookie());
                    exchange.getResponse().addCookie(loginResponseWithCookie.refreshTokenCookie());

                    return ok(Mono.just(loginResponseWithCookie.response()));
                })
                .flatMap(response -> response);
    }

    /**
     * Logs out user by clearing refresh token cookie.
     *
     * @param exchange server web exchange for setting cookies
     * @return logout confirmation
     */
    @PostMapping("/logout")
    @Operation(
            summary = "Fazer logout",
            description = """
                    Faz logout do usuário removendo o refresh token cookie.
                    
                    **Efeitos:**
                    - Remove o cookie 'refreshToken'
                    - Invalida a sessão do usuário
                    
                    **Nota:** O access token continuará válido até expirar naturalmente.
                    """,
            security = @SecurityRequirement(name = "cookieAuth")
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Logout realizado com sucesso"
            )
    })
    public Mono<ResponseEntity<ApiResponse<Void>>> logout(ServerWebExchange exchange) {
        // Clear both access and refresh token cookies
        var logoutCookies = authHandler.logout();
        exchange.getResponse().addCookie(logoutCookies.accessTokenCookie());
        exchange.getResponse().addCookie(logoutCookies.refreshTokenCookie());

        return okMessage("Logged out successfully");
    }

    /**
     * Validates token and returns user information.
     * Works with both Authorization header and access token cookie.
     *
     * @param exchange server web exchange for reading cookies and headers
     * @return user information
     */
    @GetMapping("/me")
    @Operation(
            summary = "Obter informações do usuário autenticado",
            description = """
                    Retorna as informações do usuário autenticado baseado no access token.
                    
                    **Requisitos:**
                    - Access token via cookie ou header Authorization com Bearer token válido
                    
                    **Retorna:**
                    - CPF, nome, email e papel do usuário
                    """,
            security = {
                    @SecurityRequirement(name = "bearerAuth"),
                    @SecurityRequirement(name = "cookieAuth")
            }
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Informações do usuário obtidas com sucesso",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = UserInfoDTO.class)
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Token inválido ou expirado"
            )
    })
    public Mono<ResponseEntity<ApiResponse<UserInfoDTO>>> getCurrentUser(ServerWebExchange exchange) {
        // Try to get token from Authorization header first
        String authHeader = exchange.getRequest().getHeaders().getFirst("Authorization");
        String token = null;
        
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7);
        } else {
            // Try to get token from cookies
            var cookie = exchange.getRequest().getCookies().getFirst(cookieService.getAccessTokenCookieName());
            token = cookie != null ? cookie.getValue() : null;
        }
        
        if (token == null) {
            return Mono.error(new AuthenticationException("No access token provided", "MISSING_TOKEN"));
        }

        return authHandler.validateAndGetUserInfo(token)
                .map(userInfo -> ok(Mono.just(userInfo)))
                .flatMap(response -> response);
    }

    /**
     * Health check endpoint for authentication service.
     *
     * @return health status
     */
    @GetMapping("/health")
    @Operation(
            summary = "Verificar status do serviço de autenticação",
            description = """
                    Endpoint de health check para verificar se o serviço de autenticação está funcionando.
                    
                    **Uso:**
                    - Monitoramento de infraestrutura
                    - Verificação de disponibilidade
                    - Testes de conectividade
                    """,
            tags = {"Health Check"}
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Serviço funcionando normalmente"
            )
    })
    public Mono<ResponseEntity<ApiResponse<Void>>> health() {
        return okMessage("Authentication service is running");
    }

    /**
     * Validates the JWT token and returns user information.
     * This endpoint is used to verify the token's validity and retrieve user details.
     *
     * @param authorization Authorization header containing the Bearer token
     * @return User information if token is valid
     */
    @GetMapping("/verify")
    @Operation(
            summary = "Validar token JWT",
            description = """
                    Valida o token JWT e retorna as informações do usuário associado.
                    
                    **Requisitos:**
                    - Header Authorization com Bearer token válido
                    
                    **Retorna:**
                    - Informações do usuário (CPF, nome, email, papel)
                    """,
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Token válido, informações do usuário retornadas",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = UserInfoDTO.class)
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Token inválido ou expirado"
            )
    })
    public Mono<ResponseEntity<ApiResponse<UserInfoDTO>>> verifyToken(
            @Parameter(description = "Token de autorização no formato 'Bearer {token}'", example = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
            @RequestHeader("Authorization") String authorization) {
        String token = extractTokenFromHeader(authorization);
        return authHandler.validateAndGetUserInfo(token)
                .map(userInfo -> ok(Mono.just(userInfo)))
                .flatMap(response -> response);
    }

    /**
     * Extracts JWT token from Authorization header.
     *
     * @param authHeader Authorization header value
     * @return JWT token without Bearer prefix
     * @throws IllegalArgumentException if header format is invalid
     */
    private String extractTokenFromHeader(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new IllegalArgumentException("Invalid Authorization header format");
        }
        return authHeader.substring(7); // Remove "Bearer " prefix
    }
}
