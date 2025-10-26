### Car Costing API (v1)

Base URL: `http://localhost:8080`

### What it does
- Calculates TCO (Total Cost of Ownership) per year for a car.
- Calculates cost per kilometer for a car.

### Authentication and authorization
- Uses JWT Bearer auth.
- Obtain a token via `POST /api/v1/auth/register` or `POST /api/v1/auth/login`.
- Include header: `Authorization: Bearer <token>` on all routes below.
- Route protection: both endpoints require any authenticated role: `ADMIN`, `DRIVER`, or `MEMBER`.

Notes:
- Returned amounts are strings to preserve precision.

### Endpoints

#### GET /api/v1/cars/{id}/tco (secured)
Calculate total cost of ownership per year.

Query params:
- `annualKm` (integer)

Responses:
- 200 with `CarTcoResponseDto`:
```json
{
  "carId": "c0a8012c-7d8c-4a12-ae3e-7f7c20a1b111",
  "annualKm": 15000,
  "tcoPerYear": "2450.00"
}
```
- 404 when car not found

#### GET /api/v1/cars/{id}/cost-per-km (secured)
Calculate cost per kilometer for a car.

Responses:
- 200 with `CarCostPerKmResponseDto`:
```json
{
  "carId": "c0a8012c-7d8c-4a12-ae3e-7f7c20a1b111",
  "costPerKm": "0.16"
}
```
- 404 when car not found

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

4) Calculate TCO per year (Authorization: Bearer <token>):
   - GET `{{baseUrl}}/api/v1/cars/{{id}}/tco?annualKm=20000`

5) Calculate cost per kilometer (Authorization: Bearer <token>):
   - GET `{{baseUrl}}/api/v1/cars/{{id}}/cost-per-km`


