package me.desht.modularrouters.logic.compiled;

import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import me.desht.modularrouters.config.Config;
import me.desht.modularrouters.item.upgrade.ItemUpgrade.UpgradeType;
import me.desht.modularrouters.item.module.Module;
import me.desht.modularrouters.sound.ModSounds;
import me.desht.modularrouters.util.ModuleHelper;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.util.ForgeDirection;

import javax.annotation.Nonnull;

public class CompiledFlingerModule extends CompiledDropperModule {
    public static final String NBT_SPEED = "Speed";
    public static final String NBT_PITCH = "Pitch";
    public static final String NBT_YAW = "Yaw";

    private final float speed, pitch, yaw;

    public CompiledFlingerModule(TileEntityItemRouter router, ItemStack stack) {
        super(router, stack);

        NBTTagCompound compound = ModuleHelper.validateNBT(stack);
        for (String key : new String[] { NBT_SPEED, NBT_PITCH, NBT_YAW }) {
            if (!compound.hasKey(key)) {
                compound.setFloat(key, 0.0f);
            }
        }

        speed = compound.getFloat(NBT_SPEED);
        pitch = compound.getFloat(NBT_PITCH);
        yaw = compound.getFloat(NBT_YAW);
    }

    @Override
    public boolean execute(@Nonnull TileEntityItemRouter router) {
        boolean fired = super.execute(router);
        if (fired && Config.flingerEffects) playEffects(router);
        return fired;
    }

    protected void playEffects(TileEntityItemRouter router) {
        ForgeDirection facing = getAbsoluteDirection(router);
        int x = router.xCoord + facing.offsetX;
        int y = router.yCoord + facing.offsetY;
        int z = router.zCoord + facing.offsetZ;
        World world = router.getWorldObj();
        if (shouldShowSmoke(router.getUpgradeCount(UpgradeType.MUFFLER)) && world instanceof WorldServer) {
            ((WorldServer) world).func_147487_a("largesmoke", x + 0.5, y + 0.5, z + 0.5,
                    effectParticleCount(speed), 0.0, 0.0, 0.0, 0.0);
        }
        ModSounds.playSound(world, x, y, z, "thud", 0.5f + speed, 1.0f);
    }

    @Override
    protected void setupItemVelocity(TileEntityItemRouter router, EntityItem item) {
        float basePitch = 0.0f;
        float baseYaw;
        switch (getDirection()) {
            case UP:
                basePitch = 90.0f;
                baseYaw = yawFromFacing(router.getAbsoluteFacing(Module.RelativeDirection.FRONT));
                break;
            case DOWN:
                basePitch = -90.0f;
                baseYaw = yawFromFacing(router.getAbsoluteFacing(Module.RelativeDirection.FRONT));
                break;
            default:
                baseYaw = yawFromFacing(getAbsoluteDirection(router));
                break;
        }

        double yawRad = Math.toRadians(baseYaw + yaw), pitchRad = Math.toRadians(basePitch + pitch);

        double x = (Math.cos(yawRad) * Math.cos(pitchRad));
        double y = Math.sin(pitchRad);
        double z = -(Math.sin(yawRad) * Math.cos(pitchRad));

        item.motionX = x * speed;
        item.motionY = y * speed;
        item.motionZ = z * speed;
    }

    private float yawFromFacing(ForgeDirection absoluteFacing) {
        switch (absoluteFacing) {
            case EAST: return 0.0f;
            case NORTH: return 90.0f;
            case WEST: return 180.0f;
            case SOUTH: return 270.0f;
            default: return 0;
        }
    }

    public float getYaw() { return yaw; }
    public float getPitch() { return pitch; }
    public float getSpeed() { return speed; }

    static int effectParticleCount(float speed) {
        return Math.round(speed * 5);
    }

    static boolean shouldShowSmoke(int mufflers) {
        return mufflers < 2;
    }
}
