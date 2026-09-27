# dev.4: deployment lift, overload controls, lightning range and thunder

2026-09-27. [中文](COMBAT_FIX.zh-CN.md). Per user request this patch was compiled and packaged only. No tests or game launches were run; behavior below is implementation intent pending playtesting.

- Deployment-only rated electric lift preview, shared by the height clamp and guide. No grid advancement, fuel/charge consumption or overload wear. Native staffing/damage eligibility remains; native-only ships retain their behavior. Actual combat power still controls lift. Deployment preview does not guarantee enough sustained power after combat starts.
- Text overload toggles move from the top global bar into the bottom ship panels, with On/Off/Mixed status. Normal selection supports multiple ships; direct control targets the controlled ship and reserves height for the power bar and controls. Missing device groups and unavailable commands disable the relevant toggle. Native command spending and request semantics remain. Normal controls appear in navigation/ordinary selection mode.
- Only Electric Age lightning's 360-degree outer-circle Y coordinate is corrected to follow its muzzle, camera and zoom. Actual first-hit range remains 320.
- Experimental firing audio references native thunder5 with layer volume 0.25 through the successful-fire sound path. Branches add no separate thunder playback. No native sound files are copied or changed. The roughly 1.49-second sound needs single/multiple-tower auditioning; settings are in content/devices.json.

Exit the game and replace the older same-ID JAR in mods beside Setup.cmd with Electric-Age-0.1.0-dev.4.jar. Do not keep duplicate versions. No separate graphics/audio install is needed. Acbric preserves user-modified companion files; manually changed extracted definitions may require resolving an update conflict before the new sound configuration takes effect.

Manual checks: electric-only and mixed-lift deployment versus guide; actual lift loss without power; bottom toggles in normal/direct control, spending/cooldown/mixed selection; lightning circle under camera pan/zoom/ship flip; single and simultaneous firing sound volume/tail.

JDK 21 `gradlew.bat jar --no-daemon` succeeded. No build/check/coreTest, package-check scripts, runtime or GPU probes were executed. Test version paths and the obsolete top-bar assertion were updated but not run. No player installation, Acbric/ARC changes, commit or push. Earlier validation results do not validate these changes.
