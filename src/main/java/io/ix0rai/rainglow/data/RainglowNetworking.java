package io.ix0rai.rainglow.data;

import io.ix0rai.rainglow.Rainglow;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.Entity;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;

import java.util.Map;
import java.util.UUID;

public class RainglowNetworking {
    public static void sendColoursTo(ServerPlayerEntity player) {
        ServerPlayNetworking.send(player, new ColourPayload(Rainglow.getColours()));
    }

    public static void sendColourChangeToServer(Entity entity, RainglowColour colour) {
        ClientPlayNetworking.send(new ColourPayload(Map.of(entity.getUuid(), colour)));
    }

    public static void sendColourChangeToClients(Entity entity, RainglowColour colour) {
        if (entity.getWorld() instanceof ServerWorld serverWorld) {
            serverWorld.getPlayers().forEach(player -> ServerPlayNetworking.send(player, new ColourPayload(Map.of(entity.getUuid(), colour))));
        } else {
            throw new RuntimeException("Cannot send colour change to clients from client");
        }
    }

    public record ColourPayload(Map<UUID, RainglowColour> colours) implements CustomPayload {
        public static final CustomPayload.Id<ColourPayload> PACKET_ID = new CustomPayload.Id<>(Rainglow.id("colour_change"));
        public static final PacketCodec<RegistryByteBuf, ColourPayload> PACKET_CODEC = PacketCodec.of(ColourPayload::write, ColourPayload::read);

        public void write(RegistryByteBuf buf) {
            buf.writeMap(this.colours, (b, uuid) -> b.writeUuid(uuid), RainglowColour::write);
        }

        public static ColourPayload read(RegistryByteBuf buf) {
            return new ColourPayload(buf.readMap(b -> b.readUuid(), RainglowColour::read));
        }

        @Override
        public Id<? extends CustomPayload> getId() {
            return PACKET_ID;
        }
    }
}
