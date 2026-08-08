# Modular Routers 7.5.4 Module Functionality Port Design

## Baseline And Scope

The behavioral source of truth is Modular Routers `MC1.16.5-master` commit `5b35f9d` (7.5.4). The 1.7.10 port keeps the existing module item metadata, GUI/NBT layout, router scheduler, filters, augments, upgrades, and CoFH RF integration, while adapting execution to Forge 1.7.10 APIs.

All 23 compiled module implementations are in scope. Work is split into independently testable batches:

1. Item movement: Puller Mk1/Mk2, Sender Mk1/Mk2/Mk3, Distributor, Dropper, Flinger, Player.
2. Block and entity interaction: Breaker, Placer, Activator, Detector, Extruder Mk1/Mk2.
3. Collection and fluid behavior: Vacuum, Void, Fluid Mk1/Mk2.
4. Remaining generators and energy behavior: Creative, Energy Output, Energy Distributor, plus cross-module scheduling and compatibility verification.

## Compatibility Rules

- Forge 1.7.10 `IInventory` and stable_12 `ISidedInventory.getSlotsForFace(int)` replace modern item capabilities. Every transfer must honor exposed slots, `canInsertItem`, `canExtractItem`, inventory stack limits, item stack limits, and NBT equality.
- Transfers are transactional. Items removed from a source but rejected by a destination are restored without loss or duplication.
- Explicit targets preserve dimension, position, face, and name. Bounded modules reject wrong-dimension, unloaded, or out-of-range targets without loading chunks. Sender Mk3 remains unlimited and may resolve an already-loaded target world.
- Router filters, regulator counts, stack upgrades, range augments, termination modes, redstone modes, and energy costs remain centralized in `CompiledModule` and `TileEntityItemRouter`.
- Item and energy beams use the 7.5.4 colors and are suppressed by two Muffler Upgrades. Effects never decide whether a transfer succeeds.
- Minecraft 1.7.10 has no offhand inventory. Player Module keeps the `OFFHAND` enum value for NBT compatibility but reports it as unavailable and performs no transfer.
- Runtime failures return `false` and preserve inventories. Module execution must not crash or force-load a target chunk.

## Architecture

`InventoryTransfer` is the shared 1.7.10 adapter for slot discovery, sided insertion/extraction, simulation, counting, and rollback. Compiled modules select targets and policy; the adapter performs item movement. This keeps capability-version details out of each module and gives the same transfer semantics to Puller, Sender, Distributor, and Player modules.

Each module remains a separate `Compiled*Module` class. No upstream 1.16.5 class is copied wholesale: world lookup, block solidity, entity spawning, fake-player interaction, fluid containers, and RF endpoints are translated at their existing local ownership boundaries.

## Batch 1 Behavior

- Puller Mk1 extracts from the adjacent inventory on the configured face. Puller Mk2 uses its bound target and validates dimension, loaded state, and squared range. Both apply filtering, item-per-tick limits, sided extraction, and atomic insertion into the router buffer.
- Sender Mk1 walks forward until it reaches the first accessible inventory or a blocking solid face. Sender Mk2 uses one bound target within range. Sender Mk3 uses one bound target without range restriction. All three apply filtering, regulator limits, sided insertion, rollback, and their 7.5.4 beam colors.
- Distributor supports up to eight ordered targets, push/pull mode, round-robin, random, nearest-first, and furthest-first strategies. It skips invalid or unavailable targets and advances round-robin state only through valid candidates.
- Dropper spawns the exact extracted stack at the module target face and applies pickup delay. Flinger reuses that transaction, adds configured velocity, and emits muffler-aware effects.
- Player Module resolves its bound online player, handles main inventory, main inventory without hotbar, armor, and ender chest sections, applies filter/regulator rules, and refreshes its reference across login/logout.

## Testing And Delivery

Every behavior change follows RED/GREEN TDD. Focused tests use real `ItemStack`, `IInventory`, and `ISidedInventory` implementations with small fake worlds/routers only where Minecraft state is unavoidable. Each batch must pass focused tests and the complete Gradle `clean test build`, update local-only `HANDOFF.md`, launch `runClient`, and then commit/push only tracked files.

`HANDOFF.md` remains ignored and must never be staged, committed, or pushed.
