package io.ix0rai.rainglow.mixin.client;

import io.ix0rai.rainglow.Rainglow;
import io.ix0rai.rainglow.data.EntityColour;
import io.ix0rai.rainglow.data.EntityVariantType;
import net.minecraft.client.render.entity.SlimeEntityRenderer;
import net.minecraft.entity.mob.SlimeEntity;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SlimeEntityRenderer.class)
public class SlimeEntityRendererMixin {

    @Inject(method = "getTexture*", at = @At("HEAD"), cancellable = true)
    public void getTexture(SlimeEntity slimeEntity, CallbackInfoReturnable<Identifier> cir)
    {
        String colour = Rainglow.getColour(EntityVariantType.Slime, slimeEntity.getDataTracker(), slimeEntity.getRandom());

        //TODO: Thing here?

        // if the colour is Lime we don't need to override the method
        // this optimises a tiny bit
        if (!colour.equals(EntityColour.LIME.getId())) {
            Identifier texture = Rainglow.getTexture(EntityVariantType.Slime, colour);
            cir.setReturnValue(texture != null ? texture : Rainglow.getDefaultTexture(EntityVariantType.Slime));
        }
    }
}
