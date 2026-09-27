# Contributing to Electric Age

[中文](CONTRIBUTING.zh-CN.md) · [README](README.md) · [Documentation](docs/README.md)

Use JDK 21, your own Airships installation and an Acbric distribution containing `core/` and `loader-libs/`. Copy `local.properties.example` to `local.properties` and configure both paths. Never commit personal paths, game files or framework binaries.

- `gradlew.bat jar --no-daemon`: compile/package only; no tests or game launch.
- `gradlew.bat build --no-daemon`: compile/package and core regression; no game launch.
- `python tests/verify_package.py`: inspect the built JAR.

Use `./gradlew` on Linux/macOS. Runtime scripts currently target Windows; do not assume they work elsewhere. `tests/run_runtime.py` and `tests/run_game.py` explicitly start isolated JVM/game processes. The GPU probe also requires tools and test outputs from an Acbric development checkout, not just its distribution. See each script's `--help` and [validation boundaries](TESTING.md).

Implementation lives in `src/main/java/`. Native module attributes use `content/devices.json`; grid/lightning parameters use `src/main/resources/electric_age/balance.properties`. Update both BALANCE documents when changing numbers. Editable artwork and exported parts are under `electric_age_art/modules/`; Gradle generates `build/generated/content/`. Edit inputs, not generated files. Ordinary builds do not need Aseprite.

Keep player text and main documents bilingual. Verify version-specific Mixin targets; distinguish compilation from runtime validation. Do not include game/framework binaries, decompiled research, saves, logs or private paths, and do not overwrite player installations. Keep PRs focused; describe the problem, resulting behavior and validation, including tests not run.

Original contributions use the project's MIT license. Preserve licenses and attribution for third-party content. Reference native game assets at runtime instead of copying them. Bug reports should include MOD/framework/game versions, reproduction steps and relevant log excerpts with private details removed.
