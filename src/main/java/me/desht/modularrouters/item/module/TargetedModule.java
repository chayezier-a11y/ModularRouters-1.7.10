package me.desht.modularrouters.item.module;

import me.desht.modularrouters.logic.ModuleTarget;
import me.desht.modularrouters.util.MiscUtil;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import java.util.List;

public abstract class TargetedModule extends Module {
    private static final String NBT_TARGET_X = "TargetX";
    private static final String NBT_TARGET_Y = "TargetY";
    private static final String NBT_TARGET_Z = "TargetZ";
    private static final String NBT_HAS_TARGET = "HasTarget";
    private static final String NBT_TARGET_DIM = "TargetDim";

    @Override
    public boolean isDirectional() {
        return true;
    }

    @Override
    public boolean onItemUse(ItemStack stack, EntityPlayer player, World world, int x, int y, int z,
                             int side, float hitX, float hitY, float hitZ) {
        if (player.isSneaking() && world.getTileEntity(x, y, z) instanceof IInventory) {
            if (!world.isRemote) {
                bindTarget(stack, x, y, z, world.provider.dimensionId);
                MiscUtil.sendStatusMessage(player, "chatText.target.bound", x, y, z);
            }
            return true;
        }
        return super.onItemUse(stack, player, world, x, y, z, side, hitX, hitY, hitZ);
    }

    @Override
    public ItemStack onSneakRightClick(ItemStack stack, World world, EntityPlayer player) {
        if (player.isSneaking() && hasTarget(stack) && !world.isRemote) {
            clearTarget(stack);
            MiscUtil.sendStatusMessage(player, "chatText.target.cleared");
            return stack;
        }
        return super.onSneakRightClick(stack, world, player);
    }

    public void bindTarget(ItemStack stack, int x, int y, int z, int dim) {
        if (!stack.hasTagCompound()) stack.setTagCompound(new NBTTagCompound());
        NBTTagCompound tag = stack.getTagCompound();
        tag.setInteger(NBT_TARGET_X, x);
        tag.setInteger(NBT_TARGET_Y, y);
        tag.setInteger(NBT_TARGET_Z, z);
        tag.setInteger(NBT_TARGET_DIM, dim);
        tag.setBoolean(NBT_HAS_TARGET, true);
    }

    public static ModuleTarget getTarget(ItemStack stack) {
        if (!hasTarget(stack)) return null;
        NBTTagCompound tag = stack.getTagCompound();
        return new ModuleTarget(
                tag.getInteger(NBT_TARGET_X),
                tag.getInteger(NBT_TARGET_Y),
                tag.getInteger(NBT_TARGET_Z),
                ForgeDirection.UNKNOWN,
                null
        );
    }

    public static boolean hasTarget(ItemStack stack) {
        return stack.hasTagCompound() && stack.getTagCompound().getBoolean(NBT_HAS_TARGET);
    }

    public static void clearTarget(ItemStack stack) {
        if (stack.hasTagCompound()) {
            NBTTagCompound tag = stack.getTagCompound();
            tag.removeTag(NBT_TARGET_X);
            tag.removeTag(NBT_TARGET_Y);
            tag.removeTag(NBT_TARGET_Z);
            tag.removeTag(NBT_TARGET_DIM);
            tag.setBoolean(NBT_HAS_TARGET, false);
        }
    }

    @Override
    public void addExtraInformation(ItemStack stack, EntityPlayer player, List<String> list, boolean par4) {
        super.addExtraInformation(stack, player, list, par4);
        if (hasTarget(stack)) {
            ModuleTarget t = getTarget(stack);
            list.add(MiscUtil.translate("itemText.target.bound", t.getX(), t.getY(), t.getZ()));
        } else {
            list.add(MiscUtil.translate("itemText.target.notBound"));
        }
    }
}
