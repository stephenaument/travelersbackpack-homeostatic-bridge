package com.stepaument.tbhbridge.mixin;

import com.stepaument.tbhbridge.HomeostaticWaterSipEffect;
import com.stepaument.tbhbridge.TravelersBackpackHomeostaticBridge;
import com.tiviacz.travelersbackpack.inventory.FluidTank;
import com.tiviacz.travelersbackpack.inventory.FluidVariantWrapper;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(targets = "com.tiviacz.travelersbackpack.item.HoseItem", remap = false)
public class HoseItemMixin {

    /**
     * Redirects the tank.drain() call to use a smaller amount when sneaking (sip mode).
     */
    @Redirect(
        method = "finishUsingItem",
        at = @At(value = "INVOKE", target = "Lcom/tiviacz/travelersbackpack/inventory/FluidTank;drain(JZ)Lcom/tiviacz/travelersbackpack/inventory/FluidVariantWrapper;"),
        remap = false
    )
    private FluidVariantWrapper redirectDrain(FluidTank tank, long drainAmount, boolean simulate,
                                               ItemStack stack, Level level, LivingEntity entityLiving) {
        if (entityLiving instanceof Player player && player.isShiftKeyDown()) {
            TravelersBackpackHomeostaticBridge.LOGGER.info("Sip mode: reducing drain from {} to {}", drainAmount, HomeostaticWaterSipEffect.SIP_AMOUNT);
            return tank.drain(HomeostaticWaterSipEffect.SIP_AMOUNT, simulate);
        }
        return tank.drain(drainAmount, simulate);
    }
}
