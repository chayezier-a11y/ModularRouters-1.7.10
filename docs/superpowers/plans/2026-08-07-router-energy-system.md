# Router Energy System Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox syntax for tracking.

**Goal:** Port the Modular Routers 7.5.4 multi-target binding and router RF energy subsystem to Forge 1.7.10 without breaking existing module NBT.

**Architecture:** Keep dimension-aware target data in ModuleTarget and let TargetedModule own compatible single/multi-target serialization. Isolate RF accounting in RouterEnergyStorage and EnergyTransfer, then expose it through TileEntityItemRouter and the two energy modules. Synchronize only compact telemetry and validated settings through the existing 1.7.10 container/network patterns.

**Tech Stack:** Java 8, Minecraft Forge 1.7.10, CoFH RF API, ForgeGradle 1.2, Gradle 2.14.1, JUnit 4.12.

---

## File Map

- **src/main/java/me/desht/modularrouters/logic/ModuleTarget.java**: immutable dimension, position, face and display-name value object with NBT serialization.
- **src/main/java/me/desht/modularrouters/item/module/TargetedModule.java**: one-or-many target binding, legacy fallback, capacity checks and tooltips.
- **src/main/java/me/desht/modularrouters/logic/energy/RouterEnergyStorage.java**: router RF capacity, transfer limits and hidden excess preservation.
- **src/main/java/me/desht/modularrouters/logic/energy/EnergyTransfer.java**: simulation-first RF transfer between providers, receivers and items.
- **src/main/java/me/desht/modularrouters/block/tile/TileEntityItemRouter.java**: IEnergyHandler facade, persistence, upgrade recalculation, module charging and buffer-item charging.
- **src/main/java/me/desht/modularrouters/logic/compiled/CompiledEnergyOutputModule.java**: adjacent output transfer.
- **src/main/java/me/desht/modularrouters/logic/compiled/CompiledEnergyDistributorModule.java**: fair multi-target output and beam effects.
- **src/main/java/me/desht/modularrouters/container/ContainerItemRouter.java**: server-to-client energy telemetry.
- **src/main/java/me/desht/modularrouters/gui/GuiItemRouter.java**: energy meter and direction control.
- **src/main/java/me/desht/modularrouters/network/RouterSettingsMessage.java**: bounded enum decoding and authorized direction updates.
- **src/main/java/me/desht/modularrouters/recipe/ModRecipes.java** and language files: 7.5.4 recipes and RF terminology.

### Task 1: Dimension-aware ModuleTarget

**Files:**
- Modify: **src/main/java/me/desht/modularrouters/logic/ModuleTarget.java**
- Create: **src/test/java/me/desht/modularrouters/logic/ModuleTargetTest.java**

- [x] **Step 1: Write failing value and NBT tests**

    @Test public void roundTripsDimensionPositionFaceAndName() {
        ModuleTarget source = new ModuleTarget(7, 10, 64, -3, ForgeDirection.NORTH, "tile.chest.name");
        assertEquals(source, ModuleTarget.fromNBT(source.toNBT()));
        assertEquals("tile.chest.name", ModuleTarget.fromNBT(source.toNBT()).getBlockName());
    }

    @Test public void equalityIncludesDimensionPositionAndFace() {
        ModuleTarget target = new ModuleTarget(0, 1, 2, 3, ForgeDirection.UP, "a");
        assertEquals(target, new ModuleTarget(0, 1, 2, 3, ForgeDirection.UP, "b"));
        assertNotEquals(target, new ModuleTarget(1, 1, 2, 3, ForgeDirection.UP, "a"));
        assertNotEquals(target, new ModuleTarget(0, 1, 2, 3, ForgeDirection.DOWN, "a"));
    }

- [x] **Step 2: Run the focused test and confirm RED**

    gradle -I gradle/fix-minecraft-download.gradle test --tests me.desht.modularrouters.logic.ModuleTargetTest

Expected: compilation fails because the dimension-aware constructor, NBT methods and block-name accessor do not exist.

- [x] **Step 3: Implement the immutable value object**

Add dimension and blockName fields, retain the existing five-argument constructor as dimension 0 compatibility, and add these APIs:

    public ModuleTarget(int dimension, int x, int y, int z, ForgeDirection facing, String blockName)
    public int getDimension()
    public String getBlockName()
    public boolean isSameWorld(World world)
    public NBTTagCompound toNBT()
    public static ModuleTarget fromNBT(NBTTagCompound tag)

Serialize keys Dim, X, Y, Z, Face and Name. Decode Face through a bounds-checked helper that falls back to ForgeDirection.UNKNOWN. Implement equals and hashCode from dimension, coordinates and facing only so a translated block name does not duplicate a target.

- [x] **Step 4: Run focused and full tests**

Expected: ModuleTargetTest passes, followed by all existing tests passing.

- [x] **Step 5: Update HANDOFF locally, commit tracked files, and push**

Commit message: **feat: add dimension-aware module targets**.

### Task 2: Compatible Multi-target Binding

**Files:**
- Modify: **src/main/java/me/desht/modularrouters/item/module/TargetedModule.java**
- Modify: **src/main/java/me/desht/modularrouters/item/module/EnergyDistributorModule.java**
- Modify: **src/main/java/me/desht/modularrouters/logic/compiled/CompiledModule.java**
- Create: **src/test/java/me/desht/modularrouters/item/module/TargetedModuleTest.java**

- [x] **Step 1: Write failing compatibility and capacity tests**

    @Test public void readsLegacySingleTargetIncludingDimension() {
        ItemStack stack = distributor();
        setLegacyTarget(stack, 4, 5, 6, -1);
        assertEquals(-1, TargetedModule.getTargets(stack).iterator().next().getDimension());
    }

    @Test public void storesAtMostEightDistinctTargets() {
        ItemStack stack = distributor();
        for (int i = 0; i < 9; i++) TargetedModule.addTarget(stack,
                new ModuleTarget(0, i, 64, 0, ForgeDirection.UP, "target"), 8);
        assertEquals(8, TargetedModule.getTargets(stack).size());
    }

    @Test public void clickingAnExistingTargetTogglesItOff() {
        ItemStack stack = distributor();
        ModuleTarget target = new ModuleTarget(0, 1, 2, 3, ForgeDirection.NORTH, "target");
        assertTrue(TargetedModule.toggleTarget(stack, target, 8));
        assertFalse(TargetedModule.toggleTarget(stack, target, 8));
        assertTrue(TargetedModule.getTargets(stack).isEmpty());
    }

- [x] **Step 2: Run the test and confirm RED**

Expected: compilation fails because getTargets, addTarget and toggleTarget do not exist.

- [x] **Step 3: Add single/multi-target storage**

Store new target compounds in an NBTTagList named MultiTarget. Implement:

    protected int getMaxTargets() { return 1; }
    public static Set<ModuleTarget> getTargets(ItemStack stack)
    public static void setTargets(ItemStack stack, Collection<ModuleTarget> targets)
    public static boolean addTarget(ItemStack stack, ModuleTarget target, int maximum)
    public static boolean toggleTarget(ItemStack stack, ModuleTarget target, int maximum)

When MultiTarget is absent, read TargetX, TargetY, TargetZ, TargetDim and HasTarget exactly as before. EnergyDistributorModule overrides getMaxTargets to return 8 and accepts IEnergyHandler targets instead of requiring IInventory. CompiledModule uses getTargets for multi-target modules while preserving automatic adjacent targets for ordinary directional modules.

- [x] **Step 4: Verify focused and full tests**

Expected: legacy, duplicate, toggle and eight-target tests pass; existing compiled module tests remain green.

- [x] **Step 5: Update HANDOFF locally, commit, and push**

Commit message: **feat: support energy distributor multi-target binding**.

### Task 3: RouterEnergyStorage

**Files:**
- Create: **src/main/java/me/desht/modularrouters/logic/energy/RouterEnergyStorage.java**
- Create: **src/test/java/me/desht/modularrouters/logic/energy/RouterEnergyStorageTest.java**

- [x] **Step 1: Write failing RF accounting tests**

    @Test public void preservesEnergyAboveReducedCapacity() {
        RouterEnergyStorage storage = new RouterEnergyStorage(1000, 200);
        storage.setTotalEnergyStored(900);
        storage.configure(400, 100);
        assertEquals(400, storage.getEnergyStored());
        assertEquals(500, storage.getExcessEnergy());
        storage.configure(1000, 200);
        assertEquals(900, storage.getEnergyStored());
        assertEquals(0, storage.getExcessEnergy());
    }

    @Test public void receiveAndExtractRespectTransferLimit() {
        RouterEnergyStorage storage = new RouterEnergyStorage(1000, 100);
        assertEquals(100, storage.receiveEnergy(500, false));
        assertEquals(100, storage.extractEnergy(500, false));
    }

- [x] **Step 2: Run and confirm RED**

Expected: compilation fails because RouterEnergyStorage is absent.

- [x] **Step 3: Implement storage and persistence**

Implement IEnergyStorage with non-negative int arithmetic and fields energy, excess, capacity and maxTransfer. configure first combines visible plus excess, then exposes up to the new capacity and retains the remainder as excess. readFromNBT and writeToNBT use the upstream Energy, Capacity and Excess keys; transfer rate is recalculated from upgrades. setTotalEnergyStored is package-independent so NBT loading and tests can restore total energy without bypassing bounds.

- [x] **Step 4: Verify focused and full tests**

Expected: all storage limit, simulation, shrink and restore assertions pass.

- [x] **Step 5: Update HANDOFF locally, commit, and push**

Commit message: **feat: add router RF storage**.

### Task 4: Transactional RF Transfers

**Files:**
- Create: **src/main/java/me/desht/modularrouters/logic/energy/EnergyTransfer.java**
- Create: **src/test/java/me/desht/modularrouters/logic/energy/EnergyTransferTest.java**

- [x] **Step 1: Write failing transfer tests using small fake RF endpoints**

    @Test public void commitsOnlyWhatReceiverAccepts() {
        FakeProvider source = new FakeProvider(500);
        FakeReceiver target = new FakeReceiver(120);
        assertEquals(120, EnergyTransfer.move(source, ForgeDirection.EAST,
                target, ForgeDirection.WEST, 300));
        assertEquals(380, source.energy);
        assertEquals(120, target.energy);
    }

    @Test public void zeroAndRejectedTransfersDoNotDrainSource() {
        FakeProvider source = new FakeProvider(500);
        FakeReceiver target = new FakeReceiver(0);
        assertEquals(0, EnergyTransfer.move(source, ForgeDirection.EAST,
                target, ForgeDirection.WEST, 300));
        assertEquals(500, source.energy);
    }

- [x] **Step 2: Run and confirm RED**

Expected: compilation fails because EnergyTransfer.move is absent.

- [x] **Step 3: Implement simulation-first transfer**

    int available = source.extractEnergy(sourceSide, limit, true);
    int accepted = target.receiveEnergy(targetSide, available, true);
    int extracted = source.extractEnergy(sourceSide, accepted, false);
    return target.receiveEnergy(targetSide, extracted, false);

Provide overloads for IEnergyContainerItem to router and router to item. Reject null endpoints, UNKNOWN directions, non-positive limits and disconnected sides. If a receiver commits less than its simulation result, return the committed amount and restore any recoverable difference to the source.

- [x] **Step 4: Verify focused and full tests**

Expected: partial acceptance, simulation and rejection tests pass without RF loss.

- [x] **Step 5: Update HANDOFF locally, commit, and push**

Commit message: **feat: add transactional RF transfers**.

### Task 5: Router RF Integration

**Files:**
- Modify: **src/main/java/me/desht/modularrouters/block/tile/TileEntityItemRouter.java**
- Create: **src/test/java/me/desht/modularrouters/block/tile/TileEntityItemRouterEnergyTest.java**

- [x] **Step 1: Write failing router policy tests**

Test that one energy upgrade configures Config.fePerEnergyUpgrade capacity and Config.feXferPerEnergyUpgrade transfer, external CoFH RF input/output remains bidirectional while EnergyDirection controls only the buffer item, NBT round-trips direction plus total RF, a module with insufficient RF does not execute, and an RF buffer item is charged or drained according to direction.

Use a test subclass exposing recompile and one fake CompiledModule whose execute counter proves the insufficient-energy path does not run.

- [x] **Step 2: Run and confirm RED**

Expected: compilation fails because TileEntityItemRouter does not implement IEnergyHandler and lacks EnergyDirection accessors.

- [x] **Step 3: Implement IEnergyHandler and energy policy**

Add enum values FROM_ROUTER, TO_ROUTER and NONE; a RouterEnergyStorage field; NBT keys EnergyBuffer and EnergyDirection; accessors getEnergyStored, getMaxEnergyStored, getEnergyXferRate, getEnergyDirection and setEnergyDirection. canConnectEnergy requires positive upgrade-provided transfer capacity; receive and extract remain bidirectional like 7.5.4 and delegate only while the router redstone policy allows operation.

During upgrade compilation call:

    energyStorage.configure(
        getUpgradeCount(UpgradeType.ENERGY) * Config.fePerEnergyUpgrade,
        getUpgradeCount(UpgradeType.ENERGY) * Config.feXferPerEnergyUpgrade);

Before executing a compiled module, simulate extraction of getEnergyCost. Execute only if the full cost is available, and commit that cost only after execute returns true. Once per router tick, move RF between an IEnergyContainerItem in the buffer and router storage according to direction and transfer rate. Mark dirty only when RF or direction changes.

- [x] **Step 4: Verify focused and full tests**

Expected: direction, capacity, persistence, module-cost and buffer-item tests pass.

- [x] **Step 5: Update HANDOFF locally, commit, and push**

Commit message: **feat: integrate RF storage with item routers**.

### Task 6: Energy Output And Distributor Execution

**Files:**
- Modify: **src/main/java/me/desht/modularrouters/logic/compiled/CompiledEnergyOutputModule.java**
- Modify: **src/main/java/me/desht/modularrouters/logic/compiled/CompiledEnergyDistributorModule.java**
- Modify: **src/main/java/me/desht/modularrouters/item/module/EnergyDistributorModule.java**
- Modify: **src/main/java/me/desht/modularrouters/block/tile/TileEntityItemRouter.java**
- Create: **src/test/java/me/desht/modularrouters/logic/compiled/CompiledEnergyModuleTest.java**

- [x] **Step 1: Write failing execution tests**

Cover adjacent output capped by router transfer rate; distributor splitting stored RF by valid target count; wrong-dimension, squared-range, unloaded-chunk and non-IEnergyHandler rejection; successful transfer returning true; zero transfer returning false; and two muffler upgrades suppressing beams while one allows color 0xE04040.

- [x] **Step 2: Run and confirm RED**

Expected: tests fail because both execute methods return false.

- [x] **Step 3: Implement module execution**

Energy Output resolves the target TileEntity without loading chunks, verifies IEnergyReceiver and side connectivity, then calls EnergyTransfer.move with router.getEnergyXferRate.

Energy Distributor filters getTargets by dimension, loaded chunk and distance squared. Divide current visible RF by the number of valid targets, cap each move by router transfer rate, and transfer in deterministic target-list order. On positive transfer, send ParticleBeamMessage from router center to target center to nearby players unless muffler count is at least two. Return true only when total sent is positive.

- [x] **Step 4: Verify focused and full tests**

Expected: transfer totals and validation tests pass, and all module regressions remain green.

- [x] **Step 5: Update HANDOFF locally, commit, and push**

Commit message: **feat: implement router energy modules**.

### Task 7: Energy Telemetry And Direction GUI

**Files:**
- Modify: **src/main/java/me/desht/modularrouters/container/ContainerItemRouter.java**
- Modify: **src/main/java/me/desht/modularrouters/gui/GuiItemRouter.java**
- Modify: **src/main/java/me/desht/modularrouters/network/RouterSettingsMessage.java**
- Create: **src/main/java/me/desht/modularrouters/gui/widgets/EnergyWidget.java**
- Create: **src/test/java/me/desht/modularrouters/network/RouterSettingsMessageTest.java**

- [ ] **Step 1: Write failing packet validation and meter-math tests**

Verify invalid enum bytes decode to NONE, only a permitted player within use range can alter settings, telemetry combines low/high 16-bit values without sign extension, and EnergyWidget calculates zero height at empty and full height at capacity.

- [ ] **Step 2: Run and confirm RED**

Expected: tests fail because direction is not serialized and EnergyWidget is absent.

- [ ] **Step 3: Add 1.7.10 telemetry and controls**

ContainerItemRouter implements addCraftingToCrafters and detectAndSendChanges, sending four progress bars for stored/max low and high halves. updateProgressBar reconstructs client values with value and 0xffff. EnergyWidget draws the vertical fill from router.png and shows localized current/max RF tooltip.

Add a three-state textured cycler beside the existing redstone/eco buttons. sendRouterSettings copies all three current controls before sending. RouterSettingsMessage writes one direction byte, bounds-checks both enum bytes, and keeps the existing permission plus distance validation in Handler.

- [ ] **Step 4: Verify tests and launch the GUI**

Expected: packet and meter tests pass; in runClient the meter updates without reopening and the direction button cycles all three states without overlapping slots or labels.

- [ ] **Step 5: Update HANDOFF locally, commit, and push**

Commit message: **feat: add router energy controls and telemetry**.

### Task 8: Recipes, Language And End-to-end Verification

**Files:**
- Modify: **src/main/java/me/desht/modularrouters/recipe/ModRecipes.java**
- Modify: **src/main/resources/assets/modularrouters/lang/en_US.lang**
- Modify: **src/main/resources/assets/modularrouters/lang/zh_CN.lang**
- Create: **src/test/java/me/desht/modularrouters/recipe/EnergyRecipeTest.java**

- [ ] **Step 1: Write failing recipe assertions**

Assert Energy Output uses rows space-R-space, G-B-G, space-Q-space; Energy Distributor is shapeless Energy Output plus Distributor; and Energy Upgrade uses Q-R-Q, space-B-space, Q-G-Q. Resolve R as redstone, G as gold ingot, B as blank module and Q as quartz.

- [ ] **Step 2: Run and confirm RED**

Expected: recipe assertions fail because energy recipes are absent or return null.

- [ ] **Step 3: Register recipes and language keys**

Register the three 7.5.4 recipes and add English/Chinese strings for RF stored, RF capacity, FROM_ROUTER, TO_ROUTER, NONE, target added/removed/full, wrong dimension, out of range and unloaded target. Keep FE wording only in config keys whose names are part of compatibility.

- [ ] **Step 4: Run final automated verification**

    $env:JAVA_HOME = "C:\Program Files\Zulu\zulu-8"
    & "C:\Users\chaye\.gradle\wrapper\dists\gradle-2.14.1-bin\2r579t5wehc7ew5kc8vfqezww\gradle-2.14.1\bin\gradle.bat" -I .\gradle\fix-minecraft-download.gradle clean test build

Expected: BUILD SUCCESSFUL and a rebuilt jar under build/libs.

- [ ] **Step 5: Launch and manually verify**

Run runClient with JDK 8. Verify a router with energy upgrades receives and emits RF according to direction; removing and restoring upgrades preserves total RF; Energy Output powers one adjacent receiver; Energy Distributor binds at most eight same-dimension targets, skips unloaded or distant targets, divides output, and renders red beams unless muffled.

- [ ] **Step 6: Update HANDOFF locally, commit tracked files, and push**

Commit message: **feat: finish router energy system port**. Confirm git status never lists HANDOFF.md as staged and origin/main contains only tracked implementation, tests, resources and plan documents.

## Self-review Record

- Spec coverage: target dimensions, eight-target binding, legacy NBT, excess RF, directional IO, energy costs, buffer items, both energy modules, validation, beams, GUI sync, recipes, language, build and client verification are assigned to Tasks 1-8.
- Placeholder scan: no deferred implementation markers or unspecified error-handling steps remain.
- Type consistency: ModuleTarget dimension/position/face APIs feed TargetedModule and compiled modules; RouterEnergyStorage implements CoFH IEnergyStorage; TileEntityItemRouter implements IEnergyHandler; EnergyDirection names and packet order are identical in router, GUI and network code.
