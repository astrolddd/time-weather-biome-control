package net.astrolddd.twbc;
import net.minecraft.client.MinecraftClient;

import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.minecraft.world.biome.Biome;

import java.util.List;

public final class TimeWeatherBiomeControlState {
    private final AtmosphereConfig config = AtmosphereConfig.load();

    private String biomeId = "minecraft:plains";
    private long targetTime = 6000L;
    private double visualTime = 6000.0D;
    private WeatherMode weatherMode = WeatherMode.VANILLA;
    private boolean smoothTransitions = true;
    private double transitionSpeed = 0.5D;
    private float visualRain = 0.0F;
    private float visualThunder = 0.0F;

    public void load(AtmosphereConfig loadedConfig) {
        biomeId = loadedConfig.biomeId;
        targetTime = loadedConfig.timeOfDay;
        visualTime = targetTime;
        weatherMode = loadedConfig.weatherMode;
        smoothTransitions = loadedConfig.smoothTransitions;
        transitionSpeed = clamp(loadedConfig.transitionSpeed, 0.1D, 1.0D);
        config.biomeId = loadedConfig.biomeId;
        config.timeOfDay = loadedConfig.timeOfDay;
        config.weatherMode = loadedConfig.weatherMode;
        config.smoothTransitions = smoothTransitions;
        config.transitionSpeed = transitionSpeed;
        config.presets = loadedConfig.presets;
    }

    public void tick() {
        double speed = smoothTransitions ? transitionSpeed * 0.12D : 1.0D;
        visualTime = wrapLerp(visualTime, targetTime, speed, 24000.0D);
        visualRain += (targetRain() - visualRain) * speed;
        visualThunder += (targetThunder() - visualThunder) * speed;
    }

    public RegistryKey<Biome> biomeKey() {
        return RegistryKey.of(RegistryKeys.BIOME, Identifier.of(biomeId));
    }

    public String biomeId() {
        return biomeId;
    }

    public void setBiomeId(String biomeId) {
        boolean changed = !this.biomeId.equals(biomeId);
        this.biomeId = biomeId;
        persist();
        if (changed) {
            refreshBiomeRender();
        }
    }

    public long targetTime() {
        return targetTime;
    }

    public long visualTime() {
        return Math.floorMod(Math.round(visualTime), 24000L);
    }

    public void setTargetTime(long targetTime) {
        this.targetTime = Math.floorMod(targetTime, 24000L);
        persist();
    }

    public WeatherMode weatherMode() {
        return weatherMode;
    }

    public void setWeatherMode(WeatherMode weatherMode) {
        this.weatherMode = weatherMode;
        persist();
    }

    public boolean overridesWeather() {
        return weatherMode != WeatherMode.VANILLA;
    }
    public boolean visualRaining() {
        return weatherMode == WeatherMode.RAIN || weatherMode == WeatherMode.THUNDER;
    }

    public boolean visualThundering() {
        return weatherMode == WeatherMode.THUNDER;
    }

    public boolean smoothTransitions() {
        return smoothTransitions;
    }

    public void setSmoothTransitions(boolean smoothTransitions) {
        this.smoothTransitions = smoothTransitions;
        persist();
    }

    public double transitionSpeed() {
        return transitionSpeed;
    }

    public void setTransitionSpeed(double transitionSpeed) {
        this.transitionSpeed = clamp(transitionSpeed, 0.1D, 1.0D);
        persist();
    }

    public float rainGradient() {
        return visualRain;
    }

    public float thunderGradient() {
        return visualThunder;
    }

    public List<AtmospherePreset> presets() {
        return config.presets;
    }

    public void applyPreset(AtmospherePreset preset) {
        boolean biomeChanged = !biomeId.equals(preset.biomeId());
        biomeId = preset.biomeId();
        targetTime = preset.timeOfDay();
        weatherMode = preset.weatherMode();
        persist();
        if (biomeChanged) {
            refreshBiomeRender();
        }
    }

    public void saveCurrentPreset(String name) {
        config.presets.add(new AtmospherePreset(name, biomeId, targetTime, weatherMode));
        persist();
    }

    public void resetToDefault() {
        boolean biomeChanged = !biomeId.equals("minecraft:plains");
        biomeId = "minecraft:plains";
        targetTime = 6000L;
        visualTime = targetTime;
        weatherMode = WeatherMode.VANILLA;
        smoothTransitions = true;
        transitionSpeed = 0.5D;
        persist();
        if (biomeChanged) {
            refreshBiomeRender();
        }
    }

    private void persist() {
        config.biomeId = biomeId;
        config.timeOfDay = targetTime;
        config.weatherMode = weatherMode;
        config.smoothTransitions = smoothTransitions;
        config.transitionSpeed = transitionSpeed;
        config.save();
    }

    private float targetRain() {
        return switch (weatherMode) {
            case RAIN, THUNDER -> 1.0F;
            case CLEAR, VANILLA -> 0.0F;
        };
    }

    private float targetThunder() {
        return weatherMode == WeatherMode.THUNDER ? 1.0F : 0.0F;
    }

    private static double wrapLerp(double current, double target, double speed, double wrap) {
        double delta = ((target - current + wrap * 1.5D) % wrap) - wrap / 2.0D;
        return (current + delta * speed + wrap) % wrap;
    }

    private static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }
    private static void refreshBiomeRender() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client != null && client.worldRenderer != null) {
            client.worldRenderer.reload();
        }
    }   
}
