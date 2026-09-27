# Electric Age asset integration review

Date: 2026-09-27. Scope: root-level `electric_age_art/`, reviewed against ART_SPEC v1.2. [中文](ART_REVIEW.zh-CN.md)

**Conclusion: all six module asset sets are sufficient for initial development and integration, with no blocking module-art defects found. Five UI icons remain missing; actual game loading and presentation are unverified.** Original assets were not edited, supplied Lua scripts were not run, and no MOD installation or gameplay implementation occurred.

## Independent checks

- Decoded 121 production PNGs across six exports directories. All have expected body/part/sheet/composite dimensions, RGBA format and transparent backgrounds; none is empty. Current production alpha is binary, without partially transparent scaling fringes.
- Independently parsed eight primary Aseprite files: six source.aseprite files plus propeller and barrel sources. All are 32-bit RGBA, totaling 47 layers, with expected canvases, frame counts, 100 ms frame durations and editable layer data. This was binary parsing, not opening them in the Aseprite application.
- Passed 445 static assertions, including 45 source-layer/frame versus PNG pixel comparisons. Other checks covered animation sheet slices, damage differences, nonempty/distinct states, documented cannon/battery/tower composition and a visible discharge anchor in both electrode variants. These are asset checks, not gameplay tests.
- Visually reviewed the six-device overview and state/damage previews. Silhouettes, pixel density and palette fit the brief, without obvious baked crew, selection borders or an enclosing tower armor shell. In-game scaling and adjacent-module readability still require inspection.
- Local evidence: [audit script](../build/asset-review/audit.py) and [results with file SHA256 hashes](../build/asset-review/audit.json). These ignored build outputs are not product code or shipping assets.

## Integration inventory

| Device | Supplied assets | Integration notes |
| --- | --- | --- |
| Generator | 48×32 normal/damaged structure, furnace/gauge/state layers, working and stopped frames | Loop the first four frames; the fifth is stopped. Body alone is not a complete appearance |
| Battery | 32×32 normal/damaged bodies, charge-level and flow overlays | Clip/show existing segments; flow layers are mutually exclusive and need correct mirrored semantics |
| Propulsion | 32×32 body, 16×32 propeller, working/stopped frames and damaged-stop sketch | Propeller offset (-16,0); 48×32 full preview is not a larger footprint |
| Lift | 48×32 normal/damaged crystals, working/stopped frames, charge and state layers | Synchronize crystal and charge frame indices; damage does not imply shutdown |
| Cannon | 48×32 body, 48×16 barrel, rack/tray/ram/mount/state layers | Barrel offset (24,4), body pivot (32,12), local pivot (8,8), local muzzle (47,8). Full 72×32 preview does not change footprint |
| Lightning tower | 32×48 exposed normal/damaged structure, separate electrode and three operating states | Discharge anchor (16,2), fixed upright; no fixed-length lightning or directional barrel frames needed |

Each exports/README.md and anchors.txt describes compositing, origins and draft crew access. Full previews already contain parts/states; do not overlay those parts twice. Operating state layers are exclusive; hiding them represents shutdown without an empty state_off asset.

## Outstanding items

**UI art still needed:** five 16×16 PNGs: electricity, weapon_overload_off/on and propulsion_overload_off/on. The supplied README explicitly excludes these. They do not block grid/module development but are needed to complete the intended button presentation.

**Within the agreed initial scope:** propulsion has a damaged stopped-propeller sketch but no damaged motion cycle. A damaged body can initially retain the intact working propeller; do not use the stopped damaged composite for every damaged state. A rotating damaged-blade appearance can follow later. Bump maps, fragments, complete wreckage, muzzle flash and terminal flash remain optional/deferred or procedural as previously agreed.

**Integration work:** atlas/definition packaging, state selection, barrel rotation/recoil/mirroring, external-part draw order, unarmored tower behavior, crew paths, module geometry and persistence. Transparent pixels do not automatically define collision holes or canOccupy. Omitting an armor sprite does not implement unarmored damage handling. Cannon anchors are explicit, but full-arc rotation remains untested in game.

**Lightning:** follow the discussed procedural polylines, bright cores/halos and brief fade. Resolve attacks first and record node paths; visual randomness remains separate, with no retargeting or repeated damage during rendering. The fixed electrode and supplied anchor are sufficient; no full bolt animation is required from the artist.

This is readiness for development integration, not completed in-game acceptance.

## Development preparation update

The user subsequently approved temporary native UI reuse. Five dedicated icons no longer block the first playable test. The missing items above remain an accurate inventory of the original delivery; module art and static results are unchanged. See the [implementation sequence](IMPLEMENTATION_PLAN.md).
