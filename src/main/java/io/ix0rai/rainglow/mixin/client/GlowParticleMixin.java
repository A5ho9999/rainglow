package io.ix0rai.rainglow.mixin.client;

import io.ix0rai.rainglow.data.RainglowColour;
import net.minecraft.client.particle.GlowParticle;
import net.minecraft.client.particle.SpriteProvider;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.SimpleParticleType;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Random;

@Mixin(GlowParticle.GlowFactory.class)
public class GlowParticleMixin {
    @Final
    @Shadow
    private SpriteProvider spriteProvider;

    /**
     * @author ix0rai
     * @reason recolor particles
     */
    @Inject(method = "createParticle*", at = @At("HEAD"), cancellable = true)
    public void createParticle(SimpleParticleType defaultParticleType, ClientWorld clientWorld, double d, double e, double f, double g, double h, double i, CallbackInfoReturnable<GlowParticle> cir) {
        // we use whether g is over 100 to determine if we should override the method
        if (g > 99) {
            g -= 100;
            Random random = new Random();

            GlowParticle glowParticle = new GlowParticle(clientWorld, d, e, f, 0.5 - random.nextDouble(), h, 0.5 - random.nextDouble(), this.spriteProvider);

            // we check the g value to see what the colour is
            RainglowColour.RGB rgb = RainglowColour.getPassiveParticleRGB((int) g, random);
            glowParticle.setColor(rgb.r(), rgb.g(), rgb.b());

            // set velocities - I don't entirely understand why this is necessary, it's copied from vanilla code
            glowParticle.velocityY *= 0.2;
            if (g == 0.0 && i == 0.0) {
                glowParticle.velocityX *= 0.1;
                glowParticle.velocityZ *= 0.1;
            }

            // set expiry date of particle and return
            glowParticle.setMaxAge((int) (8.0 / (clientWorld.getRandom().nextDouble() * 0.8 + 0.2)));
            cir.setReturnValue(glowParticle);
        }
    }
}
