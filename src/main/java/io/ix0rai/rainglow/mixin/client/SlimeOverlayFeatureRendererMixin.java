package io.ix0rai.rainglow.mixin.client;

import io.ix0rai.rainglow.data.EntityRenderStateTracker;
import io.ix0rai.rainglow.data.RainglowEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.SlimeEntityRenderer;
import net.minecraft.client.render.entity.feature.SlimeOverlayFeatureRenderer;
import net.minecraft.client.render.entity.state.SlimeEntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.UUID;

@Mixin(SlimeOverlayFeatureRenderer.class)
public class SlimeOverlayFeatureRendererMixin {

    @Redirect(method = "render(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;ILnet/minecraft/client/render/entity/state/SlimeEntityRenderState;FF)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/RenderLayer;getOutline(Lnet/minecraft/util/Identifier;)Lnet/minecraft/client/render/RenderLayer;"))
    private RenderLayer rainglow$getOutline(Identifier defaultTexture, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, SlimeEntityRenderState state, float f, float g) {
        Identifier overrideTexture = getOverrideTexture(state);
        return overrideTexture != null ? RenderLayer.getOutline(overrideTexture) : RenderLayer.getOutline(SlimeEntityRenderer.TEXTURE);
    }

    @Redirect(method = "render(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;ILnet/minecraft/client/render/entity/state/SlimeEntityRenderState;FF)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/RenderLayer;getEntityTranslucent(Lnet/minecraft/util/Identifier;)Lnet/minecraft/client/render/RenderLayer;"))
    private RenderLayer rainglow$getEntityTranslucent(Identifier defaultTexture, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, SlimeEntityRenderState state, float f, float g) {
        Identifier overrideTexture = getOverrideTexture(state);
        return overrideTexture != null ? RenderLayer.getEntityTranslucent(overrideTexture) : RenderLayer.getEntityTranslucent(SlimeEntityRenderer.TEXTURE);
    }

    @Unique
    private Identifier getOverrideTexture(SlimeEntityRenderState state) {
        if (state instanceof EntityRenderStateTracker) {
            UUID entityUuid = ((EntityRenderStateTracker) state).rainglow$getEntityUuid();
            if (entityUuid != null) {
                ClientWorld world = MinecraftClient.getInstance().world;
                if (world != null) {

                    try {
                        return RainglowEntity.SLIME.overrideTextureNoCallback(entityUuid);
                    } catch (Exception e) {
                        //ignore, just here to prevent possible crashes
                    }
                }
            }
        }
        return null;
    }
}
