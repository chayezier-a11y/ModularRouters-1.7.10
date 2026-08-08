# Block Interaction Modules Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Restore Modular Routers 7.5.4 behavior for Breaker, Placer, Activator, Detector, Extruder Mk1, and Extruder Mk2 on Forge 1.7.10.

**Architecture:** Centralize fake-player block placement/breaking rules in a small 1.7.10 helper, while each compiled module retains its redstone, target, template, and entity-selection policy. Persist the crafting pickaxe in module NBT, commit buffer changes only after successful world actions, and keep Extruder Mk2 frames cosmetic rather than consuming template blocks.

**Tech Stack:** Java 8, Forge 1.7.10, Minecraft `ItemBlock`/`FakePlayer`/world APIs, JUnit 4, Gradle 2.14.1.

---

### Task 1: Pickaxe-Bearing Module Contract

**Files:**
- Modify: `src/main/java/me/desht/modularrouters/item/module/IPickaxeUser.java`
- Modify: `src/main/java/me/desht/modularrouters/item/module/BreakerModule.java`
- Modify: `src/main/java/me/desht/modularrouters/item/module/ExtruderModule1.java`
- Create: `src/test/java/me/desht/modularrouters/item/module/PickaxeUserTest.java`

- [x] **Step 1: Write failing tests** proving a missing pickaxe defaults to an iron pickaxe and a metadata/NBT/enchantment-bearing pickaxe round-trips under the `Pickaxe` module tag.
- [x] **Step 2: Run** `gradle test --tests me.desht.modularrouters.item.module.PickaxeUserTest`; expect failures because both module implementations currently return `null` and the interface has no setter.
- [x] **Step 3: Add** `IPickaxeUser.NBT_PICKAXE`, default `getPickaxe()`, and `setPickaxe()` methods using `ItemStack.writeToNBT()`/`loadItemStackFromNBT()`, then remove the two `null` overrides.
- [x] **Step 4: Re-run focused and complete tests**, update local `HANDOFF.md`, commit, and push the pickaxe contract batch.

### Task 2: Transactional Block Interaction Helper

**Files:**
- Create: `src/main/java/me/desht/modularrouters/logic/BlockInteraction.java`
- Create: `src/test/java/me/desht/modularrouters/logic/BlockInteractionTest.java`

- [x] **Step 1: Write failing tests** for replaceability, collision rejection, `ItemBlock.placeBlockAt()` success/failure, unbreakable blocks, harvest-level checks, Fortune/Silk Touch drops, filter-by-block/filter-by-drop, and overflow entity rollback.
- [x] **Step 2: Run** the focused test and require failures from the absent helper APIs.
- [x] **Step 3: Implement** `tryPlace()` and `tryBreak()` using a router-scoped Forge fake player; return structured results and leave inventory/world mutation to explicit commit methods.
- [x] **Step 4: Verify** focused and complete tests before any module consumes the helper.

### Task 3: Breaker And Placer

**Files:**
- Modify: `src/main/java/me/desht/modularrouters/logic/compiled/CompiledBreakerModule.java`
- Modify: `src/main/java/me/desht/modularrouters/logic/compiled/CompiledPlacerModule.java`
- Modify: `src/test/java/me/desht/modularrouters/logic/compiled/CompiledBreakerModuleTest.java`
- Create: `src/test/java/me/desht/modularrouters/logic/compiled/CompiledPlacerModuleTest.java`

- [ ] **Step 1: Add failing tests** for stored-pickaxe drops, harvest denial, ITEM/BLOCK matching, successful-placement-only extraction, tile/orientation placement callbacks, and one-Muffler/config effect suppression.
- [ ] **Step 2: Run focused tests** and confirm direct `getDrops()`/`setBlock()` behavior fails the callback and rollback cases.
- [ ] **Step 3: Route both modules through** `BlockInteraction`; preserve router regulator/filter rules and emit break/place effects only after success.
- [ ] **Step 4: Run focused/full tests**, update `HANDOFF.md`, commit, and push.

### Task 4: Activator

**Files:**
- Modify: `src/main/java/me/desht/modularrouters/logic/compiled/CompiledActivatorModule.java`
- Modify: `src/main/java/me/desht/modularrouters/util/fake_player/RouterFakePlayer.java`
- Modify: `src/test/java/me/desht/modularrouters/logic/compiled/CompiledActivatorModuleTest.java`

- [ ] **Step 1: Add failing tests** for empty-hand right-click eligibility, block-first/item-fallback use order, failed-use buffer preservation, consumed/container items, extra fake-player inventory drops, nearest/random/round-robin entity selection, and legacy action NBT fallback.
- [ ] **Step 2: Run focused tests** and confirm empty-hand and failure-transaction cases fail.
- [ ] **Step 3: Reuse one router fake player**, set position/look/sneak state, execute block/item/entity action, commit the held stack only on success, and eject extra inventory items at the target face.
- [ ] **Step 4: Run focused/full tests**, update `HANDOFF.md`, commit, and push.

### Task 5: Detector

**Files:**
- Modify: `src/main/java/me/desht/modularrouters/logic/compiled/CompiledDetectorModule.java`
- Create: `src/test/java/me/desht/modularrouters/logic/compiled/CompiledDetectorModuleTest.java`

- [ ] **Step 1: Write failing tests** for defaults, invalid NBT fallback, signal clamping `0..15`, weak/strong type, filter rejection, all-directions emission, and compile/cleanup emission reference counting.
- [ ] **Step 2: Run focused tests** and confirm invalid signal/reference-count cases fail.
- [ ] **Step 3: Implement** bounded signal parsing and router-side detector registration that remains enabled until the last detector is cleaned up.
- [ ] **Step 4: Run focused/full tests**, update `HANDOFF.md`, commit, and push.

### Task 6: Extruder Mk1

**Files:**
- Modify: `src/main/java/me/desht/modularrouters/logic/compiled/CompiledExtruderModule1.java`
- Create: `src/test/java/me/desht/modularrouters/logic/compiled/CompiledExtruderModuleTest.java`

- [ ] **Step 1: Write failing tests** for persisted per-face distance, ALWAYS/HIGH/LOW extend policy, range/regulator/filter limits, transactional placement, stored-pickaxe retraction, air/fluid distance recovery, sound config, and entity pushing strength.
- [ ] **Step 2: Run focused tests** and confirm direct placement/breaking and no-push behavior fail.
- [ ] **Step 3: Use** `BlockInteraction`, persist distance only after committed actions (or skipped air/fluid recovery), and apply configurable entity motion away from a newly placed block.
- [ ] **Step 4: Run focused/full tests**, update `HANDOFF.md`, commit, and push.

### Task 7: Extruder Mk2 Template Frames

**Files:**
- Modify: `src/main/java/me/desht/modularrouters/logic/compiled/CompiledExtruderModule2.java`
- Modify: `src/main/java/me/desht/modularrouters/block/tile/TileEntityTemplateFrame.java`
- Modify: `src/test/java/me/desht/modularrouters/logic/compiled/CompiledExtruderModuleTest.java`

- [ ] **Step 1: Add failing tests** for ordered/count-expanded templates, non-block spacer advancement, Template Frame placement, camouflage NBT, frame-only/fluid/air retraction, obstruction preservation, and distance persistence.
- [ ] **Step 2: Run focused tests** and confirm Mk2 currently places/removes real template blocks.
- [ ] **Step 3: Place** `ModBlocks.templateFrame`, store a one-count camouflage stack in its tile entity, and retract only frames/air/replaceable fluids without consuming template items.
- [ ] **Step 4: Run focused/full tests**, update `HANDOFF.md`, commit, and push.

### Task 8: Batch Verification

**Files:**
- Modify: `docs/superpowers/plans/2026-08-08-block-interaction-modules.md`
- Modify locally only: `HANDOFF.md`

- [ ] **Step 1: Run** `gradle -I gradle/fix-minecraft-download.gradle clean test build` with Zulu JDK 8 and require `BUILD SUCCESSFUL`.
- [ ] **Step 2: Launch** `runClient` and verify all six module GUIs/actions in a disposable world, including sounds/particles and Template Frame rendering.
- [ ] **Step 3: Record** exact test/client evidence in local `HANDOFF.md`; verify `HANDOFF.md` is still ignored.
- [ ] **Step 4: Commit and push** only tracked implementation, tests, and this plan.

## Self-Review Record

- Spec coverage: all Batch 2 modules, fake-player interaction, pickaxe persistence, transactional world mutation, redstone output, templates, effects, and runtime verification have explicit tasks.
- Placeholder scan: no deferred `TODO`/`TBD` steps remain.
- Type consistency: both Breaker and Extruder Mk1 consume the same `IPickaxeUser` contract and `BlockInteraction`; Extruder Mk2 owns only Template Frame policy.
