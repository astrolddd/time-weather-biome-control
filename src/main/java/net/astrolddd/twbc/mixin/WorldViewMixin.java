package net.astrolddd.twbc.mixin;

import net.astrolddd.twbc.TimeWeatherBiomeControlMod;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldView;
import net.minecraft.world.biome.Biome;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@Mixin(WorldView.class)
public interface WorldViewMixin {
    @Inject(method = "getBiome", at = @At("HEAD"), cancellable = true)
    private void twbc$getBiome(BlockPos pos, CallbackInfoReturnable<RegistryEntry<Biome>> cir) {
        fakeBiome(cir);
    }



    private void fakeBiome(CallbackInfoReturnable<RegistryEntry<Biome>> cir) {
        if (!((Object) this instanceof ClientWorld world)) {
            return;
        }

        Optional<RegistryEntry.Reference<Biome>> biome = world.getRegistryManager()
                .get(RegistryKeys.BIOME)
                .getEntry(TimeWeatherBiomeControlMod.STATE.biomeKey());
        biome.ifPresent(cir::setReturnValue);
    }
}
