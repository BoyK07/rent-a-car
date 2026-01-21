### Reservations API (v1)

Base URL: `http://localhost:8080`

### What it does
- Lists reservations with filters.
- Retrieves a single reservation by ID.
- Creates, updates (full/partial), cancels, and deletes reservations.
- Lists currently active reservations.
- Lists driving sessions for a reservation.

### Authentication and authorization
- Uses JWT Bearer auth.
- Obtain a token via `POST /api/v1/auth/register` or `POST /api/v1/auth/login`.
- Include header: `Authorization: Bearer <token>` on secured routes.
- Route protection:
  - All reservations routes require any authenticated role: `ADMIN`, `DRIVER`, or `MEMBER`.
  - `DELETE /api/v1/reservations/{id}` requires `ADMIN` or `DRIVER`.
- Authorization:
  - Non-admin users can only view, modify, and cancel their own reservations.
  - Admin users can view and modify all reservations.

Notes:
- `ReservationStatus` = `PENDING` | `CONFIRMED` | `CANCELLED` | `COMPLETED`.
- Date-time fields use ISO 8601 format (LocalDateTime). Example: `2025-10-24T10:00:00`.
- Prices (`priceTotal`) must be JSON strings (e.g. "79.99").

### Endpoints

#### GET /api/v1/reservations (secured)
List reservations with optional filters.
- Query params: `renterId`, `carId`, `status`, `start`, `end`
- DateTime params (`start`, `end`) must be in ISO 8601 format (e.g., `2025-10-01T00:00:00`)
- Example: `/api/v1/reservations?status=CONFIRMED&start=2025-10-01T00:00:00&end=2025-10-31T23:59:59`
- Non-admin users can only see their own reservations, regardless of filters.
- Admin users can see all reservations.

Response 200:
```json
[
  {
    "id": "c0a8012c-7d8c-4a12-ae3e-7f7c20a1b111",
    "carId": "c0a8012c-7d8c-4a12-ae3e-7f7c20a1b222",
    "renterId": "c0a8012c-7d8c-4a12-ae3e-7f7c20a1b333",
    "startTime": "2025-10-24T10:00:00",
    "endTime": "2025-10-24T14:00:00",
    "status": "CONFIRMED",
    "priceTotal": "79.99",
    "pointsAwarded": 10
  }
]
```

#### GET /api/v1/reservations/{id} (secured)
Get reservation by ID.
- Non-admin users can only view their own reservations.

Responses:
- 200 with `ReservationDto`
- 403 when user is not authorized to view the reservation
- 404 when not found

#### POST /api/v1/reservations (secured)
Create a reservation for the authenticated user (as `renterId`).

Request body:
```json
{
  "carId": "c0a8012c-7d8c-4a12-ae3e-7f7c20a1b222",
  "startTime": 1761300000000,
  "endTime": 1761314400000,
  "priceTotal": "79.99"
}
```

Responses:
- 201 with created `ReservationDto` (status defaults to `PENDING`)
- 400 for invalid body

#### PUT /api/v1/reservations/{id} (secured)
Replace a reservation entirely.
- Non-admin users can only modify their own reservations.

Request body (all required):
```json
{
  "carId": "c0a8012c-7d8c-4a12-ae3e-7f7c20a1b222",
  "startTime": 1761300000000,
  "endTime": 1761314400000,
  "status": "CONFIRMED",
  "priceTotal": "79.99",
  "pointsAwarded": 10
}
```

Responses:
- 200 with updated `ReservationDto`
- 404 when not found

#### PATCH /api/v1/reservations/{id} (secured)
Partially update fields.
- Non-admin users can only modify their own reservations.

Request body (all optional):
```json
{
  "status": "CANCELLED",
  "pointsAwarded": 5
}
```

Responses:
- 200 with updated `ReservationDto`
- 403 when user is not authorized to modify the reservation
- 404 when not found

#### DELETE /api/v1/reservations/{id} (secured: ADMIN or DRIVER)
Delete a reservation.
- Non-admin users can only delete their own reservations.

Responses:
- 204 when deleted
- 403 when user is not authorized to delete the reservation
- 404 when not found

#### POST /api/v1/reservations/{id}/cancel (secured)
Cancel a reservation. Fails if already `COMPLETED` or `CANCELLED`.
- Non-admin users can only cancel their own reservations.

Responses:
- 204 when cancelled
- 403 when user is not authorized to cancel the reservation
- 404 when not found or not cancellable

#### GET /api/v1/reservations/active (secured)
List reservations active at the current time.
- Non-admin users only see their own active reservations.
- Admin users see all active reservations.

Responses:
- 200 with array of `ReservationDto`

#### GET /api/v1/reservations/{id}/driving-sessions (secured)
List driving sessions for a reservation.
- Non-admin users can only view driving sessions for their own reservations.
- Admin users can view all driving sessions.

Responses:
- 200 with array of `DrivingSession` objects
- 403 when user is not authorized to view the driving sessions
- 404 when reservation not found

### How to test in Postman
1) Start the API:
   - Windows PowerShell:
   ```bash
   cd application/api
   .\gradlew.bat run
   ```

2) Create environment variable `baseUrl = http://localhost:8080`.

3) Get a token:
   - POST `{{baseUrl}}/api/v1/auth/register`
   ```json
   { "name": "Alice", "email": "alice@example.com", "password": "Password123!" }
   ```
   - Or POST `{{baseUrl}}/api/v1/auth/login`
   ```json
   { "email": "alice@example.com", "password": "Password123!" }
   ```
   - Copy the `token` from the response.

4) Create a reservation (Authorization: Bearer <token>):
   - POST `{{baseUrl}}/api/v1/reservations`
   - Body (JSON):
   ```json
   {
     "carId": "{{carId}}",
  "startTime": 1761300000000,
  "endTime": 1761314400000,
     "priceTotal": "79.99"
   }
   ```

5) List reservations with filters:
   - GET `{{baseUrl}}/api/v1/reservations?status=PENDING`

6) Get, update, patch, cancel, delete by ID:
   - GET `{{baseUrl}}/api/v1/reservations/{{id}}`
   - PUT `{{baseUrl}}/api/v1/reservations/{{id}}`
   - PATCH `{{baseUrl}}/api/v1/reservations/{{id}}`
   - POST `{{baseUrl}}/api/v1/reservations/{{id}}/cancel`
   - DELETE `{{baseUrl}}/api/v1/reservations/{{id}}`

7) List active reservations:
   - GET `{{baseUrl}}/api/v1/reservations/active`

8) List driving sessions for a reservation:
   - GET `{{baseUrl}}/api/v1/reservations/{{id}}/driving-sessions`


