package com.stepaument.tbhbridge;

import com.tiviacz.travelersbackpack.api.fluids.EffectFluid;
import com.tiviacz.travelersbackpack.inventory.FluidVariantWrapper;
import homeostatic.common.Hydration;
import homeostatic.util.WaterHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;

/**
 * Sip mode - efficient drinking that only provides Homeostatic hydration.
 * No Regeneration, no fire extinguishing - just hydration.
 * Uses the same amount as a flask drink (~50mB = 4050 droplets).
 * Activated by sneaking while using the hose.
 */
public class HomeostaticWaterSipEffect extends EffectFluid {

    // 50mB equivalent in Fabric droplets (81000 droplets = 1000mB, so 50mB = 4050 droplets)
    public static final long SIP_AMOUNT = 4050L;

    public HomeostaticWaterSipEffect() {
        super("tbhbridge:homeostatic_water_sip", Fluids.WATER, SIP_AMOUNT);
    }

    @Override
    public void affectDrinker(FluidVariantWrapper fluidStack, Level level, Entity entity) {
        if (!(entity instanceof ServerPlayer player)) {
            return;
        }

        // Only apply if player is sneaking (sip mode)
        if (!player.isShiftKeyDown()) {
            return;
        }

        Fluid fluid = fluidStack.fluidVariant().getFluid();
        Hydration hydration = WaterHelper.getFluidHydration(fluid);

        if (hydration != null) {
            TravelersBackpackHomeostaticBridge.LOGGER.info("Sip: Applying hydration from {} - amount: {}, chance: {}",
                fluid, hydration.amount(), hydration.chance());
            WaterHelper.drink(player, hydration, true);
        }
    }

    @Override
    public boolean canExecuteEffect(FluidVariantWrapper stack, Level level, Entity entity) {
        // Only execute sip if sneaking
        if (entity instanceof Player player && !player.isShiftKeyDown()) {
            return false;
        }
        return stack.getAmount() >= amountRequired;
    }
}
