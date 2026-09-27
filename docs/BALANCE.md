# Electric Age balance register v1.11

Date: 2026-09-27. Central maintenance entry for gameplay numbers; this English mirror is updated with the Chinese register, not tuned independently. [中文](BALANCE.zh-CN.md) · [Mechanics](GDD.md) · [Art handoff](ART_SPEC.md)

**Confirmed means a first-release design baseline, not implemented or playtested balance.** TBD is never zero, infinity or an implicit default. Calculate derived values instead of maintaining them separately. Parameter IDs are document identifiers, not established API/configuration fields.

This register owns gameplay values; the GDD owns mechanics. Art canvases, frames and anchors remain in the art brief. Its footprint/crew entries are synchronized handoff copies: changes require updating the brief and informing asset authors. Historical research, native reference values and GDD examples are not MOD configuration.

## 1. Footprints and operating stations

All confirmed; dimensions are width×height. Operators exclude hauling, repairs and firefighting.

| Device ID | Equipment | Cells | Body pixels (derived) | Direct operators |
| --- | --- | --- | --- | --- |
| generator | Coal generator | 3×2 | 48×32 | 2 |
| battery | Battery | 2×2 | 32×32 | 0 |
| propulsion | Electric propulsion | 2×2 | 32×32 | 1 |
| lift | Electric lift | 3×2 | 48×32 | 1 |
| cannon | Electrical cannon | 3×2 | 48×32 | 2 |
| lightning | Chain lightning | 2×3 | 32×48 | 1 |

16 pixels/cell is a verified native technical scale, not a balance knob. External parts and UI dimensions remain in the art brief.

## 2. Consumer and overload baseline

These ratios, benefits and wear rates are confirmed this turn. Absolute rated power is confirmed as the initial test baseline in section 6.

| Device | minPowerRatio | overloadPowerRatio | maxOverloadBonus | Affected capability | wearFractionPerSecond |
| --- | ---: | ---: | ---: | --- | ---: |
| cannon | 0.60 | 1.50 | 0.50 | Reload/fire speed | 0.01 |
| lightning | 0.60 | 1.50 | 0.50 | Reload/fire speed, unchanged E0 | 0.01 |
| propulsion | 0.40 | 1.50 | 0.50 | Thrust, not travel speed | 0.01 |
| lift | 0.25 | 1.50 | 0.30 | Lift | 0.01 |

- P_min = P_rated × minPowerRatio; P_overload = P_rated × overloadPowerRatio. 1.50 means total demand is 150% rated, not an additional 150%.
- B_max = maxOverloadBonus; full-overload capability multiplier is 1+B_max.
- Wear = maximum module HP × wearFractionPerSecond × simulation seconds of actual overload. It does not scale with completion fraction and does not apply merely because overload is requested. Native maximum-HP modifier handling needs integration checks.
- 0.01 means 1% maximum HP per second. Preserve fractional wear accumulation; implementation is unvalidated.
- Generators/batteries do not join either overload group; consumer parameters are inapplicable rather than zero-power loads.

## 3. Shared coefficients and settled constants

| Parameter ID | Value | Meaning | Status |
| --- | ---: | --- | --- |
| overloadCurveLinear | 0.80 | Linear curve coefficient | Confirmed |
| overloadCurveQuadratic | 0.20 | Quadratic curve coefficient | Confirmed |
| restartPowerRatio | 1.00 | Full rated-power restart | Confirmed |
| batteryChargeEfficiency | 1.00 | Charging efficiency | Confirmed |
| batteryDischargeEfficiency | 1.00 | Discharging efficiency | Confirmed |
| batteryIdleLoss | 0 | Idle leakage | Confirmed |
| standaloneInitialChargeRatio | 1.00 | New standalone battle initial charge | Confirmed |
| resupplyChargeRatio | 1.00 | Charge target after actual resupply | Confirmed |
| extraElectricalDebitPerShot | 0 | No additional per-shot electrical debit | Confirmed |
| overloadOnCommandCost | 1 | One native command per valid activation | Confirmed |
| overloadOffCommandCost | 1 | One native command per valid deactivation | Confirmed |

Curve: `M(u) = 1 + B_max × (overloadCurveLinear × u + overloadCurveQuadratic × u²)`, using the shared completion fraction u in [0,1]. No independent cooldown; retain native command recovery. Invalid/duplicate and multi-selection charging details remain open.

Derived examples, not extra parameters: B_max=0.50 and u=0.5 yield M=1.225; full overload yields 1.5. Reload speed×1.5 means reload time÷1.5, not halved time. Twenty seconds of actual overload costs 20% maximum HP before repairs/other damage; native low-HP detonation may happen earlier, so this is not a guaranteed safe duration.

## 4. Chain-lightning parameters

| Parameter ID | Value | Meaning | Status |
| --- | --- | --- | --- |
| initialAttackEnergy | See section 12 | E0: root strength and propagation budget | Confirmed |
| hitEnergyLoss | See section 12 | Fixed post-hit loss C | Confirmed |
| maxChildren | See section 12 | B: 1 serial, larger permits branching | Confirmed |
| jumpRadius | See section 12 | Current hit to next target | Confirmed |
| initialRange | See section 12 | Weapon to initial attack point | Confirmed |
| inaccuracy | See section 12 | Extremely high accuracy with native logic | Confirmed |
| energyToBaseBlast | 1.00 | Base blast conversion, not post-armor HP loss | Confirmed |
| distanceEnergyLoss | 0 | No distance surcharge | Confirmed |
| branchingSurcharge | 0 | No extra branching cost | Confirmed |
| extraGenerationDamageMultiplier | 1.00 | No extra generational decay | Confirmed |
| directHitsPerModulePerAttack | 1 | Shared by root and all branches | Confirmed |
| ammunitionPerAttack | 0 | Ammunition-free lightning | Confirmed |
| totalNodeCap | TBD | Technical safeguard, performance validation needed | TBD |
| depthCap | TBD | Technical safeguard, performance validation needed | TBD |
| boltVisualDuration | TBD | Visual duration only, no extra hits | TBD |

No additional full-chain distance cap relative to the firing weapon; radius, energy, deduplication and technical safeguards still apply. Damage rounding remains open. The former GDD example E0=100 and C=20 is now explicitly adopted in section 12, which is authoritative.

## 5. Absolute values still open

### Confirmed battery balance target

Batteries serve short bursts and emergencies. Ordinary installations should not easily cover most short battles and eliminate generators; minutes of independent operation are not the default capacity target. Section 6 capacity, charge/discharge limits and reference-load durations are confirmed for initial testing. Mass and cost are confirmed in section 7; endurance cannot be promised independently of load.

Propose calibrating against a defined reference ship and battery count, not claiming one battery lasts a fixed time on every ship. With generation lost and rated demand N, full-charge endurance is E/N only if discharge power can sustain N. With generation G below demand D, endurance is E/(D-G). Insufficient discharge power requires the agreed derating/shedding behavior rather than a full-power endurance claim.

Short generation-free endurance does not require equally short overload support: generators still supply the base load and batteries cover the deficit. Limited capacity may be paired with high short-term output, with the confirmed initial output limit in section 6. Evaluate battery-only ships through capacity density, mass and cost rather than introducing mandatory generators, stacking caps, leakage or timed battery failure.

### Unassigned parameters

| Device | TBD values |
| --- | --- |
| generator | Coal use confirmed in section 10; hauling validation and playtest tuning |
| battery | Listed parameters confirmed; subsequent playtest tuning |
| propulsion | Output confirmed in section 9; subsequent playtest tuning |
| lift | Output confirmed in section 9; subsequent playtest tuning |
| cannon | Attack baseline confirmed in section 11; recoil, muzzle and presentation integration details remain open |
| lightning | Attack baseline in section 12; technical caps, rounding and visual duration remain open |

Power/energy use the confirmed section 6 units. Calculations must obey energy = power × simulation time without mixing milliseconds and seconds. Working coal generators consume at the agreed native cadence even without electrical load; the numerical cadence is confirmed in section 10.

## 6. Initial supply/demand test baseline (confirmed)

The user confirms these values for initial testing, with later playtest-driven adjustment. Use kW for power and kJ for stored energy: 1 kW over one simulation second consumes 1 kJ. These are game units, not real-world engineering calibration.

| Device | Rated power/output | Minimum power (derived) | Full-overload demand (derived) |
| --- | ---: | ---: | ---: |
| generator | 220 kW output | N/A | N/A |
| cannon | 60 kW load | 36 kW | 90 kW |
| lightning | 40 kW load | 24 kW | 60 kW |
| propulsion | 40 kW load | 16 kW | 60 kW |
| lift | 60 kW load | 15 kW | 90 kW |

Battery baseline: 2000 kJ capacity, 200 kW maximum discharge, 50 kW maximum charge. Batteries have no rated operating load. Lightning's lower initial demand does not settle its damage, efficiency or overall strength relative to the cannon; whole-ship balance remains necessary.

Reference load: one of each of the six equipment types, with necessary native hull/facilities. This is a power-budget fixture, not a validated flying/armored ship; device mass is confirmed, while rated lift and thrust remain open. Total rated load is 200 kW, with 100 kW each for weapons and propulsion/lift. Healthy generation leaves 20 kW surplus.

| Scenario | Demand | Battery flow | Theoretical duration |
| --- | ---: | ---: | --- |
| Rated operation, healthy generation | 200 kW | Charge 20 kW | Empty to full in 100 s |
| Weapon full overload only, healthy generation | 250 kW | Discharge 30 kW | About 66.7 s from full |
| Propulsion full overload only, healthy generation | 250 kW | Discharge 30 kW | About 66.7 s from full |
| Both groups fully overloaded, healthy generation | 300 kW | Discharge 80 kW | 25 s from full |
| No generation, overload off | 200 kW | Discharge 200 kW | 10 s from full |

These are energy-budget durations assuming unchanged equipment/crew/fuel eligibility, initial full charge and sufficient discharge limits. They exclude repairs, incoming damage, overload wear causing failure/detonation and changing movement demands. A group's available energy does not guarantee safe continuous overload for that long. Empty batteries follow existing allocation rules; remaining generator surplus may still support partial overload without clearing requests.

The 50 kW charge limit does not guarantee surplus; this reference load charges at only 20 kW. Extra batteries extend endurance and require later mass/space/cost evaluation; capacity alone does not prove battery-only ships cannot dominate. The supply/demand target is confirmed; HP/economic values are confirmed; hazard thresholds are confirmed in section 8; thrust/lift are confirmed in section 9; coal use is confirmed in section 10; weapon attack parameters follow.

## 7. Durability and economic test baseline (confirmed)

The user confirms these values for initial testing. HP, mass and cost use native field conventions; do not label mass as real-world tonnes. Values are base values before technology/faction modifiers.

| Device | Maximum HP | Mass | Cost | Rationale |
| --- | ---: | ---: | ---: | --- |
| generator | 240 | 180 | 180 | Heavy central supply equipment worth protecting |
| battery | 160 | 120 | 120 | Limited capacity with space/mass/cost tradeoffs when stacked |
| propulsion | 160 | 60 | 100 | Lighter end device, with supporting power infrastructure counted separately |
| lift | 200 | 100 | 220 | Slightly lighter, more expensive body; rated lift is listed in section 9 |
| cannon | 180 | 110 | 180 | Larger footprint than the small native cannon; offensive parameters are listed in section 11 |
| lightning | 160 | 100 | 200 | Ammunition-free deck coil tower without hull-armor coverage, with limited durability |

Local native 1.2.15.3 JSON references, not MOD configuration: PROPELLER HP/mass/cost = 160/80/60; SUSPENDIUM_CHAMBER = 200/120/180; base CANNON = 100/70/75 with a smaller footprint than the new cannon; SUSPENDIUM_RAY = 480/300/400. Compare complete supply infrastructure rather than claiming lighter ships from end-device mass alone.

Under this confirmed baseline, the six-device fixture totals mass 670 and cost 1000 before armor, hull, command/crew facilities, coal/ammunition stores and other equipment. Flight and maneuverability still need complete-ship validation.

Stacking example: a fixed 200 kW rated load operating without generation for 100 seconds needs 20000 kJ, or ten full batteries. They occupy 40 cells, with mass 1200 and cost 1200. This is an energy/cost example, not an optimized complete ship: added battery mass may require extra lift and power, and battle duration/other ship types change tradeoffs. No hard stacking cap is added.

Ignition/detonation thresholds and explosion damage are confirmed in section 8 and are not inferred automatically from HP. Derived wear per second is propulsion 1.6 HP, lift 2 HP, cannon 1.8 HP and lightning 1.6 HP before maximum-HP modifiers. Higher HP alone does not extend relative lifetime under maximum-HP-percentage wear.

## 8. Ignition and detonation test baseline (confirmed)

The user accepts this initial-test baseline on the basis of its native-data references. Configure native hazard attributes only, without new heat or charge-dependent explosion systems. These are base HP/field values; percentages explain them, while native logic scales thresholds with maximum HP.

| Device | fireHP (% maximum HP) | explodeHP (% maximum HP) | Base explodeDmg |
| --- | ---: | ---: | ---: |
| generator | 144 (60%) | 60 (25%) | 80 |
| battery | 96 (60%) | 48 (30%) | 50 |
| propulsion | 80 (50%) | 0 (disable this low-HP detonation) | 0 |
| lift | 120 (60%) | 70 (35%) | 70 |
| cannon | 108 (60%) | 54 (30%) | 50 |
| lightning | 80 (50%) | 40 (25%) | 40 |

- fireHP is a base threshold in native hit-ignition checks, not automatic ignition upon crossing it. Attack/faction modifiers affect the check. Direct overload wear does not itself invoke Tile's hit-ignition branch, but reduced HP makes later hits more dangerous.
- Native tick probabilistically starts a detonation fuse when conditions include positive HP strictly below adjusted explodeHP and positive effective explosion damage. Crossing the threshold is not a guaranteed explosion. Pure overload wear can enter this range under the settled rules.
- Base explosion damage is not fixed HP loss to every nearby module. Retain native derived radius `floor(30 + sqrt(explodeDmg × 2.5))` (zero damage gives zero radius) and default 1500 ms fuse, without custom radius/probability. Static evidence is the 1.2.15.2 ModuleType constructor; runtime integration needs verification.
- Native Module.explodeDmg scales effective damage by remaining clip/ammunition stores, so the cannon's table value is not guaranteed at all times. Do not add coal/charge-dependent explosion scaling for generators, batteries or lightning. Check actual clip/resource configuration without bypassing native ammunition modifiers.
- Propulsion can ignite on hits or fail through overload wear but has no configured low-HP detonation; it remains vulnerable to other explosions. Lift retains crystal hazards and the generator is a stronger concentrated risk source.

Local native 1.2.15.3 references: PROPELLER fireHP=110 with no configured explodeDmg; SUSPENDIUM_CHAMBER fireHP=120, explodeHP=90, explodeDmg=70; base CANNON 60/30/35. These references guide the baseline without changing native equipment. Propulsion lowers the ignition threshold from about 68.75% to 50% and retains no configured self-detonation. Lift retains the 60% ignition threshold and explosion damage 70, lowering its detonation threshold from 45% to 35%. The new cannon retains the native 60%/30% threshold ratios and raises base explosion damage from 35 to 50. Generators, batteries and chain lightning have no directly equivalent native electrical equipment; their values are design choices informed by native hazard magnitudes, not copied native equivalents.

## 9. Propulsion and lift output test baseline (confirmed)

Use native equipment of the same footprint as the initial scale. These are base values before technology, faction, captain, medal or native temporary-ability modifiers. Propulsion uses the native propulsion field convention, not speed or physical force units. The user confirms these rated outputs for initial testing; power thresholds and overload multipliers remain confirmed rules.

| Device | Native same-size base output | Electrical rated output | Full-overload output (derived) | Relative to native: rated / full overload |
| --- | ---: | ---: | ---: | --- |
| propulsion (2×2) | 0.10 | 0.15 | 0.225 | 1.50 / 2.25 times |
| lift (3×2) | 2000 | 3000 | 3900 | 1.50 / 1.95 times |

Between minimum and rated supply, apply the confirmed linear P/Pr derating: at 50% rated power, outputs are 0.075 and 1500. An already-online propulsor at exactly its 16 kW minimum produces 0.06; an already-online lift device at exactly its 15 kW minimum produces 750. Below the respective threshold, shut down; offline devices still require full rated power to restart. An online lift device does not guarantee the ship stays airborne; compare total effective lift and weight.

Begin with 50% more rated output per footprint to compensate for central supply infrastructure, space, cost and failure risk. Full overload applies the settled +50% propulsion and +30% lift benefits with wear. A propulsion multiplier is not a speed multiplier: native movement, weight and other modifiers still apply. Do not promise 50% faster travel or climb.

Infrastructure example: two propulsors and two lift devices demand 200 kW at rated operation. One eligible 220 kW generator supplies them with 20 kW spare. Including the generator, electrical module mass/cost totals 500/820 versus 400/480 for two native propellers and two native suspendium chambers. Electrical base propulsion is 0.30 versus 0.20; lift is 6000 versus 4000. Adding one battery increases electrical mass/cost to 620/940. Both exclude coal stores, hull, armor, crew facilities and other equipment; this is neither a complete ship nor a fuel-efficiency comparison. Sharing the generator with weapons requires recalculating total demand.

Sources: local native 1.2.15.3 ModuleType/PROPELLER.json (propulsion.base=0.1) and SUSPENDIUM_CHAMBER.json (lift.base=2000). Native static movement paths also apply captain, medal and temporary-ability modifiers. Apply the electrical multiplier only once; integration and complete-ship behavior require implementation validation. This section does not decide which native technology bonuses new equipment inherits.

## 10. Generator coal-use test baseline (confirmed)

| Parameter | Confirmed value | Meaning |
| --- | ---: | --- |
| generatorCoalPerDelivery | 1 native coal unit | Native pickup/delivery, no new fuel resource |
| generatorCoalReloadMs | 10000 ms | One coal unit adds 10 simulation seconds of fuel time, using native coalReload semantics |
| generatorIntegratedCoalCapacity | 0 | No integrated coal store; retain native fuel timer and advance deliveries |

Generation remains the confirmed 220 kW. Demand does not change the coal interval; working while electrically idle still consumes fuel time. Exhausted fuel without delivery stops generation, with batteries covering deficits under existing rules. Overload toggles do not change generator output or coal interval; surplus and batteries cover increased demand. The two confirmed operating stations do not eliminate additional hauling needs.

Native references: 1.2.15.3 PROPELLER coalReload=30000 and SUSPENDIUM_CHAMBER=20000, giving long-run continuous-working averages of 2 and 3 coal units/minute. The generator uses 6/minute. The two-propulsor/two-lift example in section 9 uses 10/minute with native devices versus 6/minute with one electrical generator, while providing 50% greater base output. This assumes no extra modifiers, continuous work and timely deliveries; it does not guarantee 40% savings for every ship. Idle generation, other loads and native shutdown time affect comparisons.

Native SMALL_COAL_STORE has base capacity 12, equivalent to 120 seconds of additional fuel budget for one continuously working generator. Exclude initial/already-loaded fuel, coal in transit, other consumers, technology modifiers and delivery interruptions; this is not an exact countdown from battle start.

Static evidence: 1.2.15.2 Module.takeResource removes one unit. Crewman's hauling path picks up the resource and calls Module.giveResource, whose COAL branch adds coalReload to msUntilCoal. CoalJob permits advance delivery when the timer is below one interval, so inventory deductions are not fixed events exactly ten seconds apart. Preserve native initial-fuel and timer persistence semantics; do not add a second per-second ship-wide coal debit or additional free fuel. Actual 1.2.15.3 integration must verify timer advancement, generator eligibility, hauling jobs and recovery after fuel starvation.

## 11. Electrical cannon attack test baseline (confirmed)

The user confirms this section as an initial-test baseline. A rapid-fire electrical armor-piercing cannon using native ordinary projectiles. Values are base parameters before technology, faction, personnel, captain and medal modifiers. The native reference is the base 1.2.15.3 CANNON, not its deck variant.

| Parameter | Native small cannon reference | Confirmed electrical cannon |
| --- | ---: | ---: |
| Footprint (already confirmed; comparison only) | 2×1 | 3×2 |
| penDmg | 40 | 80 |
| blastDmg / extra area damage | No extra blast-area attack | 0 / no added area damage |
| reload | 3100 ms | 2000 ms |
| clip | 2 shots | 2 shots |
| ammoPerClip | 1 native ammunition unit | 2 native ammunition units |
| clipReloadTime | Default 0 | 0 (actual crew delivery still required) |
| numShots | Default 1 | 1 |
| shotSpeed | 1.2 | 1.8 |
| inaccuracy | 0.0014 | 0.0010 |
| optimumRange | 400 | 600 |
| fireArc | forwards / 70° | forwards / 70° |

Projectile speed, scatter and distance use native units, not real-world units. Lower inaccuracy reduces scatter without guaranteeing hit rates at all distances. optimumRange is the native preferred combat-distance parameter, not a hard maximum range of 600. Add no extra cannon range cutoff; native ballistics and targeting still limit attacks. Recoil/muzzle details, projectile visuals, sound integration and native technology inheritance remain separate decisions.

Confirmed electrical ratings remain 60 kW rated, 36 kW minimum and 90 kW full overload. Full overload multiplies reload speed by 1.5 for a base interval around 1.333 seconds; damage remains 80. At minimum online power, speed is 0.6 of rated for a base interval around 3.333 seconds. No target, hold fire or waiting for ammunition still consumes online power. No extra per-shot electricity debit. Offline devices still need full rated supply to restart.

With sufficient operators/ammunition, continuous targets and no extra modifiers, nominal rates are 30 shots/minute rated and 45 fully overloaded, with long-run ammunition demands of 30/45 native units per minute. clip=2 and ammoPerClip=2 add two shots once two native ammunition units arrive; attacks do not directly debit stores. Preserve native hauling and reload preparation rather than imposing a custom strict two-shot capacity. Delivery waits, native preloaded ammunition and reload-work modifiers can change actual cadence.

Nominal damage/interval comparison: native small cannon about 12.9; electrical rated 40 and overloaded 60. These are not post-armor combat DPS. Three small cannons occupy the same six cells and total about 38.7 nominally: the electrical cannon does not gain several times the rated damage per cell. Its stronger individual shots, projectile speed and overload potential are the main differences. Native HV_CANNON is 4×2 with penDmg=100 and reload=5400 ms: the electrical cannon remains weaker per shot than that heavy cannon but fires much faster. Cell counts do not guarantee equal exposed frontage, firing arcs or placement feasibility; electrical infrastructure must also be counted.

References: native 1.2.15.3 CANNON.json and HV_CANNON.json; 1.2.15.2 ModuleType defaults ammoPerClip to 1 and clipReloadTime to 0; Module.giveResource adds clip shots after all ammunition slots are filled. Verify actual 1.2.15.3 configuration and the two operating stations' reload-work interaction during implementation; base reload is not a fixed interval under every crew condition.

## 12. Chain-lightning attack test baseline (confirmed, omnidirectional coil tower)

A short-range, highly accurate, ammunition-free weapon covering multiple surface targets. The user accepts these values and allows an upward or omnidirectional arc; use the allowed omnidirectional option. Values exclude extra technology/personnel modifiers. Section 4 definitions reference this baseline.

| Parameter | Confirmed value | Meaning |
| --- | ---: | --- |
| initialAttackEnergy | 100 | Root nominal single-point blast damage 100 with the confirmed 1:1 mapping |
| hitEnergyLoss | 20 | Apply the node's damage before deducting propagation loss |
| maxChildren | 2 | At most two children per node, subject to energy and valid candidates |
| jumpRadius | 96 native world-distance units | About six module-cell widths; current hit point to child hit point |
| initialRange | 320 native world-distance units | About twenty cells; hard initial range, without a full-chain distance cap relative to the emitter |
| inaccuracy | 0.0001 | Native small cannon is 0.0014; scatter parameter, not a direct hit-rate multiplier |
| reload | 4000 ms | Base reload cycle for one complete chain |
| fireArc | up / 360° | Omnidirectional initial targeting; up matches the upright coil appearance. Subsequent targets remain constrained by jump radius |
| clip / ammoPerClip | 0 / 0 | Native ammunition-free reload direction, requiring integration validation |
| numShots | 1 | One chain context per shot, not a new firing event for each child |

Attack energy is not ship battery kJ and is not debited per shot. Retain confirmed online ratings of 40 kW rated, 24 kW minimum and 60 kW full overload. Overload only multiplies reload speed by 1.5, giving a base cycle around 2.667 seconds without changing E0/C/B or jump radius. At minimum online power the base cycle is around 6.667 seconds. Staffing and native states still affect actual cadence.

| Example layout, assuming a root hit | Node energy = pre-armor point damage | Directly hit modules | Total nominal damage |
| --- | --- | ---: | ---: |
| No valid subsequent targets nearby | 100 | 1 | 100 |
| Root forks twice, then each child continues once | 100 → (40,40) → (20,20) | 5 | 220 |
| Exactly one valid subsequent target at every hop | 100 → 80 → 60 → 40 → 20 | 5 | 300 |

These are feasible shapes, not guaranteed trees for dense targets. Shared deduplication, disappearing targets, layout and random candidate selection may truncate the chain. With this E0/C and no additional energy modifiers, at most five different modules receive direct hits. This is derived from the confirmed energy rules, not a new independent five-target configuration. Technical node/depth caps, stable remainder allocation and visual duration remain open.

At the rated four-second cycle, the examples yield nominal combined damage/second of 25, 55 and 75. These are pre-armor totals across targets, not sustained damage to one module or directly comparable to cannon penetration damage. Native blast protection applies to every hit tile; weaker later nodes may cause almost no HP loss, while valid contact can still propagate under the confirmed rules. No armor bypass, extra area damage or repeated direct hits on one module.

Rationale: native equipment has no equivalent chain attack; E0/C, branching and ranges are original test choices. The earlier GDD's 100/20 was illustrative; this section now explicitly confirms those values for the first configuration. Native SUSPENDIUM_RAY also has reload=4000, but its numShots=12 and penDmg=15 represent a different attack and cannot be compared directly with whole-chain damage. Its inaccuracy=0.0004 is larger than this baseline. Unlike the electrical cannon's preferred combat distance of 600, lightning's 320 is a hard initial-attack range. Playtest engagement opportunities, armor, dense small modules, sparse large modules and cross-ship chains; arithmetic alone does not establish balance.

## 13. Maintenance

- Update this register and its mirror first, recording reasons and status; do not claim implementation when editing only documentation.
- Map implemented configuration/code fields to these parameter IDs later so panels and simulation cannot drift.
- Update the art brief when footprints, operating stations or image dimensions change; other balance changes normally do not require new textures.
- Confirmed baselines may be revised after testing, but new candidates must first be marked as proposals rather than silently replacing confirmed values.

v1.0 consolidated specifications/overload values; v1.1 confirmed the battery role; v1.2 proposed A; v1.3 confirmed supply/demand A and proposed B; v1.4 confirms durability/economic B and proposes unconfirmed hazard scheme C. v1.5 confirms hazard baseline C and clarifies native ratios and original design choices. v1.6 proposes unconfirmed output scheme D with same-size native references and an infrastructure example. v1.7 confirms output D and proposes unconfirmed generator coal scheme E. v1.8 confirms generator coal use E and proposes unconfirmed cannon scheme F. v1.9 confirms electrical cannon F and proposes unconfirmed chain-lightning scheme G. v1.10 confirms lightning G with an omnidirectional arc. Native GUIDED_MISSILE.json provides an up/360 example; attack obstruction integration still requires verification. v1.11 confirms the unarmored deck coil tower while retaining existing HP and attack values. Arithmetic and native-data comparisons are not playtest acceptance.

## dev.4 audio trial (2026-09-27)

Lightning fire references native `thunder5` (about 1.49 s), layer volume coefficient 0.25, once per root shot with no extra branch playback. Source: lightning.fireSound in `content/devices.json`. Native weather uses 2.5; the coefficient ratio does not represent perceived loudness. Not auditioned. Damage, range, reload and electrical balance are unchanged.
