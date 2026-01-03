CREATE TABLE IF NOT EXISTS driving_sessions
(
    id
    BINARY
(
    16
) PRIMARY KEY, reservation_id BINARY
(
    16
) NOT NULL, distance_km DECIMAL
(
    8,
    2
) NOT NULL, harsh_accelerations INT NOT NULL, harsh_brakes INT NOT NULL, CONSTRAINT fk_driving_sessions_reservation_id__id FOREIGN KEY
(
    reservation_id
) REFERENCES reservations
(
    id
) ON DELETE RESTRICT
  ON UPDATE RESTRICT);