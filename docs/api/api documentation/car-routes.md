### Cars API (v1)

Base URL: `http://localhost:8080`

### What it does
- Lists available cars with filters (basic and location-based)
- Supports location-based search with distance calculations
- Retrieves a single car by ID
- Creates, updates (full/partial), and deletes cars

### Authentication and authorization
- Uses JWT Bearer auth.
- Obtain a token via `POST /api/v1/auth/register` or `POST /api/v1/auth/login`.
- Include header: `Authorization: Bearer <token>` on secured routes.
- Route protection:
  - `GET /api/v1/cars` is public (no auth).
  - `GET /api/v1/cars/{id}` is public (no auth).
  - `POST /api/v1/cars` requires `ADMIN` or `DRIVER` role. The car is created for the authenticated `userId` as `ownerId`.
  - `PUT /api/v1/cars/{id}` requires `ADMIN` or `DRIVER` role. Only admin or owner can modify.
  - `PATCH /api/v1/cars/{id}` requires `ADMIN` or `DRIVER` role. Only admin or owner can modify.
  - `DELETE /api/v1/cars/{id}` requires `ADMIN` or `DRIVER` role. Only admin or owner can delete.

Notes:
- Enums: `category` = `ICE` | `BEV` | `FCEV`; `fuelType` = `PETROL` | `DIESEL` | `LPG` | `ELECTRIC` | `HYBRIDE`.
- Prices (`ratePerHour`) must be JSON strings (e.g. "12.50").

### Endpoints

#### GET /api/v1/cars (public)
List cars with optional filters and location-based search.

**Basic Filtering (returns simple list):**
- Query params: `ownerId`, `category`, `fuelType`, `isActive`, `maxRate`
- Example: `/api/v1/cars?category=BEV&isActive=true&maxRate=20.00`

**Location-Based Search (returns paginated results with metadata):**
When any of these parameters are provided: `latitude`, `longitude`, `maxDistance`, `minPrice`, `maxPrice`, `brand`, `page`, `limit`
- `latitude` (optional): Latitude for location search (-90 to 90)
- `longitude` (optional): Longitude for location search (-180 to 180)
- `maxDistance` (optional): Maximum distance in kilometers
- `minPrice` (optional): Minimum rate per hour (decimal)
- `maxPrice` (optional): Maximum rate per hour (decimal)
- `brand` (optional): Car brand (string)
- `category` (optional): Car category (`ICE`, `BEV`, `FCEV`)
- `fuelType` (optional): Fuel type (`PETROL`, `DIESEL`, `LPG`, `ELECTRIC`, `HYBRIDE`)
- `page` (optional): Page number (default: 1, min: 1)
- `limit` (optional): Results per page (default: 20, min: 1, max: 100)
- Example: `/api/v1/cars?latitude=51.587&longitude=4.775&maxDistance=10&category=BEV&page=1&limit=20`

**Basic Filter Response 200:**
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

**Location-Based Search Response 200:**
```json
{
  "cars": [
    {
      "id": "c0a8012c-7d8c-4a12-ae3e-7f7c20a1b111",
      "brand": "Tesla",
      "model": "Model 3",
      "category": "BEV",
      "ratePerHour": "12.50",
      "distance": 3.45,
      "locationLat": 51.590,
      "locationLng": 4.780,
      "thumbnailUrl": null,
      "isActive": true
    }
  ],
  "totalCount": 5,
  "page": 1,
  "totalPages": 1,
  "hasNext": false
}
```

#### GET /api/v1/cars/{id} (public)
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

6) Get by ID (public):
   - GET `{{baseUrl}}/api/v1/cars/{{id}}`

7) Update (PUT) or patch (PATCH) by ID (Authorization: Bearer <token>):
   - PUT `{{baseUrl}}/api/v1/cars/{{id}}` with full body (see above)
   - PATCH `{{baseUrl}}/api/v1/cars/{{id}}` with partial body

8) Delete by ID (Authorization: Bearer <token>, role: ADMIN or DRIVER):
   - DELETE `{{baseUrl}}/api/v1/cars/{{id}}`


