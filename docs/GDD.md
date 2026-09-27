# Electric Age — GDD v0.1 discussion draft

Date: 2026-09-27. Status: open for discussion, not an approved implementation specification.

## 1. Established direction

- Independent Acbric Java MOD: Electric Age / 电气时代.
- Real electricity quantities and constraints: generation, battery storage and rated electrical loads.
- Explore reusable Acbric APIs while keeping electrical gameplay separate from framework responsibilities.
- The repository currently provides only a build and initialization entrypoint; no electricity behavior, vanilla conversion or save rules exist.

## 2. Proposed player experience

Design generation, storage and load combinations, balancing sustained operation, burst output, space, weight, cost and resilience to damage.

Proposed loop: design the supply/demand balance, observe it in combat, respond to generator/battery damage, then revise the ship. Setting, art direction, research progression and final equipment roster remain open. ProjEOL's setting and assets are not adopted automatically.

## 3. Proposed first playable scope

- One shared bus per ship, without wiring; split hulls settle separately.
- One generator, one battery and one continuous load; pulse weapons can follow later.
- Energy belongs to battery instances; ship-level aggregation must not duplicate it.
- Explicit shortage behavior, read-only status display, and early save/clone design.
- Generation responds to fuel, staffing and survival; derating rules remain open.
- No automatic conversion of all vanilla equipment. Whether to prioritize new devices or vanilla conversions remains a user decision.

These are proposals, not an agreed release scope.

## 4. Open decisions

| Topic | Starting question | Impact |
| --- | --- | --- |
| Content | New equipment or conversion of vanilla systems? | Compatibility and scope |
| Network | Shared ship bus or wiring? | Damage tactics, topology and UI |
| Shortage | Priority shutdown or proportional derating? | Allocation and device behavior |
| Generation | Native fuel, efficiency and damage rules? | Logistics and endurance |
| Storage | Initial charge, rate limits and damage losses? | Burst capability and balance |
| Campaign | Retain charge; recharge while parked or travelling? | Persistence and autoresolve |
| Modes | Single-player combat first, or campaign/multiplayer at launch? | Acceptance scope |
| Framework | Foundational lifecycle APIs first; when to stabilize resource APIs? | Compatibility commitments |
| Licensing | Terms for original code/docs and future assets? | Collaboration and distribution |

## 5. Constraints and acceptance direction

Use simulation time, not wall time. Conserve energy and curtail loads when supply is insufficient. Power loss must not suspend fire, explosion or flooding updates. Repair, splitting, cloning and restoration must not create duplicate charge. Rendering must not advance resources, and each load must not rescan the entire ship. Autoresolve, campaign continuity and multiplayer synchronization require explicit separate designs. No game-version, performance or multiplayer guarantee is implied.

Proposed milestones: M0 scaffold, M1 agreed GDD, M2 isolated resource core, M3 minimal playable content and persistence, M4 agreed mode/content expansion. Only M0 has been started; this roadmap does not authorize automatic implementation.

## 6. Research basis

The workspace report on real electricity feasibility dated 2026-09-27 and static comparisons of game versions 1.2.15.2, 1.2.15.3 and 1.2.14. Nine work areas were identified: definitions, instance state, scheduling, devices, damage, persistence, UI, campaign/AI and multiplayer/replays. The earlier report discussed ARC; Electric Age is a new independent project.

Decompiled game code and experimental inputs remain outside this repository.
