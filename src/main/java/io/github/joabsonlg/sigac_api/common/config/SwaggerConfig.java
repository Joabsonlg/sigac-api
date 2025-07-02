package io.github.joabsonlg.sigac_api.common.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Configuration class for Swagger/OpenAPI documentation.
 * Configures API documentation with authentication and server information.
 */
@Configuration
public class SwaggerConfig {

    @Value("${server.port:8080}")
    private String serverPort;

    @Value("${app.version:1.0.0}")
    private String appVersion;

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(apiInfo())
                .servers(List.of(
                        new Server()
                                .url("http://localhost:" + serverPort)
                                .description("Local Development Server")
                ))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth", createAPIKeyScheme())
                        .addSecuritySchemes("cookieAuth", createCookieScheme())
                )
                .addSecurityItem(new SecurityRequirement()
                        .addList("bearerAuth")
                        .addList("cookieAuth")
                );
    }

    private Info apiInfo() {
        return new Info()
                .title("SIGAC API")
                .description("""
                        # Sistema Gerenciador de Aluguéis de Carros (SIGAC)
                        
                        API REST completa para gerenciamento de aluguéis de carros com autenticação JWT e controle de acesso baseado em papéis.
                        
                        ## Características Principais
                        
                        - **Autenticação JWT**: Sistema híbrido com Access Tokens e Refresh Tokens seguros
                        - **Controle de Acesso**: RBAC com papéis ADMIN, GERENTE, ATENDENTE e CLIENTE
                        - **Arquitetura Reativa**: Spring WebFlux + R2DBC para alta performance
                        - **Segurança**: Cookies HTTP-only para refresh tokens
                        
                        ## Autenticação
                        
                        Para usar os endpoints protegidos:
                        
                        1. **Faça login** no endpoint `/auth/login` com CPF e senha
                        2. **Use o Access Token** retornado no header `Authorization: Bearer {token}`
                        3. **Renove automaticamente** via endpoint `/auth/refresh` quando necessário
                        
                        ## Papéis de Usuário
                        
                        - **ADMIN**: Acesso total ao sistema
                        - **GERENTE**: Funcionalidades gerenciais
                        - **ATENDENTE**: Operações do dia a dia
                        - **CLIENTE**: Acesso limitado aos próprios dados
                        
                        ## Usuários de Teste
                        
                        | CPF | Email | Senha | Papel |
                        |-----|-------|-------|-------|
                        | 36900271014 | admin@sigac.com | admin123 | ADMIN |
                        | 23456789012 | ana.santos@sigac.com | func123 | ATENDENTE |
                        | 67890123456 | lucia.ferreira@sigac.com | func456 | GERENTE |
                        | 34567890123 | carlos.oliveira@email.com | cli123 | CLIENTE |
                        """)
                .version(appVersion)
                .contact(new Contact()
                        .name("Equipe SIGAC")
                        .email("contato@sigac.com")
                        .url("https://github.com/joabsonlg/sigac-api")
                )
                .license(new License()
                        .name("MIT License")
                        .url("https://opensource.org/licenses/MIT")
                );
    }

    private SecurityScheme createAPIKeyScheme() {
        return new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .in(SecurityScheme.In.HEADER)
                .name("Authorization")
                .description("""
                        JWT Access Token para autenticação.
                        
                        **Como usar:**
                        1. Faça login em `/auth/login`
                        2. Copie o `accessToken` da resposta
                        3. Use o botão "Authorize" acima e cole o token
                        4. O token será automaticamente incluído nas requisições
                        
                        **Formato:** `Bearer {seu-jwt-token-aqui}`
                        """);
    }

    private SecurityScheme createCookieScheme() {
        return new SecurityScheme()
                .type(SecurityScheme.Type.APIKEY)
                .in(SecurityScheme.In.COOKIE)
                .name("refreshToken")
                .description("""
                        Refresh Token armazenado em cookie HTTP-only seguro.
                        
                        **Uso automático:**
                        - Definido automaticamente no login
                        - Usado automaticamente para renovar tokens
                        - Removido automaticamente no logout
                        """);
    }
}
