package com.stepaument.tbhbridge;

import com.tiviacz.travelersbackpack.api.fluids.EffectFluid;
import com.tiviacz.travelersbackpack.inventory.FluidVariantWrapper;
import homeostatic.common.Hydration;
import homeostatic.util.WaterHelper;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluids;

/**
 * Adds Homeostatic hydration to the existing "gulp" drinking behavior.
 * This runs alongside the vanilla WaterEffect (Regeneration + fire extinguish).
 * Uses 1 bucket of water.
 *
 * Gulp fills hydration to max but has 90% chance of Thirst effect.
 * This makes it an "emergency" option - great when desperate, but sipping is more efficient.
 */
public class HomeostaticWaterGulpEffect extends EffectFluid {

    // Hydration amount: 12
    private static final int GULP_HYDRATION = 12;

    // Thirst effect: potency 45 (same as dirty water), 200 ticks (10 seconds), 95% chance
    private static final int THIRST_POTENCY = 45;
    private static final int THIRST_DURATION = 200;
    private static final float THIRST_CHANCE = 0.95f;

    // Nausea effect: 300 ticks (15 seconds)
    private static final int NAUSEA_DURATION = 300;

    public HomeostaticWaterGulpEffect() {
        super("tbhbridge:homeostatic_water_gulp", Fluids.WATER, FluidConstants.BUCKET);
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

        // Add 9 hydration (same as milk bucket)
        Hydration gulpHydration = new Hydration(GULP_HYDRATION, 0.0f, THIRST_POTENCY, THIRST_DURATION, THIRST_CHANCE);

        TravelersBackpackHomeostaticBridge.LOGGER.info("Gulp: Adding {} hydration with {}% Thirst chance + Nausea",
            GULP_HYDRATION, (int)(THIRST_CHANCE * 100));
        WaterHelper.drink(player, gulpHydration, true);

        // Apply nausea - you chugged a whole bucket!
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
