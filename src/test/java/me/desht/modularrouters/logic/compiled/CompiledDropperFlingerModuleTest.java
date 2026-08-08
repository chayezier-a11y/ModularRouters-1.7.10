package me.desht.modularrouters.logic.compiled;

import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import me.desht.modularrouters.config.Config;
import me.desht.modularrouters.item.ItemBase;
import me.desht.modularrouters.item.augment.PickupDelayAugment;
import me.desht.modularrouters.item.module.DropperModule;
import me.desht.modularrouters.item.module.FlingerModule;
import me.desht.modularrouters.item.module.ItemModule;
import me.desht.modularrouters.item.module.Module;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.util.ForgeDirection;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class CompiledDropperFlingerModuleTest {
    private static final double EPSILON = 0.00001;
    private static final ItemBase TEST_ITEM = new ItemBase("dropperFlingerTestItem");

    @Test
    public void dropperSpawnsAtUpstreamTargetFacePositionAndSetsPickupDelay() {
        ItemStack moduleStack = moduleStack(ItemModule.ModuleType.DROPPER);
        CapturingDropper module = new CapturingDropper(moduleStack, true);
        TestRouter router = routerWithBuffer(ForgeDirection.EAST, 4);
        router.xCoord = 10;
        router.yCoord = 20;
        router.zCoord = 30;

        assertTrue(module.execute(router));
        assertEquals(11.3, module.spawned.posX, EPSILON);
        assertEquals(20.5, module.spawned.posY, EPSILON);
        assertEquals(30.5, module.spawned.posZ, EPSILON);
        assertEquals(0, module.spawned.delayBeforeCanPickup);
        assertEquals(3, router.getBufferItemStack().stackSize);
    }

    @Test
    public void failedEntitySpawnLeavesRouterBufferUntouched() {
        CapturingDropper module = new CapturingDropper(moduleStack(ItemModule.ModuleType.DROPPER), false);
        TestRouter router = routerWithBuffer(ForgeDirection.DOWN, 4);

        assertFalse(module.execute(router));
        assertEquals(4, router.getBufferItemStack().stackSize);
    }

    @Test
    public void flingerInheritsDropperAugmentsAndUsesRouterFrontForVerticalShots() {
        assertTrue(DropperModule.class.isAssignableFrom(FlingerModule.class));
        assertEquals(30, CompiledDropperModule.pickupDelayForCount(3));
        assertEquals(20, new PickupDelayAugment().getMaxAugments(FlingerModule.class));

        ItemStack moduleStack = moduleStack(ItemModule.ModuleType.FLINGER);
        moduleStack.setTagCompound(new net.minecraft.nbt.NBTTagCompound());
        moduleStack.getTagCompound().setFloat(CompiledFlingerModule.NBT_SPEED, 2.0f);
        moduleStack.getTagCompound().setFloat(CompiledFlingerModule.NBT_PITCH, -45.0f);
        moduleStack.getTagCompound().setFloat(CompiledFlingerModule.NBT_YAW, 0.0f);
        ItemModule.getModule(moduleStack).setDirection(moduleStack, Module.RelativeDirection.UP);

        CompiledFlingerModule module = new CompiledFlingerModule(null, moduleStack);
        TestRouter router = routerWithBuffer(ForgeDirection.NORTH, 1);
        EntityItem item = new EntityItem(null, 0, 0, 0, new ItemStack(TEST_ITEM));
        module.setupItemVelocity(router, item);

        assertEquals(0.0, item.motionX, EPSILON);
        assertEquals(Math.sqrt(2.0), item.motionY, EPSILON);
        assertEquals(-Math.sqrt(2.0), item.motionZ, EPSILON);
    }

    @Test
    public void successfulFlingerInvokesEffectsAndTwoMufflersSuppressOnlySmoke() {
        boolean oldEffects = Config.flingerEffects;
        Config.flingerEffects = true;
        try {
            CapturingFlinger module = new CapturingFlinger(moduleStack(ItemModule.ModuleType.FLINGER));
            assertTrue(module.execute(routerWithBuffer(ForgeDirection.SOUTH, 1)));
            assertTrue(module.effectsPlayed);
            assertEquals(5, CompiledFlingerModule.effectParticleCount(1.0f));
            assertTrue(CompiledFlingerModule.shouldShowSmoke(1));
            assertFalse(CompiledFlingerModule.shouldShowSmoke(2));
        } finally {
            Config.flingerEffects = oldEffects;
        }
    }

    private static ItemStack moduleStack(ItemModule.ModuleType type) {
        return new ItemStack(new ItemModule(), 1, type.ordinal());
    }

    private static TestRouter routerWithBuffer(ForgeDirection front, int count) {
        TestRouter router = new TestRouter(front);
        router.getBuffer().setInventorySlotContents(0, new ItemStack(TEST_ITEM, count));
        return router;
    }

    private static class TestRouter extends TileEntityItemRouter {
        private final ForgeDirection front;

        private TestRouter(ForgeDirection front) {
            this.front = front;
        }

        @Override
        public ForgeDirection getAbsoluteFacing(Module.RelativeDirection direction) {
            return direction.toForgeDirection(front);
        }
    }

    private static class CapturingDropper extends CompiledDropperModule {
        private final boolean spawnResult;
        private EntityItem spawned;

        private CapturingDropper(ItemStack stack, boolean spawnResult) {
            super(null, stack);
            this.spawnResult = spawnResult;
        }

        @Override
        protected boolean spawnItem(TileEntityItemRouter router, EntityItem item) {
            spawned = item;
            return spawnResult;
        }
    }

    private static class CapturingFlinger extends CompiledFlingerModule {
        private boolean effectsPlayed;

        private CapturingFlinger(ItemStack stack) {
            super(null, stack);
        }

        @Override
        protected boolean spawnItem(TileEntityItemRouter router, EntityItem item) {
            return true;
        }

        @Override
        protected void playEffects(TileEntityItemRouter router) {
            effectsPlayed = true;
        }
    }
}
