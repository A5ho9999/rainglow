package io.ix0rai.rainglow.mixin.client;

import io.ix0rai.rainglow.data.EntityRenderStateTracker;
import io.ix0rai.rainglow.data.RainglowEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.entity.AllayEntityRenderer;
import net.minecraft.client.render.entity.state.AllayEntityRenderState;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.UUID;

@Mixin(AllayEntityRenderer.class)
public class AllayEntityRendererMixin {
    @Inject(method = "getTexture(Lnet/minecraft/client/render/entity/state/AllayEntityRenderState;)Lnet/minecraft/util/Identifier;", at = @At("HEAD"), cancellable = true)
    private void rainglow$getTexture(AllayEntityRenderState state, CallbackInfoReturnable<Identifier> cir) {
        if (state instanceof EntityRenderStateTracker) {
            UUID entityUuid = ((EntityRenderStateTracker) state).rainglow$getEntityUuid();
            if (entityUuid != null) {
                ClientWorld world = MinecraftClient.getInstance().world;
                if (world != null) {
                    boolean rainbowState = ((EntityRenderStateTracker) state).rainglow$isRainbow();
                    RainglowEntity.ALLAY.overrideTexture(entityUuid, rainbowState, cir);
                }
            }
        }
    }
}
