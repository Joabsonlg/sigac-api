# Reservation Module

## Overview
The Reservation module manages vehicle rental reservations in the SIGAC system. It provides complete CRUD operations with advanced filtering and search capabilities.

## Features

### ✅ Complete CRUD Operations
- **Create** new reservations
- **Read** reservations with detailed information
- **Update** existing reservations
- **Delete** reservations (with business rules)

### ✅ Advanced Search & Filtering
- **Pagination** support for large datasets
- **Status filtering** by reservation status
- **Query search** across:
  - Client names
  - Vehicle models and brands
  - Vehicle plates
- **Combined filters** (status + query)

### ✅ Business Logic
- **Vehicle availability validation** - prevents double booking
- **Status transition validation** - enforces proper workflow
- **Date validation** - ensures logical date ranges
- **Automatic status management** - new reservations start as PENDING

## API Endpoints

### Main Operations
```
GET    /reservations                    # Get paginated reservations with filters
GET    /reservations/{id}               # Get specific reservation
POST   /reservations                    # Create new reservation
PUT    /reservations/{id}               # Update reservation
PATCH  /reservations/{id}/status        # Update only status
DELETE /reservations/{id}               # Delete reservation
```

### Additional Endpoints
```
GET    /reservations/client/{cpf}       # Get reservations by client
GET    /reservations/vehicle/{plate}    # Get reservations by vehicle
GET    /reservations/status/{status}    # Get reservations by status
```

### Query Parameters
- `page` - Page number (0-based, default: 0)
- `size` - Items per page (default: 20, max: 100)
- `status` - Filter by status (PENDING, CONFIRMED, IN_PROGRESS, COMPLETED, CANCELLED)
- `query` - Search in client names, vehicle models/brands, or plates

## Database Schema

### Reservation Table
```sql
CREATE TABLE reservation (
    id                SERIAL PRIMARY KEY,
    start_date        TIMESTAMP,
    end_date          TIMESTAMP,
    reservation_date  TIMESTAMP,
    status            VARCHAR(45),
    promotion_code    INT,
    client_user_cpf   VARCHAR(45),
    employee_user_cpf VARCHAR(45),
    vehicle_plate     VARCHAR(45),
    FOREIGN KEY (promotion_code) REFERENCES promotion (code),
    FOREIGN KEY (client_user_cpf) REFERENCES client (user_cpf),
    FOREIGN KEY (employee_user_cpf) REFERENCES employee (user_cpf),
    FOREIGN KEY (vehicle_plate) REFERENCES vehicle (plate)
);
```

### Relationships
- **Client** (Many-to-One): A client can have multiple reservations
- **Employee** (Many-to-One): An employee can manage multiple reservations  
- **Vehicle** (Many-to-One): A vehicle can have multiple reservations (different times)
- **Promotion** (Many-to-One): A promotion can be used in multiple reservations

## Status Workflow

```
PENDING → CONFIRMED → IN_PROGRESS → COMPLETED
    ↓         ↓           ↓
CANCELLED  CANCELLED  CANCELLED
```

### Status Descriptions
- **PENDING**: Newly created, awaiting confirmation
- **CONFIRMED**: Approved and scheduled
- **IN_PROGRESS**: Vehicle currently in use
- **COMPLETED**: Reservation finished successfully
- **CANCELLED**: Reservation cancelled (terminal state)

## Request/Response Examples

### Create Reservation
```json
POST /reservations
{
    "startDate": "2025-07-15T09:00:00",
    "endDate": "2025-07-20T18:00:00",
    "clientUserCpf": "12345678901",
    "employeeUserCpf": "98765432109",
    "vehiclePlate": "ABC1234",
    "promotionCode": 1
}
```

### Response
```json
{
    "success": true,
    "message": "Resource created successfully",
    "data": {
        "id": 1,
        "startDate": "2025-07-15T09:00:00",
        "endDate": "2025-07-20T18:00:00",
        "reservationDate": "2025-07-07T14:30:00",
        "status": "PENDING",
        "promotionCode": 1,
        "clientUserCpf": "12345678901",
        "clientName": "João Silva",
        "employeeUserCpf": "98765432109",
        "employeeName": "Ana Santos",
        "vehiclePlate": "ABC1234",
        "vehicleModel": "Civic",
        "vehicleBrand": "Honda"
    },
    "timestamp": "2025-07-07T14:30:00Z"
}
```

### Search with Filters
```
GET /reservations?page=0&size=10&status=CONFIRMED&query=honda
```

Returns paginated results of confirmed reservations containing "honda" in vehicle model/brand.

## Validation Rules

### Date Validation
- Start date must be before end date
- Start date cannot be in the past (with 1-hour tolerance)
- End date cannot be in the past

### Vehicle Availability
- System checks for overlapping reservations
- Only considers CONFIRMED and IN_PROGRESS reservations
- Allows updates to the same reservation

### Status Transitions
- **PENDING** → CONFIRMED, CANCELLED
- **CONFIRMED** → IN_PROGRESS, CANCELLED  
- **IN_PROGRESS** → COMPLETED, CANCELLED
- **COMPLETED** → (no transitions allowed)
- **CANCELLED** → (no transitions allowed)

### Deletion Rules
- Cannot delete IN_PROGRESS reservations
- Cannot delete COMPLETED reservations
- Can delete PENDING, CONFIRMED, or CANCELLED reservations

## Architecture

### Components
```
Controller (REST API)
    ↓
Handler (Business Logic)
    ↓
Repository (Database Access)
    ↓
Database (PostgreSQL)
```

### Key Classes
- **ReservationController**: REST endpoints
- **ReservationHandler**: Business logic and validation
- **ReservationRepository**: Database operations with joins
- **ReservationValidator**: Input validation and business rules
- **Reservation**: Entity model
- **ReservationDTO**: Data transfer object with related entity info
- **CreateReservationDTO**: Creation request
- **UpdateReservationDTO**: Update request
- **ReservationStatus**: Status enumeration

## Error Handling

The module provides comprehensive error handling:

- **400 Bad Request**: Validation errors, invalid date ranges
- **404 Not Found**: Reservation not found
- **409 Conflict**: Vehicle not available, invalid status transition

All errors follow the standard API response format with descriptive messages.

## Technical Features

- **Reactive Programming**: Full reactive stack with Mono/Flux
- **Pagination**: Efficient offset-based pagination
- **Joins**: Optimized queries with LEFT JOINs for related data
- **Status Mapping**: Handles database legacy status values
- **Type Safety**: Strongly typed with Java records
- **Documentation**: Complete OpenAPI/Swagger documentation
