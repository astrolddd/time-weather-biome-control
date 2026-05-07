package net.astrolddd.twbc;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public final class AtmosphereConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path PATH = FabricLoader.getInstance().getConfigDir().resolve("time_weather_biome_control.json");

    public String biomeId = "minecraft:plains";
    public long timeOfDay = 6000L;
    public WeatherMode weatherMode = WeatherMode.VANILLA;
    public boolean smoothTransitions = true;
    public double transitionSpeed = 0.5D;
    public List<AtmospherePreset> presets = new ArrayList<>();

    public static AtmosphereConfig load() {
        if (!Files.exists(PATH)) {
            AtmosphereConfig config = new AtmosphereConfig();
            config.presets.add(new AtmospherePreset("Sunset Desert", "minecraft:desert", 12000L, WeatherMode.CLEAR));
            config.presets.add(new AtmospherePreset("Snowy Night", "minecraft:snowy_plains", 18000L, WeatherMode.RAIN));
            config.presets.add(new AtmospherePreset("End Sky in Overworld", "minecraft:the_end", 13000L, WeatherMode.CLEAR));
            config.save();
            return config;
        }

        try (Reader reader = Files.newBufferedReader(PATH)) {
            AtmosphereConfig config = GSON.fromJson(reader, AtmosphereConfig.class);
            return config == null ? new AtmosphereConfig() : config;
        } catch (IOException exception) {
            TimeWeatherBiomeControlMod.LOGGER.warn("Could not load Time Weather Biome Control config", exception);
            return new AtmosphereConfig();
        }
    }

    public void save() {
        try {
            Files.createDirectories(PATH.getParent());
            try (Writer writer = Files.newBufferedWriter(PATH)) {
                GSON.toJson(this, writer);
            }
        } catch (IOException exception) {
            TimeWeatherBiomeControlMod.LOGGER.warn("Could not save Time Weather Biome Control config", exception);
        }
    }
}
