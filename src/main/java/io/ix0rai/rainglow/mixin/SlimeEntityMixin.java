package io.ix0rai.rainglow.mixin;

import io.ix0rai.rainglow.Rainglow;
import io.ix0rai.rainglow.data.EntityColour;
import io.ix0rai.rainglow.data.EntityVariantType;
import io.ix0rai.rainglow.data.SlimeEntityData;
import io.ix0rai.rainglow.data.SlimeVariantProvider;
import net.minecraft.entity.*;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.SlimeEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.random.RandomGenerator;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SlimeEntity.class)
public abstract class SlimeEntityMixin extends Entity implements SlimeVariantProvider
{
    @Shadow public abstract int getSize();

    protected SlimeEntityMixin(EntityType<? extends SlimeEntity> entityType, World world) {
        super(entityType, world);
        throw new UnsupportedOperationException();
    }

    @Inject(method = "initDataTracker", at = @At("TAIL"))
    protected void initDataTracker(CallbackInfo ci) {
        this.getDataTracker().startTracking(Rainglow.getTrackedColourData(EntityVariantType.Slime), EntityColour.LIME.getId());
    }

    @Inject(method = "writeCustomDataToNbt", at = @At("TAIL"))
    public void writeCustomDataToNbt(NbtCompound nbt, CallbackInfo ci) {
        String colour = Rainglow.getColour(EntityVariantType.Slime, this.getDataTracker(), this.getRandom());
        nbt.putString(Rainglow.CUSTOM_NBT_KEY, colour);
    }

    @Inject(method = "readCustomDataFromNbt", at = @At("TAIL"))
    public void readCustomDataFromNbt(NbtCompound nbt, CallbackInfo ci) {
        String colour = nbt.getString(Rainglow.CUSTOM_NBT_KEY);

        if (Rainglow.colourUnloaded(colour)) {
            colour = Rainglow.generateRandomColourId(this.getRandom());
        }

        this.setVariant(EntityColour.get(colour));
    }

    @Inject(method = "remove", at = @At("HEAD"), cancellable = true)
    public void remove(RemovalReason reason, CallbackInfo ci){
        //TODO: Do something here to apply same colour to killed Slime? Maybe Completely overwrite and cancel vanilla usage.

        // Completely copy the method from vanilla and apply tracker. (Probably a better way to do this)

        int i = this.getSize();
        if (!this.world.isClient && i > 1 && !this.isAlive()) {
            Text text = this.getCustomName();
            //TODO: Something with AI (boolean bl = this.isAiDisabled();)
            float f = (float)i / 4.0f;
            int j = i / 2;
            int k = 2 + this.random.nextInt(3);
            for (int l = 0; l < k; ++l) {
                float g = ((float)(l % 2) - 0.5f) * f;
                float h = ((float)(l / 2) - 0.5f) * f;
                SlimeEntity slimeEntity = (SlimeEntity) this.getType().create(this.world);
                if (slimeEntity == null) continue;
                //TODO: Something with Persistent (if (this.isPersistent()) {slimeEntity.setPersistent();})
                slimeEntity.setCustomName(text);
                slimeEntity.setAiDisabled(false);
                slimeEntity.setInvulnerable(this.isInvulnerable());
                slimeEntity.setSize(j, true);
                slimeEntity.refreshPositionAndAngles(this.getX() + (double)g, this.getY() + 0.5, this.getZ() + (double)h, this.random.nextFloat() * 360.0f, 0.0f);

                EntityColour colour = EntityColour.get(Rainglow.getColour(EntityVariantType.Slime, this.getDataTracker(), this.getRandom()));
                slimeEntity.getDataTracker().set(Rainglow.getTrackedColourData(EntityVariantType.Slime), colour.getId());
                this.world.spawnEntity(slimeEntity);
            }
        }
        super.remove(reason);
        ci.cancel();
    }

    @Override
    public EntityColour getVariant() {
        return EntityColour.get(Rainglow.getColour(EntityVariantType.Slime, this.getDataTracker(), this.getRandom()));
    }

    @Override
    public void setVariant(EntityColour colour) {
        this.getDataTracker().set(Rainglow.getTrackedColourData(EntityVariantType.Slime), colour.getId());
    }


    @Mixin(MobEntity.class)
    public abstract static class MobEntityMixin extends LivingEntity {
        protected MobEntityMixin(EntityType<? extends MobEntity> entityType, World world) {
            super(entityType, world);
        }

        @SuppressWarnings("all")
        @Inject(method = "initialize", at = @At("RETURN"), cancellable = true)
        public void initialize(ServerWorldAccess world, LocalDifficulty difficulty, SpawnReason spawnReason, EntityData entityData, NbtCompound entityNbt, CallbackInfoReturnable<EntityData> cir) {
            if ((Object) this instanceof SlimeEntity slime) {
                String colour = Rainglow.generateRandomColourId(this.getRandom());
                ((SlimeVariantProvider) slime).setVariant(EntityColour.get(colour));
                cir.setReturnValue(new SlimeEntityData(EntityColour.get(colour)));
            }
        }
    }

    //TODO: This no work, make work
    @Redirect(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;addParticle(Lnet/minecraft/particle/ParticleEffect;DDDDDD)V"))
    public void addParticles(World instance, ParticleEffect parameters, double x, double y, double z, double velocityX, double velocityY, double velocityZ) {
        if (((Object) this) instanceof SlimeEntity)
        {
            // send in custom colour data
            String colour = Rainglow.getColour(EntityVariantType.Slime, this.getDataTracker(), this.getRandom());
            int index = Rainglow.getColourIndex(colour);

            int i = this.getSize();
            for (int j = 0; j < i * 8; ++j) {
                this.world.addParticle(this.getParticles(), this.getX() + (Math.round((x * 10)) / 10D + index / 1000D), this.getY(), this.getZ() + (Math.round((z * 10)) / 10D + index / 1000D), 0.0, 0.0, 0.0);
            }
        }
        else
        {
            int i = this.getSize();
            for (int j = 0; j < i * 8; ++j) {
                float f = this.random.nextFloat() * ((float)Math.PI * 2);
                float g = this.random.nextFloat() * 0.5f + 0.5f;
                float h = MathHelper.sin(f) * (float)i * 0.5f * g;
                float k = MathHelper.cos(f) * (float)i * 0.5f * g;
                this.world.addParticle(this.getParticles(), this.getX() + (double)h, this.getY(), this.getZ() + (double)k, 0.0, 0.0, 0.0);
            }
        }
    }

    public ParticleEffect getParticles() {
        return ParticleTypes.ITEM_SLIME;
    }

    public RandomGenerator getRandom() {
        return this.random;
    }
}
