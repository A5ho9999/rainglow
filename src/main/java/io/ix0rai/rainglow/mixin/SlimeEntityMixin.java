package io.ix0rai.rainglow.mixin;

import io.ix0rai.rainglow.Rainglow;
import io.ix0rai.rainglow.data.RainglowColour;
import io.ix0rai.rainglow.data.RainglowEntity;
import io.ix0rai.rainglow.data.SlimeVariantProvider;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.conversion.EntityConversionContext;
import net.minecraft.entity.conversion.EntityConversionType;
import net.minecraft.entity.mob.SlimeEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.scoreboard.Team;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SlimeEntity.class)
public abstract class SlimeEntityMixin extends Entity implements SlimeVariantProvider {
    @Shadow public abstract void remove(RemovalReason reason);

    protected SlimeEntityMixin(EntityType<? extends SlimeEntity> entityType, World world) {
        super(entityType, world);
        throw new UnsupportedOperationException();
    }

    @Inject(method = "writeCustomDataToNbt", at = @At("TAIL"))
    public void writeCustomDataToNbt(NbtCompound nbt, CallbackInfo ci) {
        RainglowColour colour = Rainglow.getColour(this.getUuid());
        nbt.putString(Rainglow.CUSTOM_NBT_KEY, colour.getId());
    }

    @Inject(method = "readCustomDataFromNbt", at = @At("TAIL"))
    public void readCustomDataFromNbt(NbtCompound nbt, CallbackInfo ci) {
        this.setVariant(RainglowEntity.SLIME.readNbt(nbt));
    }

    /**
     * @reason Make smaller slimes spawn with the same colour as the parent in a split
     * @reason They made this really annoying for no reason
     */
    @Inject(method = "remove", at = @At("HEAD"), cancellable = true)
    private void preserveColorOnSplit(Entity.RemovalReason reason, CallbackInfo ci) {
        SlimeEntity thisSlime = (SlimeEntity) (Object) this;
        int size = thisSlime.getSize();

        // Make sure it's dead this time, oops
        if (!thisSlime.getWorld().isClient && size > 1 && thisSlime.isDead()) {
            // Get the parent slime color before splitting
            RainglowColour parentColor = Rainglow.getColour(thisSlime.getUuid());

            float width = thisSlime.getDimensions(thisSlime.getPose()).width();
            float halfWidth = width / 2.0F;
            int newSize = size / 2;
            Team team = thisSlime.getScoreboardTeam();

            // Calculate how many baby slimes to spawn (2-4)
            int count = 2 + thisSlime.getRandom().nextInt(3);

            // Create multiple slimes individually
            for (int i = 0; i < count; i++) {
                float offsetX = ((float) (i % 2) - 0.5F) * halfWidth;
                float offsetZ = ((float) (i / 2) - 0.5F) * halfWidth;

                //noinspection unchecked
                thisSlime.convertTo((EntityType<SlimeEntity>) thisSlime.getType(), new EntityConversionContext(EntityConversionType.SPLIT_ON_DEATH, false, false, team), SpawnReason.TRIGGERED, (newSlime) -> {
                            newSlime.setSize(newSize, true);
                            newSlime.refreshPositionAndAngles(thisSlime.getX() + offsetX, thisSlime.getY() + 0.5, thisSlime.getZ() + offsetZ, thisSlime.getRandom().nextFloat() * 360.0F, 0.0F);

                            // Now that headache is done, set the child slime color to match the parent
                            ((SlimeVariantProvider) newSlime).setVariant(parentColor);
                        });
            }

            // Don't forget this, boy was that a mistake
            super.remove(reason);
            ci.cancel();
        }
    }

    @Override
    public RainglowColour getVariant() {
        return Rainglow.getColour(this.getUuid());
    }

    @Override
    public void setVariant(RainglowColour colour) {
        Rainglow.setColour(this, colour);
    }
}
