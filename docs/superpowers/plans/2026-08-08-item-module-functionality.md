# Item Module Functionality Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Restore the 7.5.4 item-moving behavior of Puller, Sender, Distributor, Dropper, Flinger, and Player modules on Forge 1.7.10.

**Architecture:** Add one sided, transactional `IInventory` adapter and keep target selection/policy in each existing compiled module. Port behavior module-by-module against upstream commit `5b35f9d`, with a failing test before every production change.

**Tech Stack:** Java 8, Forge 1.7.10, Minecraft `IInventory`/`ISidedInventory`, JUnit 4, Gradle 2.14.1.

---

### Task 1: Transactional Sided Inventory Adapter

**Files:**
- Create: `src/main/java/me/desht/modularrouters/logic/InventoryTransfer.java`
- Create: `src/test/java/me/desht/modularrouters/logic/InventoryTransferTest.java`

- [x] **Step 1: Write failing tests** for sided slot exposure, insert/extract permission, stack/NBT matching, destination capacity, simulation, counting, and rollback when only part of a stack is accepted.
- [x] **Step 2: Run** `gradle test --tests me.desht.modularrouters.logic.InventoryTransferTest` and confirm failures are caused by the missing adapter.
- [x] **Step 3: Implement** slot enumeration for stable_12 `ISidedInventory.getSlotsForFace(int)`, permission checks through `canInsertItem()`/`canExtractItem()`, and simulation-first transfer methods returning the committed item count.
- [x] **Step 4: Re-run focused and full tests** and require zero item loss/duplication in all partial-transfer cases.

### Task 2: Puller Mk1 And Mk2

**Files:**
- Modify: `src/main/java/me/desht/modularrouters/logic/compiled/CompiledPullerModule1.java`
- Modify: `src/main/java/me/desht/modularrouters/logic/compiled/CompiledPullerModule2.java`
- Create: `src/test/java/me/desht/modularrouters/logic/compiled/CompiledPullerModuleTest.java`

- [x] **Step 1: Write failing tests** for adjacent Mk1 extraction, bound Mk2 extraction, wrong dimension, unloaded target, squared range, sided extraction denial, filter rejection, partial buffer capacity, and blue reverse beam suppression at two mufflers.
- [x] **Step 2: Run the focused test** and confirm the current direct-slot loops fail sided and target validation cases.
- [x] **Step 3: Implement** target resolution without chunk loading and move accepted items through `InventoryTransfer` into the router buffer.
- [x] **Step 4: Run focused and complete tests**, then update local `HANDOFF.md`.

### Task 3: Sender Mk1, Mk2 And Mk3

**Files:**
- Modify: `src/main/java/me/desht/modularrouters/logic/compiled/CompiledSenderModule1.java`
- Modify: `src/main/java/me/desht/modularrouters/logic/compiled/CompiledSenderModule2.java`
- Modify: `src/main/java/me/desht/modularrouters/logic/compiled/CompiledSenderModule3.java`
- Create: `src/test/java/me/desht/modularrouters/logic/compiled/CompiledSenderModuleTest.java`

- [x] **Step 1: Write failing tests** for Mk1 line scanning and solid-face obstruction; Mk2 dimension/load/range validation; Mk3 unlimited loaded-world targeting; sided insertion; regulator caps; partial acceptance rollback; and yellow/orange/purple muffler-aware beams.
- [x] **Step 2: Run the focused test** and confirm failures against current first-inventory/direct insertion behavior.
- [x] **Step 3: Implement** the three target policies while sharing transactional insertion and regulation math.
- [x] **Step 4: Run focused and complete tests**, then update local `HANDOFF.md`.

### Task 4: Distributor Push/Pull Strategies

**Files:**
- Modify: `src/main/java/me/desht/modularrouters/logic/compiled/CompiledDistributorModule.java`
- Create: `src/test/java/me/desht/modularrouters/logic/compiled/CompiledDistributorModuleTest.java`

- [x] **Step 1: Write failing tests** for push/pull mode and all four strategies, invalid target skipping, ordered round-robin progression, same-distance stability, sided permissions, filtering, regulation, and beam direction/color.
- [x] **Step 2: Run the focused test** and confirm strategy or transactional cases fail.
- [x] **Step 3: Implement** sorted target setup, valid-candidate selection, persistent compiled round-robin state, and shared transfer calls.
- [x] **Step 4: Run focused and complete tests**, then update local `HANDOFF.md`.

### Task 5: Dropper And Flinger

**Files:**
- Modify: `src/main/java/me/desht/modularrouters/logic/compiled/CompiledDropperModule.java`
- Modify: `src/main/java/me/desht/modularrouters/logic/compiled/CompiledFlingerModule.java`
- Create: `src/test/java/me/desht/modularrouters/logic/compiled/CompiledDropperFlingerModuleTest.java`

- [x] **Step 1: Write failing tests** for exact spawn position/stack size, failed-spawn rollback, pickup delay, regulator floor, cardinal/up/down velocity, and muffler-aware Flinger effects.
- [x] **Step 2: Run the focused test** and confirm at least spawn rollback and effect cases fail.
- [x] **Step 3: Implement** extract-after-success spawning and the 7.5.4 velocity/effect behavior using 1.7.10 entities and sounds.
- [x] **Step 4: Run focused and complete tests**, then update local `HANDOFF.md`.

### Task 6: Player Module

**Files:**
- Modify: `src/main/java/me/desht/modularrouters/logic/compiled/CompiledPlayerModule.java`
- Modify: `src/test/java/me/desht/modularrouters/logic/compiled/CompiledPlayerModuleTest.java`

- [x] **Step 1: Add failing tests** for main/hotbar-excluded/armor/ender sections, extract/insert, online-player lookup, login/logout refresh, filter/regulator behavior, empty armor slot validation, and 1.7.10 OFFHAND no-op compatibility.
- [x] **Step 2: Run the focused test** and confirm current player inventory access fails the missing cases.
- [x] **Step 3: Implement** section slot views and server-wide per-execution player resolution using the shared transfer adapter.
- [x] **Step 4: Run focused and complete tests**, then update local `HANDOFF.md`.

### Task 7: Batch Verification And Delivery

**Files:**
- Modify: `docs/superpowers/plans/2026-08-08-item-module-functionality.md`
- Modify locally only: `HANDOFF.md`

- [ ] **Step 1: Run** `gradle -I gradle/fix-minecraft-download.gradle clean test build` with JDK 8 and require `BUILD SUCCESSFUL`.
- [ ] **Step 2: Launch** `runClient`; verify item movement, strategy controls, particles/effects, Player sections, and no GUI overlap or untranslated keys.
- [ ] **Step 3: Update** plan checkboxes and local `HANDOFF.md` with exact verification evidence.
- [ ] **Step 4: Stage only tracked implementation, tests, resources, spec, and plan; verify `HANDOFF.md` is ignored; commit and push `origin main`.

## Self-review Record

- Spec coverage: all first-batch modules, shared transfer semantics, targeting, sided inventories, filtering, regulation, beams/effects, and runtime validation have explicit tasks.
- Placeholder scan: no deferred behavior markers remain.
- Type consistency: all modules depend on one `InventoryTransfer` adapter and retain current `CompiledModule.execute(TileEntityItemRouter)` signatures.
