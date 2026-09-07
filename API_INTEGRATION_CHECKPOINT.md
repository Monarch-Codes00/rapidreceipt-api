# RapidReceipt API — Integration Checkpoint (Day 7)

This document outlines the core backend endpoints that are **ready for frontend integration**.

## What Works Currently (Core Flow)
The core business logic is fully functional and secured via JWT. The following features are ready for integration:

### 1. Authentication
All protected endpoints require the `Authorization` header:
`Authorization: Bearer <your_jwt_token>`

*   **Register**: `POST /api/auth/register` (Public)
    *   Creates a new business owner account and returns a token immediately.
*   **Login**: `POST /api/auth/login` (Public)
    *   Authenticates credentials and returns a fresh token.

### 2. User Profile
*   **Get Profile**: `GET /api/profile` (Protected)
    *   Returns the authenticated user's details (excludes password).
*   **Update Profile**: `PUT /api/profile` (Protected)
    *   Updates business name, phone, bank details, and branding colors.

### 3. Customers
*   **Create Customer**: `POST /api/customers` (Protected)
*   **Get All Customers**: `GET /api/customers` (Protected)
*   **Get Customer By ID**: `GET /api/customers/{id}` (Protected)
*   **Update Customer**: `PUT /api/customers/{id}` (Protected)
*   **Delete Customer**: `DELETE /api/customers/{id}` (Protected)

### 4. Services (Catalog)
*   **Create Service**: `POST /api/services` (Protected)
*   **Get All Services**: `GET /api/services` (Protected)
*   **Get Service By ID**: `GET /api/services/{id}` (Protected)
*   **Update Service**: `PUT /api/services/{id}` (Protected)
*   **Delete Service**: `DELETE /api/services/{id}` (Protected)

### 5. Invoices
*   **Create Invoice**: `POST /api/invoices` (Protected)
    *   Generates automatic sequential invoice numbers (e.g., `RR-2024-00042`).
    *   Server-side calculation of subtotals and totals based on items array.
*   **Get All Invoices**: `GET /api/invoices` (Protected)
*   **Get Invoice By ID**: `GET /api/invoices/{id}` (Protected)
    *   Includes embedded customer data for direct rendering without a secondary fetch.
*   **Delete Invoice**: `DELETE /api/invoices/{id}` (Protected)

## Error Handling
The API uses a unified error response shape (`ApiError`).
*   `400 Bad Request`: Validation failures (e.g., missing fields).
*   `401 Unauthorized`: Invalid credentials or missing JWT.
*   `404 Not Found`: Entity does not exist or does not belong to the authenticated user.
*   `409 Conflict`: Duplicate resources (e.g., email already taken).

## What is Delayed (Do Not Integrate Yet)
The following features are purposefully delayed until the core API is verified by the frontend team:
*   Paystack subscription integration
*   Webhooks
*   Sync endpoint (offline-first sync)
*   Logo uploads (file storage)
*   Password reset flow
*   Refresh tokens
*   Rate limiting
*   Pagination (lists currently return all items)
