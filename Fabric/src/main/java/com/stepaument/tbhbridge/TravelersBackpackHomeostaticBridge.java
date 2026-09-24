package com.stepaument.tbhbridge;

import homeostatic.common.item.WaterContainerItem;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TravelersBackpackHomeostaticBridge implements ModInitializer {
    public static final String MOD_ID = "tbhbridge";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("Initializing Traveler's Backpack - Homeostatic Bridge");

        // Register fluid storage for all Homeostatic water container items
        registerFlaskFluidStorage();

        LOGGER.info("Traveler's Backpack - Homeostatic Bridge initialized!");
    }

    private void registerFlaskFluidStorage() {
        // Find and register all WaterContainerItem instances
        for (Item item : BuiltInRegistries.ITEM) {
            if (item instanceof WaterContainerItem) {
                LOGGER.info("Registering fluid storage for: {}", BuiltInRegistries.ITEM.getKey(item));
                FluidStorage.ITEM.registerForItems(
                    (stack, context) -> new FlaskFluidStorage(context),
                    item
                );
            }
        }
    }
}
