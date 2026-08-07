package me.desht.modularrouters.container;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ContainerItemRouterEnergySyncTest {
    @Test
    public void combinesUnsignedProgressWordsWithoutSignExtension() {
        assertEquals(0x8000FFFF, ContainerItemRouter.combineProgressWords(0xFFFF, 0x8000));
        assertEquals(Integer.MAX_VALUE, ContainerItemRouter.combineProgressWords(0xFFFF, 0x7FFF));
    }
}
