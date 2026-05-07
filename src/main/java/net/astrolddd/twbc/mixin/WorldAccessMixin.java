package net.astrolddd.twbc.mixin;

import net.astrolddd.twbc.TimeWeatherBiomeControlMod;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.world.WorldAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(WorldAccess.class)
public interface WorldAccessMixin {
    @Inject(method = "getLunarTime", at = @At("HEAD"), cancellable = true)
    private void twbc$getLunarTime(CallbackInfoReturnable<Long> cir) {
        if ((Object) this instanceof ClientWorld) {
            cir.setReturnValue(TimeWeatherBiomeControlMod.STATE.visualTime());
        }
    }
}