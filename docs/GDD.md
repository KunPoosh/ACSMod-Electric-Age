# Electric Age — GDD v0.55 discussion draft

> Implementation status (2026-09-27): dev.4 implements the six-device single-player combat test package; the user confirmed the reported bugs are resolved. This file retains the original milestones/criteria; [TESTING](../TESTING.md) defines verified scope. Full campaign and actual multiplayer acceptance remain pending.

Date: 2026-09-27. This document retains the design discussion history. Confirmed core gameplay is implemented; historical proposals and outstanding validation remain marked in the sections and TESTING.

## 1. Established direction

- Independent Acbric Java MOD: Electric Age / 电气时代.
- Real electricity quantities and constraints: generation, battery storage and rated electrical loads.
- Preserve vanilla content and behavior; add new modules and mechanics without automatically converting vanilla devices.
- One shared bus per ship, without wiring. Split hulls settle independently. Sections without generation or storage lose supply; batteries only provide their remaining energy within available discharge power.
- Continue reusing native attacks while designing the first new form, chain lightning: choose an initial aim point using native targeting, then spawn child bolts from the hit point toward random nearby modules, reusing native blast damage. Propagation is implemented according to the later sections; see TESTING for validation limits.
- Explore reusable Acbric APIs while keeping electrical gameplay separate from framework responsibilities.
- Electricity, overload, lightning and module persistence are implemented; native equipment keeps its behavior. Full campaign and multiplayer validation remain pending.

## 2. Confirmed equipment identity and player experience

Design generation, storage and load combinations, balancing sustained operation, burst output, space, weight, cost and resilience to damage.

The user confirms a high-risk, high-reward direction built on centralized supply and stronger equipment:

- Electricity is allocated directly across the ship bus. Electrical end devices do not wait for crew to carry coal to each device, reducing dependence on fuel-hauling crew, paths and delivery delays. Coal generators still require native coal delivery. Reduced end-device fuel logistics does not imply eliminating all staffing requirements or changing vanilla equipment.
- Shared generation/storage exposes multiple systems to simultaneous derating or shutdown when supply is damaged. Loss of lift, propulsion and weapons may amplify subsequent battle damage into a ship-wide disaster. These are possible consequences of the agreed grid, damage and native physics, not a new scripted chain explosion or extra ship-wide damage rule.
- Ammunition-using electrical weapons retain native crew hauling; the electricity system does not universally remove ammunition or staffing requirements. Lightning weapons are explicitly ammunition-free by default while retaining a reload cycle; this does not imply crew-free operation. Electrical weapons primarily offer stronger rated performance in exchange for electrical demand and supporting infrastructure, with overload boosting them further.
- Individual new weapons may have purpose-designed base parameters. Existing power scaling remains unchanged: first-release derating/overload affects reload and fire rates, without automatically scaling per-shot damage, projectile speed or accuracy with power. Exact advantages, mass, cost and crew values remain open.
- Balance considers whole-ship generation, storage, weapons, crew and logistics investment alongside vulnerability to power loss. Do not weaken vanilla equipment to force adoption.

Proposed loop: design the supply/demand balance, observe it in combat, respond to generator/battery damage, then revise the ship. The six-type first-release roster is confirmed in section 4. Setting, research progression and remaining equipment values remain open; basic art direction is confirmed. ProjEOL's setting and assets are not adopted automatically.

## 3. Confirmed operation and controls

### Generation, storage and continuous loads

- Generators may consume native coal or consume no resource, as defined by the module. No new fuel resource is added. Coal delivery uses native mechanics; resource-free types do not require coal.
- Generators meeting native staffing/fuel eligibility provide rated output; ineligible units provide zero. No extra HP-proportional derating is added. Working units consume coal at the native cadence even when output is unused, without load-based savings. Equipment staffing and output values remain open.
- Batteries charge and discharge automatically. They actively make up generation shortfalls to maintain rated operation, constrained by remaining energy and maximum discharge power.
- Batteries also support full-budget restarts and overload, retaining the order normal supply, restarts, uniform overload. Derating/shedding occurs when combined generation and available battery output cannot meet demand.
- Generation left after current loads charges batteries. The same output cannot serve loads and charging twice, and battery discharge is not new generation to recirculate into charging. Batteries define capacity and maximum charge/discharge power separately, with 100% efficiency in the first release. Initial charge and resupply continuity are specified below; numerical ratings remain open.
- Electrically online weapons continuously request rated power, including no-target, reloading and hold-fire periods. They do not enter low-power standby. Actual consumption under shortage equals allocated power; electrically offline weapons draw none. Overload raises demand according to its allocation rules. Rated power is normal demand, not an unconditional debit despite inadequate supply.
- No additional per-shot electrical debit, standby parameter or charging-attack mechanic is introduced at this stage. Electricity does not automatically replace native ammunition rules; weapon content remains separate. Propulsion retains online demand without movement orders, but landing-induced shutdown switches affected devices off, as specified below.

### Propulsion standby versus landing shutdown

The user confirms retaining online consumption without movement orders, while requesting shutdown of devices disabled by landing. Ordinary standby and native operational shutdown are distinct:

- Standby: eligible, electrically online propulsion devices request rated power. An enabled overload request with actual extra supply still produces boosts and self-damage.
- Landing shutdown: affected electrical lift/flight-propulsion devices leave normal-load and overload allocation, consume zero electricity and cause no overload self-damage or electrically enhanced lift/thrust. Only affected devices shut down; landing does not shut down all weapons, generation or storage. Vanilla devices retain native behavior.
- Shutdown preserves group overload requests. Restored devices use those requests and available supply to determine actual overload. Unified restart is confirmed: takeoff restores native intent and staffing eligibility, then requires full rated power, just as recovery from insufficient staffing, damage or power loss does. Avoid circular waiting between electrical supply and staffing.

Local 1.2.15.2 static evidence: CommandButtonsPanel.groundShip (around line 808) sends ground, whose Combat executor (2312) calls Airship.groundShip. Airship.groundShip (1910) sets a descent target and grounding; grounded (1948) separately checks recent ground contact and target height, rather than the button press alone. Airship.tick (2951–2964) restricts flight propulsion when grounded and sets suspendiumRunning = !grounded; Module.running (537) reads propulsion/lift flags. Module.staffJobPriority (2361 onward) reduces relevant staffing priorities to 0.001 rather than deleting jobs or immediately evicting all crew; StaffJob.active (around 2658) retains its own eligibility conditions.

Proposed adaptation follows actual landing shutdown while retaining native descent capability, rather than cutting all lift immediately on command. Zero speed, no movement order or enginesRunning=false alone must not trigger electrical shutdown; the last can also occur during ordinary standby. Ground-vehicle propulsion must not shut down merely from ground contact; mixed-role modules require separate inspection. Electrical shutdown must not permanently prevent crew recovery or takeoff requests. This is static research, without live landing/takeoff validation or new cross-version hook verification.

### Power and startup

Modules define rated power P_rated, minimum operating power P_min and overload power P_overload. Overload eligibility is separate; not every module must support it. Representation of the third value for ineligible devices remains open.

Normal operation targets rated power. A running device derates at `P_min <= P < P_rated`, including exact equality at minimum, and shuts down strictly below minimum. After shutdown for any reason, restored native eligibility must be followed by a full rated-power allocation to restart. These boundaries and the first-release performance mappings below are confirmed.

### First-release performance and future tool modes

For a device that remains operational, the derated capability multiplier is `m = P_actual / P_rated`. Minimum power determines whether it can operate; the minimum-to-rated interval is not renormalized to 0–100% performance. Below minimum, existing shedding rules apply.

| Device | Power-dependent capability | Unchanged by power in the first release |
| --- | --- | --- |
| Electrical weapon | Reload speed and fire rate | Damage per shot, projectile speed and accuracy |
| Electrical propulsion | Thrust | Weight and structural properties |
| Electrical lift | Lift | Weight and structural properties |

At 60% of rated power, provided the device remains online, reload speed, thrust or lift is 60% of normal. Reload and firing intervals scale inversely with working speed: original duration divided by m. Native integrations must avoid applying the same speed boost twice. At m=0, handle shutdown rather than dividing.

Overload retains a smooth continuous curve with a modest full-overload reward, and each module defines its maximum capability boost. It affects the same capabilities listed above. Initial coefficients and benefits are confirmed in the [balance register](BALANCE.md), with playtesting still pending.

The user may add tool modes in the future. This is an extension direction only: no mode contents, switching rules or API are defined, and it is not a first-release requirement. Current supply, startup/shutdown and overload rules remain unchanged. Tools can later define their own capability mapping rather than being forced into weapon fire-rate semantics.

### Normal shortages: proportional supply, sequential shedding and surplus restart

Confirmed direction: online modules receive an equal fraction of their rated demand. Higher minimum-to-rated ratios naturally shed first. Offline modules leave the working allocation pool and do not keep receiving an insufficient share. Restart allocation only occurs after online rated demand is satisfied and surplus covers the restarting module's full rated power.

Let S be effective supply available for normal loads and A the online set. For positive-rated consumers, `r = min(1, S / sum(P_rated_i, i in A))`, `P_i = r × P_rated_i`, and each minimum ratio is `t_i = P_min_i / P_rated_i`. Zero-rated modules are excluded from the division; generators, storage and native eligibility require separate handling.

Settlement order:

1. Calculate the common ratio r for the online set.
2. If a module falls below minimum, remove the one with the highest t_i first. Internal priority breaks ties at the same minimum ratio.
3. Update online rated demand and r after each removal, reallocating released supply until all remaining modules meet their minimum. Do not shut down all modules judged deficient against the old ratio at once.
4. Once all online rated demand is met, compute `R = max(0, S - sum(P_rated_online))`. Restart an offline module only if R covers its full rated requirement, immediately subtracting that budget before considering another.

Internal priority is reversed for shedding versus restart: higher retention priority means later shedding and earlier restart. Smaller rated demand has higher retention priority by default, with stable identity breaking exact ties; the identity key remains an implementation detail to define. Minimum ratio takes precedence during shedding. This is not an overload-priority system. The user has accepted this startup/shutdown design.

A sudden large loss of supply may remove multiple modules within one simulation step; visual sequencing must not let unpowered devices continue working for extra frames. Gradually falling supply naturally produces one-by-one shutdowns. Rendering must not mutate allocation.

Illustrative example: A rated/minimum=100/80 kW, B=200/100, C=100/30. With 400 kW total rated demand and supply dropping to 280, r=70%, so A sheds first. Remaining rated demand is 300, and reallocation gives B≈186.7 and C≈93.3 kW, both operable. At 350 kW supply they run at rated power but the 50 kW surplus cannot restart A; 400 kW can.

This intentionally creates recovery hysteresis: a group may sustain derated operation at a supply level that cannot start all members from off. Small consumers restarting first can leave large devices waiting indefinitely if surplus never covers them. This follows the chosen rule rather than being a scheduling omission. The user has accepted retaining this tradeoff without preemption, aging or manual priorities.

### Ship-level overload commands

- Add Weapon Overload and Propulsion Overload buttons. They target all eligible weapons, or eligible propulsion and lift devices, respectively.
- Overload controls reuse native ship commands and their command-point consumption, replacing the separate lock/cooldown proposal. Overload is a persistent mode without automatic expiry from a separate timer.
- Requested power rises up to P_overload. Uniform overload replaces sequential internal priorities. Partial overload provides meaningful benefits along a relatively smooth curve; full overload retains a modest reward without a large threshold jump. Initial curve and benefits are confirmed in the [balance register](BALANCE.md).
- Group mode and supplied power are distinct: enabled overload does not guarantee full power, and command readiness cannot bypass shortage shutdown. A shortage does not clear the overload request; restored supply resumes allocation against that request.
- Both enabled groups share surplus with the same overload completion fraction for every participant and no group priority. Splits inherit overload requests and capture clears them, as confirmed below. No new overload timer is introduced.

### Native command reuse and switching details

The local 1.2.15.2 decompiled snapshot confirms that `Airship.readyForCommand()` checks accumulated points, command generation and disabling conditions; `commandGiven()` clears ship command points. `commandPointsGenerated()` depends on working command modules and staffing, with demand calculated by `commandPointsRequired()`. Existing `CommandButtonsPanel` actions use `Combat.giveCommand()`, and `Combat.execCommand()` dispatches through the public `EXECS` table. Native command points regenerate; they are not a once-per-battle item.

Reuse and command costs for both activation and deactivation are confirmed; multi-selection/invalid-action behavior is also confirmed but not implemented:

- The user confirms that both activation and deactivation consume one native command; deactivation is not free. Ignore duplicate or inapplicable requests without charging.
- Add distinct command types without repurposing native fire-mode or focus commands or automatically changing those modes.
- Commands carry an explicit target state rather than toggling at execution time. Recheck readiness, target and applicability when executing, and consume the native command only for an actual change.
- Both overload groups share the ship's native command recovery with other readiness-gated actions; neither gets an independent cooldown.
- Commands change only the overload request; allocation occurs in its simulation phase. Do not mutate simulation state in rendering callbacks.
- Charging both directions means depleted command points or lost command facilities may prevent manual deactivation. Shortage shutdown still applies. This risk is retained with the user's choice; no free emergency shutdown is added automatically.

Static inspection does not establish multiplayer or replay compatibility for new commands. Routing, multi-selection, saving and restoration still require validation.

#### Multi-selection and invalid actions (confirmed)

Retain two toggle buttons without another persistent command. Confirmed behavior:

- Single selection toggles the current request. For a group's multi-selection, all off means enable all; any on, including mixed, means disable all. Determine the target state at click time and send it explicitly, rather than inverting each ship independently during execution.
- Include only ships the player may operate under native ownership/control rules. A group is applicable when corresponding overload-capable equipment exists, including power-off, standby, landing-disabled or still-repairable equipment. Actual operation eligibility does not prevent setting the request in advance. If all equipment has been removed but the request remains on, allow disabling it under the same command rules.
- Execute per ship: an actual request change on a native-command-ready ship consumes one native command. Already-matching, inapplicable, unready, uncommandable or newly invalid targets are skipped without charge. Already-off ships in a mixed selection do not pay for disable-all.
- An unready ship does not block other eligible ships. Skipped actions are not queued or automatically retried when command points recover. The user may act again. This is separate from the confirmed behavior of an existing on-request resuming actual overload when power returns.
- Both buttons share each ship's native command resource. Clicking both rapidly does not guarantee both execute. Recheck readiness in simulation command order; the first successful change may make the next unavailable. No negative command budget or free switching.
- Show request state, not actual supply: all off, all on or mixed. Use a light overlay/native mixed-state marker without requiring another art icon. Hover text states whether clicking enables or disables and may briefly indicate that only command-ready ships act. Module textures and the power bar retain actual-overload feedback.
- Disable the button when no selected ship can make the intended change. Brief hints cover missing equipment or command readiness without persistent per-ship/module explanation panels. Deactivation still costs a command; no free emergency exit.

Example: ship A has weapon overload on and B has it off. Clicking the mixed selection disables A and charges A if ready, leaving B off without charge. If A is unready, B cannot pay for it. If both begin off but only A is ready, a click enables only A and leaves a mixed state; the next click means disable all, not silently retry enabling B. The user confirms this mixed-state tradeoff.

Native reference: 1.2.15.2 CommandButtonsPanel.focusOnRepair / focusOnFirefighting skip ships already in the target state or !readyForCommand, then call giveCommand per ship. This supports per-ship skipping, not a claim that every native toggle has the proposed mixed-state semantics. New command ordering, charging and multiplayer consistency require implementation validation.

### Overload self-damage (rules and initial values confirmed)

The user confirms that only actually overloaded modules take continuous maximum-HP-based damage, bypassing external armor. This is described as a fixed percentage of maximum HP per simulation second. The initial rate is confirmed in the [balance register](BALANCE.md); no behavior is implemented or playtested.

- Proposed calculation: `damage = H_max × k × dt_seconds`. k is the fraction lost per simulation second, independent of remaining HP or rendered frames. Using the instance's effective maximum HP is confirmed; interactions with native bonuses and changing limits require verification.
- Wear rates and worked examples are in the [balance register](BALANCE.md). Without repairs or other damage, devices with different HP lose the same durability fraction.
- Confirmed trigger: the group's overload request is enabled, the module is eligible and online, and allocated power exceeds its rating. Rated operation, derating and offline states produce no new overload self-damage. Leaving overload does not heal prior damage; other native damage remains active.
- A fixed rate independent of overload progress costs the same HP fraction for partial and full overload. Full overload therefore buys more benefit per durability lost. This encourages adequate supply but may weaken low-progress overload; evaluate it together with the boost curve. Do not automatically add heat or power-proportional damage.
- The overload command is a persistent target state; the user confirms that shortages do not clear it. Allocation retains normal supply, full-budget restarts, then uniform overload. Devices approach the target using available surplus without preempting normal loads or restarts. Partial overload counts as actual overload for both benefits and damage; full overload is not required. Supply loss returns a device to rated, derated or offline operation. Restored supply automatically permits another overload attempt without a new command or command charge. Revoking the request stops these automatic attempts; splits inherit requests and capture clears them, as specified below.
- The user confirms that native repairs can offset self-damage. Sufficient repair throughput may sustain overload, turning its cost into repair supplies and crew demand. Self-damage alone does not guarantee short bursts.
- Self-damage may reach zero HP and enter native destruction handling, without a 1-HP floor or low-HP automatic exit. After reviewing native behavior, the user confirms retaining low-HP detonation: wear may enter the danger range and cause spontaneous detonation. Follow the simplified UI below, without mandatory persistent per-module damage-rate text.

Static evidence: in the local 1.2.15.2 decompilation, `Module.doDamage(int)` subtracts module hp, or holdOnHp during holdOn, without external armor processing. Module tick separately handles crossing zero HP, and REPAIR restores module HP. Prefer this damage entry point and normal update sequencing over direct hp writes. Its integer argument requires deterministic fractional damage accumulation so low-HP modules do not escape damage through per-step truncation. Pause, speed changes, persistence and holdOn need targeted verification; static inspection is not runtime acceptance of the new mechanic.

### Native ignition and low-HP detonation: retention confirmed

After learning that detonation continuously checks low HP, the user explicitly chooses to retain this mechanic. Modules keep native flammable/explosive properties; overload wear may enter the danger range and cause spontaneous detonation. The earlier damage-source isolation proposal is dropped: no outstanding wear ledger, separate risk HP or repair allocation by damage source is required.

Static findings from the local 1.2.15.2 decompilation:

- `Module.java:281`: doDamage only subtracts hp/holdOnHp, with no ignition or detonation roll.
- `Tile.java:240–255` in splashHit and `:444–459` in hit separately roll low-HP ignition after damage. Calling doDamage directly bypasses those hit ignition paths. External hits still use actual HP, so overload wear can make a subsequent attack meet the ignition threshold sooner. Spreading and existing fire remain native.
- `Module.java:984–1005`: tick starts a detonation fuse using current HP, the explosion threshold, simulation step and risk modifiers. It requires no fresh damage and stores no damage cause. An explosive living module below threshold can start its fuse even after damage stops. More doDamage calls do not themselves produce more detonation checks.
- An active fuse counts down separately. Neither overload nor its deactivation clears fire/fuses or skips module tick. Deactivation stops subsequent self-damage but leaves existing low-HP risk. Repairs and active fuses retain native interactions; do not promise that repair cancels a fuse already started.

Implementation direction: apply direct module damage through doDamage using simulation time, bypassing armor without invoking hit ignition rolls. Subsequent fire spread, external hits and low-HP detonation remain native. No shared ModuleType changes or replacement detonation HP calculation is needed. Fractional damage accumulation and normal compatibility with holdOn still require implementation and verification.

Existing method-comparison.json records identical Module.tick bytecode across 1.2.15.2, 1.2.15.3 and 1.2.14. This turn adds no Tile cross-version verification or runtime test. Future validation must cover pure wear to zero, mixed damage, low HP after deactivation, repair order, existing fire/fuses, fire spread, simulation random sequences and persistence.

### Uniform overload (shared allocation and curve confirmed)

Weapons, propulsion and lift follow the same uniform-overload principle, without internal priorities or sequential filling.

Confirmed meaning of uniform: the same fraction of each participating device's extra overload requirement, rather than identical extra watts. Let `D_i = P_overload_i - P_rated_i > 0` and S be surplus available to this pool. Then `u = clamp(S / sum(D_i), 0, 1)` and `P_i = P_rated_i + u × D_i`.

This assumes participants already have their rated operating budget. Derated, stopped or natively ineligible devices must not be treated as fully supplied. Avoid division for an empty pool. Surplus above full overload demand is not forcibly consumed; charging depends on storage rules. Both enabled groups use one pool with a common u. Deactivating one group releases its extra demand and can increase the other's completion fraction.

Example: device A is rated/overloaded at 100/150 kW and B at 200/300 kW. With 75 kW spare, u=50%; allocations are 125 and 250 kW. Progress is equal while extra watts follow each device's requirement.

Confirmed curve form: `multiplier = 1 + B_max × (a × u + b × u²)`. Authoritative B_max and a/b values are in the [balance register](BALANCE.md). The linear part provides useful intermediate benefits; slight upward curvature rewards approaching full overload without a discrete bonus at 100%.

Curve examples are in the [balance register](BALANCE.md). The multiplier affects the confirmed reload/fire rates, thrust and lift. Fire-rate multipliers map inversely to reload duration rather than multiplying all time fields directly.

Normal supply and surplus restarts follow the rules above. The confirmed phase order is normal supply, full-budget restarts, then uniform overload, preventing overload from consuming all headroom and blocking recovery. Overload has no internal priorities, while normal shedding ties and restart order use internal priority. Batteries automatically cover shortfalls and charge from surplus generation as specified above.

Restart must actually reserve rated power; multiple devices cannot count the same spare power as startup permission. Different start/stop thresholds reduce noise around the minimum but do not replace a stable allocator. Native working eligibility, electrical on/off state and overload request remain distinct. Recovery after power loss, staffing loss, damage or landing shutdown uniformly requires full rated power before considering overload. Native staffing and electrical supply must not wait on each other indefinitely.

### Damage, splitting, repair and capture: confirmed

- Each split section immediately computes its own supply and inherits both pre-split overload requests. A section with power but no command capability may keep overloading and be unable to deactivate manually under the agreed command rules.
- Stored energy follows battery instances without redistribution by hull size or duplication during splitting.
- Generators/batteries damaged beyond working eligibility stop generation or charging/discharging, respectively. An existing disabled battery retains inaccessible charge; restored eligibility makes that remaining charge available again. Repair itself adds no energy. Removal of the battery instance removes its charge. No additional damage-induced leakage is added in the first release.
- Repaired loads that regain native working eligibility must receive their full rated restart budget before starting, then consider overload according to the inherited group request and spare supply. Repair does not bypass the startup threshold.
- Capture clears both overload requests for the new owner to decide, while preserving actual HP and stored charge. This ownership transition is not a free deactivation control for the original owner.
- Native command-point behavior after splitting/capture still needs inspection. These overload-state decisions do not authorize resetting or duplicating native command resources.

### Daily supply/demand and resupply continuity: confirmed

- Generators use native staffing and fuel eligibility without an extra first-release output multiplier based on remaining HP percentage. Eligible units offer rated output; ineligible units offer zero. Equipment staffing values remain content decisions.
- Working coal generators retain the native coal-consumption cadence, with no additional load-based savings when output exceeds demand. Fuel exhaustion stops supply; resource-free generators have no coal requirement. Actual native consumption paths still need adaptation and verification.
- Each battery defines capacity, maximum charge power and maximum discharge power separately. First-release charge/discharge efficiency is 100%, without conversion losses or standby leakage. Charging uses only generation left after loads and overload.
- Newly spawned ships in standalone combat start with full batteries. Restored combat state retains saved charge without refilling.
- Campaign charge persists with ship/battery state. Native resupply automatically fills batteries; consecutive battles without resupply retain the previous remaining charge, following coal/ammunition resupply and continuity semantics. Entering/leaving combat, loading, repair or capture alone never triggers a refill.
- Resupply filling is logistical resource restoration; in-combat generation still respects output, surplus and battery charge-power limits. Crew delivery of coal/ammunition to a module must not be mistaken for a ship-wide battery resupply event.
- Implementation must inspect actual native resupply triggers, post-battle state writeback, campaign new-ship initialization and autoresolve. These are confirmed design contracts, not implemented persistence or verified resupply hooks. No automatic full charge between unsupplied battles or additional world-map generation simulation is introduced.

### Combat UI: four-color power bar and battery subtrack confirmed

The user retains Weapon Overload and Propulsion Overload as toggle buttons showing requested on/off state, still consuming native commands in both directions. Real-time combat does not need detailed per-module shutdown reasons, power values or multiplier lists. State-dependent module textures/visuals are preferred; exact mappings and composition with native damage/fire layers remain open.

Electricity should occupy a single resource-bar entry alongside coal, ammunition, water and repair supplies. The user proposes green generation background, yellow occupation, red shortage and gray shutdown demand. With generation 100 and consumption 60, yellow covers 60% of the green bar. Earlier detailed summaries and per-module reason displays are not accepted requirements.

The user has accepted the illustrative design. The following encoding is the first-release design; drawing details and smoothing of scale changes remain implementation validation work:

- Yellow is actual power delivered to devices, including actual overload. Red is the unmet rated demand of devices still online. Failure to reach full overload is not a power-shortage warning.
- Gray represents full rated demand of otherwise eligible devices shut down for lack of power. Landing-disabled, damaged or unstaffed devices must not masquerade as electrical deficits. Red and gray use the allocator's final state without counting the same device twice.
- Green is an underlying supply range; yellow/red/gray encode device states above it. Gray hatching leaves unused green supply visible, with a thin supply-end marker. If online demand is 60, an offline device needs 80 and supply is 100, the spare 40 cannot restart it. Without offline demand, preserve the user's 60% yellow / 40% green example.
- Fixed screen width need not mean fixed watts. The illustrative scale is `max(available power, max(online rated demand, actual consumption) + restart rated demand)`. Yellow ends at actual consumption, red extends to online rated demand, and gray hatching starts at the larger of rated demand/consumption and spans restart demand. Green extends to the supply endpoint, avoiding additive double counting of spare supply and full offline demand. Final scale stabilization remains open.
- Batteries can cover a generation deficit. Main-bar supply includes generation plus discharge available within current battery energy/power limits. Stored energy is not added to power segments. A very thin charge subtrack at the bottom of the same resource entry shows energy, with charging/discharging direction and an optional compact percentage. This layout is confirmed; do not add a second equally sized battery resource bar.

Static UI evidence: local 1.2.15.2 CommandButtonsPanel.renderShipQuantities (around line 1156) computes native resource quantities/capacities and calls MyDraw.progressBar in this area. A segmented/layered electricity bar needs custom drawing, but placing it alongside native resources does not require changing the Resource enum. Textures, bar and toggle buttons remain unimplemented, with no game GUI validation.

### Ship-design electricity information: user-specified layout, endurance details open

- Ship-design statistics use compact rows resembling native landship weight statistics. One electricity row shows rated demand / generation, with all electrical devices in the numerator and generator output alone in the denominator. Battery discharge is not generation.
- A battery row shows total capacity (support duration). Do not list maximum discharge power separately in the ship summary. Its mechanical constraint remains active and must still be checked when estimating duration.
- Combine all three full-overload demands into one statistics item, explicitly labeled in the fixed order Weapons / Propulsion / Whole ship. Each value is total ship demand in that mode, not just group demand or its extra increment. Propulsion includes thrust and lift. Whole ship means both buttons enabled with all eligible devices fully overloaded and remaining devices at rated demand.
- With N as total rated demand and W/D as eligible devices in each group, weapon demand is `N + sum(P_overload - P_rated, W)`, propulsion demand is `N + sum(P_overload - P_rated, D)`, and whole-ship demand is `N + sum(P_overload - P_rated, W union D)`, counting each module in the union once. Do not simply add the first two ship totals, which double-count rated demand. For disjoint groups, whole-ship demand equals weapon demand + propulsion demand - N.
- Example: rated demand 200, weapon increment 60 and propulsion increment 40 display as Full overload demand (Weapons/Propulsion/Whole ship): 260 / 240 / 300. These illustrate layout and calculation, not balance values. Actual operation still uses the shared allocation pool; full-overload demand is a target, not guaranteed supply.
- Construction descriptions show minimum/rated/overload power for loads, output for generators and capacity plus charge/discharge limits for batteries. Eligible modules show full-overload boost and self-damage rate. Detailed values belong to design time without restoring per-module reason panels in combat.
- Proposed parenthetical duration uses all-rated operation with both overload modes off, explicitly labeled approximately X seconds at rated load. Assume intact devices, sufficient staffing/fuel, full batteries and constant load. Batteries cover the difference between generation and demand; usable energy divided by this gap estimates duration only when discharge capability remains sufficient. Heterogeneous batteries require accounting for declining discharge capability as individual batteries empty, rather than treating total energy division as guaranteed duration.
- Proposed boundary text: rated operation needs no discharge when generation is sufficient, rather than infinite total endurance; cannot sustain rated load when battery output cannot cover the gap. Power/energy units and exact formatting remain open. Hiding maximum discharge from the ship summary does not remove the constraint from estimation.
- Underpowered designs receive information without blocking save or use. Players may intentionally build battery bursts, derated operation or staged recovery. No automatic refits or added generators.

### New attack form: chain lightning (in design)

User-specified core: a lightning weapon uses native aiming/attack-point selection, completes its initial hit, then spawns child bolts from the actual hit point to randomly selected nearby modules. Damage reuses native blast damage rather than introducing an electrical damage type. Configurable serial and branching behavior coexist. Unified attack energy budgets propagation and affects node blast damage; the weapon owns its initial attack energy and its panel displays the derived nominal first-hit damage.

Static reusable paths in the local 1.2.15.2 snapshot:

- Module.fire (1779 onward) obtains a target ship/point and handles aiming paths, weather/accuracy and jitter. Its beam branch (1854 onward) already supports attacks based on a selected point. Reuse may follow this targeting approach, without copying slow-projectile lead or assuming targeting alone guarantees line-of-sight or a hit.
- Module.updateBeam (1697 onward) illustrates source-attributed Shot creation, configured damage and immediate point hits through Airship.hit returning a Tile. Native beams sample attacks over time; each render frame or beam sample must not accidentally create a complete lightning chain.
- Tile.hit (276 onward) reads pen, blast and direct separately; zero blastSplashRadius still permits point blast damage. Area blast uses Combat.doSplashDmg(type=1, 631 onward), Airship.splashHit (5147 onward) and Tile.splashHit for armor/module/ignition processing. Blast damage type does not imply an area explosion at every node. Area processing may damage several tiles of the same large module, rather than one module hit.
- Tile hits retain weapon source and native armor handling. Lightning is not overload wear and must not shortcut through Module.doDamage to bypass armor. Lightning visuals need not use normal explosion graphics.

First-release implementation direction (confirmed rules and proposals identified individually):

1. One weapon firing creates one chain context. Initial aiming/accuracy follows native logic, with lightning configured for extremely high accuracy and short range. Instantaneous hits and full-chain resolution within one simulation update are confirmed instead of a travelling projectile. Only a valid hit begins propagation. Lightning is ammunition-free by default but retains reload cycles; children do not independently fire, reload or add electrical debits. Native ammunition-free reload and multishot integration need inspection.
2. Filter candidate modules within jump radius of the current hit point, deduplicate by module, select randomly, then choose a valid hit tile. Serial and branching behavior use one propagation process controlled by attack properties; no candidate ends the branch. Energy, child limits and runtime safeguards are proposed below, without an exclusive choice between serial and branching weapon types.
3. Confirmed once-per-module direct targeting within a root attack, sharing a visited set across all branches. Confirmed point damage requires no splash-victim deduplication rule; secondary native damage such as detonation is not constrained by direct-target deduplication.
4. Native point blast damage is confirmed for the root and every child: each node resolves one selected hit tile with native armor handling, without an added area explosion or armor bypass. Jumps and branches provide multi-target coverage. Native consequences such as ignition or detonation retain their own rules. Node damage derives from its attack energy, reduced in descendants by consumption and splitting; fixed damage per node is no longer proposed. No extra generation-based decay is added, as confirmed. Ignition modifiers are not added automatically; integration remains to be checked.
5. Surface-only module targeting is confirmed, without directly selecting interior modules, complex targeting or internal conduction. Exact surface eligibility awaits source adaptation; this does not establish full ray obstruction handling. Children target surviving enemy modules, including damaged ones, and may jump across ships within the same jump radius.
6. Proposed stable candidate ordering and simulation RNG for target selection, with separate visual randomness for bolt shape. Simulation events apply damage once; lingering visuals do not repeat it. Total budgets and spatial queries bound work instead of scanning every module globally for every child. Multiplayer/replay determinism needs verification, not assumptions from native damage reuse.

#### Unified propagation energy and damage (rules and 1:1 base mapping confirmed)

The user accepts energy-budgeted propagation with branching parameters determining shape, and additionally specifies energy-dependent blast damage. Retain maximum children B: B=1 gives serial propagation; B>1 permits branching, naturally reducing to one child or none with insufficient energy or nearby candidates.

- The weapon defines authoritative initial attack energy E0. Each root starts at E0, while children hold their allocated energy E. Confirmed order: derive nominal blast damage from the node's incoming E, resolve native armor/damage, then deduct positive hit loss C before propagation. Children never each copy their parent's full remaining energy. The base conversion coefficient is fixed at 1; equipment balance values remain open.
- In the simplest version, with remaining R=E-C nonnegative, use `k=min(B, eligible candidates, floor(R/C))`. Randomly choose k distinct modules and distribute R among them, equally by default; each repeats the process. k=0 terminates. Unused budget is not automatically refunded to ship batteries. Rounding/remainder handling must be deterministic and cannot create energy.
- Illustration only: E0=100, C=20 leaves 80 after the root. With at most two children, each receives 40, spends 20 on its hit and may create one 20-energy descendant. This permits at most five hit nodes. B=1 can instead produce a five-node serial chain with the same budget. Missing targets or insufficient residual budgets can reduce actual hits.
- Energy maps to base blast damage at a confirmed 1:1 ratio. An integer damage entry may use `D(E)=floor(E)`, with final rounding details awaiting integration checks. The panel derives D(E0) as nominal first-hit blast damage rather than maintaining a second independent parameter that could drift. Bonus/rounding behavior must agree with resolution and avoid double application. Native modifier integration still needs validation.
- Illustration: E0=100, C=20, alpha=1 and at most two children produce nominal node damage 100 → 40/40 → 20/20; the panel displays first-hit 100. A serial configuration instead gives 100, 80, 60, 40, 20. These are examples, not balance values.
- Fixed initial energy fixes nominal first-hit damage under identical attributes, not total chain damage. The branching example totals 220 and the serial example 300 before armor and other processing. Candidates, paths, defenses and early termination further change actual damage. A nonduplicating propagation budget does not imply one-to-one conservation between E0 and summed damage. Balance must evaluate full attacks and different layouts. No travel losses or extra branching costs are added, as confirmed.
- Attack energy defines per-bolt strength and propagation budget without automatically adding a persistent resource or changing continuous online weapon consumption/no extra child debits. Paying E0 from ship grid/batteries per shot requires a separate revision of supply/firing rules. First-release overload still scales reload/fire rate, not E0 or first-hit damage.
- Positive cost bounds node counts by energy, but retain explicit total-node/depth safeguards and bounded iterative propagation. All branches share a visited set; candidate selection and budget allocation order must be stable rather than dependent on frame rate.

#### Propagation targets and contact rules (confirmed)

The user chooses surface module targets to reduce targeting complexity. Child bolts randomly select nearby surface candidates from the current hit point, striking a surface tile with native armor handling. Do not directly select interior equipment or add route finding, an internal conduction network or priority targeting of critical equipment. Candidate construction and updates after damage remain implementation details without validation.

- Target enemy modules that still exist and have positive HP; damaged but surviving modules remain eligible. Allow cross-ship jumps within the same radius measured from the current hit point, without expanding the search range.
- Share direct-target deduplication across every branch of one root attack, selecting each module at most once. Separate shots have separate sets. This does not exempt modules from secondary native effects such as detonation.
- Propagate after valid contact without requiring module HP loss: complete armor absorption does not automatically stop the chain. A module destroyed by the current hit may still branch from its recorded contact point. An initial miss has no propagation origin.
- Surface selection does not automatically require full visibility or path calculations from the current hit point. Prefer simple surface eligibility; define necessary hit checks during source adaptation rather than treating complex obstruction-aware targeting as a default requirement.
- The user has confirmed these faction, cross-ship, deduplication and contact-based continuation rules. Candidate construction and hit integration still require implementation and validation.

#### Coil-tower placement, armor and obstruction (confirmed)

The user confirms an exposed deck weapon resembling deck guns/flak: a fixed upright omnidirectional coil tower, top-mounted without hull-armor coverage. Point-hit targeting and visually crossing hulls are accepted. Retain existing HP, damage, range and other values as the initial unarmored baseline rather than automatically rebalancing them.

- Use native `topOnly` placement: no other module may sit above the tower in its occupied columns. This is more than requiring just the immediately adjacent upper cell to be empty. Native structural connection and crew-access requirements remain. The tower neither displays nor receives protection from hull armor plates; incoming damage follows native unarmored handling without an extra vulnerability multiplier. Neighboring hull modules retain their own armor. Overload wear and native low-HP hazards retain settled rules.
- Keep front-edge placement for the cannon. The tower exposes a fixed upper discharge terminal without rotation or directional sprites. Exact anchor coordinates follow the artwork during integration.
- For the first release, use point-hit targeting without an additional full line-obstruction scan from emitter to root or along child bolts. Root targeting still checks enemy eligibility, range, native accuracy and valid contact; children retain living enemy surface targets, hop radius and shared deduplication. A root miss still ends the attack.
- Accepted tradeoff: visible arcs may cross the firing ship, a third ship or terrain. Intermediate objects neither intercept the attack nor receive extra lightning damage. Only the settled enemy surface targets may be selected, never interior modules directly. This is an accepted visual/gameplay simplification, not physical pathfinding or obstacle avoidance.
- Real obstruction, if wanted later, needs a separate design for blockers, first contact, friendly fire and failed targeting. These rules are outside the first release. Native secondary detonations retain their existing behavior.

Armor integration: native 1.2.15.3 FLAK_CANNON uses topOnly, windows and external sprites, while the deck-cannon variant has a mask. In 1.2.15.2, Tile still constructs an ArmourPlate; a window flag does not directly mean unarmored. ModuleType external/armourMask behavior alone also does not prove absence of protection. Verify that build-time armor selection, rendering, mass/cost, incoming damage and persistence consistently reflect an unarmored tower rather than merely hiding armor sprites. These are implementation acceptance requirements, not completed integration or a claim that every native deck weapon lacks an armor object.

Static basis: native 1.2.15.3 GUIDED_MISSILE combines `topOnly` with an all-round arc, showing that mounting direction and targeting arc are independent. In 1.2.15.2, Airship.moduleThatObstructsTop / moduleWhoseTopIsObstructed check modules above the same columns. Module.updateBeam creates a Shot at the target point and calls enemy Airship.hit, which looks up that point's Tile rather than automatically tracing the whole emitter-to-target line. This supports an integration direction, not completed validation of artwork, placement feedback or lightning hits.

#### Resolution timing and visuals (single-update resolution confirmed)

Confirmed: resolve all targeting, damage and propagation for one attack within the same simulation update, in a stable node order, without per-jump delays or sustained electrical damage. Nodes still resolve sequentially rather than as unordered simultaneous damage. Retain total-node limits and bounded queries; instantaneous resolution does not eliminate performance costs. Briefly display the resolved bolt tree and fade it out; visual duration does not repeat damage or retarget. The first release will not use delayed jumps. Exact visual duration, energy-dependent bolt width/brightness and effect budgets remain open.

#### Firing and accuracy: no ammunition, retained reload, extremely high accuracy and short range confirmed

- The user accepts the underlying accuracy logic: native initial aiming/accuracy, with an initial miss ending the attack without snapping or retargeting and entering the normal reload cycle. Lightning content uses extremely high accuracy and short range rather than hard-coding guaranteed initial hits.
- After a child selects a hit tile on a valid surface candidate, hit it directly without another native accuracy scatter roll. Randomness comes from module selection. Descendant energy and target eligibility still follow the agreed rules.
- Lightning weapons consume no ammunition by default but retain native reload timing. Neither root nor child hits debit ammunition, and these weapons require no ammunition hauling. The user notes existing native ammunition-free weapons with reload cycles; inspect the integration path later. Operating crew requirements remain separate.
- Existing continuous online power consumption remains in force, with derating/overload affecting reload and fire rates. Reload does not introduce a separate charging resource or per-shot electrical debit.
- Initial range limits the distance from the weapon to the first attack point; jump radius limits the distance from the current hit to the next target. Both are short and separately configurable, with exact values open. Confirmed: no additional full-chain reach limit relative to the firing weapon. Reaching beyond initial range requires valid intermediate targets, still constrained by remaining energy, deduplication and protective node limits.
- Prefer one ordinary single shot creating one bolt tree, with no child reload cycles. Prefer a single-shot first-release example; inspect native multishot/burst integration separately rather than treating every callback as a new chain.
- Retain the firing weapon and its side as the source of every direct node hit so damage/kill attribution is not lost. Secondary native detonations retain their own attribution behavior; exact statistics integration needs verification.

#### First-release propagation parameters (confirmed)

Beyond native range, accuracy and reload parameters, use four main chain-content parameters: initial energy E0, fixed positive per-hit loss C, maximum children B and jump radius jumpRadius.

- Let distance determine eligibility only, without extra energy loss per distance travelled. Add neither a branching surcharge nor a generation-based damage multiplier: splitting remaining energy already reduces descendant damage.
- Use a 1:1 mapping from energy to nominal base blast damage for the first release, with consistent rounding at damage integration and the same conversion in the panel. Exact rounding and native modifiers still need implementation checks. This is not 1:1 post-armor HP loss or conservation of total chain damage against E0.
- Retain total-node/depth caps as technical safeguards, not player-operated combat controls. Values await performance validation; do not promise propagation using unlimited caps.
- The user has confirmed this parameter scheme. Equipment values and technical caps await balance work and validation.

This section records source research and design, not implementation authorization. Targeting/hit rules, RNG order, ammunition/stat attribution, energy/damage mapping, branch budgets and actual version compatibility lack runtime validation. Overload continues affecting only agreed reload/fire-rate multipliers, without automatically increasing branch count, jump range or node damage.

### Confirmed battery role

Batteries support short bursts and emergencies after supply interruption, rather than minutes of independent operation by default. Balance should prevent ordinary battery installations from covering most short battles and making generators redundant. Size capacity against a reference ship's load; see the [balance register](BALANCE.md). Initial supply/capacity values and reference-load durations are now confirmed in the balance register. This role introduces no battery lifetime countdown, generator-installation requirement or stacking prohibition.

### Native modifier boundaries (confirmed)

The user confirms no new technology-bonus system initially. Confirmed boundaries follow; unlocks and research progression are separate decisions.

- Use balance-register base parameters without automatically copying reference modules' multipliers / cases. Do not explicitly bind native cannon-specific damage/reload/accuracy, propeller engineering or suspendium lift/discount bonuses merely because new equipment resembles native roles. Discuss mappings individually later.
- Retain applicable native common combat-path modifiers: staffing, experience, captains/medals, fire modes and temporary abilities. Do not suppress these or apply them twice in the MOD. Applicability still depends on the native path and device eligibility.
- Generation, rated/minimum/overload demand, battery capacity/charge/discharge limits, overload curve and percentage wear remain configured values, unaffected automatically by captains/experience. Common bonuses to fire rate or thrust do not increase power demand or add per-shot electricity fees.
- Lightning energy, hit cost, child limit, jump radius and initial maximum range are not inferred from native explosive-damage bonuses. Native blast armor handling does not automatically inherit configuration-specific explosive-weapon bonuses. Apply any independently applicable common damage-path modifier once without feeding it back into propagation energy or target count.
- Apply electrical derating/overload once in the output path, combined normally with applicable native modifiers. Native abilities may coexist with overload, exceeding unmodified panel baselines; playtest combinations.
- Base wear on the instance's effective maximum HP, not remaining HP or repair ceiling. Verify native maximum-HP changes. Repair, ignition and detonation retain confirmed rules.
- Native logistics bonuses still affect native facilities. Extra coal-store capacity does not automatically alter the generator's runtime per coal unit. Add no equipment-specific cost/mass bonus declarations initially; native common pricing paths still need review.

The tradeoff is that some research/faction benefits specific to native guns, engines or suspendium chambers are not actively transferred to new equipment. This first-release balance choice is confirmed and does not modify native content. Static basis: 1.2.15.2 ModuleType BonusableValue parsing, Module.tick staffing/fire-mode/captain/experience modifiers, and Airship captain/medal/temporary propulsion modifiers. This classification is not a completed compatibility claim.

### Campaign unlocks and construction access (confirmed)

The user confirms unlocking all six initial devices together with native Machining (MACHINING): generator, battery, propulsion, lift, electrical cannon and coil tower. Supply infrastructure and consumers become available together. Add no separate electrical technology node, research cost or technology icon initially; do not change native research contents, prerequisites, costs or equipment.

- Unlocks grant construction eligibility only, not free modules, ships, coal or batteries. Purchases, staffing, placement and resupply retain settled rules.
- Use native locked-equipment behavior and blueprint legality checks, not merely hidden list entries. Already-researched campaigns use actual research state when MOD loading is otherwise permitted, without repeated research or rewritten records. This does not unconditionally admit the MOD into every old save.
- Non-campaign editors/standalone battles follow native technology restrictions. All-technology environments allow every device; restricted modes check eligibility. Add no separate MOD technology switch. First-release supported modes remain a separate decision.
- Clearly identify Electric Age in localized device names and use native functional categories alongside native equipment. Research access and equipment-specific bonuses remain separate under the confirmed modifier boundaries.
- AI uses the same construction eligibility without automatic conversions, ship design or overload controls. Future ship packs must meet technology requirements.

Native reference: 1.2.15.3 Tech/tech.json places MACHINING at tier 2 with ARTICULATED_MACHINERY and ECONOMICS prerequisites, granting GATLING_GUNS, GATLING_SKYBOATS and FACTORY; lang/chi.properties names it 机械加工. These are native references, not new research values. Module required fields use Bonus markers, so do not blindly put the technology ID MACHINING there. Verify research/reward checks and standalone eligibility sources without overwriting native technology definitions or claiming completed single-field integration.

Tradeoff: introduce the complete system together rather than progressively unlocking electrical devices. The user plans dedicated technology nodes later and accepts this first-release scheme. Initial testing need not wait for research and can use native all-technology environments.

## 4. Proposed playable scope and architecture

- Store energy on battery instances and aggregate at ship level without duplication.
- Use the confirmed six equipment types with one size each, covering generation, storage, propulsion, lift, native attacks and chain lightning, as listed below.
- Generation follows its coal-consuming or resource-free type and native working eligibility without extra HP-proportional derating; equipment values remain open.
- Include saving, cloning, splitting and read-only power display early.
- Use two overload toggles, one four-color electricity bar with gray hatching and a bottom battery subtrack, and module-state textures. No persistent per-module reason/value panels or separate overload cooldown. Ship-design statistics show the demand/generation ratio, capacity with parenthetical duration, and weapon/propulsion/whole-ship overload demands combined in one item; module descriptions and endurance boundary wording remain proposed.

### First-release equipment roster (confirmed)

The user confirms six equipment types with one initial size each for the first release: a coal generator, battery, electric propulsion unit, electric lift device, an electrical cannon using native projectiles/ammunition, and an ammunition-free chain-lightning weapon. The first four exercise continuous supply, storage buffering and propulsion overload. The weapons exercise native-attack adaptation and the new chain attack, while comparing weapon overload and different logistics needs. They may be combined with native hulls, armor and other facilities; the first release need not replace every native device.

Resource-free generation remains supported by the system design, with concrete equipment deferred. Coal generation provides the initial logistics baseline without removing agreed system support. Additional sizes and technology tiers are outside the initial roster; basic dimensions, visual direction and direct operating crew are confirmed; cost and remaining balance values remain open. The content roster is approved, but this does not imply completed gameplay or implementation beginning this turn. Launch support for combat, campaign and multiplayer is a separate decision.

### Equipment specifications and art preparation (confirmed)

The user confirms equipment/art specifications and will give them to another project connected to Aseprite MCP. See the authoritative [equipment and art preparation brief v1.2](ART_SPEC.md). Static inspection confirms native 16×16 pixels per cell, separate bodies/external/barrel parts, animation frames/intervals and alpha-based explicit masks.

Confirmed footprints are generator 3×2, battery 2×2, propulsion 2×2, lift 3×2, cannon 3×2 and lightning 2×3. Direct operator counts are 2, 0, 1, 1, 2 and 1, excluding hauling/repairs. Use the handoff brief for canvases, parts, state layers, initial animation and style, retaining optional items and integration boundaries. Produce six silhouette layouts before state, animation, damage and UI art; power, capacity, mass and cost remain balance work.

Authoring separates structure, moving parts and operating states, allowing later composition into replacement sprites without hand-drawing every combination. Damage and electrical eligibility are independent dimensions. State integration and runtime rendering remain unimplemented and unvalidated.

### Numerical maintenance (confirmed)

The user confirms initial minimum-power ratios, overload demand ratios, curve, device benefits and wear rates. Maintain all gameplay numbers in the [balance register](BALANCE.md), with confirmed values, derived formulas and TBD parameters. This GDD retains mechanics rather than a separately maintained equipment balance table. Initial rated loads, generation, battery capacity and charge/discharge limits are now confirmed for first testing; HP, mass and cost are also confirmed as initial-test baselines; ignition/detonation values are also confirmed for initial testing with native-data references. Rated propulsion and lift outputs are confirmed; generator coal use is confirmed; electrical cannon attack parameters are confirmed; chain-lightning attack parameters are confirmed with an omnidirectional coil-tower arc; see the balance register. Top-mounted unarmored deck placement and point hits without intermediate obstruction are confirmed; integration still needs validation.

A lower lift threshold delays power-induced shutdown, but actual lift still scales by P_actual/P_rated and cannot guarantee flight. Equal weapon thresholds use the confirmed internal shedding order. Generators and batteries do not join either overload group. Numerical design approval is not implementation or playtest acceptance.

### Multiplayer integration principles (confirmed, unvalidated)

Rechecked the local decompiled 1.2.15.2 snapshot: Combat.getTickCommands / runTickCommands (257–305) consume ordered messages from network frames; giveCommand (309) selects local execution or network sending; execCommand (450) dispatches through EXECS and records commands, rejecting unknown types; doTick (1008) processes commands before advancing simulation. Combat uses GuardedRandom, and construction/saving includes randomSeed and ship state. This static evidence supports reusing native command-driven simulation; it is not a multiplayer test of this MOD.

Native data-driven MODs typically reuse existing behavior, commands and serialization, so matching game versions and effective MOD content often require no separate per-MOD networking layer. Java MODs add runtime logic and state: matching code still requires consistent initial data, shared rules, command timing, simulation updates and random calls. Integration does not necessarily mean broadcasting every battery or lightning node each frame.

The user confirms following the native simulation model from the outset:

- Send explicit overload on/off states through native command routing and execute consistently on all peers, rather than mutating ships only in the clicking player's UI callback. Inspect both standalone and campaign combat paths.
- Advance generation, batteries, startup/shutdown, overload and wear using matching simulation steps and stable iteration order, independent of wall time or rendering. Prefer local calculation from shared inputs over extra per-frame power synchronization.
- Keep lightning candidate ordering, branch processing and simulation randomness consistent. Visual jitter must not consume combat randomness; off-screen rendering decisions must not affect hits. Prefer the validated combat RNG flow; equal seeds alone do not guarantee equal call sequences.
- Preserve battery charge, overload requests, relevant online/offline state, fractional wear and other future-affecting state through the actual combat/campaign save, restore and reconstruction paths. External Java state does not automatically participate in native serialization, checksums or replay.

Acbric's current LOBBY_HANDSHAKE / CODE_HANDSHAKE / SHARED_RULES contracts provide multiplayer campaign-lobby code and explicitly declared shared-rule checks, not a general runtime-state synchronization API. The code readiness gate does not cover standalone battle lobbies. Local configuration and CampaignData writes do not automatically broadcast. Reuse available capabilities and supply missing precise integration points during implementation.

Multiplayer is a confirmed design goal rather than being excluded because this is a Java MOD, but matching JARs do not establish support. Specific launch modes and acceptance scope remain undecided. Acceptance should compare peer charge, startup/shutdown, overload and lightning outcomes under matching inputs, plus relevant save/restore paths. No dual-client or live gameplay validation was run this turn.

### Campaign autoresolve (unchanged native behavior confirmed)

Rechecked the local 1.2.15.2 snapshot: WorldMap.autoResolveCombat (2267 onward) aggregates Airship.getResourceAdjustedStrength and randomly allocates ship losses without running full Combat. getResourceAdjustedStrength (1358 onward) starts from ship cost and adjusts for coal, ammunition, water, repair resources and crew experience, rather than precisely evaluating every weapon's combat damage. Lightning does not need node-by-node simulation for this path, but new charge and power-shortage states are not automatically recognized either.

The user explicitly confirms retaining native autoresolve without changes. Do not modify strength estimation, random resolution or loss allocation; add no supply penalty, lightning/overload strength adjustment or full battle simulation. The user accepts that charge/shortages, chain coverage and overload benefits are not modeled precisely. Dedicated autoresolve adaptation is no longer first-release work.

Autoresolve does not advance the electrical simulation, modify surviving battery charge by itself or apply overload wear. Ordinary storage must preserve surviving equipment data; actual native resupply still fills batteries under the agreed rules. Data preservation and resupply hooks belong to general campaign persistence validation, not a new autoresolve resource algorithm. Live battles still write back actual remaining charge.

### AI and ship packs (first-release scope confirmed)

- Do not extend AI decisions, controls or automatic ship design in the first release, or automatically convert native AI ships.
- AI does not use weapon or propulsion overload commands. Add no automated activation, deactivation or timing decisions. Existing split inheritance, capture clearing and save/restore rules remain shared rather than introducing AI-specific state mechanics.
- Electrical equipment is not player-exclusive. AI ships fitted with it use the same generation, storage, rated operation, shortage startup/shutdown and attack rules. Ordinary movement, aiming and firing follow native control paths; actual compatibility still requires validation.
- After the MOD becomes usable, build electrical ship designs and package them for AI use. That ship pack is not a prerequisite for delivering the first-release six equipment types; no pack is created or installed this turn.

## 5. Remaining decisions and implementation checks

Core grid rules, overload, damage, six-device baselines, chain attacks, deck-coil mounting, multi-selection, native modifier boundaries and Machining unlocks are confirmed. The balance register remains authoritative; do not treat settled gameplay as still undecided.

| Area | Remaining work | Approach |
| --- | --- | --- |
| Test delivery scope | First playable package and later mode acceptance | Use the sequence below and the implementation plan |
| Implementation details | Stable instance identity, simulation ordering, integer remainders, fractional wear, chain safety caps and visual duration | Specify before implementation; discuss gameplay changes separately and maintain numbers in the balance register |
| Native integration | Landing/takeoff, coal/ammo hauling, unarmored tower, common modifiers, commands and split/capture | Inspect and validate against settled behavior |
| Persistence | Combat state, campaign charge continuity, resupply, instance removal and copying | Prevent lost, duplicated or free energy |
| UI and assets | Summary wording, scale smoothing, procedural bolts, muzzle/recoil details | Follow agreed UI/art specifications and inspect in game |
| Framework interfaces | MOD internals versus public Acbric resource API | Discuss stability/old-API compatibility separately; no automatic framework edits |
| Licensing | Original code, documentation and artwork terms | User decision |

### First playable test package and acceptance order (development preparation baseline)

Under the user’s delegation of implementation details and request to prepare development, target a first playable package for single-player custom battles: all six devices, grid/battery/start-stop behavior, both overload commands, deck-coil chain attacks, power bar and builder statistics. Use native all-technology environments without research waiting. Campaign eligibility remains the confirmed Machining design.

Validate in sequence and report only completed checks:

1. Core rules: supply/energy accounting, one-at-a-time shedding, full-rated restart, uniform overload, fractional wear and chain budgets. Use deterministic boundary inputs as well as healthy full-power cases.
2. Single-player combat: healthy supply, fuel starvation, generator damage, exhausted batteries, landing/takeoff, splitting, repair, mixed-selection commands and isolated/dense/armored/cross-ship lightning targets. Include corresponding combat save restoration and native-equipment comparisons.
3. Campaign: research/build eligibility, charge continuity through two unsupplied battles, actual resupply, saves and unchanged native autoresolve behavior.
4. Multiplayer: matching charge, device states, wear and lightning targets/damage under matching commands; restoration for agreed modes. Report support based on mode-specific acceptance, not matching JARs alone.

Campaign and multiplayer remain design goals. Persistence and deterministic simulation must be designed from the start. Single-player acceptance for the first test package does not remove later goals or settle final release support. Six-device art has passed static review. The user approves temporary UI reuse: reference native icons at runtime; dedicated icons are not prerequisites. See the [implementation sequence](IMPLEMENTATION_PLAN.md). Gameplay implementation, installation, commits and release have not occurred.

## 6. Constraints and acceptance direction

Use simulation time, not wall time. Conserve energy and curtail loads when supply is insufficient. Power loss must not suspend fire, explosion or flooding updates. Repair, splitting, cloning and restoration must not create duplicate charge. Distinguish overload request from actual supply; verify native command consumption/readiness, duplicate and invalid requests, thresholds, rated-power restarts and allocation without charging commands or promising power twice. Rendering must not advance resources, and each load must not rescan the entire ship. Autoresolve retains native behavior; campaign continuity and multiplayer synchronization require explicit integration. No game-version, performance or multiplayer guarantee is implied.

Reallocate after each shed module to avoid excessive shutdowns. Verify same-threshold shedding and reverse restart order, sudden supply loss, persistent shortfalls and initially all-off states. Supply changes can be checked without surplus, but no power is preallocated to offline modules.

Verify full charge after resupply, charge continuity across two unsupplied battles, combat/campaign save restoration and post-battle writeback. Reconstructing combat objects or loading must not refill batteries. Confirmed resupply rules do not imply implemented campaign persistence; autoresolve adds no dedicated mechanism.

Proposed milestones: M0 scaffold, M1 agreed GDD, M2 isolated resource core, M3 minimal playable content and persistence, M4 agreed mode/content expansion. The M0 scaffold and M1 design baseline are available, and development preparation is complete. M2 onward remains unimplemented.

## 7. Research basis

The workspace report on real electricity feasibility dated 2026-09-27 and static comparisons of game versions 1.2.15.2, 1.2.15.3 and 1.2.14. Nine work areas were identified: definitions, instance state, scheduling, devices, damage, persistence, UI, campaign/AI and multiplayer/replays. The earlier report discussed ARC; Electric Age is a new independent project.

Decompiled game code and experimental inputs remain outside this repository.
