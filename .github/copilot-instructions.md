# Copilot project instructions for dabat-app

* This repository is an Android app built with Gradle Kotlin DSL, Kotlin, and Jetpack Compose.
* Keep app code in the `app` module and follow the existing Android project layout: UI code under `app/src/main/`, resources under `app/src/main/res`, and configuration under the Gradle files at the repo root.
* Prefer the project's established conventions over introducing new frameworks or app architectures. If a task touches the UI, keep Compose-first patterns and avoid ad hoc XML/imperative code unless the existing code already uses it.
* Use the smallest relevant Gradle validation command for the area you change. Typical checks are `./gradlew :app:assembleDebug` for compilation and `./gradlew :app:testDebugUnitTest` for unit tests; do not run broad suites unless the task justifies it.
* Keep Gradle and Android configuration aligned with the existing setup in `build.gradle.kts`, `settings.gradle.kts`, and `gradle.properties`; do not remove required plugins or change the project conventions without a clear reason.
* Respect the project's local secrets workflow: use `.env`/local secret files for API keys rather than hard-coding credentials, and follow the README and `.env.example` for app setup steps.
* When changing behavior or UI, add or update focused tests where the project already has coverage. If no tests exist for the changed area, validate with the relevant Gradle build for the affected module and note the limitation.
