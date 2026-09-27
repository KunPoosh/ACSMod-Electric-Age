# Validation scope

Version 0.1.0-dev.1 is a scaffold. Initialization only logs a message; there are no electricity features or gameplay tests.

2026-09-27: the JDK 21 / Gradle 8.13 build passed. JAR inspection confirmed UTF-8 bilingual metadata, the MOD ID/version, the entrypoint and Java 21 bytecode. The package contains only the MOD entrypoint, manifest and metadata, with no framework/game classes or nested dependencies. All four wrapper files match the template. The local ignored summary is `build/scaffold-verification.json`.

Compile dependencies are Acbric API dev.32 and the research installation of game version 1.2.15.3. This does not establish runtime compatibility across game versions.

No real Fabric/Acbric loading, game GUI, electrical behavior, saves, campaign or multiplayer tests have been performed. Gradle `test NO-SOURCE` is not a functional test pass.

Future acceptance directions are in the GDD and workspace research; proposed checks must not be presented as completed results.
