# Rent My Car - Kotlin Multiplatform Implementation

This is the complete implementation of the Kotlin Multiplatform foundation for the "Rent My Car" school assignment.

## Architecture

The project follows **official Kotlin Multiplatform conventions** with proper separation of concerns:

```
app/
├── modules/
│   ├── server/         # Ktor backend (existing)
│   ├── shared/         # Multiplatform shared code
│   │   ├── commonMain/ # Platform-agnostic business logic
│   │   ├── androidMain/# Android-specific implementations
│   │   └── jvmMain/    # JVM/Desktop implementations
│   └── app/            # Multiplatform UI
│       ├── commonMain/ # Compose Multiplatform UI
│       ├── androidMain/# Android app entry point
│       └── jvmMain/    # Desktop app entry point
```

## Key Components

### Shared Module (Business Logic & Networking)

**Networking Layer:**
- `HttpEngineFactory` - Platform-specific HTTP engines (expect/actual)
  - Android: OkHttp
  - JVM: CIO
- `HttpClientFactory` - Configured Ktor client with JWT auth
- API Clients: `AuthApi`, `CarsApi`, `UserApi`

**Data Layer:**
- `SettingsFactory` - Platform-specific storage (expect/actual)
  - Android: SharedPreferences
  - JVM: Java Preferences API
- `AuthTokenStorage` - JWT token persistence
- `AppDataStorage` - App data with in-memory caching
- Repositories: `AuthRepository`, `CarsRepository`, `UserRepository`

**Dependency Management:**
- `SharedModule` - Singleton container for all dependencies

### App Module (UI & Navigation)

**Navigation:**
- Voyager navigation framework
- Auth-gated screen flows
- Smooth slide transitions

**Screens:**
1. **LoginScreen** - Email/password authentication
2. **RegisterScreen** - User registration with optional role selection
3. **HomeScreen** - Cars list with loading/error states
4. **CarDetailScreen** - Detailed car view with local storage tracking
5. **ProfileScreen** - User profile with role display and logout

## Features Implemented

✅ JWT Authentication with token persistence  
✅ Role-based access (ADMIN, DRIVER, MEMBER)  
✅ Platform-specific storage implementations  
✅ RESTful API integration with Ktor  
✅ Observable auth state with Kotlin Flow  
✅ Local data caching (viewed cars)  
✅ Form validation  
✅ Error handling and loading states  
✅ Automatic navigation based on auth state  

## Configuration

### Server URL

By default, the app connects to `http://localhost:8080`. To configure for production:

```kotlin
// In your Application/Main initialization
SharedModule.configure("https://your-production-api.com")
```

**Security Note:** Always use HTTPS in production environments to protect user data and JWT tokens.

### Android Setup

The app requires initialization in the Application class:

```kotlin
class RentMyCarApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        SettingsFactory.init(applicationContext)
    }
}
```

This is already configured in `AndroidManifest.xml`.

## Dependencies

### Core Libraries
- Kotlin 2.3.0
- Kotlin Coroutines 1.10.2
- Kotlinx Serialization 1.8.0
- Compose Multiplatform 1.9.3

### Networking
- Ktor Client 3.3.3
- Platform engines: OkHttp (Android), CIO (JVM)

### Navigation
- Voyager 1.1.0-beta03

### Storage
- Multiplatform Settings 1.2.0

## Building the Project

```bash
# Build all modules
./gradlew build

# Build Android APK
./gradlew :app:assembleDebug

# Build Desktop JAR
./gradlew :app:jvmJar

# Run tests
./gradlew test
```

## API Integration

The app communicates with the backend API at the following endpoints:

### Authentication
- `POST /api/v1/auth/register` - User registration
- `POST /api/v1/auth/login` - User login

### Cars
- `GET /api/v1/cars` - List all cars (paginated)
- `GET /api/v1/cars/{id}` - Get car details

### Users
- `GET /api/v1/users/me` - Get current user profile

All authenticated requests automatically include the JWT token via Ktor's Auth plugin.

## Security Features

✅ JWT token stored securely in platform-specific storage  
✅ Passwords never stored locally  
✅ Password fields use visual transformation  
✅ Automatic token injection for authenticated requests  
✅ HTTPS support (configure for production)  
✅ Input validation on all forms  
✅ Bounded local storage (max 100 viewed cars)  

## Best Practices Followed

1. **Official Kotlin Multiplatform Structure** - Proper source set organization
2. **Separation of Concerns** - Clear boundaries between shared and app code
3. **Repository Pattern** - Clean architecture with testable layers
4. **Unidirectional Data Flow** - State management with Kotlin Flow
5. **Platform-Specific Code** - Only where necessary (expect/actual)
6. **Reusable Components** - Leveraging existing UI components
7. **Error Handling** - Proper Result types and error states
8. **Code Organization** - Logical package structure

## Testing

The project structure supports:
- Unit tests for repositories and business logic (`commonTest`)
- Platform-specific tests (`androidTest`, `jvmTest`)
- UI tests with Compose testing libraries

## Future Enhancements

Consider adding:
- Refresh token support
- Offline mode with data synchronization
- Image loading and caching
- Advanced filtering and search
- Map integration for car locations
- Push notifications
- Analytics and crash reporting

## License

MIT License - See LICENSE file for details

## Authors

- Boy Krijnen
- Imad Amazyan
- Koen van Vlimmeren
- Robin van Oudheusden
