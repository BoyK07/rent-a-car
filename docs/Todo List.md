# Rent My Car

# To-Do Lijst

## 1. Project Setup en Planning

- [x] **VEREIST** Maak een GitHub-repository aan voor code en documentatie.
- [x] **VEREIST** Stel een logische mapstructuur op (backend / android / docs / media).
- [x] **VEREIST** Wijs teamrollen toe (projectleider, backend, frontend, documentatie, testing).
- [ ] **VEREIST** Stel een globale planning op met deadlines per onderdeel.
- [x] **VEREIST** Richt communicatiekanalen in (Teams, Discord, etc.).
- [x] **VEREIST** Voeg een .gitignore, README en LICENSE toe in de repository.
- [ ] **AANBEVOLEN** Gebruik GitHub Issues of een project board voor taakbeheer.
- [ ] **AANBEVOLEN** Stel een CI/CD-pipeline in (bijv. GitHub Actions voor build en test).

---

## 2. Ontwerp en Voorbereiding

### 2.1 Functioneel en Conceptueel Ontwerp

- [x] **VEREIST** Beschrijf de belangrijkste use cases:
  - Auto aanbieden en beheren
  - Auto zoeken en filteren
  - Auto reserveren
  - Route tonen naar huurauto
  - Rijgedrag meten en bonuspunten berekenen
- [x] **VEREIST** Schrijf user stories voor alle use cases.
- [x] **VEREIST** Stel functionele en niet-functionele eisen vast.
- [x] **VEREIST** Definieer gebruikersrollen (eigenaar, huurder).
- [ ] **AANBEVOLEN** Bedenk uitbreidingen (favorieten, reviews, beheerfunctie).

### 2.2 Architectuur en UML

- [ ] **VEREIST** Maak een Use Case diagram met alle gebruikersacties.
- [ ] **VEREIST** Maak een Klassendiagram met OO-structuur (domain, API, database).
- [ ] **VEREIST** Maak een Packagediagram met logische groepering.
- [ ] **VEREIST** Maak minimaal drie Sequence diagrams voor belangrijke interacties.
- [X] **VEREIST** Bepaal API-architectuur (routes, endpoints, datamodellen).
- [ ] **VEREIST** Ontwerp Android-navigatiestructuur met ten minste drie schermen.
- [ ] **AANBEVOLEN** Maak wireframes of mockups van de app in Figma.

### 2.3 Technische Keuzes

- [ ] **VEREIST** Gebruik Ktor met Exposed ORM.
- [ ] **VEREIST** Gebruik MySQL voor productie, H2 voor lokale tests.
- [ ] **VEREIST** Definieer het database-schema (Users, Cars, Reservations, Rides, Bonuses).
- [ ] **VEREIST** Beschrijf API endpoints en datamodellen in documentatie.
- [ ] **VEREIST** Leg Kotlin code style en architectuurconventies vast.
- [ ] **AANBEVOLEN** Maak een API-mock (Hoppscotch of Postman) voor validatie.

---

## 3. Backend – Ktor Web API

### 3.1 Basisstructuur

- [ ] **VEREIST** Initialiseer een Ktor-project met Gradle.
- [ ] **VEREIST** Voeg dependencies toe: Exposed, MySQL-driver, Ktor, Kotlinx Serialization.
- [ ] **VEREIST** Maak packages aan: routes, models, controllers, services, database.
- [ ] **VEREIST** Implementeer databaseconfiguratie (MySQL + H2 fallback).
- [ ] **VEREIST** Zet JSON-serialisatie en exception handling op.
- [ ] **VEREIST** Maak basisrouting (hello endpoint, health check).

### 3.2 Datamodel en Entity-laag

- [ ] **VEREIST** Definieer entiteiten:
  - [ ] User
  - [ ] Car
  - [ ] Reservation
  - [ ] Ride
  - [ ] BonusPoints
- [ ] **VEREIST** Maak Exposed-tabellen voor alle entiteiten.
- [ ] **VEREIST** Voeg seed- en testdata toe bij applicatiestart.

### 3.3 API-Endpoints

- [ ] **VEREIST** CRUD-endpoints:
  - [ ] Cars: toevoegen, bewerken, verwijderen, lijst, filter
  - [ ] Reservations: aanmaken, bekijken, annuleren
  - [ ] Users: registreren, lijst ophalen
  - [ ] Rides: loggen van rijdata
  - [ ] BonusPoints: opvragen en bijwerken
- [ ] **VEREIST** Implementeer filters en zoekfuncties voor Cars.
- [ ] **VEREIST** Valideer inkomende JSON requests.
- [ ] **AANBEVOLEN** Voeg eenvoudige login/authenticatie toe (dummy of JWT).
- [ ] **AANBEVOLEN** Voeg route endpoint toe (externe API simulatie).

### 3.4 Backend Tests

- [ ] **VEREIST** Schrijf minimaal drie unit tests voor businesslogica.
- [ ] **VEREIST** Schrijf minimaal drie integratietests voor CRUD API's.
- [ ] **AANBEVOLEN** Voeg Postman collectie toe voor handmatige API-tests.

---

## 4. Frontend – Android App (Jetpack Compose)

### 4.1 Project Setup

- [ ] **VEREIST** Maak een nieuw Android project met Jetpack Compose in Kotlin.
- [ ] **VEREIST** Stel API-base URL in (verwijzend naar Ktor backend).
- [ ] **VEREIST** Voeg taalondersteuning toe (Nederlands en Engels via strings.xml).
- [ ] **VEREIST** Vraag permissies aan voor camera, gps, internet en sensoren.
- [ ] **VEREIST** Controleer compatibiliteit met Android Emulator.

### 4.2 Navigatie en Architectuur

- [ ] **VEREIST** Bouw een navigatiestructuur met minimaal drie schermen:
  - [ ] Home / overzicht
  - [ ] Detailpagina voor auto
  - [ ] Reserverings- of routepagina
- [ ] **VEREIST** Gebruik MVVM (ViewModel, Repository, UI state).
- [ ] **VEREIST** Maak Composables voor lijsten, formulieren en detailweergaven.
- [ ] **AANBEVOLEN** Voeg theming en animaties toe voor een consistente stijl.

### 4.3 Functionaliteit

- [ ] **VEREIST** Auto toevoegen (formulier met foto via camera).
- [ ] **VEREIST** Auto's bekijken (lijstweergave met filters).
- [ ] **VEREIST** Auto reserveren (datum en tijd selecteren).
- [ ] **VEREIST** GPS gebruiken om route te tonen naar huurauto.
- [ ] **VEREIST** Meet acceleratie en afstand met sensoren (minimaal twee sensoren).
- [ ] **VEREIST** Bereken bonuspunten op basis van rijgedrag.
- [ ] **AANBEVOLEN** Voeg login/registratie toe.
- [ ] **AANBEVOLEN** Voeg kaartweergave toe (Google Maps API).
- [ ] **AANBEVOLEN** Voeg offline caching toe (Room database).

---

## 5. Testing

### 5.1 Backend Tests

- [ ] **VEREIST** Unit tests voor minimaal drie methoden in services/controllers.
- [ ] **VEREIST** Integratietests voor drie CRUD API endpoints.
- [ ] **AANBEVOLEN** Voeg performancetests toe.
- [ ] **AANBEVOLEN** Voeg automatische testbuild toe via GitHub Actions.

### 5.2 Frontend Tests

- [ ] **VEREIST** Unit tests voor minimaal drie functies of use cases.
- [ ] **VEREIST** UI-tests met Espresso of vergelijkbaar (minimaal drie interacties).
- [ ] **VEREIST** Test sensoren (gps en acceleratie).
- [ ] **AANBEVOLEN** Documenteer handmatige testcases.

---

## 6. Documentatie en Oplevering

### 6.1 Ontwerpdocument

- [ ] **VEREIST** Voeg alle UML-diagrammen toe (Use Case, Class, Package, Sequence).
- [ ] **VEREIST** Documenteer de architectuur en design patterns.
- [ ] **VEREIST** Voeg screenshots of Figma-ontwerpen toe.
- [ ] **VEREIST** Beschrijf Git-samenwerking (branch-strategie, commits).
- [ ] **VEREIST** Geef taakverdeling per student aan.

### 6.2 Testrapportage

- [ ] **VEREIST** Documenteer alle uitgevoerde tests en resultaten.
- [ ] **VEREIST** Voeg Postman/Hoppscotch collectie toe.
- [ ] **VEREIST** Voeg beschrijving van testdata toe.

### 6.3 Productverantwoording

- [ ] **VEREIST** Beschrijf installatie en gebruik van Web API en Android app.
- [ ] **VEREIST** Voeg duurzaamheidstoelichting toe (ISO 25010 subset).
- [ ] **VEREIST** Beschrijf highlights, lowlights en verbeterpunten.

### 6.4 Filmpjes en Presentatie

- [ ] **VEREIST** Maak video deel 1: Web API demo + Hyperskill-overzicht.
- [ ] **VEREIST** Maak video deel 2: Android app demonstratie met use cases.
- [ ] **VEREIST** Leg individuele bijdragen uit in de video's.
- [ ] **AANBEVOLEN** Voeg een korte intro of outro toe met projecttitel.

---

## 7. Optionele of Aanvullende Taken

- [ ] **AANBEVOLEN** JWT-authenticatie implementeren.
- [ ] **AANBEVOLEN** Push notificaties bij reserveringsbevestigingen.
- [ ] **AANBEVOLEN** Extra sensoren gebruiken (kompas, thermometer).
- [ ] **AANBEVOLEN** CI/CD pipeline voor automatische tests en deployment.
- [ ] **AANBEVOLEN** Adminpaneel toevoegen aan backend.
- [ ] **AANBEVOLEN** Rapportage over CO₂-besparing en duurzaamheid.
- [ ] **AANBEVOLEN** App-icoon, splash screen, en dark mode toevoegen.
- [ ] **AANBEVOLEN** App publiceren in een interne of publieke store.

---