package io.ix0rai.rainglow.mixin.client;

import io.ix0rai.rainglow.data.EntityRenderStateTracker;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.state.EntityRenderState;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityRenderer.class)
public class EntityRendererMixin <T extends Entity, S extends EntityRenderState> {
    @Inject(method = "updateRenderState", at = @At("HEAD"))
    private void rainglow$updateRenderState(T entity, S state, float tickDelta, CallbackInfo ci) {
        if (state instanceof EntityRenderStateTracker) {
            ((EntityRenderStateTracker) state).rainglow$setEntity(entity);

            // TODO: This is a placeholder, add new variant or something. Don't recommend using names
            if (entity.hasCustomName()) {
                if (entity.getCustomName().getString().contains("gay")) {
                    ((EntityRenderStateTracker) state).rainglow$setRainbow(true);
                }
            } else {
                // Reset it just in case the name changed or whatever
                ((EntityRenderStateTracker) state).rainglow$setRainbow(false);
            }
        }
    }
}
