package me.desht.modularrouters.item.module;

import me.desht.modularrouters.logic.ModuleTarget;
import me.desht.modularrouters.util.MiscUtil;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public abstract class TargetedModule extends Module {
    private static final String NBT_TARGET_X = "TargetX";
    private static final String NBT_TARGET_Y = "TargetY";
    private static final String NBT_TARGET_Z = "TargetZ";
    private static final String NBT_HAS_TARGET = "HasTarget";
    private static final String NBT_TARGET_DIM = "TargetDim";
    private static final String NBT_TARGET_FACE = "TargetFace";
    private static final String NBT_TARGET_NAME = "TargetName";
    private static final String NBT_MULTI_TARGET = "MultiTarget";

    protected int getMaxTargets() {
        return 1;
    }

    protected boolean isValidTarget(World world, int x, int y, int z, ForgeDirection face) {
        return world.getTileEntity(x, y, z) instanceof IInventory;
    }

    @Override
    public boolean isDirectional() {
        return true;
    }

    @Override
    public boolean onItemUse(ItemStack stack, EntityPlayer player, World world, int x, int y, int z,
                             int side, float hitX, float hitY, float hitZ) {
        ForgeDirection face = ForgeDirection.getOrientation(side);
        if (player.isSneaking() && isValidTarget(world, x, y, z, face)) {
            if (!world.isRemote) {
                String name = world.getBlock(x, y, z).getLocalizedName();
                if (getMaxTargets() == 1) {
                    bindTarget(stack, x, y, z, world.provider.dimensionId, face, name);
                } else {
                    toggleTarget(stack, new ModuleTarget(world.provider.dimensionId,
                            x, y, z, face, name), getMaxTargets());
                }
                MiscUtil.sendStatusMessage(player, "chatText.target.bound", x, y, z);
            }
            return true;
        }
        return super.onItemUse(stack, player, world, x, y, z, side, hitX, hitY, hitZ);
    }

    @Override
    public ItemStack onSneakRightClick(ItemStack stack, World world, EntityPlayer player) {
        if (player.isSneaking() && getMaxTargets() == 1 && hasTarget(stack) && !world.isRemote) {
            clearTarget(stack);
            MiscUtil.sendStatusMessage(player, "chatText.target.cleared");
            return stack;
        }
        return super.onSneakRightClick(stack, world, player);
    }

    public void bindTarget(ItemStack stack, int x, int y, int z, int dim) {
        bindTarget(stack, x, y, z, dim, ForgeDirection.UNKNOWN, "");
    }

    public void bindTarget(ItemStack stack, int x, int y, int z, int dim,
                           ForgeDirection face, String name) {
        if (!stack.hasTagCompound()) stack.setTagCompound(new NBTTagCompound());
        NBTTagCompound tag = stack.getTagCompound();
        tag.removeTag(NBT_MULTI_TARGET);
        tag.setInteger(NBT_TARGET_X, x);
        tag.setInteger(NBT_TARGET_Y, y);
        tag.setInteger(NBT_TARGET_Z, z);
        tag.setInteger(NBT_TARGET_DIM, dim);
        tag.setByte(NBT_TARGET_FACE, (byte) (face == null ? ForgeDirection.UNKNOWN : face).ordinal());
        tag.setString(NBT_TARGET_NAME, name == null ? "" : name);
        tag.setBoolean(NBT_HAS_TARGET, true);
    }

    public static ModuleTarget getTarget(ItemStack stack) {
        if (stack == null || !stack.hasTagCompound()) return null;
        NBTTagCompound tag = stack.getTagCompound();
        if (!tag.getBoolean(NBT_HAS_TARGET)) {
            Set<ModuleTarget> targets = getTargets(stack);
            return targets.isEmpty() ? null : targets.iterator().next();
        }
        ForgeDirection[] directions = ForgeDirection.values();
        int faceIndex = tag.hasKey(NBT_TARGET_FACE) ? tag.getByte(NBT_TARGET_FACE) : ForgeDirection.UNKNOWN.ordinal();
        ForgeDirection face = faceIndex >= 0 && faceIndex < directions.length
                ? directions[faceIndex] : ForgeDirection.UNKNOWN;
        return new ModuleTarget(
                tag.getInteger(NBT_TARGET_DIM),
                tag.getInteger(NBT_TARGET_X),
                tag.getInteger(NBT_TARGET_Y),
                tag.getInteger(NBT_TARGET_Z),
                face,
                tag.getString(NBT_TARGET_NAME)
        );
    }

    public static Set<ModuleTarget> getTargets(ItemStack stack) {
        Set<ModuleTarget> targets = new LinkedHashSet<ModuleTarget>();
        if (stack == null || !stack.hasTagCompound()) return targets;

        NBTTagCompound tag = stack.getTagCompound();
        if (tag.hasKey(NBT_MULTI_TARGET)) {
            NBTTagList list = tag.getTagList(NBT_MULTI_TARGET, 10);
            for (int i = 0; i < list.tagCount(); i++) {
                targets.add(ModuleTarget.fromNBT(list.getCompoundTagAt(i)));
            }
        } else if (tag.getBoolean(NBT_HAS_TARGET)) {
            ModuleTarget target = getTarget(stack);
            if (target != null) targets.add(target);
        }
        return targets;
    }

    public static void setTargets(ItemStack stack, Collection<ModuleTarget> targets) {
        if (!stack.hasTagCompound()) stack.setTagCompound(new NBTTagCompound());
        NBTTagCompound tag = stack.getTagCompound();
        clearLegacyTarget(tag);
        NBTTagList list = new NBTTagList();
        for (ModuleTarget target : targets) list.appendTag(target.toNBT());
        tag.setTag(NBT_MULTI_TARGET, list);
    }

    public static boolean addTarget(ItemStack stack, ModuleTarget target, int maximum) {
        Set<ModuleTarget> targets = getTargets(stack);
        if (target == null || targets.contains(target) || targets.size() >= maximum) return false;
        targets.add(target);
        setTargets(stack, targets);
        return true;
    }

    public static boolean toggleTarget(ItemStack stack, ModuleTarget target, int maximum) {
        Set<ModuleTarget> targets = getTargets(stack);
        if (targets.remove(target)) {
            setTargets(stack, targets);
            return false;
        }
        if (target == null || targets.size() >= maximum) return false;
        targets.add(target);
        setTargets(stack, targets);
        return true;
    }

    public static boolean hasTarget(ItemStack stack) {
        return !getTargets(stack).isEmpty();
    }

    public static void clearTarget(ItemStack stack) {
        if (stack.hasTagCompound()) {
            NBTTagCompound tag = stack.getTagCompound();
            tag.removeTag(NBT_MULTI_TARGET);
            clearLegacyTarget(tag);
        }
    }

    private static void clearLegacyTarget(NBTTagCompound tag) {
        tag.removeTag(NBT_TARGET_X);
        tag.removeTag(NBT_TARGET_Y);
        tag.removeTag(NBT_TARGET_Z);
        tag.removeTag(NBT_TARGET_DIM);
        tag.removeTag(NBT_TARGET_FACE);
        tag.removeTag(NBT_TARGET_NAME);
        tag.setBoolean(NBT_HAS_TARGET, false);
    }

    @Override
    public void addExtraInformation(ItemStack stack, EntityPlayer player, List<String> list, boolean par4) {
        super.addExtraInformation(stack, player, list, par4);
        Set<ModuleTarget> targets = getTargets(stack);
        if (!targets.isEmpty()) {
            for (ModuleTarget t : targets) {
                list.add(MiscUtil.translate("itemText.target.bound", t.getX(), t.getY(), t.getZ()));
            }
        } else {
            list.add(MiscUtil.translate("itemText.target.notBound"));
        }
    }
}
