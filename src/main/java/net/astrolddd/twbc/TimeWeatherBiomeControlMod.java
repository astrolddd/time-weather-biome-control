package net.astrolddd.twbc;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class TimeWeatherBiomeControlMod implements ClientModInitializer {
    public static final String MOD_ID = "time-weather-biome-control";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static final TimeWeatherBiomeControlState STATE = new TimeWeatherBiomeControlState();

    private static KeyBinding openMenuKey;

    @Override
    public void onInitializeClient() {
        AtmosphereConfig config = AtmosphereConfig.load();
        STATE.load(config);

        openMenuKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.twbc.open_menu",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_0,
                "category.twbc"
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            STATE.tick();
            while (openMenuKey.wasPressed()) {
                client.setScreen(new AtmosphereScreen());
            }
        });
    }

    public static Identifier id(String path) {
        return Identifier.of(MOD_ID, path);
    }

    public static boolean hasWorld() {
        MinecraftClient client = MinecraftClient.getInstance();
        return client != null && client.world != null;
    }
}
