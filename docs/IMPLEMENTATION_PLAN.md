# Electric Age development preparation and sequence

> Implementation status (2026-09-27): dev.4 implements the six-device single-player combat test package; the user confirmed the reported bugs are resolved. This file retains the original milestones/criteria; [TESTING](../TESTING.md) defines verified scope. Full campaign and actual multiplayer acceptance remain pending.

2026-09-27. [中文](IMPLEMENTATION_PLAN.zh-CN.md)

The agreed mechanics, balance and six device asset sets are sufficient to begin development. The user approves temporary UI reuse: reference existing game icons at runtime, using labels, selection state and agreed colors to distinguish functions. Five dedicated icons no longer block development or the first playable test. Do not copy native icons into the art delivery. Button behavior, power bar and builder statistics still follow the GDD.

## Delivery target

First deliver all six devices, grid, batteries, startup/shutdown, overload and lightning in single-player custom battles using all-technology settings. Campaign and multiplayer remain goals; design persistence and determinism from the start and validate each mode separately. Routine implementation details are delegated; discuss changes to agreed gameplay.

dev.2 delivers a single-player test package for stages 1–4. Stage 5 persistence/refill entry points are integrated; complete campaign loops and stage 6 remain unvalidated. No automatic installation, commit or release occurred.

## Implementation sequence and completion criteria

| Stage | Work | Completion criteria |
| --- | --- | --- |
| 1 Power core | Definitions, instance state, per-ship allocation, batteries, sequential shedding, full-rated restart, uniform overload and fractional wear | Game-independent boundary tests verify energy accounting, ordering, depletion boundaries, pause/timestep handling and repeatable results |
| 2 Minimal live loop | Bundle resources; integrate generator, battery and propulsion first, including native coal/crew eligibility, output scaling and state art | Build and run in an isolated instance; fuel loss switches to batteries, depletion reduces/disables output, restoration allows rated startup; native equipment remains unaffected |
| 3 Six devices and overload | Lift, cannon, two native-command overload toggles, wear, landing, splitting, capture and repair | Only actual overload causes wear; charge each command once; separate requests from actual state; splitting preserves energy; use temporary icons |
| 4 Lightning and UI | Surface targets, shared visited set, energy branches, native point blast damage, procedural bolts, power bar and builder summary | Resolve attacks in one simulation update independently of visuals; validate isolated/branching/armored/cross-ship targets and deliver the six-device single-player package |
| 5 Campaign and restoration | Machining eligibility, combat/campaign serialization, consecutive battles, actual resupply and autoresolve data retention | No free charge without resupply; resupply fills batteries; restoration preserves state; native autoresolve algorithm remains unchanged |
| 6 Multiplayer validation | Native command simulation, stable iteration/random calls, dual-client state comparison and restoration | Declare support only for validated modes; matching JARs or lobby checks do not establish simulation consistency |

Define persistence formats and identity contracts in stage 1; validate reconstruction and combat restoration from stage 2. Stage 5 completes campaign integration rather than introducing persistence for the first time.

## Code boundaries

- `definition`: equipment specifications and value loading, with BALANCE authoritative and no duplicate constants across adapters.
- `simulation`: device snapshots, explicit commands, simulation time and stable identities produce allocation, energy and state results without UI or wall-clock dependencies.
- `integration`: native coal, crew, landing and module state inputs; output, damage and native-command adapters verified against actual 1.2.15.3 bytecode.
- `state`: versioned battery energy, operating states, overload requests and wear remainders. Distinguish new construction, copying, restoration, splitting and destruction. Object addresses and unordered collections are not deterministic identities.
- `lightning`: deterministic target/energy resolution with separate visual data; technical protection must not silently truncate normal attack budgets.
- `client`: icons, buttons, resource bar, textures and bolts consume simulation results. Neither each load nor each draw should rescan the ship.

Start with core data and meaningful rule tests while verifying scheduling, identity and save hooks. Keep gameplay out of the initializer logging class.

## Verified framework capabilities and integration gaps

| Capability | Existing basis | Boundary |
| --- | --- | --- |
| Native definitions/assets | Acbric `acbric_vanilla/` bundles and `AirshipsDataEvents.DATA_LOADED` | Managed JAR resources; data can reload, so avoid duplicate registration |
| Combat UI | `AirshipsCombatUiEvents` status-bar/control-panel drawing and input events | UI callbacks are not simulation ticks; layout integration still needs live checks |
| Campaign storage | `CampaignData` versioned JSON | No automatic network synchronization or combat/module serialization |
| Power/combat behavior | Inspected public APIs do not supply a ready-made power system | Implement MOD-local core and precise adapters; no framework public API edits in this task |

Existing 1.2.15.2 static research identifies candidates, while current compilation uses 1.2.15.3. The former is not runtime validation of the latter. Verify allocation versus native module-update ordering, applying reload/thrust/lift scaling once, split identities, standalone combat saves and campaign writeback early.

## Historical preparation checks (dev.1)

`gradlew.bat build --no-daemon` succeeded with the workspace JDK 21. The system default is Java 17, so select JDK 21 explicitly in development commands; system settings were not changed. There are no test sources: `test NO-SOURCE` is not gameplay validation. Retain the existing 445 static asset checks without rerunning unchanged art reviews.

Actual loading, power, combat, saves and multiplayer remain untested. Original art stays unchanged; later packaging should use an explicit production allowlist excluding previews, Aseprite sources and authoring scripts.
