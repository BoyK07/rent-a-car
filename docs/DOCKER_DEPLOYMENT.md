# Docker Deployment Guide

This guide explains how to deploy the Rent My Car server using Docker and Docker Compose.

## Quick Start

### Option 1: H2 In-Memory (Development/Testing)

The fastest way to get started - no database setup required:

```bash
docker-compose --profile h2 up
```

The server will be available at `http://localhost:8080`

### Option 2: MariaDB (Production-like Setup)

For a more production-like environment with persistent data:

```bash
docker-compose --profile mariadb up
```

This starts both the server and a MariaDB database. Data persists in a Docker volume.

## Building the Docker Image

### Build Server Image

```bash
docker build -f app/modules/server/Dockerfile -t rentmycar-server:latest .
```

### Build with specific tag

```bash
docker build -f app/modules/server/Dockerfile -t rentmycar-server:1.0.0 .
```

## Running the Server

### With H2 (In-Memory)

```bash
docker run -d \
  --name rentmycar-server \
  -p 8080:8080 \
  -e DB_PROVIDER=h2 \
  -e JWT_SECRET=your-secure-secret \
  rentmycar-server:latest
```

### With H2 (File-based with persistence)

```bash
docker run -d \
  --name rentmycar-server \
  -p 8080:8080 \
  -v rentmycar-data:/app/build/h2db \
  -e DB_PROVIDER=h2-file \
  -e JWT_SECRET=your-secure-secret \
  rentmycar-server:latest
```

### With External MariaDB

```bash
docker run -d \
  --name rentmycar-server \
  -p 8080:8080 \
  -e DB_PROVIDER=mariadb \
  -e DB_HOST=your-db-host \
  -e DB_PORT=3306 \
  -e DB_NAME=rentmycar \
  -e DB_USER=rentmycar \
  -e DB_PASSWORD=your-secure-password \
  -e JWT_SECRET=your-secure-secret \
  rentmycar-server:latest
```

## Docker Compose Profiles

The `docker-compose.yml` supports different profiles for different scenarios:

### Profile: `h2` (Development)

Starts the server with H2 in-memory database.

```bash
docker-compose --profile h2 up
```

**Use cases:**
- Quick local testing
- CI/CD pipelines
- Demo environments
- Development without database setup

### Profile: `mariadb` (Production-like)

Starts both server and MariaDB database.

```bash
docker-compose --profile mariadb up
```

**Use cases:**
- Local development with persistent data
- Testing with real database
- Production-like environment
- Performance testing

### Custom Configuration

Override environment variables:

```bash
docker-compose --profile mariadb up -e JWT_SECRET=my-custom-secret
```

Or create a `.env` file:

```env
# .env
JWT_SECRET=my-secure-secret
DB_PASSWORD=my-db-password
```

## Production Deployment

### Prerequisites

1. A running MariaDB/MySQL database
2. Database credentials
3. JWT secret key (generate with: `openssl rand -hex 32`)

### Step 1: Prepare Database

Create database and user:

```sql
CREATE DATABASE rentmycar CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER 'rentmycar'@'%' IDENTIFIED BY 'secure_password';
GRANT ALL PRIVILEGES ON rentmycar.* TO 'rentmycar'@'%';
FLUSH PRIVILEGES;
```

### Step 2: Configure Environment

Create a `.env` file or set environment variables:

```env
DB_PROVIDER=mariadb
DB_HOST=your-database-host
DB_PORT=3306
DB_NAME=rentmycar
DB_USER=rentmycar
DB_PASSWORD=your-secure-database-password
JWT_SECRET=your-secure-jwt-secret
KTOR_ENV=production
```

### Step 3: Deploy

Using Docker:

```bash
docker run -d \
  --name rentmycar-server \
  --restart unless-stopped \
  -p 8080:8080 \
  --env-file .env \
  rentmycar-server:latest
```

Using Docker Compose:

```yaml
# docker-compose.prod.yml
version: "3.8"

services:
  server:
    image: rentmycar-server:latest
    restart: unless-stopped
    ports:
      - "8080:8080"
    environment:
      - DB_PROVIDER=mariadb
      - DB_HOST=your-db-host
      - DB_PORT=3306
      - DB_NAME=rentmycar
      - DB_USER=rentmycar
      - DB_PASSWORD=${DB_PASSWORD}
      - JWT_SECRET=${JWT_SECRET}
      - KTOR_ENV=production
```

Deploy:
```bash
docker-compose -f docker-compose.prod.yml up -d
```

## Health Checks

The Docker image includes a health check that pings `/health` endpoint.

Check container health:
```bash
docker ps
```

View health check logs:
```bash
docker inspect --format='{{json .State.Health}}' rentmycar-server | jq
```

## Monitoring

### View Logs

Real-time logs:
```bash
docker logs -f rentmycar-server
```

Last 100 lines:
```bash
docker logs --tail 100 rentmycar-server
```

### Metrics

Access Prometheus metrics at:
```
http://localhost:8080/metrics
```

## Troubleshooting

### Container won't start

Check logs:
```bash
docker logs rentmycar-server
```

Common issues:
- Database connection failed: Check DB_HOST, DB_PORT, credentials
- Port already in use: Change `-p 8081:8080` to use different host port
- JWT secret not set: Set JWT_SECRET environment variable

### Database connection issues

Test database connectivity:
```bash
docker run --rm mariadb:11.8 mysql \
  -h your-db-host \
  -u rentmycar \
  -p \
  rentmycar
```

### Reset database

If using Docker volumes:
```bash
docker-compose down -v
docker-compose --profile mariadb up
```

For H2 file-based:
```bash
docker exec rentmycar-server rm -rf /app/build/h2db/*
docker restart rentmycar-server
```

### Check configuration

Inspect environment variables:
```bash
docker exec rentmycar-server env | grep DB_
```

## Updates and Rollbacks

### Update to new version

```bash
# Pull new image
docker pull rentmycar-server:2.0.0

# Stop and remove old container
docker stop rentmycar-server
docker rm rentmycar-server

# Start new version
docker run -d \
  --name rentmycar-server \
  --restart unless-stopped \
  -p 8080:8080 \
  --env-file .env \
  rentmycar-server:2.0.0
```

### Rollback

```bash
docker stop rentmycar-server
docker rm rentmycar-server
docker run -d \
  --name rentmycar-server \
  --restart unless-stopped \
  -p 8080:8080 \
  --env-file .env \
  rentmycar-server:1.0.0
```

## Backup and Restore

### Backup H2 Database

```bash
docker cp rentmycar-server:/app/build/h2db ./backup-h2db
```

### Restore H2 Database

```bash
docker cp ./backup-h2db/. rentmycar-server:/app/build/h2db/
docker restart rentmycar-server
```

### Backup MariaDB

```bash
docker exec db mysqldump \
  -u rentmycar \
  -prentmycar_password \
  rentmycar > backup.sql
```

### Restore MariaDB

```bash
docker exec -i db mysql \
  -u rentmycar \
  -prentmycar_password \
  rentmycar < backup.sql
```

## Security Best Practices

1. **Never use default passwords** - Always set strong, unique passwords
2. **Use secrets management** - Don't commit secrets to version control
3. **Run as non-root** - The Docker image uses a non-root user by default
4. **Keep images updated** - Regularly update base images and dependencies
5. **Use HTTPS** - Deploy behind a reverse proxy (nginx, traefik) with SSL
6. **Limit exposed ports** - Only expose necessary ports
7. **Regular backups** - Automate database backups
8. **Monitor logs** - Set up log aggregation and monitoring

## Reverse Proxy Setup

### Nginx Example

```nginx
server {
    listen 80;
    server_name api.rentmycar.com;
    
    location / {
        proxy_pass http://localhost:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}
```

### Traefik Example

```yaml
labels:
  - "traefik.enable=true"
  - "traefik.http.routers.rentmycar.rule=Host(`api.rentmycar.com`)"
  - "traefik.http.routers.rentmycar.entrypoints=websecure"
  - "traefik.http.routers.rentmycar.tls.certresolver=letsencrypt"
```

## Performance Tuning

### JVM Options

Customize JVM settings:

```bash
docker run -d \
  --name rentmycar-server \
  -p 8080:8080 \
  --env-file .env \
  rentmycar-server:latest \
  java -Xmx512m -Xms256m -jar app.jar
```

### Database Connection Pool

Configure via environment variables:

```env
HIKARI_MAXIMUM_POOL_SIZE=10
HIKARI_MINIMUM_IDLE=5
```

## Additional Resources

- [Database Configuration Guide](DATABASE_CONFIGURATION.md)
- [API Documentation](api/)
- [Troubleshooting Guide](TROUBLESHOOTING.md)

## Support

For issues and questions:
- GitHub Issues: https://github.com/DevKoenv/rent-a-car/issues
- Documentation: https://github.com/DevKoenv/rent-a-car/docs
