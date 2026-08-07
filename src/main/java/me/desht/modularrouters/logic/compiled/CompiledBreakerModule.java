package me.desht.modularrouters.logic.compiled;

import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import me.desht.modularrouters.util.ModuleHelper;
import net.minecraft.block.Block;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import javax.annotation.Nonnull;
import java.util.ArrayList;

public class CompiledBreakerModule extends CompiledModule {
    public static final String NBT_MATCH_TYPE = "MatchType";
    public enum MatchType { ITEM, BLOCK }
    private final MatchType matchType;

    public CompiledBreakerModule(TileEntityItemRouter router, ItemStack stack) {
        super(router, stack);
        NBTTagCompound tag = ModuleHelper.validateNBT(stack);
        int ordinal = tag.getByte(NBT_MATCH_TYPE);
        matchType = ordinal >= 0 && ordinal < MatchType.values().length
                ? MatchType.values()[ordinal] : MatchType.ITEM;
    }

    @Override
    public boolean execute(@Nonnull TileEntityItemRouter router) {
        if (isRegulationOK(router, true)) {
            World world = router.getWorldObj();
            if (world.isRemote) return false;

            ForgeDirection facing = getAbsoluteDirection(router);
            int x = router.xCoord + facing.offsetX;
            int y = router.yCoord + facing.offsetY;
            int z = router.zCoord + facing.offsetZ;

            Block block = world.getBlock(x, y, z);
            if (block == null || block == Blocks.air || block.getBlockHardness(world, x, y, z) < 0) {
                return false;
            }

            int meta = world.getBlockMetadata(x, y, z);

            if (!getFilter().isMatcherListEmpty()) {
                if (matchType == MatchType.BLOCK) {
                    if (getFilter().rejectItem(new ItemStack(block, 1, meta))) return false;
                } else {
                    boolean dropMatches = false;
                    for (ItemStack drop : block.getDrops(world, x, y, z, meta, 0)) {
                        if (!getFilter().rejectItem(drop)) {
                            dropMatches = true;
                            break;
                        }
                    }
                    if (!dropMatches) return false;
                }
            }

            // Get drops with possible fortune from pickaxe in buffer
            int fortune = 0;
            ItemStack bufferStack = router.getBufferItemStack();
            if (bufferStack != null) {
                fortune = EnchantmentHelper.getEnchantmentLevel(Enchantment.fortune.effectId, bufferStack);
            }

            ArrayList<ItemStack> drops = block.getDrops(world, x, y, z, meta, fortune);

            world.setBlockToAir(x, y, z);
            world.playAuxSFX(2001, x, y, z, Block.getIdFromBlock(block) + (meta << 12));

            for (ItemStack drop : drops) {
                ItemStack remaining = router.insertBuffer(drop);
                if (remaining != null) {
                    EntityItem entityItem = new EntityItem(world, x + 0.5, y + 0.5, z + 0.5, remaining);
                    world.spawnEntityInWorld(entityItem);
                }
            }

            return true;
        }
        return false;
    }

    public MatchType getMatchType() { return matchType; }
}
