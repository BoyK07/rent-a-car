### Users API (v1)

Base URL: `http://localhost:8080`

### What it does
- Lists all users (admin only)
- Retrieves a single user by ID
- Deletes a user (admin only)

### Authentication and authorization
- Uses JWT Bearer auth
- Obtain a token via `POST /api/v1/auth/register` or `POST /api/v1/auth/login`
- Include header: `Authorization: Bearer <token>` on all routes
- Route protection:
  - `GET /api/v1/users` requires `ADMIN` role
  - `GET /api/v1/users/{id}` requires authentication. Non-admin users can only view their own profile
  - `DELETE /api/v1/users/{id}` requires `ADMIN` role

### Endpoints

#### GET /api/v1/users (secured: ADMIN only)
List all users.

Responses:
- 200 with array of `UserDto`
- 403 when user is not admin

Response 200:
```json
[
  {
    "id": "c0a8012c-7d8c-4a12-ae3e-7f7c20a1b000",
    "name": "Alice",
    "email": "alice@example.com",
    "role": "DRIVER",
    "createdAt": "2025-01-01T00:00:00"
  }
]
```

#### GET /api/v1/users/{id} (secured)
Get user by ID.
- Admin users can view any user profile
- Non-admin users can only view their own profile

Responses:
- 200 with `UserDto`
- 403 when non-admin user tries to view another user's profile
- 404 when user not found

Response 200:
```json
{
  "id": "c0a8012c-7d8c-4a12-ae3e-7f7c20a1b000",
  "name": "Alice",
  "email": "alice@example.com",
  "role": "DRIVER",
  "createdAt": "2025-01-01T00:00:00"
}
```

#### DELETE /api/v1/users/{id} (secured: ADMIN only)
Delete a user.

Responses:
- 204 when deleted
- 403 when user is not admin
- 404 when user not found

### How to test in Postman

1) Start the API:
   ```bash
   cd application/api
   .\gradlew.bat run
   ```

2) Create environment variable `baseUrl = http://localhost:8080`.

3) Get an admin token:
   - POST `{{baseUrl}}/api/v1/auth/register`
   ```json
   { "name": "Admin", "email": "admin@example.com", "password": "Password123!", "role": "ADMIN" }
   ```
   - Or POST `{{baseUrl}}/api/v1/auth/login`
   ```json
   { "email": "admin@example.com", "password": "Password123!" }
   ```
   - Copy the `token` from the response.

4) List all users (Authorization: Bearer <token>, role: ADMIN):
   - GET `{{baseUrl}}/api/v1/users`

5) Get user by ID (Authorization: Bearer <token>):
   - GET `{{baseUrl}}/api/v1/users/{{id}}`
   - Non-admin users can only view their own profile

6) Delete user by ID (Authorization: Bearer <token>, role: ADMIN):
   - DELETE `{{baseUrl}}/api/v1/users/{{id}}`
