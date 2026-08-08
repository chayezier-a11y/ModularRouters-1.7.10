package me.desht.modularrouters.logic;

import net.minecraft.init.Blocks;
import net.minecraftforge.common.util.ForgeDirection;
import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class BlockInteractionTest {
    @Test
    public void offsetsUseTheConfiguredForgeFace() {
        assertArrayEquals(new int[] { 11, 20, 30 },
                BlockInteraction.offset(10, 20, 30, ForgeDirection.EAST));
        assertArrayEquals(new int[] { 10, 19, 30 },
                BlockInteraction.offset(10, 20, 30, ForgeDirection.DOWN));
    }

    @Test
    public void airIsReplaceableButCannotBeBroken() {
        assertTrue(BlockInteraction.isReplaceable(Blocks.air, null, 0, 0, 0));
        assertFalse(BlockInteraction.canBreak(Blocks.air, null, 0, 0, 0));
    }
}
