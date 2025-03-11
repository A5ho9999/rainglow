package io.ix0rai.rainglow.mixin.client;

import io.ix0rai.rainglow.data.EntityRenderStateTracker;
import net.minecraft.client.render.entity.state.EntityRenderState;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import java.util.UUID;

@Mixin(EntityRenderState.class)
public class EntityRenderStateMixin implements EntityRenderStateTracker {
    @Unique private UUID entityUuid;

    @Override
    public void rainglow$setEntity(Entity entity) {
        if (entity != null) {
            this.entityUuid = entity.getUuid();
        }
    }

    @Override
    public UUID rainglow$getEntityUuid() {
        return this.entityUuid;
    }
}