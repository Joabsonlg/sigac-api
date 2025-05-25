# Reactive Spring API - Module Development Guide

## Table of Contents

1. [Overview](#overview)
2. [Project Architecture](#project-architecture)
3. [Module Structure](#module-structure)
4. [Common Package Overview](#common-package-overview)
5. [Manual Query Handling](#manual-query-handling)
6. [Step-by-Step Module Creation](#step-by-step-module-creation)
7. [Using Base Classes](#using-base-classes)
8. [Error Handling & Validation](#error-handling--validation)
9. [Response Patterns](#response-patterns)
10. [Naming Conventions](#naming-conventions)
11. [Component Guidelines](#component-guidelines)
12. [Best Practices](#best-practices)
13. [Testing Strategy](#testing-strategy)

## Overview

This guide defines the standard process for creating and managing modules in a fully reactive Spring Boot API that uses WebFlux and R2DBC. JPA and Hibernate are not used; all database interactions are handled via custom queries.

## Project Architecture

The project follows a modular architecture with a robust common foundation:

* **common**: Shared classes, configuration, error handling, utilities, and base classes
* **\[module-name]**: A dedicated package for each business domain (e.g., `client`, `product`, etc.)

Technologies used:

* Spring WebFlux
* Spring Data R2DBC
* PostgreSQL driver and R2DBC implementation

### Key Benefits of Our Architecture

* **Consistency**: Standardized responses, error handling, and patterns across all modules
* **Productivity**: Base classes accelerate development of new modules
* **Maintainability**: Centralized common functionality reduces code duplication
* **Quality**: Pre-built validations and error handling ensure robustness

## Module Structure

Each module should follow this standard package structure:

```
io.github.joabsonlg.sigac_api.[module_name]/
├── controller/         # REST API controllers (extend BaseController)
├── dto/                # Data Transfer Objects
├── handler/            # Business logic handlers (extend BaseHandler)
├── mapper/             # Mapper classes (Entity <-> DTO) - optional if using BaseHandler
├── model/              # Entity models (record or class)
├── repository/         # R2DBC repositories (extend BaseRepository)
└── validator/          # Input validators (optional, use CommonValidator)
```

## Common Package Overview

The `common` package provides a solid foundation with the following components:

### 🔧 Configuration (`common.config`)
- **DatabaseConfig**: R2DBC setup with DatabaseClient and R2dbcEntityTemplate beans
- **WebConfig**: WebFlux configuration with CORS support

### 🏗️ Base Classes (`common.base`)
- **BaseRepository<T, ID>**: Common database operations (count, exists, delete)
- **BaseHandler<T, D, ID>**: DTO/Entity conversions and business logic patterns
- **BaseController<D, ID>**: Standardized HTTP responses and pagination

### ❌ Exception Handling (`common.exception`)
- **BusinessException**: Base class for business logic exceptions
- **ResourceNotFoundException**: For HTTP 404 scenarios
- **ValidationException**: For HTTP 400 scenarios
- **ConflictException**: For HTTP 409 scenarios
- **GlobalExceptionHandler**: Centralized exception handling

### 📤 Response Patterns (`common.response`)
- **ApiResponse<T>**: Standardized success responses
- **ErrorResponse**: Standardized error responses
- **PageResponse<T>**: Paginated data responses

### ✅ Validation (`common.validator`)
- **CommonValidator**: Email, CPF, CNPJ, phone, and general validations

### 🛠️ Utilities (`common.util`)
- **StringUtil**: String operations, formatting, masking
- **ReactiveUtil**: Reactive programming helpers
- **PaginationUtil**: Pagination utilities for R2DBC

## Manual Query Handling

Since we are not using JPA:

* All repositories must be implemented manually.
* Use `DatabaseClient` or `R2dbcEntityTemplate` for queries.
* Define SQL queries explicitly.

## Step-by-Step Module Creation

### 1. Define Module Purpose

Document what this module is responsible for (e.g., "Manages operations related to products").

### 2. Create Package Structure

Under `io.github.joabsonlg.sicac_api`, create the folder `yourmodule` with subfolders listed above.

### 3. Define Model

Use either a Java record or class annotated with `@Table`, `@Column` from `org.springframework.data.relational.core.mapping`

```java
/**
 * Entity model representing a Product.
 */
@Table("products")
public record Product(
    @Id Long id,
    String name,
    BigDecimal price
) {}
```

### 4. Create DTO

```java
/**
 * DTO for transferring Product data.
 */
public record ProductDTO(Long id, String name, BigDecimal price) {}
```

### 5. Create Mapper

```java
/**
 * Mapper class to convert between Product and ProductDTO.
 */
@Component
public class ProductMapper {
    public Product toEntity(ProductDTO dto) { return new Product(dto.id(), dto.name(), dto.price()); }
    public ProductDTO toDto(Product entity) { return new ProductDTO(entity.id(), entity.name(), entity.price()); }
}
```

### 6. Implement Repository (Using BaseRepository)

**Extend BaseRepository** for common operations and implement specific queries:

```java
/**
 * Repository for executing manual SQL queries related to Product.
 * Extends BaseRepository for common database operations.
 */
@Repository
public class ProductRepository extends BaseRepository<Product, Long> {
    
    public ProductRepository(DatabaseClient databaseClient) {
        super(databaseClient);
    }
    
    @Override
    protected String getTableName() {
        return "products";
    }
    
    // Custom queries
    public Flux<Product> findAll() {
        return databaseClient.sql("SELECT * FROM products")
                     .map((row, meta) -> new Product(
                         row.get("id", Long.class),
                         row.get("name", String.class),
                         row.get("price", BigDecimal.class)))
                     .all();
    }
    
    public Mono<Product> findById(Long id) {
        return databaseClient.sql("SELECT * FROM products WHERE id = $1")
                     .bind("$1", id)
                     .map((row, meta) -> new Product(
                         row.get("id", Long.class),
                         row.get("name", String.class),
                         row.get("price", BigDecimal.class)))
                     .one();
    }
    
    public Flux<Product> findWithPagination(int page, int size) {
        return databaseClient.sql("SELECT * FROM products" + createLimitOffset(page, size))
                     .map((row, meta) -> new Product(
                         row.get("id", Long.class),
                         row.get("name", String.class),
                         row.get("price", BigDecimal.class)))
                     .all();
    }
    
    public Mono<Product> save(Product product) {
        if (product.id() == null) {
            return databaseClient.sql("INSERT INTO products (name, price) VALUES ($1, $2) RETURNING *")
                         .bind("$1", product.name())
                         .bind("$2", product.price())
                         .map((row, meta) -> new Product(
                             row.get("id", Long.class),
                             row.get("name", String.class),
                             row.get("price", BigDecimal.class)))
                         .one();
        } else {
            return databaseClient.sql("UPDATE products SET name = $1, price = $2 WHERE id = $3 RETURNING *")
                         .bind("$1", product.name())
                         .bind("$2", product.price())
                         .bind("$3", product.id())
                         .map((row, meta) -> new Product(
                             row.get("id", Long.class),
                             row.get("name", String.class),
                             row.get("price", BigDecimal.class)))
                         .one();
        }
    }
    
    // Inherited methods from BaseRepository:
    // - count(): Mono<Long>
    // - existsById(Long id): Mono<Boolean>
    // - deleteById(Long id): Mono<Void>
    // - countWithCondition(String whereClause, Object... parameters): Mono<Long>
}
```

### 7. Implement Handler (Using BaseHandler)

**Extend BaseHandler** for DTO/Entity conversions and business logic:

```java
/**
 * Handler for business logic related to Product.
 * Extends BaseHandler for DTO/Entity conversions.
 */
@Service
public class ProductHandler extends BaseHandler<Product, ProductDTO, Long> {
    
    private final ProductRepository repository;
    private final CommonValidator validator;
    
    public ProductHandler(ProductRepository repository, CommonValidator validator) {
        this.repository = repository;
        this.validator = validator;
    }
    
    @Override
    protected ProductDTO toDto(Product entity) {
        return new ProductDTO(entity.id(), entity.name(), entity.price());
    }
    
    @Override
    protected Product toEntity(ProductDTO dto) {
        return new Product(dto.id(), dto.name(), dto.price());
    }
    
    // Business logic methods
    public Flux<ProductDTO> getAll() {
        return toDtoFlux(repository.findAll());
    }
    
    public Mono<ProductDTO> getById(Long id) {
        return repository.findById(id)
                .map(this::toDto)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Product", id)));
    }
    
    public Mono<PageResponse<ProductDTO>> getAllPaginated(int page, int size) {
        Flux<ProductDTO> products = toDtoFlux(repository.findWithPagination(page, size));
        Mono<Long> totalElements = repository.count();
        return createPageResponse(products, page, size, totalElements);
    }
    
    public Mono<ProductDTO> create(ProductDTO dto) {
        // Validation using CommonValidator
        validator.validateRequired(dto.name(), "name");
        validator.validatePositive(dto.price(), "price");
        
        return repository.save(toEntity(dto))
                .map(this::toDto);
    }
    
    public Mono<ProductDTO> update(Long id, ProductDTO dto) {
        validator.validateRequired(dto.name(), "name");
        validator.validatePositive(dto.price(), "price");
        
        return repository.existsById(id)
                .filter(exists -> exists)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Product", id)))
                .then(repository.save(new Product(id, dto.name(), dto.price())))
                .map(this::toDto);
    }
    
    public Mono<Void> delete(Long id) {
        return repository.existsById(id)
                .filter(exists -> exists)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Product", id)))
                .then(repository.deleteById(id));
    }
}
```

### 8. Implement Controller (Using BaseController)

**Extend BaseController** for standardized HTTP responses and patterns:

```java
/**
 * Controller that exposes REST endpoints for Product.
 * Extends BaseController for standardized HTTP responses.
 */
@RestController
@RequestMapping("/products")
public class ProductController extends BaseController<ProductDTO, Long> {
    
    private final ProductHandler handler;

    public ProductController(ProductHandler handler) {
        this.handler = handler;
    }

    @GetMapping
    public Mono<ResponseEntity<ApiResponse<List<ProductDTO>>>> getAll() {
        return handler.getAll()
                .collectList()
                .map(this::createSuccessResponse);
    }
    
    @GetMapping("/{id}")
    public Mono<ResponseEntity<ApiResponse<ProductDTO>>> getById(@PathVariable Long id) {
        return handler.getById(id)
                .map(this::createSuccessResponse);
    }
    
    @GetMapping("/paginated")
    public Mono<ResponseEntity<PageResponse<ProductDTO>>> getAllPaginated(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return handler.getAllPaginated(page, size)
                .map(pageResponse -> createPageResponse(pageResponse));
    }
    
    @PostMapping
    public Mono<ResponseEntity<ApiResponse<ProductDTO>>> create(@RequestBody ProductDTO dto) {
        return handler.create(dto)
                .map(this::createCreatedResponse);
    }
    
    @PutMapping("/{id}")
    public Mono<ResponseEntity<ApiResponse<ProductDTO>>> update(
            @PathVariable Long id, 
            @RequestBody ProductDTO dto) {
        return handler.update(id, dto)
                .map(this::createSuccessResponse);
    }
    
    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<ApiResponse<Void>>> delete(@PathVariable Long id) {
        return handler.delete(id)
                .then(Mono.fromCallable(this::createNoContentResponse));
    }
      // Inherited methods from BaseController:
    // - createSuccessResponse(T data): ResponseEntity<ApiResponse<T>>
    // - createCreatedResponse(T data): ResponseEntity<ApiResponse<T>>
    // - createNoContentResponse(): ResponseEntity<ApiResponse<Void>>
    // - createPageResponse(PageResponse<T> pageResponse): ResponseEntity<PageResponse<T>>
}
```

## Using Base Classes

The common package provides three powerful base classes that standardize development patterns:

### BaseRepository<T, ID>

Provides common database operations:

```java
public class ProductRepository extends BaseRepository<Product, Long> {
    public ProductRepository(DatabaseClient databaseClient) {
        super(databaseClient);
    }
    
    @Override
    protected String getTableName() {
        return "products";
    }
    
    // Automatically available methods:
    // - count(): Mono<Long>
    // - existsById(Long id): Mono<Boolean>
    // - deleteById(Long id): Mono<Void>
    // - countWithCondition(String whereClause, Object... parameters): Mono<Long>
    
    // Add your custom queries here...
}
```

### BaseHandler<T, D, ID>

Provides DTO/Entity conversion patterns:

```java
public class ProductHandler extends BaseHandler<Product, ProductDTO, Long> {
    @Override
    protected ProductDTO toDto(Product entity) {
        return new ProductDTO(entity.id(), entity.name(), entity.price());
    }
    
    @Override
    protected Product toEntity(ProductDTO dto) {
        return new Product(dto.id(), dto.name(), dto.price());
    }
    
    // Automatically available methods:
    // - toDtoFlux(Flux<Product> entities): Flux<ProductDTO>
    // - createPageResponse(Flux<ProductDTO> data, int page, int size, Mono<Long> total): Mono<PageResponse<ProductDTO>>
    
    // Add your business logic here...
}
```

### BaseController<D, ID>

Provides standardized HTTP response patterns:

```java
public class ProductController extends BaseController<ProductDTO, Long> {
    // Automatically available methods:
    // - createSuccessResponse(ProductDTO data): ResponseEntity<ApiResponse<ProductDTO>>
    // - createCreatedResponse(ProductDTO data): ResponseEntity<ApiResponse<ProductDTO>>
    // - createNoContentResponse(): ResponseEntity<ApiResponse<Void>>
    // - createPageResponse(PageResponse<ProductDTO> pageResponse): ResponseEntity<PageResponse<ProductDTO>>
    
    // Add your endpoints here...
}
```

## Error Handling & Validation

### Exception Hierarchy

The common package provides a structured exception hierarchy:

```java
// Base business exception
throw new BusinessException("Custom business logic error");

// For missing resources (HTTP 404)
throw new ResourceNotFoundException("Product", id);

// For validation errors (HTTP 400)
throw new ValidationException("Name is required");

// For conflicts (HTTP 409)
throw new ConflictException("Product already exists");
```

### Global Exception Handling

All exceptions are automatically handled by `GlobalExceptionHandler`:

```java
// Automatically handles:
// - ResourceNotFoundException -> HTTP 404
// - ValidationException -> HTTP 400
// - ConflictException -> HTTP 409
// - BusinessException -> HTTP 400
// - Generic exceptions -> HTTP 500

// Returns standardized ErrorResponse:
{
  "success": false,
  "error": {
    "message": "Product not found with id: 123",
    "details": "Resource not found",
    "timestamp": "2024-01-01T12:00:00Z"
  }
}
```

### Using CommonValidator

The `CommonValidator` provides ready-to-use validation methods:

```java
@Service
public class ProductHandler extends BaseHandler<Product, ProductDTO, Long> {
    private final CommonValidator validator;
    
    public Mono<ProductDTO> create(ProductDTO dto) {
        // Basic validations
        validator.validateRequired(dto.name(), "name");
        validator.validatePositive(dto.price(), "price");
        validator.validateMaxLength(dto.name(), 100, "name");
        
        // Brazilian document validations
        validator.validateCPF(dto.ownerCpf(), "ownerCpf");
        validator.validateCNPJ(dto.companyCnpj(), "companyCnpj");
        
        // Contact validations
        validator.validateEmail(dto.email(), "email");
        validator.validatePhone(dto.phone(), "phone");
        
        // Custom validation with lambda
        validator.validateCondition(
            dto.price().compareTo(BigDecimal.ZERO) > 0,
            "Price must be positive"
        );
        
        return repository.save(toEntity(dto)).map(this::toDto);
    }
}
```

### Custom Validation Example

```java
public class ProductValidator {
    private final CommonValidator commonValidator;
    
    public void validateProduct(ProductDTO product) {
        // Use common validations
        commonValidator.validateRequired(product.name(), "name");
        commonValidator.validateMaxLength(product.name(), 100, "name");
        
        // Custom business validation
        if (product.category() != null && !isValidCategory(product.category())) {
            throw new ValidationException("Invalid product category: " + product.category());
        }
    }
    
    private boolean isValidCategory(String category) {
        return List.of("ELECTRONICS", "CLOTHING", "BOOKS").contains(category);
    }
}
```

## Response Patterns

### Success Responses

The common package standardizes all API responses:

```java
// Single object response
{
  "success": true,
  "data": {
    "id": 1,
    "name": "Product Name",
    "price": 99.99
  },
  "timestamp": "2024-01-01T12:00:00Z"
}

// List response
{
  "success": true,
  "data": [
    {"id": 1, "name": "Product 1"},
    {"id": 2, "name": "Product 2"}
  ],
  "timestamp": "2024-01-01T12:00:00Z"
}

// Paginated response
{
  "content": [
    {"id": 1, "name": "Product 1"},
    {"id": 2, "name": "Product 2"}
  ],
  "page": 0,
  "size": 10,
  "totalElements": 25,
  "totalPages": 3,
  "first": true,
  "last": false
}
```

### Error Responses

```java
// Validation error (HTTP 400)
{
  "success": false,
  "error": {
    "message": "Name is required",
    "details": "Validation failed",
    "timestamp": "2024-01-01T12:00:00Z"
  }
}

// Resource not found (HTTP 404)
{
  "success": false,
  "error": {
    "message": "Product not found with id: 123",
    "details": "Resource not found",
    "timestamp": "2024-01-01T12:00:00Z"
  }
}

// Conflict error (HTTP 409)
{
  "success": false,
  "error": {
    "message": "Product already exists",
    "details": "Conflict detected",
    "timestamp": "2024-01-01T12:00:00Z"
  }
}
```

### Using Response Patterns in Controllers

```java
@RestController
@RequestMapping("/products")
public class ProductController extends BaseController<ProductDTO, Long> {
    
    // Success response with data
    @GetMapping("/{id}")
    public Mono<ResponseEntity<ApiResponse<ProductDTO>>> getById(@PathVariable Long id) {
        return handler.getById(id)
                .map(this::createSuccessResponse);  // HTTP 200 with ApiResponse wrapper
    }
    
    // Created response
    @PostMapping
    public Mono<ResponseEntity<ApiResponse<ProductDTO>>> create(@RequestBody ProductDTO dto) {
        return handler.create(dto)
                .map(this::createCreatedResponse);  // HTTP 201 with ApiResponse wrapper
    }
    
    // No content response
    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<ApiResponse<Void>>> delete(@PathVariable Long id) {
        return handler.delete(id)
                .then(Mono.fromCallable(this::createNoContentResponse));  // HTTP 204
    }
    
    // Paginated response
    @GetMapping("/paginated")
    public Mono<ResponseEntity<PageResponse<ProductDTO>>> getAllPaginated(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return handler.getAllPaginated(page, size)
                .map(this::createPageResponse);  // HTTP 200 with PageResponse
    }
}
```

## Naming Conventions

* Package names should be lowercase.
* DTOs use `*DTO`, handlers use `*Handler`, mappers use `*Mapper`.
* Use singular names for modules (e.g., `product`, `client`).

## Component Guidelines

### Entity Models

* Must use annotations from `org.springframework.data.relational.core.mapping`
* Prefer `record` for immutability and brevity

### Repositories

* Must use `DatabaseClient` or `R2dbcEntityTemplate`
* Define clear method names (e.g., `findById`, `save`, `deleteById`)

### Controllers

* Use WebFlux return types (`Flux`, `Mono`)
* Avoid blocking calls

### Services/Handlers

* Pure business logic, no DB or HTTP logic here

## Best Practices

* Keep methods reactive (`Mono`, `Flux`) throughout the chain
* Avoid `.block()` or `.subscribe()` in your code
* Use `@Validated` and `@Valid` where necessary
* Validate input early in the request chain
* Write SQL manually, keep it clean and parameterized

## Testing Strategy

### Repository Tests

Use `@DataR2dbcTest` for repository tests with the common package:

```java
/**
 * Test class for ProductRepository extending BaseRepository.
 */
@DataR2dbcTest
@Import(DatabaseConfig.class)
public class ProductRepositoryTest {
    
    @Autowired private DatabaseClient databaseClient;
    private ProductRepository repository;
    
    @BeforeEach
    void setUp() {
        repository = new ProductRepository(databaseClient);
    }
    
    @Test
    void shouldCountProducts() {
        StepVerifier.create(repository.count())
                .expectNextMatches(count -> count >= 0)
                .verifyComplete();
    }
    
    @Test
    void shouldCheckIfProductExists() {
        // Given
        Product product = new Product(null, "Test Product", BigDecimal.valueOf(99.99));
        
        // When & Then
        StepVerifier.create(
                repository.save(product)
                        .flatMap(saved -> repository.existsById(saved.id()))
        )
                .expectNext(true)
                .verifyComplete();
    }
    
    @Test
    void shouldDeleteProduct() {
        // Given
        Product product = new Product(null, "Test Product", BigDecimal.valueOf(99.99));
        
        // When & Then
        StepVerifier.create(
                repository.save(product)
                        .flatMap(saved -> repository.deleteById(saved.id())
                                .then(repository.existsById(saved.id())))
        )
                .expectNext(false)
                .verifyComplete();
    }
}
```

### Handler Tests

Test business logic in handlers using mocked repositories:

```java
/**
 * Test class for ProductHandler extending BaseHandler.
 */
@ExtendWith(MockitoExtension.class)
public class ProductHandlerTest {
    
    @Mock private ProductRepository repository;
    @Mock private CommonValidator validator;
    @InjectMocks private ProductHandler handler;
    
    @Test
    void shouldGetProductById() {
        // Given
        Long productId = 1L;
        Product product = new Product(productId, "Test Product", BigDecimal.valueOf(99.99));
        when(repository.findById(productId)).thenReturn(Mono.just(product));
        
        // When & Then
        StepVerifier.create(handler.getById(productId))
                .expectNextMatches(dto -> 
                        dto.id().equals(productId) && 
                        dto.name().equals("Test Product"))
                .verifyComplete();
    }
    
    @Test
    void shouldThrowExceptionWhenProductNotFound() {
        // Given
        Long productId = 999L;
        when(repository.findById(productId)).thenReturn(Mono.empty());
        
        // When & Then
        StepVerifier.create(handler.getById(productId))
                .expectError(ResourceNotFoundException.class)
                .verify();
    }
    
    @Test
    void shouldCreateProduct() {
        // Given
        ProductDTO dto = new ProductDTO(null, "New Product", BigDecimal.valueOf(149.99));
        Product savedProduct = new Product(1L, "New Product", BigDecimal.valueOf(149.99));
        
        when(repository.save(any(Product.class))).thenReturn(Mono.just(savedProduct));
        doNothing().when(validator).validateRequired(anyString(), anyString());
        doNothing().when(validator).validatePositive(any(BigDecimal.class), anyString());
        
        // When & Then
        StepVerifier.create(handler.create(dto))
                .expectNextMatches(result -> 
                        result.id().equals(1L) && 
                        result.name().equals("New Product"))
                .verifyComplete();
    }
    
    @Test
    void shouldCreatePaginatedResponse() {
        // Given
        List<Product> products = List.of(
                new Product(1L, "Product 1", BigDecimal.valueOf(99.99)),
                new Product(2L, "Product 2", BigDecimal.valueOf(149.99))
        );
        
        when(repository.findWithPagination(0, 10)).thenReturn(Flux.fromIterable(products));
        when(repository.count()).thenReturn(Mono.just(2L));
        
        // When & Then
        StepVerifier.create(handler.getAllPaginated(0, 10))
                .expectNextMatches(pageResponse ->
                        pageResponse.getContent().size() == 2 &&
                        pageResponse.getTotalElements() == 2L &&
                        pageResponse.getTotalPages() == 1)
                .verifyComplete();
    }
}
```

### Controller Tests

Use `@WebFluxTest` for controller tests with the common package:

```java
/**
 * Test class for ProductController extending BaseController.
 */
@WebFluxTest(ProductController.class)
public class ProductControllerTest {
    
    @Autowired private WebTestClient client;
    @MockBean private ProductHandler handler;
    
    @Test
    void shouldGetAllProducts() {
        // Given
        List<ProductDTO> products = List.of(
                new ProductDTO(1L, "Product 1", BigDecimal.valueOf(99.99)),
                new ProductDTO(2L, "Product 2", BigDecimal.valueOf(149.99))
        );
        when(handler.getAll()).thenReturn(Flux.fromIterable(products));
        
        // When & Then
        client.get().uri("/products")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.success").isEqualTo(true)
                .jsonPath("$.data").isArray()
                .jsonPath("$.data.length()").isEqualTo(2)
                .jsonPath("$.data[0].name").isEqualTo("Product 1")
                .jsonPath("$.timestamp").exists();
    }
    
    @Test
    void shouldGetProductById() {
        // Given
        Long productId = 1L;
        ProductDTO product = new ProductDTO(productId, "Test Product", BigDecimal.valueOf(99.99));
        when(handler.getById(productId)).thenReturn(Mono.just(product));
        
        // When & Then
        client.get().uri("/products/{id}", productId)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.success").isEqualTo(true)
                .jsonPath("$.data.id").isEqualTo(productId)
                .jsonPath("$.data.name").isEqualTo("Test Product");
    }
    
    @Test
    void shouldCreateProduct() {
        // Given
        ProductDTO inputDto = new ProductDTO(null, "New Product", BigDecimal.valueOf(149.99));
        ProductDTO createdDto = new ProductDTO(1L, "New Product", BigDecimal.valueOf(149.99));
        when(handler.create(any(ProductDTO.class))).thenReturn(Mono.just(createdDto));
        
        // When & Then
        client.post().uri("/products")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(inputDto)
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.success").isEqualTo(true)
                .jsonPath("$.data.id").isEqualTo(1)
                .jsonPath("$.data.name").isEqualTo("New Product");
    }
    
    @Test
    void shouldReturnNotFoundWhenProductDoesNotExist() {
        // Given
        Long productId = 999L;
        when(handler.getById(productId))
                .thenReturn(Mono.error(new ResourceNotFoundException("Product", productId)));
        
        // When & Then
        client.get().uri("/products/{id}", productId)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.success").isEqualTo(false)
                .jsonPath("$.error.message").value(containsString("Product not found"));
    }
    
    @Test
    void shouldGetPaginatedProducts() {
        // Given
        List<ProductDTO> products = List.of(
                new ProductDTO(1L, "Product 1", BigDecimal.valueOf(99.99)),
                new ProductDTO(2L, "Product 2", BigDecimal.valueOf(149.99))
        );
        PageResponse<ProductDTO> pageResponse = new PageResponse<>(products, 0, 10, 2L);
        when(handler.getAllPaginated(0, 10)).thenReturn(Mono.just(pageResponse));
        
        // When & Then
        client.get().uri("/products/paginated?page=0&size=10")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.content").isArray()
                .jsonPath("$.content.length()").isEqualTo(2)
                .jsonPath("$.page").isEqualTo(0)
                .jsonPath("$.size").isEqualTo(10)
                .jsonPath("$.totalElements").isEqualTo(2)
                .jsonPath("$.totalPages").isEqualTo(1);
    }
}
```

### Validation Tests

Test the CommonValidator utility:

```java
/**
 * Test class for CommonValidator.
 */
public class CommonValidatorTest {
    
    private CommonValidator validator = new CommonValidator();
    
    @Test
    void shouldValidateRequiredFields() {
        assertDoesNotThrow(() -> validator.validateRequired("value", "field"));
        
        assertThrows(ValidationException.class, 
                () -> validator.validateRequired(null, "field"));
        assertThrows(ValidationException.class, 
                () -> validator.validateRequired("", "field"));
        assertThrows(ValidationException.class, 
                () -> validator.validateRequired("   ", "field"));
    }
    
    @Test
    void shouldValidateEmails() {
        assertDoesNotThrow(() -> validator.validateEmail("test@example.com", "email"));
        assertDoesNotThrow(() -> validator.validateEmail("user.name+tag@domain.co.uk", "email"));
        
        assertThrows(ValidationException.class, 
                () -> validator.validateEmail("invalid-email", "email"));
        assertThrows(ValidationException.class, 
                () -> validator.validateEmail("@domain.com", "email"));
    }
    
    @Test
    void shouldValidateCPF() {
        assertDoesNotThrow(() -> validator.validateCPF("12345678901", "cpf"));
        
        assertThrows(ValidationException.class, 
                () -> validator.validateCPF("123", "cpf"));
        assertThrows(ValidationException.class, 
                () -> validator.validateCPF("11111111111", "cpf"));
    }
    
    @Test
    void shouldValidatePositiveNumbers() {
        assertDoesNotThrow(() -> validator.validatePositive(BigDecimal.valueOf(10.50), "price"));
        
        assertThrows(ValidationException.class, 
                () -> validator.validatePositive(BigDecimal.valueOf(-1), "price"));
        assertThrows(ValidationException.class, 
                () -> validator.validatePositive(BigDecimal.ZERO, "price"));
    }
}
```

### Integration Tests

Test complete flows with `@SpringBootTest`:

```java
/**
 * Integration test for Product module.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = {
        "spring.r2dbc.url=r2dbc:h2:mem:///testdb",
        "logging.level.org.springframework.r2dbc=DEBUG"
})
public class ProductIntegrationTest {
    
    @Autowired private WebTestClient webTestClient;
    @Autowired private DatabaseClient databaseClient;
    
    @BeforeEach
    void setUp() {
        // Create table for testing
        databaseClient.sql("""
                CREATE TABLE IF NOT EXISTS products (
                    id IDENTITY PRIMARY KEY,
                    name VARCHAR(255) NOT NULL,
                    price DECIMAL(10,2) NOT NULL
                )
                """)
                .fetch()
                .rowsUpdated()
                .block();
    }
    
    @Test
    void shouldPerformCompleteProductCRUD() {
        ProductDTO createDto = new ProductDTO(null, "Integration Test Product", BigDecimal.valueOf(199.99));
        
        // Create
        EntityExchangeResult<byte[]> createResult = webTestClient
                .post().uri("/products")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(createDto)
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.success").isEqualTo(true)
                .jsonPath("$.data.name").isEqualTo("Integration Test Product")
                .returnResult();
        
        // Extract ID from created product
        String responseBody = new String(createResult.getResponseBody());
        // Parse response to get ID...
        
        // Read, Update, Delete operations...
    }
}
```

### Testing Guidelines

* **Repository Tests**: Focus on database operations and SQL queries
* **Handler Tests**: Focus on business logic and DTO/Entity conversions
* **Controller Tests**: Focus on HTTP request/response handling
* **Integration Tests**: Test complete flows end-to-end
* Use `StepVerifier` for testing reactive streams
* Mock external dependencies in unit tests
* Use TestContainers for database integration tests when needed
* Follow naming pattern: `[Class]Test.java`

### Common Testing Utilities

Create test utilities for the common package:

```java
/**
 * Utility class for testing with common package components.
 */
public class TestUtils {
    
    public static <T> void expectSuccessResponse(WebTestClient.BodyContentSpec body, 
                                                 String dataPath, 
                                                 Object expectedValue) {
        body.jsonPath("$.success").isEqualTo(true)
            .jsonPath(dataPath).isEqualTo(expectedValue)
            .jsonPath("$.timestamp").exists();
    }
    
    public static void expectErrorResponse(WebTestClient.BodyContentSpec body, 
                                          String expectedMessage) {
        body.jsonPath("$.success").isEqualTo(false)
            .jsonPath("$.error.message").value(containsString(expectedMessage))
            .jsonPath("$.error.timestamp").exists();
    }
    
    public static void expectPageResponse(WebTestClient.BodyContentSpec body,
                                         int expectedSize,
                                         long expectedTotal) {
        body.jsonPath("$.content").isArray()
            .jsonPath("$.content.length()").isEqualTo(expectedSize)
            .jsonPath("$.totalElements").isEqualTo(expectedTotal);
    }
}

---

This guide ensures consistent, maintainable development across all reactive modules using Spring WebFlux and R2DBC.
