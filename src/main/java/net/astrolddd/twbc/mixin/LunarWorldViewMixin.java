package net.astrolddd.twbc.mixin;

import net.astrolddd.twbc.TimeWeatherBiomeControlMod;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.world.LunarWorldView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LunarWorldView.class)
public interface LunarWorldViewMixin {
    @Inject(method = "getSkyAngle", at = @At("HEAD"), cancellable = true)
    private void twbc$getSkyAngle(float tickDelta, CallbackInfoReturnable<Float> cir) {
        if ((Object) this instanceof ClientWorld world) {
            cir.setReturnValue(world.getDimension().getSkyAngle(TimeWeatherBiomeControlMod.STATE.visualTime()));
        }
    }
}