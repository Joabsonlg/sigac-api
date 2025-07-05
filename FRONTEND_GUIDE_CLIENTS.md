# Guia de Integração Frontend - Gerenciamento de Clientes

## 📋 Visão Geral

Este guia fornece todas as informações necessárias para integrar o frontend com os endpoints de gerenciamento de clientes da API SIGAC.

**Base URL:** `http://localhost:8080/api/clients`

---

## 🎯 Endpoints Disponíveis

### 1. Listar Clientes
**GET** `/api/clients`

**Parâmetros de Query (opcionais):**
- `page` (int): Número da página (padrão: 0)
- `size` (int): Tamanho da página (padrão: 10)

**Exemplo de Requisição:**
```javascript
// Lista todos os clientes (sem paginação)
const response = await fetch('/api/clients');

// Lista com paginação
const response = await fetch('/api/clients?page=0&size=10');
```

**Resposta de Sucesso (200):**
```json
{
  "success": true,
  "data": {
    "content": [
      {
        "cpf": "12345678901",
        "email": "cliente@email.com",
        "name": "Maria Silva",
        "address": "Rua das Flores, 123",
        "phone": "(11) 99999-9999"
      }
    ],
    "page": 0,
    "size": 10,
    "totalElements": 25,
    "totalPages": 3,
    "hasNext": true,
    "hasPrevious": false
  },
  "timestamp": "2025-07-05T10:30:00Z"
}
```

---

### 2. Buscar Cliente por CPF
**GET** `/api/clients/{cpf}`

**Exemplo de Requisição:**
```javascript
const cpf = "12345678901";
const response = await fetch(`/api/clients/${cpf}`);
```

**Resposta de Sucesso (200):**
```json
{
  "success": true,
  "data": {
    "cpf": "12345678901",
    "email": "cliente@email.com",
    "name": "Maria Silva",
    "address": "Rua das Flores, 123",
    "phone": "(11) 99999-9999"
  },
  "timestamp": "2025-07-05T10:30:00Z"
}
```

---

### 3. Buscar Cliente por Email
**GET** `/api/clients/email/{email}`

**Exemplo de Requisição:**
```javascript
const email = "cliente@email.com";
const response = await fetch(`/api/clients/email/${email}`);
```

**Resposta:** Mesma estrutura do endpoint anterior.

---

### 4. Criar Novo Cliente
**POST** `/api/clients`

**Estrutura do Body:**
```json
{
  "cpf": "12345678901",
  "email": "novocliente@email.com",
  "name": "João Santos",
  "password": "senha123456",
  "address": "Av. Principal, 456",
  "phone": "(11) 88888-8888"
}
```

**Exemplo de Requisição:**
```javascript
const clientData = {
  cpf: "12345678901",
  email: "novocliente@email.com",
  name: "João Santos",
  password: "senha123456",
  address: "Av. Principal, 456",
  phone: "(11) 88888-8888"
};

const response = await fetch('/api/clients', {
  method: 'POST',
  headers: {
    'Content-Type': 'application/json',
  },
  body: JSON.stringify(clientData)
});
```

**Resposta de Sucesso (201):**
```json
{
  "success": true,
  "data": {
    "cpf": "12345678901",
    "email": "novocliente@email.com",
    "name": "João Santos",
    "address": "Av. Principal, 456",
    "phone": "(11) 88888-8888"
  },
  "timestamp": "2025-07-05T10:30:00Z"
}
```

---

### 5. Converter Usuário em Cliente
**POST** `/api/clients/{cpf}/convert`

**Exemplo de Requisição:**
```javascript
const cpf = "12345678901";
const response = await fetch(`/api/clients/${cpf}/convert`, {
  method: 'POST'
});
```

**Resposta:** Mesma estrutura do endpoint de criação.

---

### 6. Atualizar Cliente
**PUT** `/api/clients/{cpf}`

**Estrutura do Body:**
```json
{
  "email": "emailatualizado@email.com",
  "name": "Nome Atualizado",
  "address": "Novo Endereço, 789",
  "phone": "(11) 77777-7777"
}
```

**Exemplo de Requisição:**
```javascript
const cpf = "12345678901";
const updateData = {
  email: "emailatualizado@email.com",
  name: "Nome Atualizado",
  address: "Novo Endereço, 789",
  phone: "(11) 77777-7777"
};

const response = await fetch(`/api/clients/${cpf}`, {
  method: 'PUT',
  headers: {
    'Content-Type': 'application/json',
  },
  body: JSON.stringify(updateData)
});
```

---

### 7. Alterar Senha do Cliente
**PATCH** `/api/clients/{cpf}/password`

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

const response = await fetch(`/api/clients/${cpf}/password`, {
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

### 8. Excluir Cliente
**DELETE** `/api/clients/{cpf}`

**Exemplo de Requisição:**
```javascript
const cpf = "12345678901";
const response = await fetch(`/api/clients/${cpf}`, {
  method: 'DELETE'
});
```

**Resposta de Sucesso (200):**
```json
{
  "success": true,
  "message": "Cliente excluído com sucesso",
  "timestamp": "2025-07-05T10:30:00Z"
}
```

---

### 9. Verificar se Cliente Existe
**GET** `/api/clients/{cpf}/exists`

**Exemplo de Requisição:**
```javascript
const cpf = "12345678901";
const response = await fetch(`/api/clients/${cpf}/exists`);
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

### 10. Verificar se Email Existe
**GET** `/api/clients/email/{email}/exists`

**Exemplo de Requisição:**
```javascript
const email = "cliente@email.com";
const response = await fetch(`/api/clients/email/${email}/exists`);
```

**Resposta:** Mesma estrutura do endpoint anterior.

---

## 🚨 Tratamento de Erros

### Códigos de Status HTTP

- **200 OK:** Operação realizada com sucesso
- **201 Created:** Recurso criado com sucesso
- **400 Bad Request:** Dados inválidos ou ausentes
- **404 Not Found:** Cliente não encontrado
- **409 Conflict:** Conflito (ex: CPF/email já existe)
- **500 Internal Server Error:** Erro interno do servidor

### Estrutura de Resposta de Erro

```json
{
  "success": false,
  "error": {
    "code": "RESOURCE_NOT_FOUND",
    "message": "Cliente não encontrado",
    "details": "Nenhum cliente encontrado com o CPF: 12345678901"
  },
  "timestamp": "2025-07-05T10:30:00Z"
}
```

---

## 🛠️ Exemplo de Implementação JavaScript

```javascript
class ClientService {
  constructor(baseUrl = '/api/clients') {
    this.baseUrl = baseUrl;
  }

  async getAllClients(page = 0, size = 10) {
    try {
      const response = await fetch(`${this.baseUrl}?page=${page}&size=${size}`);
      return await response.json();
    } catch (error) {
      console.error('Erro ao buscar clientes:', error);
      throw error;
    }
  }

  async getClientByCpf(cpf) {
    try {
      const response = await fetch(`${this.baseUrl}/${cpf}`);
      if (!response.ok) {
        throw new Error(`Cliente não encontrado: ${cpf}`);
      }
      return await response.json();
    } catch (error) {
      console.error('Erro ao buscar cliente:', error);
      throw error;
    }
  }

  async createClient(clientData) {
    try {
      const response = await fetch(this.baseUrl, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(clientData)
      });
      
      if (!response.ok) {
        const errorData = await response.json();
        throw new Error(errorData.error.message);
      }
      
      return await response.json();
    } catch (error) {
      console.error('Erro ao criar cliente:', error);
      throw error;
    }
  }

  async updateClient(cpf, updateData) {
    try {
      const response = await fetch(`${this.baseUrl}/${cpf}`, {
        method: 'PUT',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(updateData)
      });
      
      if (!response.ok) {
        const errorData = await response.json();
        throw new Error(errorData.error.message);
      }
      
      return await response.json();
    } catch (error) {
      console.error('Erro ao atualizar cliente:', error);
      throw error;
    }
  }

  async deleteClient(cpf) {
    try {
      const response = await fetch(`${this.baseUrl}/${cpf}`, {
        method: 'DELETE'
      });
      
      if (!response.ok) {
        const errorData = await response.json();
        throw new Error(errorData.error.message);
      }
      
      return await response.json();
    } catch (error) {
      console.error('Erro ao excluir cliente:', error);
      throw error;
    }
  }
}

// Uso da classe
const clientService = new ClientService();

// Exemplo de uso
async function exemploUso() {
  try {
    // Listar clientes
    const clients = await clientService.getAllClients(0, 10);
    console.log('Clientes:', clients);

    // Criar novo cliente
    const newClient = await clientService.createClient({
      cpf: "98765432101",
      email: "novo@email.com",
      name: "Novo Cliente",
      password: "senha123",
      address: "Rua Nova, 123",
      phone: "(11) 99999-9999"
    });
    console.log('Cliente criado:', newClient);

  } catch (error) {
    console.error('Erro:', error.message);
  }
}
```

---

## 🔍 Validações de Campo

### Campos Obrigatórios (Criação):
- **cpf:** 11 dígitos exatos
- **email:** Formato de email válido
- **name:** Entre 2 e 255 caracteres
- **password:** Mínimo 6 caracteres

### Campos Opcionais:
- **address:** Endereço do cliente
- **phone:** Telefone do cliente

### Campos Obrigatórios (Atualização):
- **email:** Formato de email válido
- **name:** Entre 2 e 255 caracteres

---

## 📝 Notas Importantes

1. **CPF deve ser único** no sistema
2. **Email deve ser único** no sistema
3. **Senhas são criptografadas** automaticamente
4. **Todos os endpoints** retornam respostas reativas (Mono)
5. **Paginação é opcional** - sem parâmetros retorna todos os resultados
6. **Timestamps** seguem o padrão ISO 8601 (UTC)

---

Este guia cobre todos os endpoints disponíveis para gerenciamento de clientes. Para dúvidas ou problemas, consulte a documentação da API ou entre em contato com a equipe de desenvolvimento.
