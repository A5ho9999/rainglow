package io.ix0rai.rainglow;

import io.ix0rai.rainglow.data.*;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.Entity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class Rainglow implements ModInitializer {
    public static final String MOD_ID = "rainglow";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static final String CUSTOM_NBT_KEY = "Colour";
    private static final Map<UUID, RainglowColour> COLOURS = new HashMap<>();

    @Override
    public void onInitialize() {
        PayloadTypeRegistry.playS2C().register(RainglowNetworking.ColourPayload.PACKET_ID, RainglowNetworking.ColourPayload.PACKET_CODEC);
        PayloadTypeRegistry.playC2S().register(RainglowNetworking.ColourPayload.PACKET_ID, RainglowNetworking.ColourPayload.PACKET_CODEC);

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            // send all colours to client
            RainglowNetworking.sendColoursTo(handler.player);
        });

        ServerPlayNetworking.registerGlobalReceiver(RainglowNetworking.ColourPayload.PACKET_ID, (payload, context) -> {
            for (var entry : payload.colours().entrySet()) {
                RainglowColour colour = entry.getValue();
                Rainglow.setColour(entry.getKey(), colour);
            }
        });


        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            // Clear only if it's NOT single-player (Let SERVER_STOPPED handle single-player clearing)
            if (!server.isSingleplayer()) {
                COLOURS.clear();
            }
        });

        // Correctly clear colours on server stopped, not disconnect (Applies mainly to single player as if dedicated server would be completely offline anyway)
        ServerLifecycleEvents.SERVER_STOPPED.register(minecraftServer -> COLOURS.clear());
    }

    public static Identifier id(String id) {
        return Identifier.of(MOD_ID, id);
    }

    public static RainglowColour generateRandomColour(World world, Random random) {
        var colours = List.of(RainglowColour.values());
        return colours.get(random.nextInt(colours.size()));
    }

    public static String translatableTextKey(String key) {
        if (key.split("\\.").length < 2) throw new IllegalArgumentException("key must be in format \"category.key\": " + key);
        return MOD_ID + "." + key;
    }

    public static Text translatableText(String key, Object... args) {
        return Text.translatable(translatableTextKey(key), args);
    }

    public static Text translatableText(String key) {
        return Text.translatable(translatableTextKey(key));
    }

    public static RainglowColour getColour(UUID entity) {
        return COLOURS.get(entity);
    }

    public static void setColour(Entity entity, RainglowColour colour) {
        setColour(entity.getUuid(), colour);

        if (entity.getWorld().isClient()) {
            // sync to server; will then be synced to all clients
            RainglowNetworking.sendColourChangeToServer(entity, colour);
        } else if (entity.getWorld().getServer().isDedicated()) {
            // sync to all clients
            RainglowNetworking.sendColourChangeToClients(entity, colour);
        }
    }

    public static void setColour(UUID uuid, RainglowColour colour) {
        COLOURS.put(uuid, colour);
    }

    public static Map<UUID, RainglowColour> getColours() {
        return COLOURS;
    }
}
