# dev.3 rendering companion fix

2026-09-27. [中文](RENDER_FIX.zh-CN.md)

User comparisons exposed a dev.2 omission: bright interiors, flat armor without seams/bevels, and MOD warnings that `ea_devices` and `ea_propeller` lacked damaged/fragments sheets. Earlier GPU checks covered interiors and general rendering but missed armor coverage and native MOD warning inspection.

## Cause

dev.2 bundles declared only `name`, without `bump` or `fragments`. Delivered damaged color art was already used in the state atlas, but cannot substitute for native bump/fragment metadata or generated damage sheets and fragment mappings.

Native `Appearance.lockMaskedBevelledShader` disables lighting when either the armor bundle or its module-mask bundle lacks a bump texture. Missing module support therefore also removes native armor seams, bevels and lighting. No additional framework or MOD dependency is missing on the player's system.

`SpritesheetBundle` creates damaged/fragments associations only when both inputs are declared. Native generation then produces lighting variants, damage sheets, fragments and mappings.

## Fix

- Declare matching bump and fragment inputs for the device atlas, external propeller animation and cannon barrel.
- Build neutral surface/outline bump data and 8-pixel fragment regions; native code merges regions into actual debris. Transparent pixels use the native white exclusion marker to prevent generated damage filling the background.
- Retain delivered color/state/damage art and allow native destroyed rendering to use its generated variant. Source PNG/Aseprite artwork is unchanged; no native artwork is copied.
- Balance, grid algorithms, save format and commands remain unchanged. Artists may later refine these basic material maps; this fix restores the complete native render path.

## Update

Close the game and replace dev.2 with `Electric-Age-0.1.0-dev.3.jar`; do not keep both same-ID JARs. Acbric updates managed companion resources and the game regenerates derived images. Allow time on the first start. Follow Acbric's conflict handling if companion resources were manually edited instead of overwriting personal changes.

No separate art pack, complete redraw or save deletion is required. Initial logs may report not-yet-generated derived sheets before generation; acceptance requires actual loaded textures, no post-generation MOD warnings, and correct armor/destroyed rendering. See [TESTING](../TESTING.md) for measured results.
