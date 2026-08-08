package me.desht.modularrouters.logic.compiled;

import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import me.desht.modularrouters.ModularRouters;
import me.desht.modularrouters.config.Config;
import me.desht.modularrouters.item.module.TargetedModule;
import me.desht.modularrouters.item.upgrade.ItemUpgrade.UpgradeType;
import me.desht.modularrouters.logic.ModuleTarget;
import me.desht.modularrouters.network.ParticleBeamMessage;
import net.minecraft.item.ItemStack;
import cpw.mods.fml.common.network.NetworkRegistry;

import java.util.Collections;
import java.util.List;

public class CompiledPullerModule2 extends CompiledPullerModule1 {
    public CompiledPullerModule2(TileEntityItemRouter router, ItemStack stack) {
        super(router, stack);
    }

    @Override
    List<ModuleTarget> setupTargets(TileEntityItemRouter router, ItemStack stack) {
        return Collections.singletonList(TargetedModule.getTarget(stack));
    }

    @Override
    boolean validateTarget(TileEntityItemRouter router, ModuleTarget target) {
        return target != null && router.getWorldObj() != null
                && isTargetLocationValid(router.getWorldObj().provider.dimensionId,
                router.xCoord, router.yCoord, router.zCoord, getRangeSquared(), target,
                router.getWorldObj().blockExists(target.getX(), target.getY(), target.getZ()));
    }

    static boolean isTargetLocationValid(int routerDimension, int routerX, int routerY, int routerZ,
                                         int rangeSquared, ModuleTarget target, boolean loaded) {
        if (target == null || !loaded || target.getDimension() != routerDimension) return false;
        long dx = (long) target.getX() - routerX;
        long dy = (long) target.getY() - routerY;
        long dz = (long) target.getZ() - routerZ;
        return dx * dx + dy * dy + dz * dz <= rangeSquared;
    }

    @Override
    void playParticles(TileEntityItemRouter router, ModuleTarget target, ItemStack stack) {
        if (!Config.pullerParticles || router.getUpgradeCount(UpgradeType.MUFFLER) >= 2
                || ModularRouters.network == null) return;
        ModularRouters.network.sendToAllAround(
                new ParticleBeamMessage(target.getX() + 0.5, target.getY() + 0.5, target.getZ() + 0.5,
                        router.xCoord + 0.5, router.yCoord + 0.5, router.zCoord + 0.5,
                        0x6080FF, 0.08f),
                new NetworkRegistry.TargetPoint(router.getWorldObj().provider.dimensionId,
                        router.xCoord + 0.5, router.yCoord + 0.5, router.zCoord + 0.5, 64));
    }
}
