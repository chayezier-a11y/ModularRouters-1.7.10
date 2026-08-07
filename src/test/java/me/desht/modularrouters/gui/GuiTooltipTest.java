package me.desht.modularrouters.gui;

import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;

public class GuiTooltipTest {
    @Test
    public void splitBreakTokensAndTrimVisualPadding() {
        assertEquals(Arrays.asList("Header", "Body", "Footer"),
                GuiTooltip.split("Header ${br} Body${br}Footer"));
    }
}
