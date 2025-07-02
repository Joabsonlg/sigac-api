# 📚 SIGAC API - Documentação Swagger/OpenAPI

## 🎯 Acesso à Documentação

Após iniciar a aplicação, você pode acessar a documentação interativa da API através dos seguintes endpoints:

### 🌐 Swagger UI (Interface Visual)
```
http://localhost:8080/swagger-ui.html
```

### 📄 Documentação OpenAPI (JSON)
```
http://localhost:8080/v3/api-docs
```

### 📋 Documentação OpenAPI (YAML)
```
http://localhost:8080/v3/api-docs.yaml
```

## 🚀 Como Usar o Swagger

### 1. **Acessar a Interface**
- Abra o navegador e vá para `http://localhost:8080/swagger-ui.html`
- Você verá uma interface interativa com todos os endpoints da API

### 2. **Autenticação no Swagger**

#### Passo 1: Fazer Login
1. Localize o endpoint `POST /auth/login`
2. Clique em "Try it out"
3. Use um dos usuários de teste:
   ```json
   {
     "cpf": "36900271014",
     "password": "admin123"
   }
   ```
4. Execute a requisição
5. Copie o `accessToken` da resposta

#### Passo 2: Configurar Autorização
1. Clique no botão **"Authorize" 🔒** no topo da página
2. No campo "bearerAuth", cole o token copiado
3. Clique em "Authorize"
4. Agora todos os endpoints protegidos podem ser testados

### 3. **Testando Endpoints**
- Todos os endpoints agora têm documentação completa
- Exemplos de request e response
- Schemas detalhados dos dados
- Códigos de status HTTP explicados

## 🔐 Autenticação Documentada

O Swagger está configurado com dois esquemas de autenticação:

### Bearer Token (JWT)
- **Tipo**: HTTP Bearer
- **Descrição**: Access Token JWT
- **Header**: `Authorization: Bearer {token}`

### Cookie Auth
- **Tipo**: API Key (Cookie)
- **Nome**: `refreshToken`
- **Descrição**: Refresh Token em cookie HTTP-only

## 👥 Usuários de Teste

| CPF | Email | Senha | Role |
|-----|-------|-------|------|
| 36900271014 | admin@sigac.com | admin123 | ADMIN |
| 23456789012 | ana.santos@sigac.com | func123 | ATENDENTE |
| 67890123456 | lucia.ferreira@sigac.com | func456 | GERENTE |
| 34567890123 | carlos.oliveira@email.com | cli123 | CLIENTE |

## 📋 Endpoints Principais

### Autenticação
- `POST /auth/login` - Fazer login
- `POST /auth/refresh` - Renovar token
- `POST /auth/logout` - Fazer logout
- `GET /auth/me` - Obter informações do usuário
- `GET /auth/health` - Health check

### Futuros Módulos
- `/clients` - Gerenciamento de clientes
- `/vehicles` - Gerenciamento de veículos
- `/reservations` - Gerenciamento de reservas
- `/payments` - Gerenciamento de pagamentos

## 🛠️ Configuração

### Propriedades do Swagger
```properties
# SpringDoc OpenAPI Configuration
springdoc.api-docs.enabled=true
springdoc.api-docs.path=/v3/api-docs
springdoc.swagger-ui.enabled=true
springdoc.swagger-ui.path=/swagger-ui.html
springdoc.swagger-ui.operations-sorter=method
springdoc.swagger-ui.tags-sorter=alpha
springdoc.swagger-ui.try-it-out-enabled=true
springdoc.swagger-ui.filter=true
```

### Segurança
Os endpoints do Swagger estão configurados como públicos na segurança:
- `/swagger-ui/**`
- `/v3/api-docs/**`
- `/webjars/**`

## 🎨 Características da Documentação

- **Interface Responsiva**: Funciona em desktop e mobile
- **Testes Interativos**: Execute requisições diretamente da documentação
- **Schemas Detalhados**: Estruturas de dados completas
- **Exemplos Práticos**: Valores de exemplo para todos os campos
- **Autenticação Integrada**: Sistema de autorização integrado
- **Filtros e Busca**: Encontre endpoints rapidamente
- **Exportação**: Baixe a documentação em JSON/YAML

## 🔄 Fluxo de Teste Completo

1. **Acesse** `http://localhost:8080/swagger-ui.html`
2. **Teste** o endpoint `/auth/health` (não requer autenticação)
3. **Faça login** usando `/auth/login` com credenciais de teste
4. **Configure** a autorização com o token recebido
5. **Teste** o endpoint `/auth/me` (requer autenticação)
6. **Explore** outros endpoints conforme necessário

## 📚 Recursos Adicionais

- **OpenAPI 3.0**: Padrão da indústria para documentação de APIs
- **SpringDoc**: Biblioteca moderna para Spring Boot 3+
- **WebFlux**: Suporte completo para APIs reativas
- **JWT**: Documentação de autenticação JWT integrada

---

**🎉 Pronto! Agora você tem uma documentação interativa completa da sua API SIGAC!**
