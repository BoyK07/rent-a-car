# Database Configuration Guide

## Overview

The Rent My Car application supports multiple database providers to suit different development, testing, and production scenarios. This flexibility allows you to:

- **Develop locally** without external database dependencies
- **Test automatically** with in-memory databases
- **Deploy to production** with enterprise-grade databases

## Supported Database Providers

### 1. H2 In-Memory (`h2` or `h2-memory`)
**Recommended for:** Testing, quick local development

An in-memory database that is fast, requires no external setup, and is automatically cleaned up when the application stops.

**Pros:**
- Zero setup required
- Fast performance
- Automatic cleanup
- No port conflicts
- Perfect for tests

**Cons:**
- Data is lost when application stops
- Not suitable for production

**Configuration:**
```yaml
db:
  provider: "h2"
  name: "rentmycar"
```

**Environment Variable:**
```bash
DB_PROVIDER=h2
```

### 2. H2 File-Based (`h2-file`)
**Recommended for:** Local development with data persistence

A file-based H2 database that persists data between application restarts while still requiring no external database server.

**Pros:**
- No external database required
- Data persists between restarts
- Fast performance
- No port conflicts

**Cons:**
- Not suitable for production
- Single-user access

**Configuration:**
```yaml
db:
  provider: "h2-file"
  name: "rentmycar"
  path: "build/h2db/rentmycar"  # Relative path (converted to absolute automatically)
```

**Environment Variable:**
```bash
DB_PROVIDER=h2-file
DB_PATH=./data/rentmycar  # Relative path (starting with ./)
# Or use absolute path:
DB_PATH=/absolute/path/to/db
```

**Note:** H2 file paths are automatically converted to absolute paths. You can use:
- Relative paths (e.g., `build/h2db/rentmycar`) - automatically converted
- Explicitly relative paths (e.g., `./data/rentmycar`)
- Absolute paths (e.g., `/var/db/rentmycar`)
- Home directory paths (e.g., `~/databases/rentmycar`)

### 3. MariaDB External (`mariadb` or `external`)
**Recommended for:** Production, staging environments

Connect to an external MariaDB or MySQL server.

**Pros:**
- Production-ready
- Full feature set
- Multi-user support
- Scalable

**Cons:**
- Requires external database setup
- More configuration needed

**Configuration:**
```yaml
db:
  provider: "mariadb"
  host: "localhost"
  port: "3306"
  name: "rentmycar"
  user: "root"
  password: "password"
```

**Environment Variables:**
```bash
DB_PROVIDER=mariadb
DB_HOST=localhost
DB_PORT=3306
DB_NAME=rentmycar
DB_USER=root
DB_PASSWORD=password
```

### 4. MariaDB Embedded (`mariadb-embedded` or `embedded`)
**⚠️ Not Recommended:** Causes issues with Gradle process management

An embedded MariaDB instance using MariaDB4j.

**Why not recommended:**
- Gradle doesn't properly stop the database process
- Can cause port conflicts on subsequent runs
- Requires manual process cleanup
- Better alternatives available (H2)

**If you must use it:**
```yaml
db:
  provider: "mariadb-embedded"
  name: "rentmycar"
```

## Quick Start Configurations

### Development Setup (Default)
The default configuration uses H2 in-memory for easy local development:

```yaml
# application.yaml (default)
db:
  provider: "h2"
  name: "rentmycar"
```

No external database needed - just run the application!

### Testing Setup
Tests automatically use H2 with database reset enabled:

```yaml
# src/test/resources/application.yaml
db:
  provider: "h2"
  name: "test"
  reset: true  # Clean database before each test run
```

### Production Setup
For production, use an external MariaDB instance:

```bash
# Environment variables for production
DB_PROVIDER=mariadb
DB_HOST=your-db-server.com
DB_PORT=3306
DB_NAME=rentmycar_prod
DB_USER=prod_user
DB_PASSWORD=secure_password_here
```

## Database Reset

You can reset the database (drop all tables and re-run migrations) using:

**Configuration:**
```yaml
db:
  reset: true
```

**Environment Variable:**
```bash
DB_RESET=true
```

**⚠️ Warning:** Only use this in development! This will **delete all data**.

## Migration Management

The application uses Flyway for database migrations. Migrations are located in:
```
app/modules/server/src/main/resources/migrations/
```

These migrations are loaded via `classpath:migrations` and run automatically on application startup. They work with all database providers.

## Data Seeding

The application includes a data seeder that populates the database with test data for development.

**What's included:**
- 3 users (admin, driver/owner, member/renter) with different roles
- 3 cars with different categories (ICE, BEV, FCEV) and fuel types
- Car photos for each vehicle
- Availability windows for the next 7 days
- Sample reservations in different states (confirmed, completed, cancelled)
- Sample driving session with telemetry data

**Enable seeding:**
```bash
# Via environment variable
DB_SEED=true ./gradlew :server:run

# Via configuration
db:
  seed: true
```

**Default: Disabled** - Data seeding is disabled by default. Enable it explicitly in development when needed.

**Default credentials:**
- Admin: `admin@rentmycar.dev` / `admin123`
- Driver: `john.doe@example.com` / `driver123`
- Member: `jane.smith@example.com` / `member123`

The seeder is **idempotent** - it only runs if the database is empty (no users exist), so it's safe to call multiple times.

**⚠️ Production:** Always ensure `DB_SEED=false` (or omit the setting) in production environments.

## Switching Between Providers

### During Development

Simply change the `DB_PROVIDER` environment variable:

```bash
# Use H2 in-memory
DB_PROVIDER=h2 ./gradlew :server:run

# Use H2 file-based
DB_PROVIDER=h2-file ./gradlew :server:run

# Use external MariaDB
DB_PROVIDER=mariadb DB_HOST=localhost ./gradlew :server:run
```

### In Configuration Files

Edit `application.yaml`:

```yaml
db:
  provider: "$DB_PROVIDER:h2"  # Default to h2 if not set
```

### For Tests

Tests always use the configuration from `src/test/resources/application.yaml`, which is set to use H2 by default.

## Troubleshooting

### Port 3306 Already in Use

If you get an error about port 3306 being in use:

1. **Check for running MariaDB/MySQL processes:**
   ```bash
   # Linux/Mac
   lsof -i :3306
   
   # Windows
   netstat -ano | findstr :3306
   ```

2. **Kill the process:**
   ```bash
   # Linux/Mac
   kill -9 <PID>
   
   # Windows
   taskkill /F /PID <PID>
   ```

3. **Or switch to H2:**
   ```bash
   DB_PROVIDER=h2 ./gradlew :server:run
   ```

### Connection Refused

If you can't connect to external MariaDB:

1. Check that MariaDB is running
2. Verify host and port are correct
3. Check firewall settings
4. Verify user credentials
5. Ensure user has proper permissions

### Migrations Failing

If migrations fail:

1. Check migration SQL files in `src/main/resources/migrations/`
2. Verify database provider supports the SQL syntax
3. Try resetting the database: `DB_RESET=true`
4. Check Flyway logs for detailed error messages

## Best Practices

1. **Development:** Use H2 in-memory (`h2`) for quick iteration
2. **Testing:** Always use H2 with reset enabled
3. **CI/CD:** Use H2 for fast, isolated tests
4. **Staging:** Use external MariaDB that mirrors production
5. **Production:** Use external MariaDB with proper credentials
6. **Never commit:** Database passwords in configuration files (use environment variables)

## Example Configurations

### Local Development (H2)
```bash
# No configuration needed - it's the default!
./gradlew :server:run
```

### Local Development (MariaDB Docker)
```bash
# Start MariaDB in Docker
docker run -d --name mariadb \
  -e MYSQL_ROOT_PASSWORD=dev_password \
  -e MYSQL_DATABASE=rentmycar \
  -p 3306:3306 \
  mariadb:latest

# Configure application
DB_PROVIDER=mariadb \
DB_PASSWORD=dev_password \
./gradlew :server:run
```

### Running Tests
```bash
# Tests use H2 automatically
./gradlew :server:test
```

### Production Deployment
```bash
# Set environment variables in your deployment platform
DB_PROVIDER=mariadb
DB_HOST=prod-db.example.com
DB_PORT=3306
DB_NAME=rentmycar
DB_USER=app_user
DB_PASSWORD=<secure-password-from-secrets-manager>
```

## API Reference

### Environment Variables

| Variable | Description | Default | Required |
|----------|-------------|---------|----------|
| `DB_PROVIDER` | Database provider type | `h2` | No |
| `DB_HOST` | Database host (MariaDB only) | `localhost` | For MariaDB |
| `DB_PORT` | Database port (MariaDB only) | `3306` | For MariaDB |
| `DB_NAME` | Database name | `rentmycar` | No |
| `DB_USER` | Database user (MariaDB only) | `root` | For MariaDB |
| `DB_PASSWORD` | Database password (MariaDB only) | `password` | For MariaDB |
| `DB_PATH` | Database file path (H2 file only) | `build/h2db/rentmycar` | For H2 file |
| `DB_RESET` | Reset database on startup | `false` | No |
| `DB_SEED` | Seed test data on startup | `false` | No |

### Configuration Properties

```yaml
db:
  provider: string         # h2, h2-memory, h2-file, mariadb, mariadb-embedded
  host: string            # Database host (MariaDB only)
  port: string            # Database port (MariaDB only)
  name: string            # Database name
  user: string            # Database user (MariaDB only)
  password: string        # Database password (MariaDB only)
  path: string            # File path (H2 file only)
  reset: boolean          # Reset database on startup
  seed: boolean           # Seed sample data on startup
```
  path: string            # File path (H2 file only)
  reset: boolean          # Reset database on startup
```
