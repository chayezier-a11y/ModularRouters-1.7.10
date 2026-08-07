package me.desht.modularrouters.event;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import me.desht.modularrouters.ModularRouters;
import me.desht.modularrouters.util.Scheduler;
import net.minecraft.world.World;

public class TickEventHandler {
    public static long TickCounter = 0;

    @SubscribeEvent
    public void onWorldTick(TickEvent.WorldTickEvent event) {
        if (event.world.provider.dimensionId == 0 && event.phase == TickEvent.Phase.END) {
            TickCounter++;
            Scheduler.server().tick();
            if (event.world.isRemote) {
                Scheduler.client().tick();
            }
        }
    }
}
