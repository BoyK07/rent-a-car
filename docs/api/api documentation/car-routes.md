### Cars API (v1)

Base URL: `http://localhost:8080`

### What it does
- Lists available cars with filters.
- Retrieves a single car by ID.
- Creates, updates (full/partial), and deletes cars.

### Authentication and authorization
- Uses JWT Bearer auth.
- Obtain a token via `POST /api/v1/auth/register` or `POST /api/v1/auth/login`.
- Include header: `Authorization: Bearer <token>` on secured routes.
- Route protection:
  - `GET /api/v1/cars` is public (no auth).
  - `GET /api/v1/cars/{id}` requires any authenticated role: `ADMIN`, `DRIVER`, or `MEMBER`.
  - `POST /api/v1/cars` requires any authenticated role. The car is created for the authenticated `userId` as `ownerId`.
  - `PUT /api/v1/cars/{id}` requires any authenticated role.
  - `PATCH /api/v1/cars/{id}` requires any authenticated role.
  - `DELETE /api/v1/cars/{id}` requires `ADMIN` or `DRIVER`.

Notes:
- Enums: `category` = `ICE` | `BEV` | `FCEV`; `fuelType` = `PETROL` | `DIESEL` | `LPG` | `ELECTRIC` | `HYBRIDE`.
- Prices (`ratePerHour`) must be JSON strings (e.g. "12.50").

### Endpoints

#### GET /api/v1/cars (public)
List cars with optional filters.
- Query params: `ownerId`, `category`, `fuelType`, `isActive`, `maxRate`
- Example: `/api/v1/cars?category=BEV&isActive=true&maxRate=20.00`

Response 200:
```json
[
  {
    "id": "c0a8012c-7d8c-4a12-ae3e-7f7c20a1b111",
    "ownerId": "c0a8012c-7d8c-4a12-ae3e-7f7c20a1b000",
    "brand": "Tesla",
    "model": "Model 3",
    "category": "BEV",
    "fuelType": "ELECTRIC",
    "ratePerHour": "12.50",
    "locationLat": 51.587,
    "locationLng": 4.775,
    "isActive": true
  }
]
```

#### GET /api/v1/cars/{id} (secured)
Get car by ID.

Responses:
- 200 with `CarDto`
- 404 when not found

#### POST /api/v1/cars (secured)
Create a car for the authenticated user (ownerId is taken from JWT).

Request body:
```json
{
  "brand": "Tesla",
  "model": "Model 3",
  "category": "BEV",
  "fuelType": "ELECTRIC",
  "ratePerHour": "12.50",
  "locationLat": 51.587,
  "locationLng": 4.775,
  "isActive": true
}
```

Responses:
- 201 with created `CarDto`
- 400 for invalid body

#### PUT /api/v1/cars/{id} (secured)
Replace a car entirely.

Request body (same fields as POST; all required):
```json
{
  "brand": "Tesla",
  "model": "Model 3 Long Range",
  "category": "BEV",
  "fuelType": "ELECTRIC",
  "ratePerHour": "15.00",
  "locationLat": 51.590,
  "locationLng": 4.780,
  "isActive": true
}
```

Responses:
- 200 with updated `CarDto`
- 404 when not found

#### PATCH /api/v1/cars/{id} (secured)
Partially update fields.

Request body (all optional):
```json
{
  "ratePerHour": "13.75",
  "isActive": false
}
```

Responses:
- 200 with updated `CarDto`
- 404 when not found

#### DELETE /api/v1/cars/{id} (secured: ADMIN or DRIVER)
Delete a car.

Responses:
- 204 when deleted
- 404 when not found

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

4) Create a car (Authorization: Bearer <token>):
   - POST `{{baseUrl}}/api/v1/cars`
   - Body (JSON), set header `Content-Type: application/json`:
   ```json
   {
     "brand": "Tesla",
     "model": "Model 3",
     "category": "BEV",
     "fuelType": "ELECTRIC",
     "ratePerHour": "12.50",
     "locationLat": 51.587,
     "locationLng": 4.775,
     "isActive": true
   }
   ```

5) List cars (public):
   - GET `{{baseUrl}}/api/v1/cars?category=BEV&isActive=true&maxRate=20.00`

6) Get by ID (Authorization: Bearer <token>):
   - GET `{{baseUrl}}/api/v1/cars/{{id}}`

7) Update (PUT) or patch (PATCH) by ID (Authorization: Bearer <token>):
   - PUT `{{baseUrl}}/api/v1/cars/{{id}}` with full body (see above)
   - PATCH `{{baseUrl}}/api/v1/cars/{{id}}` with partial body

8) Delete by ID (Authorization: Bearer <token>, role: ADMIN or DRIVER):
   - DELETE `{{baseUrl}}/api/v1/cars/{{id}}`


