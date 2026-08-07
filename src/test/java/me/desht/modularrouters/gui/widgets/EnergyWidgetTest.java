package me.desht.modularrouters.gui.widgets;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class EnergyWidgetTest {
    @Test
    public void scalesEmptyHalfAndFullEnergyWithoutOverflow() {
        assertEquals(0, EnergyWidget.scaledHeight(0, 100, 64));
        assertEquals(32, EnergyWidget.scaledHeight(50, 100, 64));
        assertEquals(64, EnergyWidget.scaledHeight(100, 100, 64));
        assertEquals(64, EnergyWidget.scaledHeight(Integer.MAX_VALUE, Integer.MAX_VALUE, 64));
        assertEquals(0, EnergyWidget.scaledHeight(100, 0, 64));
    }
}
