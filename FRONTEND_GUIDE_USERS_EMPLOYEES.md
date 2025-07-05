# Guia de Integração Frontend - Gerenciamento de Usuários e Funcionários

## 📋 Visão Geral

Este guia fornece todas as informações necessárias para integrar o frontend com os endpoints de gerenciamento de usuários e funcionários da API SIGAC.

**Base URL:** `http://localhost:8080/api/users`

---

## 👥 Endpoints de Usuários (Consulta)

### 1. Listar Usuários
**GET** `/api/users`

**Parâmetros de Query (opcionais):**
- `page` (int): Número da página (padrão: 0)
- `size` (int): Tamanho da página (padrão: 10)

**Exemplo de Requisição:**
```javascript
// Lista todos os usuários (sem paginação)
const response = await fetch('/api/users');

// Lista com paginação
const response = await fetch('/api/users?page=0&size=10');
```

**Resposta de Sucesso (200):**
```json
{
  "success": true,
  "data": {
    "content": [
      {
        "cpf": "12345678901",
        "email": "usuario@email.com",
        "name": "João Silva",
        "address": "Rua Principal, 123",
        "phone": "(11) 99999-9999"
      }
    ],
    "page": 0,
    "size": 10,
    "totalElements": 50,
    "totalPages": 5,
    "hasNext": true,
    "hasPrevious": false
  },
  "timestamp": "2025-07-05T10:30:00Z"
}
```

---

### 2. Buscar Usuário por CPF
**GET** `/api/users/{cpf}`

**Exemplo de Requisição:**
```javascript
const cpf = "12345678901";
const response = await fetch(`/api/users/${cpf}`);
```

**Resposta de Sucesso (200):**
```json
{
  "success": true,
  "data": {
    "cpf": "12345678901",
    "email": "usuario@email.com",
    "name": "João Silva",
    "address": "Rua Principal, 123",
    "phone": "(11) 99999-9999"
  },
  "timestamp": "2025-07-05T10:30:00Z"
}
```

---

### 3. Buscar Usuário por Email
**GET** `/api/users/email/{email}`

**Exemplo de Requisição:**
```javascript
const email = "usuario@email.com";
const response = await fetch(`/api/users/email/${email}`);
```

**Resposta:** Mesma estrutura do endpoint anterior.

---

### 4. Alterar Senha do Usuário
**PATCH** `/api/users/{cpf}/password`

**Estrutura do Body:**
```json
{
  "currentPassword": "senhaAtual123",
  "newPassword": "novaSenha456"
}
```

**Exemplo de Requisição:**
```javascript
const cpf = "12345678901";
const passwordData = {
  currentPassword: "senhaAtual123",
  newPassword: "novaSenha456"
};

const response = await fetch(`/api/users/${cpf}/password`, {
  method: 'PATCH',
  headers: {
    'Content-Type': 'application/json',
  },
  body: JSON.stringify(passwordData)
});
```

**Resposta de Sucesso (200):**
```json
{
  "success": true,
  "message": "Senha alterada com sucesso",
  "timestamp": "2025-07-05T10:30:00Z"
}
```

---

### 5. Excluir Usuário
**DELETE** `/api/users/{cpf}`

**Exemplo de Requisição:**
```javascript
const cpf = "12345678901";
const response = await fetch(`/api/users/${cpf}`, {
  method: 'DELETE'
});
```

**Resposta de Sucesso (200):**
```json
{
  "success": true,
  "message": "Usuário excluído com sucesso",
  "timestamp": "2025-07-05T10:30:00Z"
}
```

---

### 6. Verificar se Usuário Existe por CPF
**GET** `/api/users/{cpf}/exists`

**Exemplo de Requisição:**
```javascript
const cpf = "12345678901";
const response = await fetch(`/api/users/${cpf}/exists`);
```

**Resposta de Sucesso (200):**
```json
{
  "success": true,
  "data": true,
  "timestamp": "2025-07-05T10:30:00Z"
}
```

---

### 7. Verificar se Email Existe
**GET** `/api/users/email/{email}/exists`

**Exemplo de Requisição:**
```javascript
const email = "usuario@email.com";
const response = await fetch(`/api/users/email/${email}/exists`);
```

**Resposta:** Mesma estrutura do endpoint anterior.

---

## 👨‍💼 Endpoints de Funcionários

### 8. Listar Funcionários
**GET** `/api/users/employees`

**Parâmetros de Query (opcionais):**
- `page` (int): Número da página (padrão: 0)
- `size` (int): Tamanho da página (padrão: 10)

**Exemplo de Requisição:**
```javascript
// Lista todos os funcionários
const response = await fetch('/api/users/employees');

// Lista com paginação
const response = await fetch('/api/users/employees?page=0&size=10');
```

**Resposta de Sucesso (200):**
```json
{
  "success": true,
  "data": {
    "content": [
      {
        "cpf": "12345678901",
        "email": "funcionario@email.com",
        "name": "Maria Santos",
        "address": "Av. Trabalho, 456",
        "phone": "(11) 88888-8888",
        "role": "MANAGER"
      }
    ],
    "page": 0,
    "size": 10,
    "totalElements": 15,
    "totalPages": 2,
    "hasNext": true,
    "hasPrevious": false
  },
  "timestamp": "2025-07-05T10:30:00Z"
}
```

---

### 9. Buscar Funcionário por CPF
**GET** `/api/users/employees/{cpf}`

**Exemplo de Requisição:**
```javascript
const cpf = "12345678901";
const response = await fetch(`/api/users/employees/${cpf}`);
```

**Resposta de Sucesso (200):**
```json
{
  "success": true,
  "data": {
    "cpf": "12345678901",
    "email": "funcionario@email.com",
    "name": "Maria Santos",
    "address": "Av. Trabalho, 456",
    "phone": "(11) 88888-8888",
    "role": "MANAGER"
  },
  "timestamp": "2025-07-05T10:30:00Z"
}
```

---

### 10. Buscar Funcionários por Cargo
**GET** `/api/users/employees/role/{role}`

**Parâmetros de Query (opcionais):**
- `page` (int): Número da página (padrão: 0)
- `size` (int): Tamanho da página (padrão: 10)

**Exemplo de Requisição:**
```javascript
const role = "MANAGER";
const response = await fetch(`/api/users/employees/role/${role}`);

// Com paginação
const response = await fetch(`/api/users/employees/role/${role}?page=0&size=5`);
```

**Resposta:** Mesma estrutura do endpoint de listar funcionários.

---

### 11. Criar Novo Funcionário
**POST** `/api/users/employees`

**Estrutura do Body:**
```json
{
  "cpf": "12345678901",
  "email": "novofuncionario@email.com",
  "name": "Pedro Oliveira",
  "password": "senha123456",
  "address": "Rua Empresa, 789",
  "phone": "(11) 77777-7777",
  "role": "EMPLOYEE"
}
```

**Exemplo de Requisição:**
```javascript
const employeeData = {
  cpf: "12345678901",
  email: "novofuncionario@email.com",
  name: "Pedro Oliveira",
  password: "senha123456",
  address: "Rua Empresa, 789",
  phone: "(11) 77777-7777",
  role: "EMPLOYEE"
};

const response = await fetch('/api/users/employees', {
  method: 'POST',
  headers: {
    'Content-Type': 'application/json',
  },
  body: JSON.stringify(employeeData)
});
```

**Resposta de Sucesso (201):**
```json
{
  "success": true,
  "data": {
    "cpf": "12345678901",
    "email": "novofuncionario@email.com",
    "name": "Pedro Oliveira",
    "address": "Rua Empresa, 789",
    "phone": "(11) 77777-7777",
    "role": "EMPLOYEE"
  },
  "timestamp": "2025-07-05T10:30:00Z"
}
```

---

### 12. Converter Usuário em Funcionário
**POST** `/api/users/employees/{cpf}/convert`

**Parâmetros de Query:**
- `role` (string): Cargo do funcionário (obrigatório)

**Exemplo de Requisição:**
```javascript
const cpf = "12345678901";
const role = "MANAGER";
const response = await fetch(`/api/users/employees/${cpf}/convert?role=${role}`, {
  method: 'POST'
});
```

**Resposta:** Mesma estrutura do endpoint de criação.

---

### 13. Atualizar Cargo do Funcionário
**PATCH** `/api/users/employees/{cpf}/role`

**Parâmetros de Query:**
- `role` (string): Novo cargo do funcionário (obrigatório)

**Exemplo de Requisição:**
```javascript
const cpf = "12345678901";
const newRole = "SENIOR_MANAGER";
const response = await fetch(`/api/users/employees/${cpf}/role?role=${newRole}`, {
  method: 'PATCH'
});
```

**Resposta de Sucesso (200):**
```json
{
  "success": true,
  "data": {
    "cpf": "12345678901",
    "email": "funcionario@email.com",
    "name": "Maria Santos",
    "address": "Av. Trabalho, 456",
    "phone": "(11) 88888-8888",
    "role": "SENIOR_MANAGER"
  },
  "timestamp": "2025-07-05T10:30:00Z"
}
```

---

### 14. Remover Funcionário (Manter Usuário)
**DELETE** `/api/users/employees/{cpf}`

**Exemplo de Requisição:**
```javascript
const cpf = "12345678901";
const response = await fetch(`/api/users/employees/${cpf}`, {
  method: 'DELETE'
});
```

**Resposta de Sucesso (200):**
```json
{
  "success": true,
  "message": "Funcionário removido com sucesso",
  "timestamp": "2025-07-05T10:30:00Z"
}
```

---

### 15. Excluir Funcionário e Usuário
**DELETE** `/api/users/employees/{cpf}/with-user`

**Exemplo de Requisição:**
```javascript
const cpf = "12345678901";
const response = await fetch(`/api/users/employees/${cpf}/with-user`, {
  method: 'DELETE'
});
```

**Resposta de Sucesso (200):**
```json
{
  "success": true,
  "message": "Funcionário e usuário excluídos com sucesso",
  "timestamp": "2025-07-05T10:30:00Z"
}
```

---

### 16. Verificar se Usuário é Funcionário
**GET** `/api/users/employees/{cpf}/exists`

**Exemplo de Requisição:**
```javascript
const cpf = "12345678901";
const response = await fetch(`/api/users/employees/${cpf}/exists`);
```

**Resposta de Sucesso (200):**
```json
{
  "success": true,
  "data": true,
  "timestamp": "2025-07-05T10:30:00Z"
}
```

---

## 🚨 Tratamento de Erros

### Códigos de Status HTTP

- **200 OK:** Operação realizada com sucesso
- **201 Created:** Recurso criado com sucesso
- **400 Bad Request:** Dados inválidos ou ausentes
- **404 Not Found:** Usuário/Funcionário não encontrado
- **409 Conflict:** Conflito (ex: CPF/email já existe)
- **500 Internal Server Error:** Erro interno do servidor

### Estrutura de Resposta de Erro

```json
{
  "success": false,
  "error": {
    "code": "RESOURCE_NOT_FOUND",
    "message": "Funcionário não encontrado",
    "details": "Nenhum funcionário encontrado com o CPF: 12345678901"
  },
  "timestamp": "2025-07-05T10:30:00Z"
}
```

---

## 🛠️ Exemplo de Implementação JavaScript

```javascript
class UserEmployeeService {
  constructor(baseUrl = '/api/users') {
    this.baseUrl = baseUrl;
  }

  // ===== MÉTODOS DE USUÁRIOS =====
  
  async getAllUsers(page = 0, size = 10) {
    try {
      const response = await fetch(`${this.baseUrl}?page=${page}&size=${size}`);
      return await response.json();
    } catch (error) {
      console.error('Erro ao buscar usuários:', error);
      throw error;
    }
  }

  async getUserByCpf(cpf) {
    try {
      const response = await fetch(`${this.baseUrl}/${cpf}`);
      if (!response.ok) {
        throw new Error(`Usuário não encontrado: ${cpf}`);
      }
      return await response.json();
    } catch (error) {
      console.error('Erro ao buscar usuário:', error);
      throw error;
    }
  }

  async changeUserPassword(cpf, passwordData) {
    try {
      const response = await fetch(`${this.baseUrl}/${cpf}/password`, {
        method: 'PATCH',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(passwordData)
      });
      
      if (!response.ok) {
        const errorData = await response.json();
        throw new Error(errorData.error.message);
      }
      
      return await response.json();
    } catch (error) {
      console.error('Erro ao alterar senha:', error);
      throw error;
    }
  }

  // ===== MÉTODOS DE FUNCIONÁRIOS =====
  
  async getAllEmployees(page = 0, size = 10) {
    try {
      const response = await fetch(`${this.baseUrl}/employees?page=${page}&size=${size}`);
      return await response.json();
    } catch (error) {
      console.error('Erro ao buscar funcionários:', error);
      throw error;
    }
  }

  async getEmployeesBRole(role, page = 0, size = 10) {
    try {
      const response = await fetch(`${this.baseUrl}/employees/role/${role}?page=${page}&size=${size}`);
      return await response.json();
    } catch (error) {
      console.error('Erro ao buscar funcionários por cargo:', error);
      throw error;
    }
  }

  async createEmployee(employeeData) {
    try {
      const response = await fetch(`${this.baseUrl}/employees`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(employeeData)
      });
      
      if (!response.ok) {
        const errorData = await response.json();
        throw new Error(errorData.error.message);
      }
      
      return await response.json();
    } catch (error) {
      console.error('Erro ao criar funcionário:', error);
      throw error;
    }
  }

  async convertUserToEmployee(cpf, role) {
    try {
      const response = await fetch(`${this.baseUrl}/employees/${cpf}/convert?role=${role}`, {
        method: 'POST'
      });
      
      if (!response.ok) {
        const errorData = await response.json();
        throw new Error(errorData.error.message);
      }
      
      return await response.json();
    } catch (error) {
      console.error('Erro ao converter usuário em funcionário:', error);
      throw error;
    }
  }

  async updateEmployeeRole(cpf, newRole) {
    try {
      const response = await fetch(`${this.baseUrl}/employees/${cpf}/role?role=${newRole}`, {
        method: 'PATCH'
      });
      
      if (!response.ok) {
        const errorData = await response.json();
        throw new Error(errorData.error.message);
      }
      
      return await response.json();
    } catch (error) {
      console.error('Erro ao atualizar cargo do funcionário:', error);
      throw error;
    }
  }

  async deleteEmployee(cpf, deleteUser = false) {
    try {
      const endpoint = deleteUser ? `${this.baseUrl}/employees/${cpf}/with-user` : `${this.baseUrl}/employees/${cpf}`;
      const response = await fetch(endpoint, {
        method: 'DELETE'
      });
      
      if (!response.ok) {
        const errorData = await response.json();
        throw new Error(errorData.error.message);
      }
      
      return await response.json();
    } catch (error) {
      console.error('Erro ao excluir funcionário:', error);
      throw error;
    }
  }
}

// Uso da classe
const userEmployeeService = new UserEmployeeService();

// Exemplo de uso
async function exemploUso() {
  try {
    // Listar funcionários
    const employees = await userEmployeeService.getAllEmployees(0, 10);
    console.log('Funcionários:', employees);

    // Criar novo funcionário
    const newEmployee = await userEmployeeService.createEmployee({
      cpf: "98765432101",
      email: "novofunc@email.com",
      name: "Novo Funcionário",
      password: "senha123",
      address: "Rua Nova, 123",
      phone: "(11) 99999-9999",
      role: "EMPLOYEE"
    });
    console.log('Funcionário criado:', newEmployee);

    // Buscar funcionários por cargo
    const managers = await userEmployeeService.getEmployeesByRole("MANAGER");
    console.log('Gerentes:', managers);

  } catch (error) {
    console.error('Erro:', error.message);
  }
}
```

---

## 🔍 Validações de Campo

### Campos Obrigatórios (Criação de Funcionário):
- **cpf:** 11 dígitos exatos
- **email:** Formato de email válido
- **name:** Entre 2 e 255 caracteres
- **password:** Mínimo 6 caracteres
- **role:** Cargo do funcionário (não pode estar vazio)

### Campos Opcionais:
- **address:** Endereço do funcionário
- **phone:** Telefone do funcionário

### Cargos Comuns (Exemplos):
- `ADMIN` - Administrador
- `MANAGER` - Gerente
- `SENIOR_MANAGER` - Gerente Sênior
- `EMPLOYEE` - Funcionário
- `INTERN` - Estagiário

---

## 📝 Fluxos de Trabalho Recomendados

### 1. Criação de Funcionário
```javascript
// Opção 1: Criar funcionário diretamente
const employee = await userEmployeeService.createEmployee({
  cpf: "12345678901",
  email: "func@email.com",
  name: "João Silva",
  password: "senha123",
  role: "EMPLOYEE"
});

// Opção 2: Criar usuário via cliente e depois converter
// (Usuário já existe como cliente)
const employee = await userEmployeeService.convertUserToEmployee("12345678901", "EMPLOYEE");
```

### 2. Gestão de Cargos
```javascript
// Buscar funcionários por cargo específico
const managers = await userEmployeeService.getEmployeesByRole("MANAGER");

// Promover funcionário
await userEmployeeService.updateEmployeeRole("12345678901", "SENIOR_MANAGER");
```

### 3. Remoção de Funcionário
```javascript
// Remover apenas o vínculo de funcionário (usuário permanece)
await userEmployeeService.deleteEmployee("12345678901", false);

// Remover funcionário e usuário completamente
await userEmployeeService.deleteEmployee("12345678901", true);
```

---

## 📋 Notas Importantes

1. **CPF deve ser único** no sistema (entre todos os usuários)
2. **Email deve ser único** no sistema (entre todos os usuários)
3. **Senhas são criptografadas** automaticamente
4. **Funcionários são usuários** com cargo atribuído
5. **Remoção de funcionário** não remove o usuário por padrão
6. **Conversão** permite transformar cliente em funcionário e vice-versa
7. **Cargos são flexíveis** - defina conforme sua necessidade
8. **Paginação é opcional** - sem parâmetros retorna todos os resultados
9. **Todas as operações** são reativas e assíncronas

---

Este guia cobre todos os endpoints disponíveis para gerenciamento de usuários e funcionários. Para dúvidas ou problemas, consulte a documentação da API ou entre em contato com a equipe de desenvolvimento.
