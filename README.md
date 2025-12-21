# FairDraw - Event Lottery System

## Project Overview

**FairDraw** is an Android mobile application developed for CMPUT 301 (Fall 2025) that implements a fair and accessible event registration system using a lottery-based approach. The application addresses the challenge of equitable access to popular community events that fill up quickly, ensuring that individuals with time constraints, disabilities, or other limitations have a fair opportunity to participate.

### The Problem

Traditional first-come-first-served event registration systems disadvantage people who cannot constantly monitor registration openings. FairDraw solves this by implementing a lottery system where:
- Users can join a waiting list during an extended registration period (e.g., one week)
- After registration closes, the system randomly selects participants
- Selected participants receive notifications and can accept or decline their spot
- If someone declines, a replacement is automatically drawn from the remaining pool

This approach provides **accessibility** by removing time pressure and giving everyone an equal chance regardless of when they sign up during the registration period.

## Key Features

### For Entrants (Event Participants)
- **Event Discovery & Registration**
  - Browse available events with filtering by interests and availability
  - Join/leave event waiting lists during registration periods
  - Scan promotional QR codes to view event details and register
  - View waiting list size and lottery selection criteria

- **Profile Management**
  - Device-based identification (no username/password required)
  - Personal information management (name, email, optional phone)
  - Event history tracking (registered events, selection status)
  - Profile deletion option

- **Lottery Participation**
  - Receive notifications for lottery wins and losses
  - Accept or decline event invitations when selected
  - Automatic consideration for replacement spots if others decline
  - Optional notification preferences

- **Geolocation** (Optional)
  - Location tracking when joining waiting lists (device-provided)

### For Organizers (Event Creators)
- **Event Management**
  - Create events with comprehensive details (title, description, dates, location, price)
  - Set registration periods (open/close dates)
  - Generate unique promotional QR codes for events
  - Upload and update event posters
  - Set waiting list capacity limits (optional)
  - Enable/disable geolocation requirements

- **Lottery System**
  - Specify number of participants to draw from waiting list
  - View all waiting list entrants
  - Automatically sample participants at registration close
  - Draw replacement participants when spots become available
  - Cancel entrants who don't respond

- **Participant Management**
  - View lists of: waiting list, invited, enrolled, and cancelled entrants
  - Geographic visualization of waiting list (map view)
  - Export final enrollment lists in CSV format

- **Communication**
  - Send targeted notifications to:
    - All waiting list entrants
    - Selected/invited participants
    - Cancelled entrants
  - Notify winners and non-winners of lottery results

### For Administrators
- **Content Moderation**
  - Browse and remove events
  - Browse and remove user profiles
  - Browse and remove uploaded images
  - Remove organizers violating app policies

- **System Oversight**
  - Review notification logs sent by organizers to entrants
  - Monitor overall system usage

## Technologies & Architecture

### Platform & Development
- **Android**: Native Android application (API Level 24+, Target SDK 36)
- **Language**: Java
- **Build System**: Gradle with Kotlin DSL
- **IDE**: Android Studio

### Backend & Services
- **Firebase Firestore**: NoSQL database for events, users, and entrants
- **Firebase Cloud Storage**: Image storage for event posters and profile pictures
- **Firebase Cloud Messaging**: Push notifications for lottery results and organizer messages
- **Google Maps API**: Geolocation visualization for waiting list participants

### Key Libraries
- **AndroidX**: Core Android Jetpack libraries
- **Material Design**: Modern UI components
- **Glide**: Image loading and caching
- **ZXing**: QR code generation and scanning
- **JUnit & Espresso**: Unit and instrumented testing

### Architecture Patterns
- **Model-View-Controller (MVC)**: Separation of concerns
- **Repository Pattern**: Database abstraction layer
- **Observer Pattern**: Real-time Firebase listeners
- **Adapter Pattern**: RecyclerView implementations for dynamic lists

## Project Structure

```
FairDraw/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/example/fairdraw/
│   │   │   │   ├── Activities/      # UI screens (58 Java files total)
│   │   │   │   │   ├── EntrantHomeActivity.java
│   │   │   │   │   ├── OrganizerMainPage.java
│   │   │   │   │   ├── AdminEventsPage.java
│   │   │   │   │   └── ...
│   │   │   │   ├── Adapters/        # RecyclerView adapters
│   │   │   │   ├── DBs/             # Firebase database interfaces
│   │   │   │   ├── Fragments/       # Reusable UI fragments
│   │   │   │   ├── Models/          # Data models (Event, User, Entrant, etc.)
│   │   │   │   ├── Others/          # Utilities and enums
│   │   │   │   └── ServiceUtility/  # Firebase and device services
│   │   │   ├── res/                 # Resources (layouts, strings, drawables)
│   │   │   └── AndroidManifest.xml
│   │   ├── test/                    # Unit tests (13 test files)
│   │   └── androidTest/             # Instrumented tests
│   └── build.gradle.kts
├── docs/
│   └── javadoc/                     # Generated API documentation
├── UMLDiagram.puml                  # PlantUML class diagram
├── UMLDiagram.svg                   # Visual class diagram
└── README.md
```

## Setup & Build Instructions

### Prerequisites
- Android Studio Hedgehog (2023.1.1) or later
- Java Development Kit (JDK) 11 or later
- Android SDK with API Level 24+ installed
- Firebase project configuration (google-services.json)

### Build Steps
1. Clone the repository:
   ```bash
   git clone https://github.com/CMPUT301f25Orchid/Project.git
   cd Project/FairDraw
   ```

2. Open the project in Android Studio:
   - File → Open → Select the `FairDraw` directory

3. Sync Gradle dependencies:
   - Android Studio will prompt to sync automatically
   - Or manually: File → Sync Project with Gradle Files

4. Add Firebase configuration:
   - Place your `google-services.json` in `app/` directory
   - (Note: A configuration file is included in the repository that has been disabled)

5. Build the project:
   ```bash
   ./gradlew build
   ```

6. Run on emulator or device:
   - Select target device in Android Studio
   - Click Run (▶️) or use: `./gradlew installDebug`

### Generate Documentation
```bash
./gradlew androidJavadoc
```
Documentation will be generated in `docs/javadoc/`

## Testing

The project includes comprehensive testing:
- **Unit Tests**: 13 test files covering core business logic
- **Instrumented Tests**: UI and integration tests using Espresso
- **Test Coverage**: Models, database operations, lottery algorithm, and user workflows

Run tests:
```bash
# Unit tests
./gradlew test

# Instrumented tests (requires connected device/emulator)
./gradlew connectedAndroidTest
```

## Team & Context

- **Course**: CMPUT 301 - Introduction to Software Engineering (Fall 2025)
- **Team**: Orchid
- **Institution**: University of Alberta
- **Repository**: [CMPUT301f25Orchid/Project](https://github.com/CMPUT301f25Orchid/Project)

## Achievements

This project successfully implemented:
- ✅ All core user stories for Entrants (15+ requirements)
- ✅ All core user stories for Organizers (20+ requirements)
- ✅ All core user stories for Administrators (8+ requirements)
- ✅ Complete lottery system with replacement drawing
- ✅ QR code generation and scanning functionality
- ✅ Real-time Firebase integration with live updates
- ✅ Geolocation tracking and map visualization
- ✅ Multi-role user system with role-based permissions
- ✅ Comprehensive notification system
- ✅ Image upload and management
- ✅ CSV export functionality for organizers
- ✅ Device-based authentication (no passwords required)
- ✅ Material Design UI with accessibility considerations

## License

This project was developed as coursework for CMPUT 301 at the University of Alberta.

---

*Built with ❤️ by Team Orchid for accessible and fair event registration*
