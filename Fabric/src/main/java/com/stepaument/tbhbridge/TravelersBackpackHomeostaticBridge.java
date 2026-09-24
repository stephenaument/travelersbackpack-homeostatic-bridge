package com.stepaument.tbhbridge;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import homeostatic.common.item.WaterContainerItem;
import homeostatic.common.water.WaterInfo;
import homeostatic.platform.Services;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.minecraft.commands.Commands;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TravelersBackpackHomeostaticBridge implements ModInitializer {
    public static final String MOD_ID = "tbhbridge";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    // Effect instances (registered when server starts)
    public static HomeostaticWaterGulpEffect GULP_EFFECT;
    public static HomeostaticWaterSipEffect SIP_EFFECT;
    public static HomeostaticPurifiedWaterGulpEffect PURIFIED_GULP_EFFECT;
    public static HomeostaticPurifiedWaterSipEffect PURIFIED_SIP_EFFECT;

    @Override
    public void onInitialize() {
        LOGGER.info("Initializing Traveler's Backpack - Homeostatic Bridge");

        // Register fluid storage for all Homeostatic water container items
        registerFlaskFluidStorage();

        // Register hose drinking effects when server starts (after TB has initialized its registry)
        ServerLifecycleEvents.SERVER_STARTING.register(server -> {
            registerHoseEffects();
        });

        // Register debug commands only if enabled (pass -Dtbhbridge.debug=true)
        if (Boolean.getBoolean("tbhbridge.debug")) {
            registerCommands();
            LOGGER.info("Debug commands enabled");
        }

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

    private void registerHoseEffects() {
        // Only register once (SERVER_STARTING fires each time a world loads)
        if (GULP_EFFECT != null) {
            LOGGER.debug("Hose effects already registered, skipping");
            return;
        }

        // Register dirty water effects
        GULP_EFFECT = new HomeostaticWaterGulpEffect();
        LOGGER.info("Registered dirty water gulp effect");

        SIP_EFFECT = new HomeostaticWaterSipEffect();
        LOGGER.info("Registered dirty water sip effect");

        // Register purified water effects
        PURIFIED_GULP_EFFECT = new HomeostaticPurifiedWaterGulpEffect();
        LOGGER.info("Registered purified water gulp effect");

        PURIFIED_SIP_EFFECT = new HomeostaticPurifiedWaterSipEffect();
        LOGGER.info("Registered purified water sip effect");
    }

    private void registerCommands() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher.register(Commands.literal("tbhbridge")
                .then(Commands.literal("hydration")
                    .then(Commands.literal("get")
                        .executes(context -> {
                            if (context.getSource().getEntity() instanceof ServerPlayer player) {
                                Services.PLATFORM.getWaterCapabilty(player).ifPresent(data -> {
                                    int level = data.getWaterLevel();
                                    float saturation = data.getWaterSaturationLevel();
                                    float exhaustion = data.getWaterExhaustionLevel();
                                    context.getSource().sendSuccess(() -> Component.literal(
                                        String.format("Hydration: %d/%d, Saturation: %.2f, Exhaustion: %.2f",
                                            level, WaterInfo.MAX_WATER_LEVEL, saturation, exhaustion)), false);
                                });
                            }
                            return 1;
                        }))
                    .then(Commands.literal("set")
                        .then(Commands.argument("level", IntegerArgumentType.integer(0, WaterInfo.MAX_WATER_LEVEL))
                            .executes(context -> {
                                if (context.getSource().getEntity() instanceof ServerPlayer player) {
                                    int targetLevel = IntegerArgumentType.getInteger(context, "level");
                                    Services.PLATFORM.getWaterCapabilty(player).ifPresent(data -> {
                                        WaterInfo newInfo = new WaterInfo(targetLevel, 0.0f, 0.0f);
                                        data.setWaterData(newInfo);
                                        Services.PLATFORM.syncWaterData(player, newInfo);
                                        context.getSource().sendSuccess(() -> Component.literal(
                                            String.format("Set hydration to %d", targetLevel)), false);
                                    });
                                }
                                return 1;
                            })))
                    .then(Commands.literal("fill")
                        .executes(context -> {
                            if (context.getSource().getEntity() instanceof ServerPlayer player) {
                                Services.PLATFORM.getWaterCapabilty(player).ifPresent(data -> {
                                    WaterInfo newInfo = new WaterInfo(WaterInfo.MAX_WATER_LEVEL, WaterInfo.MAX_SATURATION_LEVEL, 0.0f);
                                    data.setWaterData(newInfo);
                                    Services.PLATFORM.syncWaterData(player, newInfo);
                                    context.getSource().sendSuccess(() -> Component.literal(
                                        "Filled hydration to max"), false);
                                });
                            }
                            return 1;
                        }))
                    .then(Commands.literal("empty")
                        .executes(context -> {
                            if (context.getSource().getEntity() instanceof ServerPlayer player) {
                                Services.PLATFORM.getWaterCapabilty(player).ifPresent(data -> {
                                    WaterInfo newInfo = new WaterInfo(0, 0.0f, 0.0f);
                                    data.setWaterData(newInfo);
                                    Services.PLATFORM.syncWaterData(player, newInfo);
                                    context.getSource().sendSuccess(() -> Component.literal(
                                        "Emptied hydration"), false);
                                });
                            }
                            return 1;
                        }))));
        });
        LOGGER.info("Registered /tbhbridge commands");
    }
}
