### Car Photos API (v1)

Base URL: `http://localhost:8080`

### What it does
- Lists photos for a specific car.
- Retrieves a single car photo by ID.
- Creates, updates (partial), and deletes car photos.

### Authentication and authorization
- Uses JWT Bearer auth.
- Obtain a token via `POST /api/v1/auth/register` or `POST /api/v1/auth/login`.
- Include header: `Authorization: Bearer <token>` on all routes below.
- Route protection:
  - All photo routes require any authenticated role: `ADMIN`, `DRIVER`, or `MEMBER`.
  - `DELETE /api/v1/cars/{id}/photos/{photoId}` requires `ADMIN` or `DRIVER`.

Notes:
- Photo DTO format:
  - `CarPhotoDto = { id: UUID, carId: UUID, url: string, isPrimary: boolean }`

### Endpoints

#### GET /api/v1/cars/{id}/photos (secured)
List photos for a car by its ID.

Responses:
- 200 with array of `CarPhotoDto`:
```json
[
  {
    "id": "c0a8012c-7d8c-4a12-ae3e-7f7c20a1b111",
    "carId": "c0a8012c-7d8c-4a12-ae3e-7f7c20a1b000",
    "url": "https://cdn.example.com/cars/tesla/model-3/front.jpg",
    "isPrimary": true
  }
]
```

#### GET /api/v1/cars/{id}/photos/{photoId} (secured)
Get a specific photo by its ID. Returns 404 if the photo does not belong to the given car.

Responses:
- 200 with `CarPhotoDto`
- 404 when not found

#### POST /api/v1/cars/{id}/photos (secured)
Add a photo to a car.

Request body:
```json
{
  "url": "https://cdn.example.com/cars/tesla/model-3/front.jpg",
  "isPrimary": true
}
```

Responses:
- 201 with created `CarPhotoDto`
- 400 for invalid body

#### PATCH /api/v1/cars/{id}/photos/{photoId} (secured)
Partially update a car photo (e.g., set primary or change URL). Returns 404 if the photo does not belong to the given car.

Request body (all optional):
```json
{
  "url": "https://cdn.example.com/cars/tesla/model-3/interior.jpg",
  "isPrimary": true
}
```

Responses:
- 200 with updated `CarPhotoDto`
- 404 when not found

#### DELETE /api/v1/cars/{id}/photos/{photoId} (secured: ADMIN or DRIVER)
Delete a car photo. Returns 404 if the photo does not belong to the given car.

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

4) Create or use an existing car to obtain `carId` (see Cars API docs). 

5) Add a photo (Authorization: Bearer <token>):
   - POST `{{baseUrl}}/api/v1/cars/{{carId}}/photos`
   - Body (JSON), set header `Content-Type: application/json`:
   ```json
   {
     "url": "https://cdn.example.com/cars/tesla/model-3/front.jpg",
     "isPrimary": true
   }
   ```

6) List photos for a car:
   - GET `{{baseUrl}}/api/v1/cars/{{carId}}/photos`

7) Get a specific photo:
   - GET `{{baseUrl}}/api/v1/cars/{{carId}}/photos/{{photoId}}`

8) Update a photo:
   - PATCH `{{baseUrl}}/api/v1/cars/{{carId}}/photos/{{photoId}}`
   - Body (JSON):
   ```json
   { "isPrimary": true }
   ```

9) Delete a photo (Authorization: Bearer <token>, role: ADMIN or DRIVER):
   - DELETE `{{baseUrl}}/api/v1/cars/{{carId}}/photos/{{photoId}}`


