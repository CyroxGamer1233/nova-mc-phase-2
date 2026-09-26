# NovaMC Client — Phase 2

Phase 2 adds the **Minecraft installation/version manager foundation** while keeping authentication and runtime launch in Phase 3.

## Added in Phase 2

- Official Minecraft version manifest retrieval from Mojang's public metadata endpoint.
- Version list with release/snapshot/type information.
- Per-version NovaMC instance directories.
- Version metadata installation into each instance.
- Installation status and repair operation.
- Installed-version management.
- Java runtime detection.
- RAM selection persisted to `~/.novamc/launcher.json`.
- Selected version persisted to launcher settings.
- Background network operations so the UI stays responsive.
- Installation/progress/status feedback.

## Legal/runtime boundary

Phase 2 does not bypass Minecraft ownership or Microsoft authentication and does not bundle Mojang assets. It prepares official version metadata and local installation state. Authenticated Minecraft runtime acquisition and launch are Phase 3.

## Build

Requirements: JDK 17 and Gradle 8.10+.

```bat
gradle clean build
gradle :launcher:run
```

On Windows, use `gradlew.bat` once the standard Gradle wrapper JAR is present.

## GitHub Actions

Push the repository to GitHub and run the existing Windows workflow from **Actions**. The workflow should be updated to use the current `0.2.0-phase2` launcher artifact when packaging.
