package io.ix0rai.rainglow.mixin.client;

import io.ix0rai.rainglow.data.EntityRenderStateTracker;
import io.ix0rai.rainglow.data.RainglowEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.entity.GlowSquidEntityRenderer;
import net.minecraft.client.render.entity.state.SquidRenderState;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.UUID;

@Mixin(GlowSquidEntityRenderer.class)
public class GlowSquidEntityRendererMixin {
    /**
     * @reason use the colour from the entity's NBT data for textures
     * @author ix0rai
     */
    @Inject(method = "getTexture*", at = @At("HEAD"), cancellable = true)
    public void getTexture(SquidRenderState state, CallbackInfoReturnable<Identifier> cir) {
        if (state instanceof EntityRenderStateTracker) {
            UUID entityUuid = ((EntityRenderStateTracker) state).rainglow$getEntityUuid();
            if (entityUuid != null) {
                ClientWorld world = MinecraftClient.getInstance().world;
                if (world != null) {
                    RainglowEntity type = RainglowEntity.GLOW_SQUID;
                    Identifier texture = type.overrideTexture(entityUuid, world);
                    cir.setReturnValue(texture != null ? texture : type.getDefaultTexture());
                }
            }
        }
    }
}
