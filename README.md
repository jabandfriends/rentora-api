# Rentora API Documentation

## Table of Contents
1. [Development Setup](#development-setup)
2. [Introduction](#introduction)
1. [Introduction](#introduction)
2. [Authentication](#authentication)
3. [Base URL](#base-url)
4. [Response Format](#response-format)
5. [Error Handling](#error-handling)
6. [API Endpoints](#api-endpoints)
   - [Apartments](#apartments)
   - [Buildings](#buildings)
   - [Units](#units)
   - [Contracts](#contracts)
   - [Tenant Contracts](#tenant-contracts)
7. [Pagination](#pagination)
8. [Sorting](#sorting)
9. [Status Codes](#status-codes)
10. [Examples](#examples)

## Development Setup

### Prerequisites
- Docker
- Docker Compose

### Environment Variables
Create a `.env` file in the root directory with the following variables:

```env
POSTGRES_DB=apartment_db
POSTGRES_USER=admin
POSTGRES_PASSWORD=admin
SPRING_DATASOURCE_HOST=localhost
```

### Makefile Commands

| Command | Description |
|---------|-------------|
| `make api/build/up` | Build and start the API and database containers |
| `make api/up` | Start the existing API and database containers |
| `make db/up` | Start only the database container |
| `make docker/down` | Stop and remove all containers |
| `make ps` | List running containers |

## Introduction
Welcome to the Rentora API documentation. This API allows you to manage properties, buildings, units, and rental contracts programmatically.

## Authentication
All API endpoints require authentication. Include a valid JWT token in the `Rentora-Auth-Token` header:

```
Rentora-Auth-Token: your_jwt_token_here
```

## Base URL
```
https://api.rentora.com/v1
```

## Response Format
All API responses follow a standard format with `success`, `message`, and `data` fields.

### Success Response (Single Item)
```json
{
  "success": true,
  "message": "Success",
  "data": {
    // Item details (varies by endpoint)
  }
}
```

### Paginated Response (List Endpoints)
```json
{
  "success": true,
  "message": "Success",
  "data": {
    "data": [
      // Array of items
    ],
    "pagination": {
      "page": 0,
      "size": 10,
      "totalPages": 5,
      "totalElements": 42
    }
  }
}
```

### Data Models

#### Apartment Detail
```json
{
  "id": "uuid",
  "name": "Apartment Name",
  "logoUrl": "https://example.com/logo.png",
  "phoneNumber": "+1234567890",
  "taxId": "TAX123456",
  "paymentDueDay": 1,
  "lateFee": 50.00,
  "lateFeeType": "FIXED_AMOUNT",
  "gracePeriodDays": 5,
  "address": "123 Main St",
  "city": "City",
  "state": "State",
  "postalCode": "12345",
  "country": "Country",
  "timezone": "UTC+7",
  "currency": "USD",
  "status": "ACTIVE",
  "createdAt": "2023-01-01T00:00:00Z",
  "updatedAt": "2023-01-01T00:00:00Z",
  "buildingCount": 5,
  "unitCount": 100,
  "activeContractCount": 85,
  "totalTenants": 120
}
```

#### Unit
```json
{
  "id": "uuid",
  "unitName": "A-101",
  "unitType": "APARTMENT",
  "bedrooms": 2,
  "bathrooms": 2.0,
  "squareMeters": 85.5,
  "balconyCount": 1,
  "parkingSpaces": 1,
  "status": "available",
  "furnishingStatus": "furnished",
  "floorPlan": "https://example.com/floorplan-a101.png",
  "notes": "Corner unit with city view",
  "createdAt": "2023-01-01T00:00:00Z",
  "updatedAt": "2023-01-01T00:00:00Z"
}
```

### Error Response
```json
{
  "success": false,
  "message": "Error description",
  "data": null
}
```

## Error Handling
Errors follow this format:

```json
{
  "success": false,
  "message": "Error description",
  "error": "ERROR_CODE",
  "timestamp": "2023-09-13T00:00:00Z"
}
```

## API Endpoints

### Apartments
Base Path: `/api/apartments`

#### Get All Apartments
```
GET /api/apartments
```

**Query Parameters:**
| Parameter | Type | Required | Default | Description |
|-----------|------|----------|---------|-------------|
| `page` | number | No | 0 | Page number (0-based) |
| `size` | number | No | 10 | Number of items per page (1-100) |
| `sortBy` | string | No | "name" | Field to sort by: `name`, `createdAt`, `updatedAt` |
| `sortDir` | string | No | "asc" | Sort direction: `asc` (ascending) or `desc` (descending) |
| `search` | string | No | - | Search term to filter apartments by name or address |

**Response:**
```json
{
  "success": true,
  "message": "Success",
  "data": {
    "data": [
      {
        "id": "uuid",
        "name": "Apartment Name",
        "logoUrl": "https://example.com/logo.png",
        "phoneNumber": "+1234567890",
        "address": "123 Main St, City, Country",
        "status": "ACTIVE",
        "createdAt": "2023-01-01T00:00:00Z",
        "buildingCount": 5,
        "unitCount": 100,
        "activeContractCount": 85,
        "totalTenants": 120
      }
    ],
    "pagination": {
      "page": 0,
      "size": 10,
      "totalPages": 1,
      "totalElements": 1
    }
  }
}
```

#### Get Apartment by ID
```
GET /api/apartments/{apartmentId}
```

**Path Parameters:**
| Parameter | Type   | Required | Description           |
|-----------|--------|----------|-----------------------|
| apartmentId | UUID | Yes      | ID of the apartment   |

**Response:**
```json
{
  "id": "uuid",
  "name": "Apartment Name",
  "address": "Full Address",
  "description": "Detailed description",
  "createdAt": "2023-01-01T00:00:00Z",
  "updatedAt": "2023-01-01T00:00:00Z"
}
```

#### Create New Apartment
```
POST /api/apartments
```

**Request Body:**

| Field | Type | Required | Validation | Default | Description |
|-------|------|----------|------------|---------|-------------|
| `name` | string | Yes | max 100 chars | - | Name of the apartment complex |
| `logoUrl` | string | No | URL format | null | URL to the apartment's logo |
| `phoneNumber` | string | No | 15 chars max, phone format | - | Contact phone number |
| `taxId` | string | No | 13 chars max | - | Tax identification number |
| `paymentDueDay` | integer | No | 1-31 | 30 | Day of month when payments are due |
| `lateFee` | number | No | ≥ 0 | 0 | Late fee amount |
| `lateFeeType` | enum | No | FIXED/PERCENTAGE | FIXED | Type of late fee |
| `gracePeriodDays` | integer | No | ≥ 0 | 3 | Grace period in days before late fees apply |
| `address` | string | No | - | - | Street address |
| `city` | string | No | - | - | City name |
| `state` | string | No | - | - | State/Province |
| `postalCode` | string | No | 10 chars max | - | Postal/ZIP code |
| `country` | string | No | - | "Thailand" | Country name |
| `timezone` | string | No | - | "Asia/Bangkok" | IANA timezone |
| `currency` | string | No | 3 chars | "THB" | ISO currency code |

**Example Request:**
```http
POST /api/apartments
Content-Type: application/json
Rentora-Auth-Token: your_jwt_token_here

{
  "name": "Sunset Apartments",
  "phoneNumber": "+66123456789",
  "taxId": "1234567890123",
  "paymentDueDay": 1,
  "lateFee": 500.00,
  "lateFeeType": "FIXED",
  "gracePeriodDays": 3,
  "address": "123 Main St",
  "city": "Bangkok",
  "state": "Bangkok",
  "postalCode": "10110",
  "country": "Thailand"
}
```

**Response (201 Created):**
```json
{
  "success": true,
  "message": "Apartment created successfully",
  "data": {
    "id": "uuid",
    "name": "Sunset Apartments",
    "phoneNumber": "+66123456789",
    "taxId": "1234567890123",
    "paymentDueDay": 1,
    "lateFee": 500.00,
    "lateFeeType": "FIXED",
    "gracePeriodDays": 3,
    "address": "123 Main St",
    "city": "Bangkok",
    "state": "Bangkok",
    "postalCode": "10110",
    "country": "Thailand",
    "timezone": "Asia/Bangkok",
    "currency": "THB",
    "status": "ACTIVE",
    "createdAt": "2023-01-01T00:00:00Z",
    "updatedAt": "2023-01-01T00:00:00Z"
  }
}
```

#### Update Apartment
```
PUT /api/apartments/{apartmentId}
```

**Path Parameters:**
| Parameter | Type   | Required | Description           |
|-----------|--------|----------|-----------------------|
| apartmentId | UUID | Yes      | ID of the apartment to update |

**Request Body:**
Same fields as Create Apartment, but all fields are optional.

**Example Request:**
```http
PUT /api/apartments/123e4567-e89b-12d3-a456-426614174000
Content-Type: application/json
Rentora-Auth-Token: your_jwt_token_here

{
  "name": "Sunset Residences",
  "phoneNumber": "+66987654321",
  "address": "456 Park Avenue"
}
```

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Apartment updated successfully",
  "data": {
    "id": "123e4567-e89b-12d3-a456-426614174000",
    "name": "Sunset Residences",
    "phoneNumber": "+66987654321",
    "taxId": "1234567890123",
    "paymentDueDay": 1,
    "lateFee": 500.00,
    "lateFeeType": "FIXED",
    "gracePeriodDays": 3,
    "address": "456 Park Avenue",
    "city": "Bangkok",
    "state": "Bangkok",
    "postalCode": "10110",
    "country": "Thailand",
    "timezone": "Asia/Bangkok",
    "currency": "THB",
    "status": "ACTIVE",
    "createdAt": "2023-01-01T00:00:00Z",
    "updatedAt": "2023-01-10T15:30:00Z"
  }
}
```

### Buildings
Base Path: `/api/apartments/{apartmentId}/buildings`

#### Get All Buildings in Apartment
```
GET /api/apartments/{apartmentId}/buildings
```

**Path Parameters:**
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `apartmentId` | UUID | Yes | ID of the parent apartment |

**Query Parameters:**
| Parameter | Type | Required | Default | Description |
|-----------|------|----------|---------|-------------|
| `page` | number | No | 0 | Page number (0-based) |
| `size` | number | No | 10 | Number of items per page (1-100) |
| `sortBy` | string | No | "name" | Field to sort by: `name`, `createdAt`, `updatedAt` |
| `sortDir` | string | No | "asc" | Sort direction: `asc` or `desc` |
| `search` | string | No | - | Search term to filter buildings by name or description |

### Units
Base Path: `/api/apartments/{apartmentId}/units`

#### Get All Units in Apartment
```
GET /api/apartments/{apartmentId}/units
```

**Path Parameters:**
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `apartmentId` | UUID | Yes | ID of the parent apartment |

**Query Parameters:**
| Parameter | Type | Required | Default | Description |
|-----------|------|----------|---------|-------------|
| `page` | number | No | 0 | Page number (0-based) |
| `size` | number | No | 10 | Number of items per page (1-100) |
| `sortBy` | string | No | "unitName" | Field to sort by: `unitName`, `createdAt`, `updatedAt`, `bedrooms`, `squareMeters` |
| `sortDir` | string | No | "asc" | Sort direction: `asc` or `desc` |
| `status` | string | No | - | Filter by unit status. Possible values: `available`, `occupied`, `maintenance`, `reserved` |
| `unitType` | string | No | - | Filter by unit type. Possible values: `APARTMENT`, `STUDIO`, `PENTHOUSE`, `COMMERCIAL` |
| `floorId` | UUID | No | - | Filter by specific floor ID |
| `bedrooms` | number | No | - | Filter by number of bedrooms |
| `minArea` | number | No | - | Filter by minimum square meters |
| `maxArea` | number | No | - | Filter by maximum square meters |
| `furnishing` | string | No | - | Filter by furnishing status: `unfurnished`, `furnished`, `semi_furnished` |

### Contracts
Base Path: `/api/apartments/{apartmentId}/contracts`

#### Create Contract
```
POST /api/apartments/{apartmentId}/contracts
```

**Request Body:**
```json
{
  "tenantId": "uuid",
  "unitId": "uuid",
  "startDate": "2023-01-01",
  "endDate": "2024-01-01",
  "monthlyRent": 1000.00,
  "securityDeposit": 1000.00,
  "termsAndConditions": "Additional terms"
}
```

#### Terminate Contract
```
POST /api/apartments/{apartmentId}/contracts/{contractId}/terminate
```

**Request Body:**
```json
{
  "terminationDate": "2023-06-01",
  "terminationReason": "Early termination"
}
```

### Tenant Contracts
Base Path: `/api/my-contracts`

#### Get My Contracts
```
GET /api/my-contracts
```

**Query Parameters:**
| Parameter | Type | Required | Default | Description |
|-----------|------|----------|---------|-------------|
| `page` | number | No | 0 | Page number (0-based) |
| `size` | number | No | 10 | Number of items per page (1-100) |
| `sortBy` | string | No | "createdAt" | Field to sort by: `createdAt`, `startDate`, `endDate` |
| `sortDir` | string | No | "desc" | Sort direction: `asc` or `desc` |
| `status` | string | No | - | Filter by contract status: `ACTIVE`, `TERMINATED`, `EXPIRED`, `UPCOMING` |
| `apartmentId` | UUID | No | - | Filter by specific apartment |
| `unitId` | UUID | No | - | Filter by specific unit |

## Pagination
All list endpoints support pagination with the following query parameters:
- `page` - Page number (0-based)
- `size` - Number of items per page
- `sortBy` - Field to sort by
- `sortDir` - Sort direction (asc/desc)

## Sorting
Available sort fields:
- Apartments: `name`, `createdAt`
- Buildings: `name`, `createdAt`
- Units: `unitName`, `createdAt`
- Contracts: `createdAt`, `startDate`, `endDate`

## Status Codes
- 200 OK - Request successful
- 201 Created - Resource created successfully
- 400 Bad Request - Invalid request data
- 401 Unauthorized - Authentication required
- 403 Forbidden - Insufficient permissions
- 404 Not Found - Resource not found
- 500 Internal Server Error - Server error

## Examples

### Create New Apartment
```http
POST /api/apartments
Content-Type: application/json
Rentora-Auth-Token: your_jwt_token_here

{
  "name": "Sunset Apartments",
  "address": "123 Main St, City, Country",
  "description": "Luxury apartment complex with modern amenities"
}
```

### Get Units with Filtering
```http
GET /api/apartments/{apartmentId}/units?status=AVAILABLE&unitType=2BHK&page=0&size=5&sortBy=unitName&sortDir=asc
Rentora-Auth-Token: your_jwt_token_here
```
