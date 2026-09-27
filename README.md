# Electric Age / 电气时代

An independent Acbric Java MOD for Airships: Conquer the Skies, exploring generation, energy storage and electrical loads.

**Current version: 0.1.0-dev.1, project scaffold. Electricity gameplay is not implemented.**

- MOD ID: `electric_age`; Java package: `net.poosh.electricage`
- Build environment: JDK 21 and Gradle Wrapper 8.13
- Declared minimum: Acbric API `0.3.3-dev.32`; later releases are not automatically considered tested
- Initialization only logs a message; no game resources, modules, campaigns or saves are modified
- Independent of ARC Overhaul; no ARC dependency and no inclusion in the framework's default distribution

[中文](README.zh-CN.md) · [GDD discussion draft](docs/GDD.md) · [Validation scope](TESTING.md) · [Third-party notices](THIRD_PARTY_NOTICES.md)

## Build

Use JDK 21 and your own game installation and Acbric distribution. Copy `local.properties.example` to `local.properties`, setting the game directory and framework directory containing `core` and `loader-libs`. Run `gradlew.bat build` on Windows or `./gradlew build` on Linux/macOS.

Override paths with `-PframeworkDir=... -PgameInstallDir=...` if needed. Output: `build/libs/Electric-Age-0.1.0-dev.1.jar`. Building does not launch the game or install the MOD. Dependencies are compile-only and excluded from the JAR.

There are no gameplay tests yet. Gradle reporting `test NO-SOURCE` is not a functional test pass. See TESTING for the actual validation boundary.

## Design and licensing status

GDD v0.1 is a discussion draft. Naming and the generation/storage/load direction are established; scope, allocation rules, campaign behavior and multiplayer support remain open. Proposals do not imply implementation approval or completion.

The owner has not selected a license for this project's original code and documentation. No license from another project has been automatically applied. Gradle Wrapper retains its upstream terms; see the third-party notices.
