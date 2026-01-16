# Rent My Car
- Boy Krijnen
- Imad Amazyan
- Koen van Vlimmeren
- Robin van Oudheusden

## Inleiding
**Rent My Car** is een platform dat duurzaam vervoer stimuleert door autodelen toegankelijk te maken. Het systeem maakt het mogelijk voor gebruikers om hun eigen auto aan te bieden voor verhuur, en voor anderen om eenvoudig een auto te reserveren en te huren voor een dagdeel.

Het platform bestaat uit twee hoofdonderdelen:
1. **Web API** – biedt functionaliteit voor het beheren van gegevens van te huur aangeboden auto's, inclusief beschikbaarheid en berekeningen.
2. **Android App** – stelt gebruikers in staat om auto's toe te voegen, beschikbare voertuigen te zoeken en reserveringen te maken.

---

## Functionaliteit

### Autoverhuur aanbieden
Gebruikers kunnen hun eigen auto beschikbaar stellen voor verhuur. Hierbij kunnen ze:
- Autogegevens invoeren (merk, type, categorie, enz.).
- Verhuurvoorwaarden opgeven (prijs, ophaal- en inlevertijd).
- Foto’s toevoegen van de auto via de camera van de mobiele telefoon.

### Autocategorieën
Er wordt onderscheid gemaakt tussen de volgende typen voertuigen:
- **ICE (Internal Combustion Engine)** – traditionele brandstofauto (benzine, diesel, gas).  
- **BEV (Battery Electric Vehicle)** – elektrische auto met accu.  
- **FCEV (Fuel Cell Electric Vehicle)** – elektrische auto met waterstof-brandstofcel.

---

## Berekeningen

Voor elke toegevoegde auto kan de eigenaar:
- **TCO (Total Cost of Ownership)** berekenen op basis van een standaard aantal kilometers per jaar.  
- **Verbruikskosten per kilometer** berekenen, afhankelijk van de categorie en het brandstoftype van de auto.

---

## Auto huren
Gebruikers die een auto willen huren kunnen:
- Zoeken en filteren op criteria zoals afstand, prijs of type voertuig.
- De beschikbare auto's bekijken in een **lijstweergave** of op een **kaartweergave**.
- Een auto selecteren en reserveren voor een specifiek tijdsblok.
- De (loop)route opvragen naar de huurauto vanaf hun huidige locatie.

---

## Rijgedrag en beloningen
Tijdens de huurperiode worden gegevens bijgehouden over:
- De **gereden afstand**.
- Het **rijgedrag** van de gebruiker (zoals acceleratie en afremmen).

Een **rustig rijgedrag** wordt beloond met **bonuspunten**, die kunnen bijdragen aan duurzaam en verantwoordelijk rijden.

---

## Doel
Het doel van **Rent My Car** is om:
- Het delen van voertuigen te stimuleren.  
- De ecologische voetafdruk van vervoer te verkleinen.  
- Gebruikers bewust te maken van de kosten en het verbruik van hun rijgedrag.

---

## Samenvatting
| Onderdeel | Beschrijving |
|------------|---------------|
| **Naam** | Rent My Car |
| **Type project** | Autodeelplatform |
| **Hoofdonderdelen** | Web API en Android App |
| **Belangrijkste functies** | Auto aanbieden, reserveren, TCO- en verbruiksberekeningen, routeweergave, rijgedragsanalyse |
| **Doel** | Duurzamer vervoer en kostenbewust autodelen stimuleren |

---

## CI/CD en Releases

Het project maakt gebruik van GitHub Actions voor geautomatiseerde builds en releases:

### Workflows

#### CI Workflow
- **Trigger**: Push/PR naar `main` of `develop` branches
- **Functionaliteit**: Bouwt en test alle modules (app, server, shared)
- **Badge**: ![CI](https://github.com/DevKoenv/rent-a-car/workflows/CI/badge.svg)

#### Build Android App
- **Trigger**: Push naar `main` (met wijzigingen in app/shared modules)
- **Output**: Debug en Release APK's
- **Artifacts**: Beschikbaar voor 30 dagen

#### Build Server
- **Trigger**: Push naar `main` (met wijzigingen in server/shared modules)
- **Output**: Server JAR en Fat JAR
- **Artifacts**: Beschikbaar voor 30 dagen

#### Release Workflow
- **Trigger**: Push van een tag met format `v*` (bijvoorbeeld `v1.0.0`)
- **Output**: 
  - Android APK
  - Android AAB (voor Play Store)
  - Server Fat JAR
- **Functionaliteit**: Creëert automatisch een GitHub Release met alle artifacts

### Een Release Maken

Om een nieuwe release te maken:

```bash
# Tag de huidige commit met een versienummer
git tag -a v1.0.0 -m "Release version 1.0.0"

# Push de tag naar GitHub
git push origin v1.0.0
```

Dit triggert automatisch de release workflow die:
1. Alle modules bouwt (Android app + Server)
2. Een GitHub Release creëert
3. Alle build artifacts toevoegt aan de release
4. Release notes genereert met download instructies

### Deployment

**Android App**: Download de APK van de releases pagina en installeer op Android apparaten (API 24+)

**Server**: 

Optie 1 - Docker (aanbevolen voor productie):
```bash
# Build de Docker image
docker build -f app/modules/server/Dockerfile -t rentmycar-server:latest .

# Start de server
docker run -d \
  --name rentmycar-server \
  -p 8080:8080 \
  -e DB_PROVIDER=mariadb \
  -e DB_HOST=your-db-host \
  -e DB_USER=rentmycar \
  -e DB_PASSWORD=your-password \
  -e JWT_SECRET=your-secure-secret \
  rentmycar-server:latest
```

Optie 2 - Docker Compose (lokale ontwikkeling):
```bash
# Start server + database
docker-compose up -d

# Bekijk logs
docker-compose logs -f server
```

Optie 3 - Direct met JAR:
```bash
java -jar rentmycar-server-{version}.jar
```

Zie [Docker Deployment Guide](docs/DOCKER_DEPLOYMENT.md) voor gedetailleerde instructies.

---

## Database Configuratie

Het project ondersteunt meerdere database providers voor verschillende use cases:

- **H2 In-Memory** (standaard): Voor snelle lokale ontwikkeling en testen zonder externe database
- **H2 File-Based**: Voor lokale ontwikkeling met data persistentie
- **MariaDB External**: Voor productie en staging omgevingen

### Snelle Start

Standaard gebruikt de applicatie H2 in-memory – gewoon starten zonder extra configuratie:
```bash
cd app
./gradlew :server:run
```

Voor andere database providers, gebruik omgevingsvariabelen:
```bash
# H2 file-based (met persistentie)
DB_PROVIDER=h2-file ./gradlew :server:run

# Externe MariaDB
DB_PROVIDER=mariadb DB_HOST=localhost DB_PASSWORD=your-password ./gradlew :server:run
```

### Voor Tests

Tests gebruiken automatisch H2 in-memory met een schone database:
```bash
./gradlew :server:test
```

Zie [Database Configuration Guide](docs/DATABASE_CONFIGURATION.md) voor gedetailleerde configuratie opties, troubleshooting, en best practices.

---

## Licentie

Dit project is gelicentieerd onder de MIT License – zie het [LICENSE-bestand](LICENSE) voor meer informatie.
