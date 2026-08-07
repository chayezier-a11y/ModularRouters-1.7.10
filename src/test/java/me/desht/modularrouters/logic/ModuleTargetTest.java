package me.desht.modularrouters.logic;

import net.minecraftforge.common.util.ForgeDirection;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

public class ModuleTargetTest {
    @Test
    public void roundTripsDimensionPositionFaceAndName() {
        ModuleTarget source = new ModuleTarget(7, 10, 64, -3, ForgeDirection.NORTH, "tile.chest.name");

        ModuleTarget restored = ModuleTarget.fromNBT(source.toNBT());

        assertEquals(source, restored);
        assertEquals(7, restored.getDimension());
        assertEquals("tile.chest.name", restored.getBlockName());
    }

    @Test
    public void equalityIncludesDimensionPositionAndFaceButNotName() {
        ModuleTarget target = new ModuleTarget(0, 1, 2, 3, ForgeDirection.UP, "first");

        assertEquals(target, new ModuleTarget(0, 1, 2, 3, ForgeDirection.UP, "second"));
        assertNotEquals(target, new ModuleTarget(1, 1, 2, 3, ForgeDirection.UP, "first"));
        assertNotEquals(target, new ModuleTarget(0, 1, 2, 3, ForgeDirection.DOWN, "first"));
    }

    @Test
    public void invalidSerializedFaceFallsBackToUnknown() {
        ModuleTarget source = new ModuleTarget(0, 1, 2, 3, ForgeDirection.SOUTH, "target");
        net.minecraft.nbt.NBTTagCompound tag = source.toNBT();
        tag.setByte("Face", (byte) 127);

        assertEquals(ForgeDirection.UNKNOWN, ModuleTarget.fromNBT(tag).getFacing());
    }
}
