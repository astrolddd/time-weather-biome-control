package net.astrolddd.twbc;

public record AtmospherePreset(
        String name,
        String biomeId,
        long timeOfDay,
        WeatherMode weatherMode
) {
}
