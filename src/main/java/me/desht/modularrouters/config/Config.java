package me.desht.modularrouters.config;

import cpw.mods.fml.client.event.ConfigChangedEvent;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import me.desht.modularrouters.ModularRouters;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

public class Config {

    public static class Defaults {
        static final int BASE_TICK_RATE = 20;
        static final int TICKS_PER_UPGRADE = 2;
        static final int SENDER1_BASE_RANGE = 8;
        static final int SENDER1_MAX_RANGE = SENDER1_BASE_RANGE * 2;
        static final int SENDER2_BASE_RANGE = 24;
        static final int SENDER2_MAX_RANGE = SENDER2_BASE_RANGE * 2;
        static final int PULLER2_BASE_RANGE = 12;
        static final int PULLER2_MAX_RANGE = PULLER2_BASE_RANGE * 2;
        static final int VACUUM_BASE_RANGE = 6;
        static final int VACUUM_MAX_RANGE = VACUUM_BASE_RANGE * 2;
        static final int FLUID_BASE_TRANSFER = 50;
        static final int FLUID_MAX_TRANSFER = 400;
        static final int MB_PER_FLUID_UPGRADE = 10;
        static final boolean SENDER_PARTICLES = true;
        static final boolean PULLER_PARTICLES = true;
        static final boolean VACUUM_PARTICLES = true;
        static final boolean PLACER_PARTICLES = true;
        static final boolean BREAKER_PARTICLES = true;
        static final boolean FLINGER_EFFECTS = true;
        static final boolean EXTRUDER_SOUND = true;
        static final char CONFIG_KEY = 'c';
        static final int ECO_TIMEOUT = 300;
        static final int LOW_POWER_INTERVAL = 100;
        static final int EXTRUDER_BASE_RANGE = 12;
        static final int EXTRUDER_MAX_RANGE = EXTRUDER_BASE_RANGE * 2;
        static final int EXTRUDER2_BASE_RANGE = 24;
        static final int EXTRUDER2_MAX_RANGE = EXTRUDER2_BASE_RANGE * 2;
        static final int FLUID2_BASE_RANGE = 16;
        static final int FLUID2_MAX_RANGE = 32;
        static final int FE_PER_ENERGY_UPGRADE = 50000;
        static final int FE_XFER_PER_UPGRADE = 100;
        static final boolean EXTRUDER_PUSH_ENTITIES = true;
        static final boolean BREAKER_HARVEST_LEVEL_LIMIT = true;
    }

    public static char configKey;
    public static int baseTickRate;
    public static int ticksPerUpgrade;
    public static int hardMinTickRate;
    public static int ecoTimeout;
    public static int lowPowerTickRate;
    public static int sender1BaseRange;
    public static int sender1MaxRange;
    public static int sender2BaseRange;
    public static int sender2MaxRange;
    public static int vacuumBaseRange;
    public static int vacuumMaxRange;
    public static int extruderBaseRange;
    public static int extruderMaxRange;
    public static int extruder2BaseRange;
    public static int extruder2MaxRange;
    public static int puller2MaxRange;
    public static int puller2BaseRange;
    public static int fluidBaseTransferRate;
    public static int fluidMaxTransferRate;
    public static int mBperFluidUpgrade;
    public static boolean senderParticles;
    public static boolean pullerParticles;
    public static boolean breakerParticles;
    public static boolean flingerEffects;
    public static boolean extruderSound;
    public static boolean placerParticles;
    public static boolean vacuumParticles;
    public static int fluid2BaseRange;
    public static int fluid2MaxRange;
    public static int fePerEnergyUpgrade;
    public static int feXferPerEnergyUpgrade;
    public static boolean extruderPushEntities;
    public static boolean breakerHarvestLevelLimit;

    static final String CATEGORY_ROUTER = "category_router";
    static final String CATEGORY_MODULE = "category_module";
    private static Configuration config;

    public static Configuration getConfig() {
        return config;
    }

    private static final Pattern oneCharPattern = Pattern.compile("^.$");

    public static void preInit() {
        File configFile = new File(Loader.instance().getConfigDir(), "modularrouters.cfg");
        config = new Configuration(configFile);
        syncFromFile();
    }

    public static void syncFromFile() {
        syncConfig(true, true);
    }

    public static void syncFromGUI() {
        syncConfig(false, true);
    }

    private static void syncConfig(boolean loadConfigFromFile, boolean readFieldsFromConfig) {
        if (loadConfigFromFile) {
            config.load();
        }

        Property propConfigKey = config.get(CATEGORY_ROUTER, "configKey", String.valueOf(Defaults.CONFIG_KEY),
                "Keypress to configure installed modules in-place", oneCharPattern);
        Property propBaseTickRate = config.get(CATEGORY_ROUTER, "baseTickRate", Defaults.BASE_TICK_RATE,
                "Base router tick rate", 1, Integer.MAX_VALUE);
        Property propTicksPerUpgrade = config.get(CATEGORY_ROUTER, "ticksPerUpgrade", Defaults.TICKS_PER_UPGRADE,
                "Tick rate reduction per upgrade installed", 2, 10);
        Property propHardMinTicks = config.get(CATEGORY_ROUTER, "hardMinTicks", 2,
                "Hard minimum tick rate", 1, Integer.MAX_VALUE);
        Property propEcoTimeout = config.get(CATEGORY_ROUTER, "ecoTimeout", Defaults.ECO_TIMEOUT,
                "Idle time (ticks) before an eco-mode router goes into low power mode", 20, Integer.MAX_VALUE);
        Property propLowPowerTickRate = config.get(CATEGORY_ROUTER, "lowPowerTickRate", Defaults.LOW_POWER_INTERVAL,
                "Activation interval (ticks) for a low-power eco-mode router", 20, Integer.MAX_VALUE);

        Property propSender1BaseRange = config.get(CATEGORY_MODULE, "sender1BaseRange", Defaults.SENDER1_BASE_RANGE,
                "Sender Module Mk1 Base Range", 1, Integer.MAX_VALUE);
        Property propSender1MaxRange = config.get(CATEGORY_MODULE, "sender1MaxRange", Defaults.SENDER1_MAX_RANGE,
                "Sender Module Mk1 Max Range", 1, Integer.MAX_VALUE);
        Property propSender2BaseRange = config.get(CATEGORY_MODULE, "sender2BaseRange", Defaults.SENDER2_BASE_RANGE,
                "Sender Module Mk2 Base Range", 1, Integer.MAX_VALUE);
        Property propSender2MaxRange = config.get(CATEGORY_MODULE, "sender2MaxRange", Defaults.SENDER2_MAX_RANGE,
                "Sender Module Mk2 Max Range", 1, Integer.MAX_VALUE);
        Property propVacuumBaseRange = config.get(CATEGORY_MODULE, "vacuumBaseRange", Defaults.VACUUM_BASE_RANGE,
                "Vacuum Module Base Range", 1, Integer.MAX_VALUE);
        Property propVacuumMaxRange = config.get(CATEGORY_MODULE, "vacuumMaxRange", Defaults.VACUUM_MAX_RANGE,
                "Vacuum Module Max Range", 1, Integer.MAX_VALUE);
        Property propExtruderBaseRange = config.get(CATEGORY_MODULE, "extruderBaseRange", Defaults.EXTRUDER_BASE_RANGE,
                "Extruder Module Base Range", 1, Integer.MAX_VALUE);
        Property propExtruderMaxRange = config.get(CATEGORY_MODULE, "extruderMaxRange", Defaults.EXTRUDER_MAX_RANGE,
                "Extruder Module Max Range", 1, Integer.MAX_VALUE);
        Property propPuller2BaseRange = config.get(CATEGORY_MODULE, "puller2BaseRange", Defaults.PULLER2_BASE_RANGE,
                "Puller Module Mk2 Base Range", 1, Integer.MAX_VALUE);
        Property propPuller2MaxRange = config.get(CATEGORY_MODULE, "puller2MaxRange", Defaults.PULLER2_MAX_RANGE,
                "Puller Module Mk2 Max Range", 1, Integer.MAX_VALUE);
        Property propExtruder2BaseRange = config.get(CATEGORY_MODULE, "extruder2BaseRange", Defaults.EXTRUDER2_BASE_RANGE,
                "Extruder Module Mk2 Base Range", 1, Integer.MAX_VALUE);
        Property propExtruder2MaxRange = config.get(CATEGORY_MODULE, "extruder2MaxRange", Defaults.EXTRUDER2_MAX_RANGE,
                "Extruder Module Mk2 Max Range", 1, Integer.MAX_VALUE);

        Property propFluidBaseTransfer = config.get(CATEGORY_ROUTER, "fluidBaseTransfer", Defaults.FLUID_BASE_TRANSFER,
                "Fluid Module Base Transfer Rate", 0, Integer.MAX_VALUE);
        Property propFluidMaxTransfer = config.get(CATEGORY_ROUTER, "fluidMaxTransfer", Defaults.FLUID_MAX_TRANSFER,
                "Fluid Module Hard Max Transfer Rate", 0, Integer.MAX_VALUE);
        Property propMBperFluidUpgrade = config.get(CATEGORY_ROUTER, "mBperFluidUpgrade", Defaults.MB_PER_FLUID_UPGRADE,
                "Fluid transfer rate increase per Fluid Upgrade", 0, Integer.MAX_VALUE);

        Property propVacuumParticles = config.get(CATEGORY_MODULE, "vacuumParticles", Defaults.VACUUM_PARTICLES,
                "Show particles when Vacuum Module absorbs items");
        Property propSenderParticles = config.get(CATEGORY_MODULE, "senderParticles", Defaults.SENDER_PARTICLES,
                "Show particles when Sender Modules send items");
        Property propPullerParticles = config.get(CATEGORY_MODULE, "pullerParticles", Defaults.PULLER_PARTICLES,
                "Show particles when Puller Mk2 Module pulls items");
        Property propPlacerParticles = config.get(CATEGORY_MODULE, "placerParticles", Defaults.PLACER_PARTICLES,
                "Show particles when Placer Module places a block");
        Property propBreakerParticles = config.get(CATEGORY_MODULE, "breakerParticles", Defaults.BREAKER_PARTICLES,
                "Show particles when Breaker Module breaks a block");
        Property propExtruderSound = config.get(CATEGORY_MODULE, "extruderSound", Defaults.EXTRUDER_SOUND,
                "Play sounds when Extruder Module extends or withdraws");
        
        Property propFluid2BaseRange = config.get(CATEGORY_MODULE, "fluid2BaseRange", Defaults.FLUID2_BASE_RANGE,
                "Base range for Fluid Module Mk2 (no range upgrades)", 1, Integer.MAX_VALUE);
        Property propFluid2MaxRange = config.get(CATEGORY_MODULE, "fluid2MaxRange", Defaults.FLUID2_MAX_RANGE,
                "Max range for Fluid Module Mk2", 1, Integer.MAX_VALUE);
        Property propFEPerEnergyUpgrade = config.get(CATEGORY_ROUTER, "fePerEnergyUpgrade", Defaults.FE_PER_ENERGY_UPGRADE,
                "FE capacity added per energy upgrade", 0, Integer.MAX_VALUE);
        Property propFEXferPerUpgrade = config.get(CATEGORY_ROUTER, "feXferPerEnergyUpgrade", Defaults.FE_XFER_PER_UPGRADE,
                "FE transfer rate per energy upgrade", 0, Integer.MAX_VALUE);
        Property propExtruderPushEntities = config.get(CATEGORY_MODULE, "extruderPushEntities", Defaults.EXTRUDER_PUSH_ENTITIES,
                "Extruder module pushes entities when extending");
        Property propBreakerHarvestLevelLimit = config.get(CATEGORY_MODULE, "breakerHarvestLevelLimit", Defaults.BREAKER_HARVEST_LEVEL_LIMIT,
                "Breaker module requires appropriate harvest level");
        Property propFlingerEffects = config.get(CATEGORY_MODULE, "flingerEffects", Defaults.FLINGER_EFFECTS,
                "Play sound & smoke effect when Flinger Module flings an item");

        List<String> routerOrder = new java.util.ArrayList<>(Arrays.asList(
                "baseTickRate", "ticksPerUpgrade", "hardMinTicks", "configKey",
                "ecoTimeout", "lowPowerTickRate", "fluidBaseTransfer",
                "fluidMaxTransfer", "mBperFluidUpgrade", "fePerEnergyUpgrade",
                "feXferPerEnergyUpgrade"));
        config.setCategoryPropertyOrder(CATEGORY_ROUTER, routerOrder);

        List<String> moduleOrder = new java.util.ArrayList<>(Arrays.asList(
                "sender1BaseRange", "sender1MaxRange", "sender2BaseRange", "sender2MaxRange",
                "vacuumBaseRange", "vacuumMaxRange", "extruderBaseRange", "extruderMaxRange",
                "extruder2BaseRange", "extruder2MaxRange", "puller2BaseRange", "puller2MaxRange",
                "fluid2BaseRange", "fluid2MaxRange",
                "senderParticles", "pullerParticles", "vacuumParticles",
                "placerParticles", "breakerParticles", "extruderSound", "flingerEffects",
                "extruderPushEntities", "breakerHarvestLevelLimit"));
        config.setCategoryPropertyOrder(CATEGORY_MODULE, moduleOrder);

        if (readFieldsFromConfig) {
            baseTickRate = Math.max(1, propBaseTickRate.getInt(Defaults.BASE_TICK_RATE));
            ticksPerUpgrade = propTicksPerUpgrade.getInt(Defaults.TICKS_PER_UPGRADE);
            hardMinTickRate = propHardMinTicks.getInt();
            String s = propConfigKey.getString();
            configKey = s.length() > 0 ? propConfigKey.getString().charAt(0) : Defaults.CONFIG_KEY;
            ecoTimeout = propEcoTimeout.getInt();
            lowPowerTickRate = propLowPowerTickRate.getInt();
            sender1BaseRange = propSender1BaseRange.getInt();
            sender1MaxRange = propSender1MaxRange.getInt();
            sender2BaseRange = propSender2BaseRange.getInt();
            sender2MaxRange = propSender2MaxRange.getInt();
            vacuumBaseRange = propVacuumBaseRange.getInt();
            vacuumMaxRange = propVacuumMaxRange.getInt();
            extruderBaseRange = propExtruderBaseRange.getInt();
            extruderMaxRange = propExtruderMaxRange.getInt();
            extruder2BaseRange = propExtruder2BaseRange.getInt();
            extruder2MaxRange = propExtruder2MaxRange.getInt();
            puller2BaseRange = propPuller2BaseRange.getInt();
            puller2MaxRange = propPuller2MaxRange.getInt();
            fluidBaseTransferRate = propFluidBaseTransfer.getInt();
            fluidMaxTransferRate = propFluidMaxTransfer.getInt();
            mBperFluidUpgrade = propMBperFluidUpgrade.getInt();
            senderParticles = propSenderParticles.getBoolean();
            pullerParticles = propPullerParticles.getBoolean();
            vacuumParticles = propVacuumParticles.getBoolean();
            placerParticles = propPlacerParticles.getBoolean();
            breakerParticles = propBreakerParticles.getBoolean();
            extruderSound = propExtruderSound.getBoolean();
            flingerEffects = propFlingerEffects.getBoolean();
            fluid2BaseRange = propFluid2BaseRange.getInt();
            fluid2MaxRange = propFluid2MaxRange.getInt();
            fePerEnergyUpgrade = propFEPerEnergyUpgrade.getInt();
            feXferPerEnergyUpgrade = propFEXferPerUpgrade.getInt();
            extruderPushEntities = propExtruderPushEntities.getBoolean();
            breakerHarvestLevelLimit = propBreakerHarvestLevelLimit.getBoolean();
        }

        propBaseTickRate.set(baseTickRate);
        propTicksPerUpgrade.set(ticksPerUpgrade);
        propHardMinTicks.set(hardMinTickRate);
        propConfigKey.set(String.valueOf(configKey));
        propEcoTimeout.set(ecoTimeout);
        propLowPowerTickRate.set(lowPowerTickRate);
        propSender1BaseRange.set(sender1BaseRange);
        propSender1MaxRange.set(sender1MaxRange);
        propSender2BaseRange.set(sender2BaseRange);
        propSender2MaxRange.set(sender2MaxRange);
        propVacuumBaseRange.set(vacuumBaseRange);
        propVacuumMaxRange.set(vacuumMaxRange);
        propExtruderBaseRange.set(extruderBaseRange);
        propExtruderMaxRange.set(extruderMaxRange);
        propExtruder2BaseRange.set(extruder2BaseRange);
        propExtruder2MaxRange.set(extruder2MaxRange);
        propPuller2BaseRange.set(puller2BaseRange);
        propPuller2MaxRange.set(puller2MaxRange);
        propFluidBaseTransfer.set(fluidBaseTransferRate);
        propFluidMaxTransfer.set(fluidMaxTransferRate);
        propMBperFluidUpgrade.set(mBperFluidUpgrade);
        propSenderParticles.set(senderParticles);
        propPullerParticles.set(pullerParticles);
        propVacuumParticles.set(vacuumParticles);
        propPlacerParticles.set(placerParticles);
        propBreakerParticles.set(breakerParticles);
        propExtruderSound.set(extruderSound);
        propFlingerEffects.set(flingerEffects);
        propFluid2BaseRange.set(fluid2BaseRange);
        propFluid2MaxRange.set(fluid2MaxRange);
        propFEPerEnergyUpgrade.set(fePerEnergyUpgrade);
        propFEXferPerUpgrade.set(feXferPerEnergyUpgrade);
        propExtruderPushEntities.set(extruderPushEntities);
        propBreakerHarvestLevelLimit.set(breakerHarvestLevelLimit);

        if (config.hasChanged()) {
            config.save();
        }
    }

    public static class ConfigEventHandler {
        @SubscribeEvent(priority = EventPriority.NORMAL)
        public void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent event) {
            if (ModularRouters.modId.equals(event.modID) && !event.isWorldRunning) {
                syncFromGUI();
            }
        }
    }
}
