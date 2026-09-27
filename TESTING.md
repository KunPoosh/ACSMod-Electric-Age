# Validation: 0.1.0-dev.4

On 2026-09-27 the user reported the bugs now behave correctly, recorded as user acceptance for deployment height, overload access and the range circle. No detailed scenario log or separate audio evaluation was supplied; this is not full campaign/network/all-UI acceptance. Repository preparation changes licensing, metadata and documentation only; no tests or gameplay package rebuild were run.

2026-09-27: Per user request, only JDK 21 `gradlew.bat jar --no-daemon` was executed. Compilation and resource packaging succeeded. **No core tests, package-check scripts, Knot/GPU probes or game launch were run.** At delivery, the four changes awaited user playtesting; dev.3 results are historical, not dev.4 acceptance. See [patch notes](docs/COMBAT_FIX.md). No installation, commit or push.

## History: dev.3

2026-09-27. [中文](TESTING.zh-CN.md). [Rendering fix](docs/RENDER_FIX.md).

This patch supplies bump/fragment inputs and bundle associations. Compared with dev.2, the JAR adds six companion images and changes only bundle declarations/version metadata. All product classes, balance inputs and original color images are byte-identical; see local `build/render-delta.json`.

- JDK 21 build and 6042 core assertions pass.
- 223 package checks pass, including companion declarations, matching dimensions and power-of-two images.
- Actual-game evidence: `build/game-tests/render-dev3-03/summary.json`, game 1.2.15.3 / API dev.33. Checks cover actual bump/damaged/fragment texture loading, six device fragment mappings and empty post-generation `Mod.getWarnings()`.
- Added native/electrical lift comparisons under wood/steel armor, plus destroyed-module rendering at HP=0. The probe sets `EditShipIntent.mode=ARMOUR`; changing only the placement tool does not enter the armor view.
- Earlier probe fixtures corrected a wrong steel ID and missing editor-mode/camera setup. Those intermediate runs are not the final armor visual acceptance.

Final live result: 196 assertions pass; battle/editor/armor comparison/destroyed scenes each render 30 frames and all four screenshots were inspected. No missing-sheet warnings, 0 GL errors, all 5325 source-install files unchanged.

Final JAR SHA-256: `4bdbadb98c85b1a171a01a2d88a854e08ed92d9c6a601cafe1360103632e099c`, 191231 bytes. Existing campaign/network limits remain. English UI acceptance was not repeated: text/UI code are unchanged. No player installation, source artwork or Acbric/ARC source was modified.

Historical dev.2 evidence and acceptance boundaries follow.

## Historical: 0.1.0-dev.2

2026-09-27. [中文](TESTING.zh-CN.md). This is the six-device single-player combat test build, not complete campaign/multiplayer release acceptance.

| Layer | Result and evidence |
| --- | --- |
| Build/core | JDK 21 / Gradle 8.13 build passes; 6042 `coreTest` assertions covering randomized conservation/permutation, thresholds/restarts, exact depletion, pause, fractional wear and chain budgets/visited targets |
| Artifact | `tests/verify_package.py`: 202 checks; MOD-only classes/resources, no game/framework/probe/source-art/nested dependencies, eight definitions including mirrors, Java 21 bytecode |
| Real headless Knot | `build/runtime-tests/native-delivery`: 26 checks with transformed native classes and synthetic modules, covering coal boundaries, output scaling, shortage/recovery, landing, serialization, refill and duplicate identities |
| Actual game/GPU | Chinese `build/game-tests/gpu14` and English `build/game-tests/gpu-en-final`: 178 assertions each; production definitions/art, native commands/cost, splitting/capture, repair/refill, lightning damage/branches, button callbacks and restored simulation |
| Rendering | Per language: 30 battle frames and 30 editor frames, screenshots inspected, OpenAL initialized. RTX 5060; 0 observed KHR_debug GL errors |
| Input protection | All 5325 source-install files hashed before/after each full run: no additions, removals or modifications. Networking blocked and writes confined to isolated run directories |

178 denotes assertions, including 120 per-step hashes from one paired restored-combat scenario, not 178 independent scenarios. Two same-source restores run 120×16 ms in one process, followed by electrical-state comparison. This is not a two-client network test. UI tests use actual native callbacks/components and GPU rendering; extended manual interaction has not been performed.

Final JAR: `Electric-Age-0.1.0-dev.2.jar`, 116605 bytes, SHA-256 `1c8c41e00323ba19aa836e81a7daedd1d1df14e21d3ebd6788a6c34bfa947201`.

Compilation baseline: game 1.2.15.3, API dev.32. Final runtime API: dev.33. Another task concurrently builds the framework; each probe records actual API/loader hashes, so framework inputs across probes are not assumed identical. GPU runs copy loader inputs before starting. This task does not modify framework or ARC source. Earlier dev.32 development probes do not imply all-version compatibility.

## Issues found and fixed

- Native companion images belong under `images/`; the shader's scalar texture size requires square power-of-two atlases/subtextures.
- Lightning requires a weapon appearance object; explicit `blastSplashRadius=0` prevents legacy damage division by four and aligns displayed first-hit damage with energy.
- Native non-JSON ship constructors require lazy default overload requests; fragments inherit requests at construction, before first tick/save.
- Stable duplicate-identity reconciliation preserves copied/moved module charge/state; mirrored variants now flip state artwork.
- Coal exhaustion within a simulation step splits allocation at that boundary rather than granting an extra whole step of generation.

Failed development runs remain local evidence, not passes. Earlier 445 static art assertions (45 pixel comparisons) retain their original scope and are not counted again here.

## Reproduce

Select JDK 21, run `gradlew.bat build --no-daemon`, then `python tests/verify_package.py`. Native probes require prebuilt workspace Acbric test helpers and your own game inputs:

```text
python tests/run_runtime.py --tag new-native --game <game> --framework <Acbric-source> --java-home <JDK21>
python tests/run_game.py --tag new-chi --language chi --game <game> --framework <Acbric-source> --java-home <JDK21>
python tests/run_game.py --tag new-en --language en --game <game> --framework <Acbric-source> --java-home <JDK21>
```

Use a fresh tag. GPU probes depend on ExternalPreflight, SmokeGuard and the framework's read-only snapshot helper; they do not rebuild/edit the framework. Preserve evidence before `clean`. Headless Unsafe usage constructs a particle fixture only and is absent from the product JAR.

## Remaining acceptance

Full campaign consecutive battles and actual resupply/autoresolve writeback; cross-process/machine networking and complete replay flow; large-fleet long-session performance; display scales/DPI, manual interaction and other MOD combinations. The program-built screenshot ship has access/supply warnings and is not a finished ship pack. Persistence/command integration does not establish these broader guarantees. Zero GL errors here does not resolve historical framework GL issues in other scenes.
