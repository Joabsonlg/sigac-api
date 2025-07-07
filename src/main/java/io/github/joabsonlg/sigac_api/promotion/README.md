# Promotion Module

This module manages promotional discounts that can be applied to reservations in the SIGAC system.

## Overview

The Promotion module handles:
- Creating and managing promotional campaigns
- Validating promotion periods and discounts
- Tracking promotion usage and status
- Automatic activation/deactivation based on dates

## Structure

```
promotion/
├── controller/        # REST endpoints
├── dto/              # Data Transfer Objects
├── enumeration/      # Enums (PromotionStatus)
├── handler/          # Business logic
├── model/            # Entity models
├── repository/       # Data access layer
└── validator/        # Input validation
```

## Key Components

### PromotionStatus Enum
- `SCHEDULED`: Promotion is scheduled for future activation
- `ACTIVE`: Promotion is currently active and can be used
- `INACTIVE`: Promotion is inactive and cannot be used

### Promotion Entity
- `code`: Unique identifier (auto-generated)
- `discountPercentage`: Percentage discount (1-100)
- `status`: Current promotion status
- `startDate`: When the promotion becomes valid
- `endDate`: When the promotion expires

### DTOs
- `PromotionDTO`: Complete promotion information
- `CreatePromotionDTO`: Data for creating new promotions
- `UpdatePromotionDTO`: Data for updating existing promotions

## REST Endpoints

### Basic Operations
- `GET /api/promotions` - Get all promotions
- `GET /api/promotions/{code}` - Get promotion by code
- `POST /api/promotions` - Create new promotion
- `PUT /api/promotions/{code}` - Update promotion
- `DELETE /api/promotions/{code}` - Delete promotion

### Filtering and Pagination
- `GET /api/promotions/page?page=0&size=10` - Paginated promotions
- `GET /api/promotions/page/status?status=ACTIVE&page=0&size=10` - Filter by status
- `GET /api/promotions/active` - Get currently active promotions

### Status Management
- `PATCH /api/promotions/{code}/activate` - Activate promotion
- `PATCH /api/promotions/{code}/deactivate` - Deactivate promotion

## Business Rules

### Creation Rules
- Discount percentage must be between 1 and 100
- Start date cannot be in the past
- End date must be after start date
- New promotions are created with `SCHEDULED` status

### Update Rules
- Cannot modify code (immutable)
- Cannot change dates of active promotions
- Status transitions must be valid

### Deletion Rules
- Cannot delete active promotions
- Must deactivate before deletion

### Activation Rules
- Can only activate scheduled or inactive promotions
- Cannot activate before start date
- Cannot activate after end date
- Promotion must be within valid date range

## Validation

The module includes comprehensive validation:
- Date range validation
- Discount percentage limits
- Status transition validation
- Business rule enforcement

## Integration

### With Reservations
- Promotions can be applied to reservations
- Validates promotion availability during reservation creation
- Tracks promotion usage

### Database Schema
```sql
CREATE TABLE promotion (
    code                SERIAL PRIMARY KEY,
    discount_percentage INT,
    status              VARCHAR(45),
    start_date          TIMESTAMP,
    end_date            TIMESTAMP
);
```

## Error Handling

The module provides consistent error responses:
- `ResourceNotFoundException`: When promotion is not found
- `ValidationException`: For business rule violations
- Detailed error messages for debugging

## Security

- All endpoints require appropriate authentication
- Status changes require elevated permissions
- Audit trails for promotion modifications

## Example Usage

### Create a new promotion
```bash
POST /api/promotions
{
    "discountPercentage": 20,
    "startDate": "2025-08-01T00:00:00",
    "endDate": "2025-08-31T23:59:59"
}
```

### Get active promotions
```bash
GET /api/promotions/active
```

### Activate a promotion
```bash
PATCH /api/promotions/1/activate
```

## Testing

The module includes comprehensive tests covering:
- Repository operations
- Business logic validation
- Controller endpoints
- Error scenarios
- Integration with other modules
