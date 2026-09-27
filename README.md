# Electric Age / 电气时代

An independent Acbric Java MOD for Airships: Conquer the Skies. **0.1.0-dev.4 implements the six-device single-player combat test build**: coal generator, battery, electric propulsion, electric lift, electrical cannon and chain-lightning tower.

Shared power reduces fuel hauling at individual devices, but supply failures can disable several systems together. Cannons retain ammunition logistics; lightning requires reload cycles without ammunition. Native devices retain their rules. No ARC Overhaul dependency or inclusion in Acbric's default distribution.

[中文](README.zh-CN.md) · [Install and playtest](docs/PLAYTEST.md) · [Validation](TESTING.md) · [GDD](docs/GDD.md) · [Balance](docs/BALANCE.md) · [Art](docs/ART_SPEC.md)

## Implemented

- Per-ship allocation, proportional derating, sequential shutdown, full-rated restart, battery support and uniform overload.
- Weapon/propulsion toggles through native commands, spending a command in either direction. Actual overload causes module wear with native fire/detonation risk.
- Serialized charge, online state and fractional wear; split inheritance, capture clearing and native resupply refill.
- Instant chain lightning: native aiming, surface targets, branching energy, point blast damage and procedural bolts.
- Six device state textures, two text overload toggles in the bottom ship panels, one power bar and three builder statistics.

The probe results below apply to dev.3 and earlier. Per user request, dev.4 was compiled and packaged without rerunning automated tests; the user subsequently confirmed the reported bugs behave correctly; see [patch notes](docs/COMBAT_FIX.md).

Historical automated probes cover actual loading, combat, native serialization, partial repair/resupply entry points and GPU rendering. **Full campaign consecutive-battle/resupply loops, actual multiplayer and extended manual play remain unvalidated.** No AI control or autoresolve algorithm extensions. See the [implementation plan](docs/IMPLEMENTATION_PLAN.md).

## Build

Use JDK 21 and your own game and Acbric distribution. Copy `local.properties.example` to `local.properties`, setting the game directory and framework directory containing `core` and `loader-libs`. Run `gradlew.bat build --no-daemon` (or `./gradlew build --no-daemon`). Override paths with `-PframeworkDir=... -PgameInstallDir=...` if needed.

Output: `build/libs/Electric-Age-0.1.0-dev.4.jar`. The build runs `coreTest`; it does not launch the game, install the MOD or commit Git. Dependencies are excluded from the JAR.

MOD ID: `electric_age`; package: `net.poosh.electricage`. Declared minimum Acbric API: `0.3.3-dev.32`. See TESTING for observed versions; later versions are not automatically validated.

BALANCE is the design authority. Machine inputs are `content/devices.json` for native properties and `src/main/resources/electric_age/balance.properties` for power/attack settings. Keep the register synchronized. Resource generation composites delivered artwork without changing source art.

Original code, documentation and artwork use the [MIT license](LICENSE). Gradle Wrapper retains upstream terms; see [third-party notices](THIRD_PARTY_NOTICES.md). No automatic commit, push, publication or player installation.

[dev.3 rendering fix and update notes](docs/RENDER_FIX.md)

## Repository layout

| Directory | Purpose |
| --- | --- |
| `src/main/` | Java implementation, Mixins and runtime configuration |
| `src/test/`, `tests/` | Core regression, package checks and explicitly invoked isolated probes |
| `content/` | Native module attribute inputs |
| `electric_age_art/` | Original editable artwork and exported build inputs |
| `gradle/` | Wrapper and resource generation |
| `docs/` | Bilingual design, balance, art and playtest documents |
| `licenses/` | Third-party licenses and notices |

[Documentation index](docs/README.md) · [Contributing](CONTRIBUTING.md) · [Changelog](CHANGELOG.md)

Local dependency paths, builds, game files, saves, logs and art-production history are ignored and retained locally. They are not required in GitHub to build from the checked-in PNG inputs.
