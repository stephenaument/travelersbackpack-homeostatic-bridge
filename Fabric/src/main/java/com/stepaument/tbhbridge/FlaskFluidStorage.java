package com.stepaument.tbhbridge;

import homeostatic.common.fluid.FluidInfo;
import homeostatic.platform.Services;
import homeostatic.util.WaterHelper;
import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.StoragePreconditions;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleSlotStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.fabricmc.fabric.api.transfer.v1.transaction.base.SnapshotParticipant;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;

import java.util.Optional;

/**
 * Fabric Transfer API storage adapter for Homeostatic flasks.
 * This allows the flask to be used with Traveler's Backpack fluid slots.
 */
public class FlaskFluidStorage extends SnapshotParticipant<FlaskFluidStorage.FlaskSnapshot> implements SingleSlotStorage<FluidVariant> {

    private final ContainerItemContext context;

    public FlaskFluidStorage(ContainerItemContext context) {
        this.context = context;
    }

    record FlaskSnapshot(Fluid fluid, long amount) {}

    @Override
    protected FlaskSnapshot createSnapshot() {
        ItemStack stack = context.getItemVariant().toStack();
        Optional<FluidInfo> info = Services.PLATFORM.getFluidInfo(stack);
        if (info.isPresent() && !info.get().isEmpty()) {
            return new FlaskSnapshot(info.get().fluid(), info.get().amount());
        }
        return new FlaskSnapshot(Fluids.EMPTY, 0);
    }

    @Override
    protected void readSnapshot(FlaskSnapshot snapshot) {
        // Snapshot is read-only for rollback purposes
        // The actual state is stored in the ItemStack via context
    }

    @Override
    public long insert(FluidVariant resource, long maxAmount, TransactionContext transaction) {
        StoragePreconditions.notBlankNotNegative(resource, maxAmount);

        ItemStack stack = context.getItemVariant().toStack();
        if (stack.isEmpty()) return 0;

        Fluid fluid = resource.getFluid();

        // Only accept water or fluids that Homeostatic recognizes as hydratable
        if (!isValidFluid(fluid)) {
            return 0;
        }

        // Get current state
        Optional<FluidInfo> currentInfo = Services.PLATFORM.getFluidInfo(stack);
        long currentAmount = currentInfo.map(FluidInfo::amount).orElse(0L);
        Fluid currentFluid = currentInfo.map(FluidInfo::fluid).orElse(Fluids.EMPTY);

        // If flask has a different fluid, can't insert
        if (currentFluid != Fluids.EMPTY && currentFluid != fluid) {
            return 0;
        }

        long capacity = Services.PLATFORM.getFluidCapacity(stack);
        long space = capacity - currentAmount;
        long toInsert = Math.min(maxAmount, space);

        if (toInsert <= 0) return 0;

        // Update the snapshot for transaction support
        updateSnapshots(transaction);

        // Create the filled flask
        ItemStack filledStack = WaterHelper.getFilledItem(stack, fluid, (int) (currentAmount + toInsert));

        // Exchange the item in the context
        if (context.exchange(ItemVariant.of(filledStack), 1, transaction) == 1) {
            return toInsert;
        }

        return 0;
    }

    @Override
    public long extract(FluidVariant resource, long maxAmount, TransactionContext transaction) {
        StoragePreconditions.notBlankNotNegative(resource, maxAmount);

        ItemStack stack = context.getItemVariant().toStack();
        if (stack.isEmpty()) return 0;

        // Get current state
        Optional<FluidInfo> currentInfo = Services.PLATFORM.getFluidInfo(stack);
        if (currentInfo.isEmpty() || currentInfo.get().isEmpty()) {
            return 0;
        }

        FluidInfo info = currentInfo.get();
        Fluid currentFluid = info.fluid();

        // Check if the requested fluid matches
        if (resource.getFluid() != currentFluid) {
            return 0;
        }

        long currentAmount = info.amount();
        long toExtract = Math.min(maxAmount, currentAmount);

        if (toExtract <= 0) return 0;

        // Update the snapshot for transaction support
        updateSnapshots(transaction);

        long newAmount = currentAmount - toExtract;

        // Create the updated flask
        ItemStack newStack;
        if (newAmount <= 0) {
            // Empty the flask - get an empty version
            newStack = WaterHelper.getFilledItem(stack, Fluids.EMPTY, 0);
        } else {
            newStack = WaterHelper.getFilledItem(stack, currentFluid, (int) newAmount);
        }

        // Exchange the item in the context
        if (context.exchange(ItemVariant.of(newStack), 1, transaction) == 1) {
            return toExtract;
        }

        return 0;
    }

    @Override
    public boolean isResourceBlank() {
        ItemStack stack = context.getItemVariant().toStack();
        Optional<FluidInfo> info = Services.PLATFORM.getFluidInfo(stack);
        return info.isEmpty() || info.get().isEmpty();
    }

    @Override
    public FluidVariant getResource() {
        ItemStack stack = context.getItemVariant().toStack();
        Optional<FluidInfo> info = Services.PLATFORM.getFluidInfo(stack);
        if (info.isPresent() && !info.get().isEmpty()) {
            return FluidVariant.of(info.get().fluid());
        }
        return FluidVariant.blank();
    }

    @Override
    public long getAmount() {
        ItemStack stack = context.getItemVariant().toStack();
        Optional<FluidInfo> info = Services.PLATFORM.getFluidInfo(stack);
        return info.map(FluidInfo::amount).orElse(0L);
    }

    @Override
    public long getCapacity() {
        ItemStack stack = context.getItemVariant().toStack();
        return Services.PLATFORM.getFluidCapacity(stack);
    }

    /**
     * Checks if a fluid is valid for the flask (water or hydratable).
     */
    private boolean isValidFluid(Fluid fluid) {
        if (fluid == Fluids.WATER || fluid == Fluids.FLOWING_WATER) {
            return true;
        }
        // Check if Homeostatic recognizes it as hydratable
        return WaterHelper.getFluidHydration(fluid) != null;
    }
}
