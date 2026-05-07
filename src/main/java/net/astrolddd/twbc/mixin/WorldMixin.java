package net.astrolddd.twbc.mixin;

import net.astrolddd.twbc.TimeWeatherBiomeControlMod;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(World.class)
public abstract class WorldMixin {
    @Inject(method = "getTimeOfDay", at = @At("HEAD"), cancellable = true)
    private void twbc$getTimeOfDay(CallbackInfoReturnable<Long> cir) {
        if ((Object) this instanceof ClientWorld) {
            cir.setReturnValue(TimeWeatherBiomeControlMod.STATE.visualTime());
        }
    }
    @Inject(method = "getSkyAngleRadians", at = @At("HEAD"), cancellable = true)
    private void twbc$getSkyAngleRadians(float tickDelta, CallbackInfoReturnable<Float> cir) {
        if ((Object) this instanceof ClientWorld world) {
            float skyAngle = world.getDimension().getSkyAngle(TimeWeatherBiomeControlMod.STATE.visualTime());
            cir.setReturnValue((float) (skyAngle * Math.PI * 2.0D));
        }
    }

    @Inject(method = "getRainGradient", at = @At("HEAD"), cancellable = true)
    private void twbc$getRainGradient(float delta, CallbackInfoReturnable<Float> cir) {
        if ((Object) this instanceof ClientWorld && TimeWeatherBiomeControlMod.STATE.overridesWeather()) {
            cir.setReturnValue(TimeWeatherBiomeControlMod.STATE.rainGradient());
        }
    }

    @Inject(method = "getThunderGradient", at = @At("HEAD"), cancellable = true)
    private void twbc$getThunderGradient(float delta, CallbackInfoReturnable<Float> cir) {
        if ((Object) this instanceof ClientWorld && TimeWeatherBiomeControlMod.STATE.overridesWeather()) {
            cir.setReturnValue(TimeWeatherBiomeControlMod.STATE.thunderGradient());
        }
    }

    @Inject(method = "isRaining", at = @At("HEAD"), cancellable = true)
    private void twbc$isRaining(CallbackInfoReturnable<Boolean> cir) {
        if ((Object) this instanceof ClientWorld && TimeWeatherBiomeControlMod.STATE.overridesWeather()) {
            cir.setReturnValue(TimeWeatherBiomeControlMod.STATE.visualRaining());
        }
    }

    @Inject(method = "isThundering", at = @At("HEAD"), cancellable = true)
    private void twbc$isThundering(CallbackInfoReturnable<Boolean> cir) {
        if ((Object) this instanceof ClientWorld && TimeWeatherBiomeControlMod.STATE.overridesWeather()) {
            cir.setReturnValue(TimeWeatherBiomeControlMod.STATE.visualThundering());
        }
    }

    @Inject(method = "hasRain", at = @At("HEAD"), cancellable = true)
    private void twbc$hasRain(BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        if ((Object) this instanceof ClientWorld && TimeWeatherBiomeControlMod.STATE.overridesWeather()) {
            cir.setReturnValue(TimeWeatherBiomeControlMod.STATE.visualRaining());
        }
    }
}

