package com.stepaument.tbhbridge;

import com.tiviacz.travelersbackpack.api.fluids.EffectFluid;
import com.tiviacz.travelersbackpack.inventory.FluidVariantWrapper;
import homeostatic.common.Hydration;
import homeostatic.common.fluid.HomeostaticFluids;
import homeostatic.util.WaterHelper;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

/**
 * Gulp effect for purified water from the hose.
 * Clean water = no thirst debuff, but still nausea from chugging a bucket.
 */
public class HomeostaticPurifiedWaterGulpEffect extends EffectFluid {

    // Hydration amount: 12 (same as dirty gulp)
    private static final int GULP_HYDRATION = 12;

    // Saturation: 0.7 (same as purified water default)
    private static final float GULP_SATURATION = 0.7f;

    // Nausea effect: 300 ticks (15 seconds) - you still chugged a bucket
    private static final int NAUSEA_DURATION = 300;

    public HomeostaticPurifiedWaterGulpEffect() {
        super("tbhbridge:homeostatic_purified_water_gulp", HomeostaticFluids.PURIFIED_WATER, FluidConstants.BUCKET);
    }

    @Override
    public void affectDrinker(FluidVariantWrapper fluidStack, Level level, Entity entity) {
        if (!(entity instanceof ServerPlayer player)) {
            return;
        }

        // Don't apply if player is sneaking (they're using sip mode)
        if (player.isShiftKeyDown()) {
            return;
        }

        // Clean water: good hydration, no thirst effect
        Hydration gulpHydration = new Hydration(GULP_HYDRATION, GULP_SATURATION, 0, 0, 0.0f);

        TravelersBackpackHomeostaticBridge.LOGGER.info("Purified Gulp: Adding {} hydration with {} saturation + Nausea",
            GULP_HYDRATION, GULP_SATURATION);
        WaterHelper.drink(player, gulpHydration, true);

        // Apply nausea - you still chugged a whole bucket!
        player.addEffect(new MobEffectInstance(MobEffects.NAUSEA, NAUSEA_DURATION, 0, false, true, true));
    }

    @Override
    public boolean canExecuteEffect(FluidVariantWrapper stack, Level level, Entity entity) {
        // Only execute gulp if NOT sneaking
        if (entity instanceof Player player && player.isShiftKeyDown()) {
            return false;
        }
        return stack.getAmount() >= amountRequired;
    }
}
