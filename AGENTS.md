# Repository Guidelines

## Project Structure & Module Organization
- Single Android app module: source in `app/src/main/java/com/teknos/oncolly`, Compose screens under `screens/`, navigation in `navigation/AppNavigation.kt`, view models in `viewmodel/`, data models in `entity/`, and Retrofit wiring in `network/`. Theme assets live in `ui/theme`; XML/graphics go under `app/src/main/res`.
- Tests: place unit tests in `app/src/test/java` and instrumented/UI tests in `app/src/androidTest/java` mirroring the main package path.

## Build, Test, and Development Commands
- `./gradlew assembleDebug` — compile the app for local/debug installs; use `installDebug` to push to a connected device/emulator.
- `./gradlew connectedAndroidTest` — run instrumentation and Compose UI tests on an attached device/emulator.
- `./gradlew test` — run JVM unit tests.
- `./gradlew clean` — clear build outputs when Gradle state gets stale.

## Coding Style & Naming Conventions
- Kotlin/Compose project using Java 11 and Kotlin 2.0.21; prefer 4-space indentation and trailing commas in multiline calls.
- Classes, composables, and enums use `PascalCase`; functions/variables use `camelCase`; constants use `SCREAMING_SNAKE_CASE`.
- Match file names to the primary class or composable. Navigation routes follow the existing snake-case strings (e.g., `home_pacient`, `detail_pacient/{id}`).
- Favor small, focused composables; keep navigation logic centralized in `AppNavigation` and data flow in `PatientViewModel`.

## Testing Guidelines
- Default to JUnit for unit tests and Espresso/Compose testing libraries for UI flows. Name tests with the subject and expectation (e.g., `PacientScreenTest`, `shouldShowActivitiesForPatient`).
- Aim to cover navigation, ViewModel state changes, and Retrofit serialization boundaries. Prefer fake data sources over network calls in tests.
- Ensure new UI flows have at least one happy-path instrumented test when screens or navigation routes change.

## Commit & Pull Request Guidelines
- Use short, imperative commit messages (e.g., "Add dynamic activity screen", "Fix login navigation"). Group related changes; avoid mixed concerns.
- PRs should describe the change, include reproduction/verification steps, and link issues/tasks. Add screenshots or screen recordings for UI-impacting updates. Note any schema/API changes explicitly.

## Security & Configuration Tips
- Keep secrets out of VCS; prefer `local.properties` or environment variables for per-developer values. Do not hardcode credentials or tokens in `ApiService` calls.
- Validate Retrofit requests/responses and handle nullables defensively in ViewModels and screens to avoid crashes from malformed data.
