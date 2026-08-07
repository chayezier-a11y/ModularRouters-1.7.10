package me.desht.modularrouters.logic.compiled;

import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import me.desht.modularrouters.item.module.Module;
import me.desht.modularrouters.util.ModuleHelper;
import me.desht.modularrouters.util.ModularRoutersFakePlayer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.AxisAlignedBB;
import net.minecraftforge.common.util.ForgeDirection;

import java.util.List;

public class CompiledActivatorModule extends CompiledModule {
    public static final String NBT_ACTION_TYPE = "ActionType";
    public static final String NBT_LOOK_DIRECTION = "LookDirection";
    public static final String NBT_ENTITY_MODE = "EntityMode";
    public static final String NBT_SNEAKING = "Sneaking";

    public enum ActionType {
        RIGHT_CLICK,
        USE_ITEM_ON_ENTITY,
        ATTACK_ENTITY
    }

    public enum LookDirection {
        LEVEL,
        ABOVE,
        BELOW
    }

    public enum EntityMode {
        NEAREST,
        RANDOM,
        ROUND_ROBIN
    }

    private final ActionType actionType;
    private final LookDirection lookDirection;
    private final EntityMode entityMode;
    private final boolean sneaking;
    private int entityIdx;

    public CompiledActivatorModule(TileEntityItemRouter router, ItemStack stack) {
        super(router, stack);
        NBTTagCompound compound = ModuleHelper.validateNBT(stack);
        this.actionType = getEnum(compound, NBT_ACTION_TYPE, ActionType.values(), ActionType.RIGHT_CLICK);
        this.lookDirection = getEnum(compound, NBT_LOOK_DIRECTION, LookDirection.values(), LookDirection.LEVEL);
        this.entityMode = getEnum(compound, NBT_ENTITY_MODE, EntityMode.values(), EntityMode.NEAREST);
        this.sneaking = compound.getBoolean(NBT_SNEAKING);
        this.entityIdx = 0;
    }

    public ActionType getActionType() { return actionType; }
    public LookDirection getLookDirection() { return lookDirection; }
    public EntityMode getEntityMode() { return entityMode; }
    public boolean isSneaking() { return sneaking; }

    private static <T extends Enum<T>> T getEnum(NBTTagCompound compound, String key, T[] values, T defaultValue) {
        int ordinal = compound.hasKey(key) ? compound.getByte(key) : defaultValue.ordinal();
        return ordinal >= 0 && ordinal < values.length ? values[ordinal] : defaultValue;
    }

    @Override
    public boolean execute(TileEntityItemRouter router) {
        if (router.isBufferEmpty()) return false;

        ItemStack toUse = router.peekBuffer(1);
        if (toUse == null || getFilter().rejectItem(toUse)) return false;

        ForgeDirection facing = getAbsoluteDirection(router);

        if (actionType == ActionType.RIGHT_CLICK) {
            return executeRightClick(router, facing);
        } else {
            return executeEntityAction(router, facing);
        }
    }

    private boolean executeRightClick(TileEntityItemRouter router, ForgeDirection facing) {
        int x = router.xCoord + facing.offsetX;
        int y = router.yCoord + facing.offsetY;
        int z = router.zCoord + facing.offsetZ;

        ItemStack stack = router.extractBuffer(1);
        if (stack == null) return false;

        ModularRoutersFakePlayer fakePlayer = new ModularRoutersFakePlayer(router.getWorldObj());
        fakePlayer.setPosition(router.xCoord + 0.5, router.yCoord + 0.5, router.zCoord + 0.5);
        fakePlayer.setSneaking(sneaking);
        fakePlayer.setCurrentItemOrArmor(0, stack.copy());

        // Try to use item on block in front
        boolean used = stack.getItem().onItemUse(stack, fakePlayer, router.getWorldObj(),
                x, y, z, facing.getOpposite().ordinal(),
                0.5f, 0.5f, 0.5f);

        // Retrieve remaining items from fake player
        ItemStack remaining = fakePlayer.getCurrentEquippedItem();
        if (remaining != null && remaining.stackSize > 0) {
            router.insertBuffer(remaining);
        }

        return used;
    }

    @SuppressWarnings("unchecked")
    private boolean executeEntityAction(TileEntityItemRouter router, ForgeDirection facing) {
        ForgeDirection absDir = getAbsoluteDirection(router);
        double x = router.xCoord + 0.5 + absDir.offsetX * 2.5;
        double y = router.yCoord + 0.5 + absDir.offsetY * 2.5
                + (lookDirection == LookDirection.ABOVE ? 1.5 : lookDirection == LookDirection.BELOW ? -1.5 : 0.0);
        double z = router.zCoord + 0.5 + absDir.offsetZ * 2.5;

        AxisAlignedBB box = AxisAlignedBB.getBoundingBox(x - 2, y - 2, z - 2, x + 2, y + 2, z + 2);
        List<EntityLivingBase> entities = router.getWorldObj().getEntitiesWithinAABB(EntityLivingBase.class, box);

        if (entities.isEmpty()) return false;

        EntityLivingBase target;
        switch (entityMode) {
            case RANDOM:
                target = entities.get(router.getWorldObj().rand.nextInt(entities.size()));
                break;
            case ROUND_ROBIN:
                entityIdx = (entityIdx + 1) % entities.size();
                target = entities.get(entityIdx);
                break;
            case NEAREST:
            default:
                // Sort by distance - nearest first
                final double rx = router.xCoord + 0.5, ry = router.yCoord + 0.5, rz = router.zCoord + 0.5;
                java.util.Collections.sort(entities, new java.util.Comparator<EntityLivingBase>() { public int compare(EntityLivingBase a, EntityLivingBase b) { return Double.compare(
                        a.getDistanceSq(rx, ry, rz), b.getDistanceSq(rx, ry, rz)); }});
                target = entities.get(0);
                break;
        }

        if (target == null) return false;

        if (actionType == ActionType.ATTACK_ENTITY) {
            ItemStack weapon = router.extractBuffer(1);
            if (weapon == null) return false;

            ModularRoutersFakePlayer fakePlayer = new ModularRoutersFakePlayer(router.getWorldObj());
            fakePlayer.setPosition(router.xCoord + 0.5, router.yCoord + 0.5, router.zCoord + 0.5);
            fakePlayer.setSneaking(sneaking);
            fakePlayer.setCurrentItemOrArmor(0, weapon);

            fakePlayer.attackTargetEntityWithCurrentItem(target);

            ItemStack remaining = fakePlayer.getCurrentEquippedItem();
            if (remaining != null && remaining.stackSize > 0) {
                router.insertBuffer(remaining);
            }
            return true;
        } else {
            // USE_ITEM_ON_ENTITY
            ItemStack stack = router.extractBuffer(1);
            if (stack == null) return false;

            ModularRoutersFakePlayer fakePlayer = new ModularRoutersFakePlayer(router.getWorldObj());
            fakePlayer.setPosition(router.xCoord + 0.5, router.yCoord + 0.5, router.zCoord + 0.5);
            fakePlayer.setSneaking(sneaking);
            fakePlayer.setCurrentItemOrArmor(0, stack);

            boolean result = target.interactFirst(fakePlayer);

            ItemStack remaining = fakePlayer.getCurrentEquippedItem();
            if (remaining != null && remaining.stackSize > 0) {
                router.insertBuffer(remaining);
            }
            return result;
        }
    }
}
