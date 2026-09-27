# Electric Age: first-release equipment and art preparation v1.2

Date: 2026-09-27. User-confirmed art handoff baseline. The user will give this file to another project connected to Aseprite MCP; no message was sent to that project this turn. Footprints, operating crew, base canvases, visual direction and layer organization are confirmed. Suggestions below are accepted initial production defaults; optional/deferred items and validation boundaries remain as written. Assets have not been produced or accepted here, and loading/state switching remain unimplemented.

[中文](ART_SPEC.zh-CN.md) · [GDD](GDD.md) · [Balance register](BALANCE.md)

Maintain gameplay tuning in the balance register. This brief retains footprint/crew tables as art handoff copies; update both and notify asset authors when those specifications change. Canvas, animation and anchor requirements remain owned here. This turn changes no handed-off dimensions or crew counts.

## 1. Verified native conventions

- Ordinary Appearance x/y/w/h values are multiplied by 16: one module cell is 16×16 source pixels. A 3×2 body occupies 48×32 pixels before screen scaling.
- Appearance supports frames and a millisecond interval. Four/six frames are examples, not engine requirements.
- Body, externalAppearances and weaponAppearance barrels can be separate. Barrel/projectile images use Loadable.img pixel coordinates, unlike Appearance cell coordinates.
- Native module flipping and image mirroring exist. Prepare one orientation initially; keep text and non-mirrorable markings separate.
- SpriteUtils loads PNG from MOD images directories. Deliver RGBA transparent PNG plus editable sources; integration handles atlases and JSON.
- ModuleType.mask defaults to full cells. An explicit mask samples alpha from a separate region of the same atlas, with any nonzero alpha treated as solid. Transparent holes in body art do not automatically become physical holes. Glow is not a valid structural mask.
- SpritesheetBundle links bump, damaged and fragment sheets. Variants must keep aligned atlas positions. Do not assume a plain PNG automatically gains all lighting/destruction behavior. Preserve editable layers; supplement bump/fragment/derived sheets after an integration trial.

Evidence is static 1.2.15.2 decompiled code with local 1.2.15.3 data examples. New-asset rendering and cross-version compatibility remain untested.

## 2. Confirmed equipment specifications

Dimensions are width×height. Crew counts describe direct operating stations, excluding coal/ammunition hauling, repairs and firefighting. They do not settle output or balance.

| Equipment | Footprint | Body canvas | Direct operators | Parts and visual identity |
| --- | --- | --- | --- | --- |
| Coal generator | 3×2 cells | 48×32 px | 2 | Boiler/firebox and generator windings; separate gauges, moving parts and fire |
| Battery | 2×2 | 32×32 px | 0 | Cell banks, busbars and service panel; separate charge indicator |
| Electric propulsion | 2×2 | 32×32 px | 1 | Motor/frame plus separate 16×32 propeller on the left; combined preview 48×32 |
| Electric lift | 3×2 | 48×32 px | 1 | Recognizable suspendium crystal with powered coils/support; separate crystal and lights |
| Electrical cannon | 3×2 | 48×32 px | 2 | Mount, ammunition feed and electric mechanism; separate provisional 48×16 rotating barrel |
| Chain-lightning weapon | 2×3 | 32×48 px | 1 | Exposed deck-mounted vertical coil and terminal; fixed body without hull-armor coverage, omnidirectional procedural bolts from the top, no rotating barrel |

External propeller and barrel canvases do not add occupied cells. Occlusion, draw order and neighboring-device restrictions need integration checks. Keep front-edge placement for the cannon. Lightning uses a fixed upright coil tower with a marked upper discharge anchor; bolts can extend toward targets in any direction, with the arc defined in the balance register. No rotating tower or directional barrel frames are needed. Top mounting, no hull-armor coverage and point hits without intermediate bolt obstruction are confirmed; integration remains to be validated.

The generator centralizes supply and coal logistics; batteries buffer energy at a mass/cost tradeoff without operators; propulsion/lift reduce local fuel hauling; cannon retains ammunition logistics and lightning exchanges range for ammunition-free chained attacks. Confirmed gameplay values are maintained in the balance register.

## 3. Authoring layers

1. Structural body: housing, frame and machinery. Do not bake in ship armor, selection outlines, crew or UI labels drawn elsewhere by the game.
2. Moving/weapon parts: rotor, propeller, barrel and crystal, separable for rotation, recoil and stopping.
3. State layers: indicators, coil glow and overload heat. Integration may compose replacement frames or draw layers; no existing general state-texture API is claimed.

Keep a shared origin and identical canvas size across each part's frames; do not trim transparent borders per frame. Deliver Aseprite, PSD or Krita source plus original-resolution PNGs, not only enlarged previews.

Use side-view pixel art at native scale, with steel, brass, copper windings and ceramic insulation with restrained blue-white glow. Combine color with light on/off, motion or shape cues. Use nearest-neighbor integer scaling for previews, avoiding smoothing at source size.

## 4. States and animation

For propulsion, lift, cannon and lightning:

| State | Proposed appearance | Deliverable |
| --- | --- | --- |
| Off | Indicators dark, work animation stopped | Complete off reference using base/still parts |
| Rated | Stable indicators, normal motion | Rated state layer and relevant motion frames |
| Derated | Dim/intermittent indicators, slower machinery | Derated layer; reuse motion frames |
| Actually overloaded | Brighter coils/local heat, stronger motion | Overload layer; do not show merely because the command is requested |

Damage is independent: provide normal and damaged structural layers. Damage must not imply shutdown or permanently dark indicators. Do not hand-paint every state×damage×animation combination; prepare reusable layers for later composition.

| Equipment | Initial animation target | Reuse/defer |
| --- | --- | --- |
| Generator | Four moving frames plus one stopped frame | Native fire/smoke particles may be reused; no load-proportional generation states |
| Battery | Static body | Small charge/direction overlay or code; no full image per charge level |
| Propulsion | Four propeller frames plus one stopped frame | Reuse frames at different visual speeds |
| Lift | Four crystal/coil frames plus one stopped frame | Reuse motion with state layers |
| Cannon | One separate barrel with procedural recoil | Optional three-frame muzzle flash; try native effects first |
| Lightning | One body and state layers | Optional three-frame terminal flash; generate the variable bolt tree in code |

Use 100 ms/frame as a provisional handoff note; integration will tune it independently of gameplay timing. Include at least a damaged external-propeller/barrel sketch. Full wreckage, fragments and lighting assets follow the first loading trial rather than blocking initial sketches.

Coil-tower art addition: draw an exposed base, supports, insulators, coils and upper discharge terminal without an enclosing hull-armor shell. Normal/damaged layers depict equipment damage and retain the agreed operational layers. No directional frames or canvas changes are required. Keep footprint and hard-edged hit mask separate; transparent background does not automatically mean unhittable space. Other equipment armor designs are unaffected.

## 5. Anchors and shapes

- Handoff coordinates use the body's top-left origin, x right/y down, in pixels. The default unflipped cannon faces right and the lightning coil tower faces up; the proposed propeller sits left. Integration converts/mirrors into native coordinates.
- Mark the cannon pivot in both body and barrel canvases, and the muzzle position. Do not infer pivots from image centers.
- Mark the lightning terminal and external-part offsets; the proposed propeller offset is (-16,0) px. Annotate other protrusions separately.
- Sketch crew locations, walkways and intended doors/ladders. These inform canOccupy/path configuration and are not automatically derived from artwork.
- Prefer rectangular footprints initially. Lighting must not change structure. If irregular shapes are desired, supply a separate hard-edged opaque/transparent mask sketch. Irregular-shape rules remain unconfirmed.

## 6. UI art

- Electricity resource symbol: one proposed 16×16 image.
- Weapon/propulsion overload: 16×16 each, default and active variants, four images total; no baked text. UI handles disabled/hover backgrounds.
- Native command icons provide the 16×16 precedent; new button layouts remain unvalidated.
- Draw the segmented electricity bar, hatching and battery subtrack procedurally. Try deriving build-list thumbnails from module art before requesting six separate large icons.
- Workshop cover, branding, technology icons and audio are later deliverables, not initial-sketch prerequisites.

## 7. Delivery sequence and names

First: six body silhouette/layout sketches at the confirmed source sizes, crew/anchor annotations and one combined comparison preview. Footprints are settled; this stage checks readability, part alignment and layout before detailed art, states and animation.

Second: normal/damaged structural layers, essential moving parts, working-state layers and five UI icons. Third: supplement lighting, fragments, full wreckage and polish after real loading. Unloaded PNGs are not accepted runtime assets.

Suggested handoff paths, not mandatory runtime paths:

```text
art-source/<device>/source.aseprite
art-export/<device>/body.png
art-export/<device>/body_damaged.png
art-export/<device>/moving_00.png
art-export/<device>/state_rated.png
art-export/<device>/state_derated.png
art-export/<device>/state_overload.png
art-export/<device>/anchors.txt
art-export/ui/electricity.png
art-export/ui/weapon_overload_off.png
art-export/ui/weapon_overload_on.png
art-export/ui/propulsion_overload_off.png
art-export/ui/propulsion_overload_on.png
```

Device names: generator, battery, propulsion, lift, cannon, lightning. Omit irrelevant parts; generators/batteries do not need every consumer state. Record authorship, sources and permissions and preserve editable files. No native artwork has been copied or redistributed for this brief.

## 8. Local evidence

Decompiled 1.2.15.2 snapshot: `06-游戏反编译/steam-root-bc8de3d71e07-55f97106b4bd/sources`, outside this repository:

- asplit-A Appearance.java near 1440 and 1488: cell×16 frames and JSON intervals.
- asplit-A Loadable.java:435: pixel-based img.
- asplit-A ModuleType.java near 484: full-cell default and separately sampled mask; external/flip paths.
- asplit-B TileMask.java near 122: nonzero alpha solidity.
- asplit-B WeaponAppearance.java near 61: barrel/projectile images, animations and offsets.
- asplit-B SpritesheetBundle.java near 68: bump/damage/fragments; SpriteUtils.java:33: PNG loading.
- asplit-A CommandButtonsPanel.java:64 onward: 16×16 command icons.

Local 1.2.15.3 data examples: CANNON 2×1; PROPELLER 2×2 with four frames and external propeller; SUSPENDIUM_CHAMBER 3×2 with six frames; SUSPENDIUM_RAY 4×4 with separate mask. data/SpritesheetBundle/modules.json declares companion sheets. These are native references, not final Electric Age sizes.
