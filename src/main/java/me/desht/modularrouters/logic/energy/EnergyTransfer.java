package me.desht.modularrouters.logic.energy;

import cofh.api.energy.IEnergyContainerItem;
import cofh.api.energy.IEnergyProvider;
import cofh.api.energy.IEnergyReceiver;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.util.ForgeDirection;

public final class EnergyTransfer {
    private EnergyTransfer() {
    }

    public static int move(IEnergyProvider source, ForgeDirection sourceSide,
                           IEnergyReceiver target, ForgeDirection targetSide, int limit) {
        if (source == null || target == null || !validSide(sourceSide) || !validSide(targetSide)
                || limit <= 0 || !source.canConnectEnergy(sourceSide) || !target.canConnectEnergy(targetSide)) {
            return 0;
        }

        int available = positive(source.extractEnergy(sourceSide, limit, true));
        int accepted = Math.min(available, positive(target.receiveEnergy(targetSide, available, true)));
        int extracted = Math.min(accepted, positive(source.extractEnergy(sourceSide, accepted, false)));
        int committed = Math.min(extracted, positive(target.receiveEnergy(targetSide, extracted, false)));
        restoreToEndpoint(source, sourceSide, extracted - committed);
        return committed;
    }

    public static int moveToItem(IEnergyProvider source, ForgeDirection sourceSide,
                                 IEnergyContainerItem target, ItemStack targetStack, int limit) {
        if (source == null || target == null || targetStack == null || !validSide(sourceSide)
                || limit <= 0 || !source.canConnectEnergy(sourceSide)) {
            return 0;
        }

        int available = positive(source.extractEnergy(sourceSide, limit, true));
        int accepted = Math.min(available, positive(target.receiveEnergy(targetStack, available, true)));
        int extracted = Math.min(accepted, positive(source.extractEnergy(sourceSide, accepted, false)));
        int committed = Math.min(extracted, positive(target.receiveEnergy(targetStack, extracted, false)));
        restoreToEndpoint(source, sourceSide, extracted - committed);
        return committed;
    }

    public static int moveFromItem(IEnergyContainerItem source, ItemStack sourceStack,
                                   IEnergyReceiver target, ForgeDirection targetSide, int limit) {
        if (source == null || sourceStack == null || target == null || !validSide(targetSide)
                || limit <= 0 || !target.canConnectEnergy(targetSide)) {
            return 0;
        }

        int available = positive(source.extractEnergy(sourceStack, limit, true));
        int accepted = Math.min(available, positive(target.receiveEnergy(targetSide, available, true)));
        int extracted = Math.min(accepted, positive(source.extractEnergy(sourceStack, accepted, false)));
        int committed = Math.min(extracted, positive(target.receiveEnergy(targetSide, extracted, false)));
        if (extracted > committed) source.receiveEnergy(sourceStack, extracted - committed, false);
        return committed;
    }

    private static void restoreToEndpoint(IEnergyProvider source, ForgeDirection sourceSide, int amount) {
        if (amount > 0 && source instanceof IEnergyReceiver) {
            IEnergyReceiver receiver = (IEnergyReceiver) source;
            if (receiver.canConnectEnergy(sourceSide)) receiver.receiveEnergy(sourceSide, amount, false);
        }
    }

    private static boolean validSide(ForgeDirection side) {
        return side != null && side != ForgeDirection.UNKNOWN;
    }

    private static int positive(int amount) {
        return Math.max(0, amount);
    }
}
