# Electric Age dev.4 playtest

[中文](PLAYTEST.zh-CN.md). Start with single-player custom combat. Baseline: game 1.2.15.3 and JDK 21. Framework versions and checks: [TESTING](../TESTING.md).

dev.4 was compiled and packaged without running tests. Start with the [patch-specific manual checks](COMBAT_FIX.md). Text overload toggles are in the bottom ship panel for normal selection and direct control.

## Install and construct

1. Close the test instance. Place `Electric-Age-0.1.0-dev.4.jar` in `mods` beside Acbric's `Setup.cmd`, removing older same-ID JARs when upgrading.
2. Start Acbric, enable the Java MOD and its companion resources in the native MOD list; restart/reload as prompted. Initial texture generation can take time.
3. Use the standalone editor or an all-tech custom battle. Six names begin with Electric Age and use the native Machining reward requirement.
4. Provide native coal/ammunition logistics and operating crew, plus control, quarters and access paths. Lightning is a top-deck weapon without armor coverage.

Use a separate test instance and ships. The optional `Electric Age Test Ship.json` is an integration fixture with access/supply warnings, intended for inspection and adjustment, not a balanced or flight-optimized ship pack. Building does not install into player directories.

## Behaviors

| Scenario | Expected result |
| --- | --- |
| Normal | Generation 220 kW; propulsion/lift/cannon/lightning draw 40/60/60/40 kW, total 200. Idle weapons draw power; idle generators burn coal |
| Battery | 2000 kJ, charge limit 50 kW, discharge limit 200 kW. Rated operation precedes restart and overload; no simultaneous charge/discharge |
| Shortage | Battery support, then derating and sequential shutdown by minimum/rated ratio. Restart needs full rated surplus |
| Overload | Both groups may be enabled; switching either way spends a native command. Requests survive shortages and retry when power returns |
| Wear | Actual above-rated operation loses 1% maximum HP/second directly from the module. Native fire/detonation and repair apply |
| Landing/split/capture | Landing disables propulsion/lift demand; fragments retain their charge and inherit requests; capture clears requests |
| Lightning | Range 320, high accuracy, no ammo, base reload 4 seconds, first point-blast damage 100. Instant branches target nearby surface modules |

The native resource area shows yellow actual draw, red online rated deficit, gray hatch eligible offline demand, and green unused available supply. A lower battery track shows charge and flow. Device art reflects operating/damage state. Builder rows show rated draw/generation, battery capacity with no-generation duration, and weapon/propulsion/all overload demand.

Full campaign consecutive battles, supply routes, autoresolve writeback, actual networking and extended manual combat remain unvalidated. Native serialization/resupply entry-point tests do not establish complete campaign acceptance.

Include versions, layout, steps and instance logs in feedback; check reload, movement/lift, state art and the power bar first.
