CREATE TABLE IF NOT EXISTS cars
(
    id
    BINARY
(
    16
) PRIMARY KEY, owner_id BINARY
(
    16
) NOT NULL, make VARCHAR
(
    100
) NOT NULL, model VARCHAR
(
    100
) NOT NULL, category VARCHAR
(
    10
) NOT NULL, fuel_type VARCHAR
(
    10
) NULL, rate_per_hour DECIMAL
(
    10,
    2
) NOT NULL, location_lat DOUBLE PRECISION NOT NULL, location_lng DOUBLE PRECISION NOT NULL, is_active BOOLEAN DEFAULT TRUE NOT NULL, CONSTRAINT fk_cars_owner_id__id FOREIGN KEY
(
    owner_id
) REFERENCES users
(
    id
) ON DELETE RESTRICT
  ON UPDATE RESTRICT);