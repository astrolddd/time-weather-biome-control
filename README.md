# Time Weather Biome Control

A Minecraft Fabric `1.21.1` client-side mod that lets the player locally fake time, weather, and biome ambience.
the main motive of creating this mod is to change the sky appearance and weather. Custom sky texture packs have different skies for different biomes to alter between them easily without affecting the actual game can be done with this mod.
Default keybind: `0`

## Features

- Client-only keybind menu.
- Time slider from `0` to `24000`.
- Weather mode: vanilla, clear, rain, thunder.
- Fake biome selector.
- Presets saved in `config/time_weather_biome_control.json`.
- Smooth visual transitions for time and weather.
- Passive compatibility with Iris/shader packs and custom sky resource packs by reporting fake values through normal client world methods instead of drawing a replacement sky.

## Notes

The biome fake is intentionally visual/client-side. It does not change server logic, mob spawning, structures, or actual world data.

Shader packs and custom sky packs can still choose to ignore vanilla world time, weather, biome, or moon values. In that case the pack's own settings win.
