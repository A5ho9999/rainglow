package io.ix0rai.rainglow.mixin.client;

import io.ix0rai.rainglow.data.EntityRenderStateTracker;
import io.ix0rai.rainglow.data.RainbowManager;
import io.ix0rai.rainglow.data.RainglowEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.render.entity.state.LivingEntityRenderState;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.UUID;

@Mixin(value = LivingEntityRenderer.class)
public abstract class LivingEntityRendererMixin<T extends LivingEntity, S extends LivingEntityRenderState, M extends EntityModel<? super S>> extends EntityRenderer<T, S> implements FeatureRendererContext<S, M> {
    protected LivingEntityRendererMixin(EntityRendererFactory.Context context) {
        super(context);
    }

    @Inject(method = "getMixColor", at = @At("RETURN"), cancellable = true)
    private void getMixColour(S state, CallbackInfoReturnable<Integer> cir) {
        // Only if this entity uses the State Tracker and has valid UUID
        if (state instanceof EntityRenderStateTracker) {
            UUID entityUuid = ((EntityRenderStateTracker) state).rainglow$getEntityUuid();
            if (entityUuid != null) {
                boolean rainbowState = ((EntityRenderStateTracker) state).rainglow$isRainbow();
                if (rainbowState) {
                    ClientWorld world = MinecraftClient.getInstance().world;
                    if (world != null) {
                        LivingEntity entity = RainbowManager.findEntityByUuid(world, entityUuid);
                        if (RainglowEntity.get(entity) != null) {
                            int colour = RainbowManager.getRainbowColor(entityUuid);
                            cir.setReturnValue(colour);
                        }
                    }
                }
            }
        }
    }
}
