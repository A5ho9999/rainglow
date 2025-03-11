package io.ix0rai.rainglow.client;

import io.ix0rai.rainglow.Rainglow;
import io.ix0rai.rainglow.data.RainglowColour;
import io.ix0rai.rainglow.data.RainglowNetworking;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;

@Environment(EnvType.CLIENT)
public class RainglowClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ClientPlayNetworking.registerGlobalReceiver(RainglowNetworking.ColourPayload.PACKET_ID, (payload, context) -> {
            MinecraftClient client = context.client();
            client.execute(() -> {
                for (var entry : payload.colours().entrySet()) {
                    RainglowColour colour = entry.getValue();
                    Rainglow.setColour(entry.getKey(), colour);
                }
            });
        });
    }
}
