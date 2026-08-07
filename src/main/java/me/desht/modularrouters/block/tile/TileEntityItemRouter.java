package me.desht.modularrouters.block.tile;

import cofh.api.energy.IEnergyContainerItem;
import cofh.api.energy.IEnergyHandler;
import com.google.common.collect.Sets;
import me.desht.modularrouters.ModularRouters;
import me.desht.modularrouters.block.BlockItemRouter;
import me.desht.modularrouters.block.ModBlocks;
import me.desht.modularrouters.config.Config;
import me.desht.modularrouters.container.handler.BufferHandler;
import me.desht.modularrouters.container.handler.ModuleHandler;
import me.desht.modularrouters.item.ModItems;
import me.desht.modularrouters.item.module.DetectorModule;
import me.desht.modularrouters.item.module.FluidModule1;
import me.desht.modularrouters.item.module.ItemModule;
import me.desht.modularrouters.item.module.Module;
import me.desht.modularrouters.item.upgrade.ItemUpgrade;
import me.desht.modularrouters.item.upgrade.ItemUpgrade.UpgradeType;
import me.desht.modularrouters.item.upgrade.Upgrade;
import me.desht.modularrouters.logic.RouterRedstoneBehaviour;
import me.desht.modularrouters.logic.compiled.CompiledModule;
import me.desht.modularrouters.logic.energy.EnergyTransfer;
import me.desht.modularrouters.logic.energy.RouterEnergyStorage;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import org.apache.commons.lang3.tuple.Pair;

import java.util.*;

public class TileEntityItemRouter extends TileEntity implements ISidedInventory, IEnergyHandler {
    public static final int N_MODULE_SLOTS = 9;
    public static final int N_UPGRADE_SLOTS = 5;
    public static final int BUFFER_SLOT = 0;

    public static final int COMPILE_MODULES = 0x01;
    public static final int COMPILE_UPGRADES = 0x02;

    private static final String NBT_ACTIVE = "Active";
    private static final String NBT_ECO_MODE = "EcoMode";
    private static final String NBT_SIDES = "Sides";
    private static final String NBT_PERMITTED = "Permitted";
    private static final String NBT_BUFFER = "Buffer";
    private static final String NBT_MODULES = "Modules";
    private static final String NBT_UPGRADES = "Upgrades";
    private static final String NBT_EXTRA = "Extra";
    private static final String NBT_REDSTONE_MODE = "Redstone";
    private static final String NBT_TICK_RATE = "TickRate";
    private static final String NBT_ITEMS_PER_TICK = "ItemsPerTick";
    private static final String NBT_ENERGY = "EnergyBuffer";
    private static final String NBT_ENERGY_DIRECTION = "EnergyDirection";

    private int counter = 0;
    private int pulseCounter = 0;

    private RouterRedstoneBehaviour redstoneBehaviour = RouterRedstoneBehaviour.ALWAYS;

    private final BufferHandler bufferHandler = new BufferHandler();
    private final ModuleHandler modulesHandler = new ModuleHandler(N_MODULE_SLOTS);
    private final ModuleHandler upgradesHandler = new ModuleHandler(N_UPGRADE_SLOTS, 64);

    private final List<CompiledModule> compiledModules = new ArrayList<CompiledModule>();
    private byte recompileNeeded = COMPILE_MODULES | COMPILE_UPGRADES;
    private int tickRate = Config.baseTickRate;
    private int itemsPerTick = 1;
    private final int[] upgradeCount = new int[UpgradeType.values().length];
    private int totalUpgradeCount;
    private int moduleCount;

    private int fluidTransferRate;
    private int fluidTransferRemainingIn = 0;
    private int fluidTransferRemainingOut = 0;

    private final RouterEnergyStorage energyStorage = new RouterEnergyStorage(0, 0);
    private EnergyDirection energyDirection = EnergyDirection.FROM_ROUTER;

    private static final int SIDES = 6;
    private final int[] redstoneLevels = new int[SIDES];
    private final int[] newRedstoneLevels = new int[SIDES];
    private final DetectorModule.SignalType[] signalType = new DetectorModule.SignalType[SIDES];
    private final DetectorModule.SignalType[] newSignalType = new DetectorModule.SignalType[SIDES];
    private boolean canEmit, prevCanEmit;

    private final Map<UUID, Pair<Integer, Integer>> playerToSlot = new HashMap<UUID, Pair<Integer, Integer>>();

    private int redstonePower = -1;
    private int lastPower;
    private boolean active;
    private int activeTimer = 0;
    private final Set<UUID> permitted = Sets.newHashSet();
    private byte sidesOpen;
    private boolean ecoMode = false;
    private int ecoCounter = Config.ecoTimeout;
    private boolean hasPulsedModules = false;
    private NBTTagCompound extData;
    private Block camouflage = null;
    private int camouflageMeta = 0;
    private int tunedSyncValue = -1;
    private boolean executing;

    public TileEntityItemRouter() {
        super();
        bufferHandler.setDirtyCallback(new Runnable() { public void run() { markDirty(); } });
        modulesHandler.setDirtyCallback(new Runnable() { public void run() { recompileNeeded(COMPILE_MODULES); markDirty(); } });
        upgradesHandler.setDirtyCallback(new Runnable() { public void run() { recompileNeeded(COMPILE_UPGRADES); markDirty(); } });
        for (int i = 0; i < SIDES; i++) {
            signalType[i] = DetectorModule.SignalType.NONE;
            newSignalType[i] = DetectorModule.SignalType.NONE;
        }
    }

    @Override
    public boolean canUpdate() {
        return true;
    }

    // ---- ISidedInventory implementation ----

    @Override
    public int[] getSlotsForFace(int side) {
        return new int[] { BUFFER_SLOT };
    }

    @Override
    public boolean canInsertItem(int slot, ItemStack stack, int side) {
        return slot == BUFFER_SLOT && isSideOpen(side);
    }

    @Override
    public boolean canExtractItem(int slot, ItemStack stack, int side) {
        return slot == BUFFER_SLOT && isSideOpen(side);
    }

    // ---- IInventory implementation ----

    @Override
    public int getSizeInventory() {
        return 1;
    }

    @Override
    public ItemStack getStackInSlot(int slot) {
        return bufferHandler.getStackInSlot(0);
    }

    @Override
    public ItemStack decrStackSize(int slot, int amount) {
        return bufferHandler.decrStackSize(0, amount);
    }

    @Override
    public ItemStack getStackInSlotOnClosing(int slot) {
        return bufferHandler.getStackInSlotOnClosing(0);
    }

    @Override
    public void setInventorySlotContents(int slot, ItemStack stack) {
        bufferHandler.setInventorySlotContents(0, stack);
    }

    @Override
    public String getInventoryName() {
        return "itemRouter";
    }

    @Override
    public boolean isCustomInventoryName() {
        return false;
    }

    @Override
    public int getInventoryStackLimit() {
        return 64;
    }

    @Override
    public boolean isUseableByPlayer(EntityPlayer player) {
        return worldObj.getTileEntity(xCoord, yCoord, zCoord) == this
                && player.getDistanceSq(xCoord + 0.5, yCoord + 0.5, zCoord + 0.5) <= 64.0;
    }

    @Override
    public void openChest() {}

    @Override
    public void closeChest() {}

    @Override
    public boolean isItemValidForSlot(int slot, ItemStack stack) {
        return true;
    }

    // ---- Public API ----

    public World getWorldObj() {
        return this.worldObj;
    }

    public IInventory getBuffer() {
        return bufferHandler;
    }

    public ModuleHandler getModules() {
        return modulesHandler;
    }

    public ModuleHandler getUpgrades() {
        return upgradesHandler;
    }

    public ModuleHandler getModuleInventory() { return modulesHandler; }
    public ModuleHandler getUpgradeInventory() { return upgradesHandler; }

    public ItemStack getBufferItemStack() {
        return bufferHandler.getStackInSlot(0);
    }

    public boolean isBufferFull() {
        ItemStack stack = getBufferItemStack();
        return stack != null && stack.stackSize >= bufferHandler.getInventoryStackLimit();
    }

    public boolean isBufferEmpty() {
        return getBufferItemStack() == null;
    }

    public ItemStack peekBuffer(int amount) {
        return bufferHandler.extractItem(amount, true);
    }

    public ItemStack extractBuffer(int amount) {
        return bufferHandler.extractItem(amount, false);
    }

    public ItemStack insertBuffer(ItemStack stack) {
        return bufferHandler.insertItem(stack, false);
    }

    public int getModuleCount() {
        return moduleCount;
    }

    public int getUpgradeCount() {
        return totalUpgradeCount;
    }

    public int getUpgradeCount(UpgradeType type) {
        return upgradeCount[type.ordinal()];
    }

    public void recompileNeeded(int what) {
        recompileNeeded |= what;
    }

    public int getTickRate() {
        return ecoMode && ecoCounter == 0 ? Config.lowPowerTickRate : tickRate;
    }

    public int getItemsPerTick() {
        return itemsPerTick;
    }

    public int getEnergyCapacity() {
        return energyStorage.getMaxEnergyStored();
    }

    public int getEnergyXferRate() {
        return energyStorage.getTransferRate();
    }

    public RouterEnergyStorage getEnergyStorage() {
        return energyStorage;
    }

    public EnergyDirection getEnergyDirection() {
        return energyDirection;
    }

    public void setEnergyDirection(EnergyDirection direction) {
        EnergyDirection newDirection = direction == null ? EnergyDirection.FROM_ROUTER : direction;
        if (energyDirection != newDirection) {
            energyDirection = newDirection;
            markDirty();
        }
    }

    @Override
    public boolean canConnectEnergy(ForgeDirection from) {
        return energyStorage.getTransferRate() > 0;
    }

    @Override
    public int receiveEnergy(ForgeDirection from, int maxReceive, boolean simulate) {
        if (!canConnectEnergy(from) || !isEnergyOperationAllowed()) return 0;
        int received = energyStorage.receiveEnergy(maxReceive, simulate);
        if (received > 0 && !simulate) markDirty();
        return received;
    }

    @Override
    public int extractEnergy(ForgeDirection from, int maxExtract, boolean simulate) {
        if (!canConnectEnergy(from) || !isEnergyOperationAllowed()) return 0;
        int extracted = energyStorage.extractEnergy(maxExtract, simulate);
        if (extracted > 0 && !simulate) markDirty();
        return extracted;
    }

    @Override
    public int getEnergyStored(ForgeDirection from) {
        return energyStorage.getEnergyStored();
    }

    @Override
    public int getMaxEnergyStored(ForgeDirection from) {
        return energyStorage.getMaxEnergyStored();
    }

    private boolean isEnergyOperationAllowed() {
        if (redstoneBehaviour == RouterRedstoneBehaviour.ALWAYS) return true;
        if (redstoneBehaviour == RouterRedstoneBehaviour.NEVER || worldObj == null) return false;
        return redstoneBehaviour.shouldRun(getRedstonePower() > 0, false);
    }

    public enum EnergyDirection {
        FROM_ROUTER,
        TO_ROUTER,
        NONE;

        static EnergyDirection fromName(String name) {
            try {
                return valueOf(name);
            } catch (IllegalArgumentException e) {
                return FROM_ROUTER;
            }
        }
    }

        public boolean careAboutItemAttributes() {
        for (CompiledModule cm : compiledModules) {
            if (cm.careAboutItemAttributes()) return true;
        }
        return false;
    }

    public RouterRedstoneBehaviour getRedstoneBehaviour() {
        return redstoneBehaviour;
    }

    public void setRedstoneBehaviour(RouterRedstoneBehaviour behaviour) {
        this.redstoneBehaviour = behaviour;
        if (behaviour == RouterRedstoneBehaviour.PULSE) {
            lastPower = getRedstonePower();
        }
        handleSync(false);
    }

    public boolean isActive() {
        return active;
    }

    public boolean isSideOpen(int side) {
        ForgeDirection dir = ForgeDirection.getOrientation(side);
        return (sidesOpen & (1 << dir.ordinal())) != 0;
    }

    public boolean isSideOpen(Module.RelativeDirection side) {
        return (sidesOpen & side.getMask()) != 0;
    }

    public void setEcoMode(boolean newEco) {
        if (newEco != ecoMode) {
            ecoMode = newEco;
            ecoCounter = Config.ecoTimeout;
            handleSync(false);
        }
    }

    public Block getCamouflage() {
        return camouflage;
    }

    public int getCamouflageMeta() {
        return camouflageMeta;
    }

    public void setCamouflage(Block newCamo, int newMeta) {
        if (newCamo != camouflage || newMeta != camouflageMeta) {
            camouflage = newCamo;
            camouflageMeta = newMeta;
            handleSync(true);
        }
    }

    public boolean getEcoMode() {
        return ecoMode;
    }

    public void setHasPulsedModules(boolean hasPulsedModules) {
        this.hasPulsedModules = hasPulsedModules;
    }

    public void setTunedSyncValue(int newValue) {
        tunedSyncValue = newValue;
    }

    public int getEffectiveRange(int base, int min, int max) {
        return base;
    }

    public ForgeDirection getAbsoluteFacing(Module.RelativeDirection direction) {
        int meta = worldObj.getBlockMetadata(xCoord, yCoord, zCoord);
        ForgeDirection facing = ForgeDirection.getOrientation(meta);
        return direction.toForgeDirection(facing);
    }

    public void playerConfiguringModule(EntityPlayer player, int slotIndex, int filterIndex) {
        if (slotIndex >= 0) {
            playerToSlot.put(player.getUniqueID(), Pair.of(slotIndex, filterIndex));
        } else {
            playerToSlot.remove(player.getUniqueID());
        }
    }

    public void playerConfiguringModule(EntityPlayer player, int slotIndex) {
        playerConfiguringModule(player, slotIndex, -1);
    }

    public void clearConfigSlot(EntityPlayer player) {
        playerToSlot.remove(player.getUniqueID());
    }

    public int getModuleConfigSlot(EntityPlayer player) {
        if (playerToSlot.containsKey(player.getUniqueID())) {
            return playerToSlot.get(player.getUniqueID()).getLeft();
        }
        return -1;
    }

    public int getFilterConfigSlot(EntityPlayer player) {
        if (playerToSlot.containsKey(player.getUniqueID())) {
            return playerToSlot.get(player.getUniqueID()).getRight();
        }
        return -1;
    }

    public void addPermittedIds(Set<UUID> permittedIds) {
        this.permitted.addAll(permittedIds);
    }

    public boolean isPermitted(EntityPlayer player) {
        if (permitted.isEmpty() || permitted.contains(player.getUniqueID())) {
            return true;
        }
        if (player.getHeldItem() != null && player.getHeldItem().getItem() == ModItems.overrideCard) {
            return true;
        }
        return false;
    }

    public NBTTagCompound getExtData() {
        if (extData == null) {
            extData = new NBTTagCompound();
        }
        return extData;
    }

    // ---- Fluid transfer ----

    public int getFluidTransferRate() {
        return fluidTransferRate;
    }

    private void allocateFluidTransfer(int ticks) {
        int maxTransfer = Config.baseTickRate * fluidTransferRate;
        fluidTransferRemainingIn = Math.min(fluidTransferRemainingIn + ticks * fluidTransferRate, maxTransfer);
        fluidTransferRemainingOut = Math.min(fluidTransferRemainingOut + ticks * fluidTransferRate, maxTransfer);
    }

    public int getCurrentFluidTransferAllowance(FluidModule1.FluidDirection dir) {
        return dir == FluidModule1.FluidDirection.IN ? fluidTransferRemainingIn : fluidTransferRemainingOut;
    }

    public void transferredFluid(int amount, FluidModule1.FluidDirection dir) {
        switch (dir) {
            case IN:
                fluidTransferRemainingIn = Math.max(0, fluidTransferRemainingIn - amount);
                break;
            case OUT:
                fluidTransferRemainingOut = Math.max(0, fluidTransferRemainingOut - amount);
                break;
        }
    }

    // ---- Redstone emission ----

    public void setAllowRedstoneEmission(boolean allow) {
        canEmit = allow;
    }

    public void emitRedstone(Module.RelativeDirection direction, int power, DetectorModule.SignalType signalType) {
        if (direction == Module.RelativeDirection.NONE) {
            Arrays.fill(newRedstoneLevels, power);
            Arrays.fill(newSignalType, signalType);
        } else {
            ForgeDirection facing = getAbsoluteFacing(direction).getOpposite();
            newRedstoneLevels[facing.ordinal()] = power;
            newSignalType[facing.ordinal()] = signalType;
        }
    }

    public int getRedstoneLevel(int side, boolean strong) {
        if (!canEmit) return -1;
        if (strong) {
            return signalType[side] == DetectorModule.SignalType.STRONG ? redstoneLevels[side] : 0;
        } else {
            return signalType[side] != DetectorModule.SignalType.NONE ? redstoneLevels[side] : 0;
        }
    }

    public int getRedstonePower() {
        if (redstonePower < 0) {
            redstonePower = worldObj.isBlockIndirectlyGettingPowered(xCoord, yCoord, zCoord) ? 15 : 0;
        }
        return redstonePower;
    }

    private void handleRedstoneEmission() {
        boolean notifyOwnNeighbours = false;
        EnumSet<ForgeDirection> toNotify = EnumSet.noneOf(ForgeDirection.class);

        if (!canEmit) {
            notifyOwnNeighbours = true;
            for (ForgeDirection f : ForgeDirection.VALID_DIRECTIONS) {
                if (signalType[f.ordinal()] == DetectorModule.SignalType.STRONG) {
                    toNotify.add(f.getOpposite());
                }
            }
            Arrays.fill(redstoneLevels, 0);
            Arrays.fill(signalType, DetectorModule.SignalType.NONE);
        } else {
            for (ForgeDirection facing : ForgeDirection.VALID_DIRECTIONS) {
                int i = facing.ordinal();
                if (newSignalType[i] != signalType[i]) {
                    toNotify.add(facing.getOpposite());
                    signalType[i] = newSignalType[i];
                }
                if (newRedstoneLevels[i] != redstoneLevels[i]) {
                    notifyOwnNeighbours = true;
                    if (newSignalType[i] == DetectorModule.SignalType.STRONG) {
                        toNotify.add(facing.getOpposite());
                    }
                    redstoneLevels[i] = newRedstoneLevels[i];
                }
            }
        }

        for (ForgeDirection f : toNotify) {
            int nx = xCoord + f.offsetX;
            int ny = yCoord + f.offsetY;
            int nz = zCoord + f.offsetZ;
            worldObj.notifyBlockOfNeighborChange(nx, ny, nz, ModBlocks.itemRouter);
        }
        if (notifyOwnNeighbours) {
            worldObj.notifyBlockOfNeighborChange(xCoord, yCoord, zCoord, ModBlocks.itemRouter);
        }
    }

    // ---- Core update logic ----

    @Override
    public void updateEntity() {
        if (recompileNeeded != 0) {
            compile();
        }

        if (worldObj.isRemote) {
            return;
        }

        counter++;
        pulseCounter++;

        if (getRedstoneBehaviour() == RouterRedstoneBehaviour.PULSE) {
            if (activeTimer > 0) {
                if (--activeTimer == 0) {
                    setActive(false);
                }
            }
        } else {
            if (counter >= getTickRate()) {
                allocateFluidTransfer(counter);
                executeModules(false);
                counter = 0;
            }
        }

        if (ecoMode) {
            if (active) {
                ecoCounter = Config.ecoTimeout;
            } else if (ecoCounter > 0) {
                ecoCounter--;
            }
        }

        transferBufferEnergy();
    }

    void transferBufferEnergy() {
        ItemStack stack = getBufferItemStack();
        if (stack == null || !(stack.getItem() instanceof IEnergyContainerItem)
                || getEnergyXferRate() <= 0 || !isEnergyOperationAllowed()) return;

        IEnergyContainerItem item = (IEnergyContainerItem) stack.getItem();
        int moved = 0;
        if (energyDirection == EnergyDirection.FROM_ROUTER) {
            moved = EnergyTransfer.moveToItem(this, ForgeDirection.UP,
                    item, stack, getEnergyXferRate());
        } else if (energyDirection == EnergyDirection.TO_ROUTER) {
            moved = EnergyTransfer.moveFromItem(item, stack,
                    this, ForgeDirection.UP, getEnergyXferRate());
        }
        if (moved > 0) markDirty();
    }

    private void executeModules(boolean pulsed) {
        executing = true;
        boolean newActive = false;
        boolean powered = pulsed || getRedstonePower() > 0;

        if (redstoneBehaviour.shouldRun(powered, pulsed)) {
            if (prevCanEmit || canEmit) {
                Arrays.fill(newRedstoneLevels, 0);
                Arrays.fill(newSignalType, DetectorModule.SignalType.NONE);
            }
            for (CompiledModule cm : compiledModules) {
                if (cm != null && cm.hasTarget() && cm.shouldRun(powered, pulsed)
                        && tryExecuteEnergyModule(cm)) {
                    newActive = true;
                    if (cm.termination() != me.desht.modularrouters.util.ModuleHelper.Termination.NONE) {
                        break;
                    }
                }
            }
            if (prevCanEmit || canEmit) {
                handleRedstoneEmission();
            }
        }
        setActive(newActive);
        prevCanEmit = canEmit;
        executing = false;
    }

    boolean tryExecuteEnergyModule(CompiledModule module) {
        int cost = Math.max(0, module.getEnergyCost());
        if (energyStorage.getEnergyStored() < cost || !module.execute(this)) return false;
        if (cost > 0) {
            energyStorage.consumeEnergy(cost);
            markDirty();
        }
        return true;
    }

    public void checkForRedstonePulse() {
        redstonePower = worldObj.isBlockIndirectlyGettingPowered(xCoord, yCoord, zCoord) ? 15 : 0;
        if (executing) return;
        if (redstoneBehaviour == RouterRedstoneBehaviour.PULSE
                || hasPulsedModules && redstoneBehaviour == RouterRedstoneBehaviour.ALWAYS) {
            if (redstonePower > lastPower && pulseCounter >= tickRate) {
                allocateFluidTransfer(Math.min(pulseCounter, Config.baseTickRate));
                executeModules(true);
                pulseCounter = 0;
                if (active) {
                    activeTimer = tickRate;
                }
            }
            lastPower = redstonePower;
        }
    }

    private void setActive(boolean newActive) {
        if (active != newActive) {
            active = newActive;
            handleSync(true);
        }
    }

    private void setSidesOpen(byte sidesOpen) {
        if (this.sidesOpen != sidesOpen) {
            this.sidesOpen = sidesOpen;
            handleSync(true);
        }
    }

    private void handleSync(boolean renderUpdate) {
        if (!worldObj.isRemote) {
            worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
        } else if (worldObj.isRemote && renderUpdate) {
            worldObj.markBlockForRenderUpdate(xCoord, yCoord, zCoord);
        }
    }

    // ---- Compilation ----

    private void compile() {
        if (worldObj.isRemote) return;

        compileModules();
        compileUpgrades();

        if (tunedSyncValue >= 0) {
            counter = calculateSyncCounter();
        } else if (counter < 0) {
            counter = new Random().nextInt(tickRate);
        }

        if (recompileNeeded != 0) {
            worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
            worldObj.notifyBlockOfNeighborChange(xCoord, yCoord, zCoord, ModBlocks.itemRouter);
            markDirty();
            recompileNeeded = 0;
        }
    }

    private void compileModules() {
        if ((recompileNeeded & COMPILE_MODULES) != 0) {
            setHasPulsedModules(false);
            byte newSidesOpen = 0;
            for (CompiledModule cm : compiledModules) {
                cm.cleanup(this);
            }
            compiledModules.clear();
            for (int i = 0; i < N_MODULE_SLOTS; i++) {
                ItemStack stack = modulesHandler.getStackInSlot(i);
                Module m = ItemModule.getModule(stack);
                if (m != null) {
                    CompiledModule cms = m.compile(this, stack);
                    compiledModules.add(cms);
                    cms.onCompiled(this);
                    newSidesOpen |= (byte) cms.getDirection().getMask();
                }
            }
            moduleCount = compiledModules.size();
            setSidesOpen(newSidesOpen);
        }
    }

    void compileUpgrades() {
        if ((recompileNeeded & COMPILE_UPGRADES) != 0) {
            Arrays.fill(upgradeCount, 0);
            totalUpgradeCount = 0;
            permitted.clear();
            setCamouflage(null, 0);
            tunedSyncValue = -1;
            for (int i = 0; i < N_UPGRADE_SLOTS; i++) {
                ItemStack stack = upgradesHandler.getStackInSlot(i);
                Upgrade upgrade = ItemUpgrade.getUpgrade(stack);
                if (upgrade != null) {
                    upgradeCount[stack.getMetadata()] += stack.stackSize;
                    totalUpgradeCount += stack.stackSize;
                    upgrade.onCompiled(stack, this);
                }
            }

            itemsPerTick = 1 << (Math.min(6, getUpgradeCount(UpgradeType.STACK)));
            bufferHandler.setStackLimit(64 * itemsPerTick);
            tickRate = Math.max(Config.hardMinTickRate,
                    Config.baseTickRate - Config.ticksPerUpgrade * getUpgradeCount(UpgradeType.SPEED));
            fluidTransferRate = Math.min(Config.fluidMaxTransferRate,
                    Config.fluidBaseTransferRate + getUpgradeCount(UpgradeType.FLUID) * Config.mBperFluidUpgrade);
            energyStorage.configure(
                    getUpgradeCount(UpgradeType.ENERGY) * Config.fePerEnergyUpgrade,
                    getUpgradeCount(UpgradeType.ENERGY) * Config.feXferPerEnergyUpgrade);
        }
    }

    private int calculateSyncCounter() {
        int compileTime = (int) (worldObj.getTotalWorldTime() % tickRate);
        int tuning = tunedSyncValue % tickRate;
        int delta = tuning - compileTime;
        if (delta <= 0) delta += tickRate;
        return tickRate - delta;
    }

    // ---- Static helper ----

    public static TileEntityItemRouter getRouterAt(net.minecraft.world.IBlockAccess world, int x, int y, int z) {
        TileEntity te = world.getTileEntity(x, y, z);
        return te instanceof TileEntityItemRouter ? (TileEntityItemRouter) te : null;
    }

    // ---- NBT ----

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        bufferHandler.readFromNBT(nbt.getCompoundTag(NBT_BUFFER));
        modulesHandler.readFromNBT(nbt.getCompoundTag(NBT_MODULES));
        upgradesHandler.readFromNBT(nbt.getCompoundTag(NBT_UPGRADES));
        try {
            redstoneBehaviour = RouterRedstoneBehaviour.valueOf(nbt.getString(NBT_REDSTONE_MODE));
        } catch (IllegalArgumentException e) {
            redstoneBehaviour = RouterRedstoneBehaviour.ALWAYS;
        }
        active = nbt.getBoolean(NBT_ACTIVE);
        ecoMode = nbt.getBoolean(NBT_ECO_MODE);
        if (nbt.hasKey(NBT_ENERGY)) energyStorage.readFromNBT(nbt.getCompoundTag(NBT_ENERGY));
        energyDirection = EnergyDirection.fromName(nbt.getString(NBT_ENERGY_DIRECTION));

        NBTTagCompound ext = nbt.getCompoundTag(NBT_EXTRA);
        NBTTagCompound ext1 = getExtData();
        if (ext != null) {
            for (Object keyObj : ext.getKeySet()) {
                String key = (String) keyObj;
                ext1.setTag(key, ext.getTag(key));
            }
        }

        counter = -1;
    }

    @Override
    public void writeToNBT(NBTTagCompound nbt) {
        super.writeToNBT(nbt);
        nbt.setTag(NBT_BUFFER, bufferHandler.writeToNBT(new NBTTagCompound()));
        nbt.setTag(NBT_MODULES, modulesHandler.writeToNBT(new NBTTagCompound()));
        nbt.setTag(NBT_UPGRADES, upgradesHandler.writeToNBT(new NBTTagCompound()));
        nbt.setString(NBT_REDSTONE_MODE, redstoneBehaviour.name());
        nbt.setBoolean(NBT_ACTIVE, active);
        nbt.setBoolean(NBT_ECO_MODE, ecoMode);
        nbt.setTag(NBT_ENERGY, energyStorage.writeToNBT(new NBTTagCompound()));
        nbt.setString(NBT_ENERGY_DIRECTION, energyDirection.name());

        NBTTagCompound ext = new NBTTagCompound();
        NBTTagCompound ext1 = getExtData();
        for (Object keyObj : ext1.getKeySet()) {
            String key = (String) keyObj;
            ext.setTag(key, ext1.getTag(key));
        }
        nbt.setTag(NBT_EXTRA, ext);
    }

    @Override
    public Packet getDescriptionPacket() {
        NBTTagCompound tag = getUpdateTag();
        return new S35PacketUpdateTileEntity(xCoord, yCoord, zCoord, 0, tag);
    }

    @Override
    public void onDataPacket(NetworkManager net, S35PacketUpdateTileEntity pkt) {
        processClientSync(pkt.getNbtCompound());
    }

    private NBTTagCompound getUpdateTag() {
        NBTTagCompound compound = new NBTTagCompound();
        compound.setInteger("x", xCoord);
        compound.setInteger("y", yCoord);
        compound.setInteger("z", zCoord);

        NBTTagList list = new NBTTagList();
        for (UUID u : permitted) {
            list.appendTag(new NBTTagString(u.toString()));
        }
        compound.setTag(NBT_PERMITTED, list);
        compound.setInteger(BlockItemRouter.NBT_MODULE_COUNT, getModuleCount());
        for (UpgradeType type : UpgradeType.values()) {
            compound.setInteger(BlockItemRouter.NBT_UPGRADE_COUNT + "." + type, getUpgradeCount(type));
        }
        compound.setByte(NBT_REDSTONE_MODE, (byte) redstoneBehaviour.ordinal());
        compound.setBoolean(NBT_ECO_MODE, ecoMode);
        compound.setInteger(NBT_TICK_RATE, tickRate);
        compound.setInteger(NBT_ITEMS_PER_TICK, itemsPerTick);
        compound.setBoolean(NBT_ACTIVE, active);
        compound.setByte(NBT_SIDES, sidesOpen);
        if (camouflage != null) {
            compound.setInteger("CamoBlock", Block.getIdFromBlock(camouflage));
            compound.setInteger("CamoMeta", camouflageMeta);
        }
        return compound;
    }

    private void processClientSync(NBTTagCompound compound) {
        boolean rerenderNeeded = false;

        NBTTagList l = compound.getTagList(NBT_PERMITTED, 8);
        permitted.clear();
        for (int i = 0; i < l.tagCount(); i++) {
            permitted.add(UUID.fromString(l.getStringTagAt(i)));
        }
        moduleCount = compound.getInteger(BlockItemRouter.NBT_MODULE_COUNT);
        int mufflers = getUpgradeCount(UpgradeType.MUFFLER);
        for (UpgradeType type : UpgradeType.values()) {
            upgradeCount[type.ordinal()] = compound.getInteger(BlockItemRouter.NBT_UPGRADE_COUNT + "." + type);
        }
        if (mufflers < 3 && getUpgradeCount(UpgradeType.MUFFLER) >= 3 || mufflers >= 3 && getUpgradeCount(UpgradeType.MUFFLER) < 3) {
            rerenderNeeded = true;
        }

        RouterRedstoneBehaviour newRb = RouterRedstoneBehaviour.values()[compound.getByte(NBT_REDSTONE_MODE)];
        setRedstoneBehaviour(newRb);
        tickRate = compound.getInteger(NBT_TICK_RATE);
        itemsPerTick = compound.getInteger(NBT_ITEMS_PER_TICK);

        boolean newActive = compound.getBoolean(NBT_ACTIVE);
        byte newSidesOpen = compound.getByte(NBT_SIDES);
        boolean newEco = compound.getBoolean(NBT_ECO_MODE);

        Block newCamo = null;
        int newCamoMeta = 0;
        if (compound.hasKey("CamoBlock")) {
            newCamo = Block.getBlockById(compound.getInteger("CamoBlock"));
            newCamoMeta = compound.getInteger("CamoMeta");
        }

        setActive(newActive);
        setSidesOpen(newSidesOpen);
        setEcoMode(newEco);
        setCamouflage(newCamo, newCamoMeta);

        if (rerenderNeeded) {
            worldObj.markBlockForRenderUpdate(xCoord, yCoord, zCoord);
        }
    }
}
