package com.stepaument.tbhbridge;

import com.tiviacz.travelersbackpack.api.fluids.EffectFluid;
import com.tiviacz.travelersbackpack.inventory.FluidVariantWrapper;
import homeostatic.common.Hydration;
import homeostatic.common.fluid.HomeostaticFluids;
import homeostatic.util.WaterHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;

/**
 * Sip effect for purified water from the hose.
 * Uses Homeostatic's purified water hydration values (3 hydration, 0.7 saturation, no thirst).
 */
public class HomeostaticPurifiedWaterSipEffect extends EffectFluid {

    // Same sip amount as dirty water
    public static final long SIP_AMOUNT = HomeostaticWaterSipEffect.SIP_AMOUNT;

    public HomeostaticPurifiedWaterSipEffect() {
        super("tbhbridge:homeostatic_purified_water_sip", HomeostaticFluids.PURIFIED_WATER, SIP_AMOUNT);
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
            TravelersBackpackHomeostaticBridge.LOGGER.info("Purified Sip: Applying hydration from {} - amount: {}, saturation: {}",
                fluid, hydration.amount(), hydration.saturation());
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
