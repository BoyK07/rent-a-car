### API Routes

This document lists the API routes to implement.

### Cars
- `GET /api/v1/cars`: 
  List cars (filters: `ownerId`, `category`, `fuelType`, `isActive`, `maxRate`).
- `GET /api/v1/cars/{id}`: Get car by ID.
- `POST /api/v1/cars`: Create a car.
- `PUT /api/v1/cars/{id}`: Replace a car.
- `PATCH /api/v1/cars/{id}`: Partially update a car.
- `DELETE /api/v1/cars/{id}`: Delete a car.

#### Search
- `GET /api/v1/cars/search/available`: Find available cars in a time range (query: `start`, `end`, `maxRate`).
- `GET /api/v1/cars/search/near`: Find cars near coordinates (query: `lat`, `lng`, `radiusKm`).

#### Costing
- `GET /api/v1/cars/{id}/tco`: Calculate TCO per year for a car.
- `GET /api/v1/cars/{id}/cost-per-km`: Calculate cost per kilometer for a car.

#### Photos
- `GET /api/v1/cars/{id}/photos`: List photos for a car.
- `GET /api/v1/cars/{id}/photos/{photoId}`: Get a specific car photo.
- `POST /api/v1/cars/{id}/photos`: Add a photo to a car.
- `PATCH /api/v1/cars/{id}/photos/{photoId}`: Update a car photo (e.g., set primary).
- `DELETE /api/v1/cars/{id}/photos/{photoId}`: Delete a car photo.

#### Availability
- `GET /api/v1/cars/{id}/availability`: List availability windows for a car.
- `GET /api/v1/cars/{id}/availability/{availabilityId}`: Get a specific availability window.
- `POST /api/v1/cars/{id}/availability`: Add availability window for a car.
- `PUT /api/v1/cars/{id}/availability/{availabilityId}`: Replace an availability window.
- `PATCH /api/v1/cars/{id}/availability/{availabilityId}`: Partially update an availability window.
- `DELETE /api/v1/cars/{id}/availability/{availabilityId}`: Delete an availability window.

### Availability
- `GET /api/v1/availability`: List availability windows (filters: `carId`, `start`, `end`).

### Reservations
- `GET /api/v1/reservations`: List reservations (filters: `renterId`, `carId`, `status`, `start`, `end`).
- `GET /api/v1/reservations/{id}`: Get reservation by ID.
- `POST /api/v1/reservations`: Create a reservation.
- `PUT /api/v1/reservations/{id}`: Replace a reservation.
- `PATCH /api/v1/reservations/{id}`: Partially update a reservation.
- `DELETE /api/v1/reservations/{id}`: Delete a reservation.
- `POST /api/v1/reservations/{id}/cancel`: Cancel a reservation.
- `GET /api/v1/reservations/active`: List currently active reservations (at now).
- `GET /api/v1/reservations/{id}/driving-sessions`: List driving sessions for a reservation.

### Driving Sessions
- `GET /api/v1/driving-sessions`: List driving sessions (filter by `reservationId`).
- `GET /api/v1/driving-sessions/{id}`: Get driving session by ID.
- `POST /api/v1/driving-sessions`: Create a driving session.
- `PUT /api/v1/driving-sessions/{id}`: Replace a driving session.
- `PATCH /api/v1/driving-sessions/{id}`: Partially update a driving session.
- `DELETE /api/v1/driving-sessions/{id}`: Delete a driving session.
