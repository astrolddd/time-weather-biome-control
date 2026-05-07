package net.astrolddd.twbc;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.CyclingButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.world.biome.Biome;

import java.util.ArrayList;
import java.util.List;

public final class AtmosphereScreen extends Screen {
    private static final int CONTROL_HEIGHT = 20;
    private static final int PANEL_COLOR = 0xD8181D24;
    private static final int PANEL_BORDER = 0x90707A86;
    private static final int CARD_COLOR = 0xB80E131A;
    private static final int TEXT_MUTED = 0xFFC4CAD3;
    private static final int GREEN = 0xFF52D568;
    private static final int GOLD = 0xFFFFD45A;
    private static final int BLUE = 0xFFBFD9FF;
    private static final int PURPLE = 0xFFC58BFF;

    private List<String> biomeIds = List.of("minecraft:plains");
    private String biomeSearch = "";
    private boolean biomeSearchFocused = false;
    private int scrollOffset = 0;

    public AtmosphereScreen() {
        super(Text.literal("Control Panel   "));
    }

    @Override
    public void blur() {
    }

    @Override
    protected void applyBlur(float delta) {
    }

    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
    }

    @Override
    public void renderInGameBackground(DrawContext context) {
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    @Override
    protected void init() {
        biomeIds = filterBiomeIds(collectBiomeIds());

        int panelX = panelX();
        int panelY = panelY();
        int panelWidth = panelWidth();
        int viewportHeight = viewportHeight();
        int contentX = panelX + 18;
        int contentWidth = panelWidth - 36;
        int contentY = panelY - scrollOffset;
        scrollOffset = clamp(scrollOffset, 0, maxScroll());

        addCloseButton();
        addFooterButtons(panelX, panelWidth);

        int y = contentY + 48;
        if (visible(y, CONTROL_HEIGHT)) {
            TextFieldWidget searchField = new TextFieldWidget(textRenderer, contentX, y, contentWidth, CONTROL_HEIGHT, Text.literal("Search biomes"));
            searchField.setMaxLength(64);
            searchField.setText(biomeSearch);
            searchField.setPlaceholder(Text.literal("Search biomes..."));
            searchField.setFocused(biomeSearchFocused);
            if (biomeSearchFocused) {
                setFocused(searchField);
            }
            searchField.setChangedListener(value -> {
                biomeSearch = value;
                biomeSearchFocused = true;
                clearAndInit();
            });
            addDrawableChild(searchField);
        }

        y = contentY + 76;
        if (visible(y, CONTROL_HEIGHT)) {
            String selectedBiome = biomeIds.contains(TimeWeatherBiomeControlMod.STATE.biomeId())
                    ? TimeWeatherBiomeControlMod.STATE.biomeId()
                    : biomeIds.get(0);
            addDrawableChild(CyclingButtonWidget.builder(AtmosphereScreen::biomeLabel)
                    .values(biomeIds)
                    .initially(selectedBiome)
                    .build(contentX, y, contentWidth, CONTROL_HEIGHT, Text.literal("Biome"), (button, value) -> {
                        TimeWeatherBiomeControlMod.STATE.setBiomeId(value);
                        clearAndInit();
                    }));
        }

        y = contentY + 164;
        if (visible(y, CONTROL_HEIGHT)) {
            addDrawableChild(new TimeSliderWidget(contentX, y, Math.max(100, contentWidth - 88), CONTROL_HEIGHT, TimeWeatherBiomeControlMod.STATE.targetTime()));
        }

        y = contentY + 266;
        if (visible(y, CONTROL_HEIGHT)) {
            int buttonWidth = Math.max(58, (contentWidth - 8) / 3);
            addWeatherButton(contentX, y, buttonWidth, WeatherMode.CLEAR);
            addWeatherButton(contentX + buttonWidth + 4, y, buttonWidth, WeatherMode.RAIN);
            addWeatherButton(contentX + (buttonWidth + 4) * 2, y, contentWidth - (buttonWidth + 4) * 2, WeatherMode.THUNDER);
        }

        y = contentY + 354;
        if (visible(y, CONTROL_HEIGHT)) {
            addDrawableChild(ButtonWidget.builder(Text.literal(TimeWeatherBiomeControlMod.STATE.smoothTransitions() ? "On" : "Off"), button -> {
                TimeWeatherBiomeControlMod.STATE.setSmoothTransitions(!TimeWeatherBiomeControlMod.STATE.smoothTransitions());
                clearAndInit();
            }).dimensions(contentX + contentWidth - 64, y, 64, CONTROL_HEIGHT).build());
        }

        y = contentY + 428;
        if (visible(y, CONTROL_HEIGHT)) {
            addDrawableChild(new TransitionSpeedSliderWidget(contentX, y, Math.max(100, contentWidth - 88), CONTROL_HEIGHT, TimeWeatherBiomeControlMod.STATE.transitionSpeed()));
        }

        y = contentY + 504;
        if (visible(y, CONTROL_HEIGHT)) {
            addDrawableChild(ButtonWidget.builder(Text.literal("Save Preset"), button -> {
                TimeWeatherBiomeControlMod.STATE.saveCurrentPreset("Preset " + (TimeWeatherBiomeControlMod.STATE.presets().size() + 1));
                clearAndInit();
            }).dimensions(contentX, y, contentWidth, CONTROL_HEIGHT).build());
        }

        int presetY = contentY + presetStartOffset();
        for (AtmospherePreset preset : TimeWeatherBiomeControlMod.STATE.presets()) {
            if (visible(presetY, 52)) {
                addDrawableChild(ButtonWidget.builder(Text.literal("Use"), button -> {
                    TimeWeatherBiomeControlMod.STATE.applyPreset(preset);
                    clearAndInit();
                }).dimensions(contentX + contentWidth - 58, presetY + 16, 50, CONTROL_HEIGHT).build());
            }
            presetY += 58;
        }

        y = contentY + presetStartOffset() + TimeWeatherBiomeControlMod.STATE.presets().size() * 58 + 10;
        if (visible(y, 24)) {
            addDrawableChild(ButtonWidget.builder(Text.literal("+ New Preset"), button -> {
                TimeWeatherBiomeControlMod.STATE.saveCurrentPreset("Preset " + (TimeWeatherBiomeControlMod.STATE.presets().size() + 1));
                clearAndInit();
            }).dimensions(contentX, y, contentWidth, 24).build());
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (mouseX >= panelX() && mouseX <= panelX() + panelWidth() && mouseY >= panelY() && mouseY <= panelY() + viewportHeight()) {
            int nextOffset = clamp(scrollOffset - (int) Math.round(verticalAmount * 34.0D), 0, maxScroll());
            if (nextOffset != scrollOffset) {
                scrollOffset = nextOffset;
                clearAndInit();
            }
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        drawBackdrop(context);

        int panelX = panelX();
        int panelY = panelY();
        int panelWidth = panelWidth();
        int viewportHeight = viewportHeight();
        int contentX = panelX + 18;
        int contentWidth = panelWidth - 36;
        int contentY = panelY - scrollOffset;

        drawPanel(context, panelX, panelY, panelWidth, viewportHeight);

        context.enableScissor(panelX + 1, panelY + 1, panelX + panelWidth - 1, panelY + viewportHeight - 1);
        drawCards(context, contentX, contentY, contentWidth);
        context.disableScissor();

        super.render(context, mouseX, mouseY, delta);

        context.drawCenteredTextWithShadow(textRenderer, title, width / 2, titleY(), 0xFFFFFFFF);
        context.drawCenteredTextWithShadow(textRenderer, Text.literal("Customize your sky, time, weather and biome."), width / 2, titleY() + 22, 0xFFE4E8EF);

        context.enableScissor(panelX + 1, panelY + 1, panelX + panelWidth - 1, panelY + viewportHeight - 1);
        drawContentText(context, contentX, contentY, contentWidth);
        context.disableScissor();

        drawScrollBar(context, panelX + panelWidth - 7, panelY, viewportHeight);
        context.drawCenteredTextWithShadow(textRenderer, Text.literal("Client-side only. Changes do not affect the server."), width / 2, height - 14, 0xFF9AA1AA);
        context.drawTextWithShadow(textRenderer, Text.literal("v1.0.0"), width - 44, height - 14, 0xFF9AA1AA);
    }

    private void addCloseButton() {
        addDrawableChild(ButtonWidget.builder(Text.literal("X"), button -> close())
                .dimensions(width - 36, 18, 22, 22).build());
    }

    private void addFooterButtons(int panelX, int panelWidth) {
        int footerY = height - 38;
        int buttonWidth = Math.min(116, (panelWidth - 10) / 2);
        int applyX = panelX + panelWidth - buttonWidth;
        int cancelX = applyX - buttonWidth - 8;

        addDrawableChild(ButtonWidget.builder(Text.literal("Reset"), button -> {
            TimeWeatherBiomeControlMod.STATE.resetToDefault();
            clearAndInit();
        }).dimensions(panelX, footerY, buttonWidth, CONTROL_HEIGHT).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Cancel"), button -> close())
                .dimensions(cancelX, footerY, buttonWidth, CONTROL_HEIGHT).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Apply"), button -> close())
                .dimensions(applyX, footerY, buttonWidth, CONTROL_HEIGHT).build());
    }

    private void addWeatherButton(int x, int y, int buttonWidth, WeatherMode mode) {
        boolean selected = TimeWeatherBiomeControlMod.STATE.weatherMode() == mode;
        ButtonWidget button = ButtonWidget.builder(Text.literal(weatherLabel(mode)), clicked -> {
            TimeWeatherBiomeControlMod.STATE.setWeatherMode(mode);
            clearAndInit();
        }).dimensions(x, y, buttonWidth, CONTROL_HEIGHT).build();
        button.active = !selected;
        addDrawableChild(button);
    }

    private void drawBackdrop(DrawContext context) {
        context.fillGradient(0, 0, width, height, 0x99141F30, 0xCC05070A);
        context.fill(0, 0, width, Math.max(58, panelY() - 10), 0x661C3858);
        context.fill(0, height - 46, width, height, 0xB0000000);
    }

    private void drawPanel(DrawContext context, int x, int y, int panelWidth, int panelHeight) {
        context.fill(x, y, x + panelWidth, y + panelHeight, PANEL_COLOR);
        context.drawBorder(x, y, panelWidth, panelHeight, PANEL_BORDER);
    }

    private void drawCards(DrawContext context, int x, int y, int contentWidth) {
        drawCard(context, x, y + 16, contentWidth, 112);
        drawCard(context, x, y + 142, contentWidth, 100);
        drawCard(context, x, y + 254, contentWidth, 70);
        drawCard(context, x, y + 340, contentWidth, 58);
        drawCard(context, x, y + 414, contentWidth, 76);
        drawCard(context, x, y + 528, contentWidth, presetContentHeight());

        int presetY = y + presetStartOffset();
        for (AtmospherePreset preset : TimeWeatherBiomeControlMod.STATE.presets()) {
            drawCard(context, x + 10, presetY, contentWidth - 20, 50);
            presetY += 58;
        }
    }

    private void drawCard(DrawContext context, int x, int y, int cardWidth, int cardHeight) {
        context.fill(x, y, x + cardWidth, y + cardHeight, CARD_COLOR);
        context.drawBorder(x, y, cardWidth, cardHeight, 0x554C5663);
    }

    private void drawContentText(DrawContext context, int x, int y, int contentWidth) {
        context.drawTextWithShadow(textRenderer, Text.literal("Biome"), x + 12, y + 26, GREEN);
        context.drawTextWithShadow(textRenderer, Text.literal("Filter the list, then choose the fake biome."), x + 12, y + 106, TEXT_MUTED);

        context.drawTextWithShadow(textRenderer, Text.literal("Time of Day"), x + 12, y + 148, GOLD);
        drawValueBox(context, x + contentWidth - 82, y + 164, 70, CONTROL_HEIGHT, String.valueOf(TimeWeatherBiomeControlMod.STATE.targetTime()));
        context.drawTextWithShadow(textRenderer, Text.literal("0"), x + 12, y + 190, TEXT_MUTED);
        context.drawCenteredTextWithShadow(textRenderer, Text.literal(timePhaseLabel(TimeWeatherBiomeControlMod.STATE.targetTime())), x + contentWidth / 2, y + 190, TEXT_MUTED);
        context.drawTextWithShadow(textRenderer, Text.literal("24000"), x + contentWidth - 54, y + 190, TEXT_MUTED);
        context.drawTextWithShadow(textRenderer, Text.literal("Controls lighting and sky angle."), x + 12, y + 216, TEXT_MUTED);

        context.drawTextWithShadow(textRenderer, Text.literal("Weather"), x + 12, y + 256, BLUE);
        context.drawTextWithShadow(textRenderer, Text.literal("Rain, clouds, thunder, and shader weather values."), x + 12, y + 298, TEXT_MUTED);

        context.drawTextWithShadow(textRenderer, Text.literal("Smooth Transitions"), x + 12, y + 356, PURPLE);
        context.fill(x + contentWidth - 64, y + 354, x + contentWidth, y + 374, TimeWeatherBiomeControlMod.STATE.smoothTransitions() ? GREEN : 0xFF5D636B);
        context.drawTextWithShadow(textRenderer, Text.literal("Blend between settings instead of snapping."), x + 12, y + 380, TEXT_MUTED);

        context.drawTextWithShadow(textRenderer, Text.literal("Transition Speed"), x + 12, y + 420, 0xFFE6E6E6);
        drawValueBox(context, x + contentWidth - 82, y + 428, 70, CONTROL_HEIGHT, String.format("%.2fx", TimeWeatherBiomeControlMod.STATE.transitionSpeed()));
        context.drawTextWithShadow(textRenderer, Text.literal("Lower values transition more slowly."), x + 12, y + 458, TEXT_MUTED);

        context.drawTextWithShadow(textRenderer, Text.literal("Actions"), x + 12, y + 500, 0xFFE6E6E6);
        context.drawTextWithShadow(textRenderer, Text.literal("Save the current look as a preset."), x + 12, y + 528, TEXT_MUTED);

        context.drawTextWithShadow(textRenderer, Text.literal("Presets"), x + 12, y + 542, GOLD);
        int presetY = y + presetStartOffset();
        for (AtmospherePreset preset : TimeWeatherBiomeControlMod.STATE.presets()) {
            drawPresetText(context, x + 22, presetY, contentWidth - 44, preset);
            presetY += 58;
        }
    }

    private void drawPresetText(DrawContext context, int x, int y, int contentWidth, AtmospherePreset preset) {
        context.drawTextWithShadow(textRenderer, Text.literal(preset.name()), x, y + 8, 0xFFFFFFFF);
        context.drawTextWithShadow(textRenderer, Text.literal(preset.timeOfDay() + "  " + weatherLabel(preset.weatherMode())), x, y + 28, TEXT_MUTED);
        if (contentWidth > 260) {
            context.drawTextWithShadow(textRenderer, Text.literal(shortBiome(preset.biomeId())), x + 150, y + 28, TEXT_MUTED);
        }
    }

    private void drawValueBox(DrawContext context, int x, int y, int boxWidth, int boxHeight, String value) {
        context.fill(x, y, x + boxWidth, y + boxHeight, 0xCC10151B);
        context.drawBorder(x, y, boxWidth, boxHeight, 0x805B6570);
        context.drawCenteredTextWithShadow(textRenderer, Text.literal(value), x + boxWidth / 2, y + 6, 0xFFFFFFFF);
    }

    private void drawScrollBar(DrawContext context, int x, int y, int viewportHeight) {
        int maxScroll = maxScroll();
        if (maxScroll <= 0) {
            return;
        }

        int trackTop = y + 8;
        int trackHeight = viewportHeight - 16;
        int thumbHeight = Math.max(22, trackHeight * viewportHeight / contentHeight());
        int thumbY = trackTop + (trackHeight - thumbHeight) * scrollOffset / maxScroll;
        context.fill(x, trackTop, x + 3, trackTop + trackHeight, 0x5539424D);
        context.fill(x, thumbY, x + 3, thumbY + thumbHeight, 0xCCB4BDC8);
    }

    private List<String> collectBiomeIds() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.world == null) {
            return List.of(TimeWeatherBiomeControlMod.STATE.biomeId());
        }

        List<String> ids = new ArrayList<>();
        for (Biome biome : client.world.getRegistryManager().get(RegistryKeys.BIOME)) {
            Identifier id = client.world.getRegistryManager().get(RegistryKeys.BIOME).getId(biome);
            if (id != null) {
                ids.add(id.toString());
            }
        }
        ids.sort(String::compareTo);
        return ids.isEmpty() ? List.of(TimeWeatherBiomeControlMod.STATE.biomeId()) : ids;
    }

    private List<String> filterBiomeIds(List<String> ids) {
        String query = biomeSearch.trim().toLowerCase();
        if (query.isEmpty()) {
            return ids;
        }

        List<String> filtered = new ArrayList<>();
        for (String id : ids) {
            if (id.toLowerCase().contains(query) || shortBiome(id).toLowerCase().contains(query)) {
                filtered.add(id);
            }
        }
        return filtered.isEmpty() ? List.of(TimeWeatherBiomeControlMod.STATE.biomeId()) : filtered;
    }

    private boolean visible(int y, int widgetHeight) {
        return y + widgetHeight >= panelY() + 2 && y <= panelY() + viewportHeight() - 2;
    }

    private int titleY() {
        return height < 300 ? 14 : 24;
    }

    private int panelY() {
        return height < 300 ? 58 : 74;
    }

    private int panelWidth() {
        return clamp(width - 32, 270, 560);
    }

    private int panelX() {
        return (width - panelWidth()) / 2;
    }

    private int viewportHeight() {
        return Math.max(130, height - panelY() - 58);
    }

    private int presetStartOffset() {
        return 570;
    }

    private int presetContentHeight() {
        return 54 + TimeWeatherBiomeControlMod.STATE.presets().size() * 58 + 48;
    }

    private int contentHeight() {
        return presetStartOffset() + TimeWeatherBiomeControlMod.STATE.presets().size() * 58 + 48;
    }

    private int maxScroll() {
        return Math.max(0, contentHeight() - viewportHeight() + 18);
    }

    private static Text biomeLabel(String biomeId) {
        return Text.literal(shortBiome(biomeId));
    }

    private static String shortBiome(String biomeId) {
        int separator = biomeId.indexOf(':');
        String value = separator >= 0 ? biomeId.substring(separator + 1) : biomeId;
        String[] words = value.split("_");
        StringBuilder label = new StringBuilder();
        for (String word : words) {
            if (label.length() > 0) {
                label.append(' ');
            }
            label.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1));
        }
        return label.toString();
    }

    private static String weatherLabel(WeatherMode mode) {
        return switch (mode) {
            case VANILLA -> "Vanilla";
            case CLEAR -> "Clear";
            case RAIN -> "Rain";
            case THUNDER -> "Thunder";
        };
    }

    private static String timePhaseLabel(long time) {
        if (time >= 11000L && time <= 13000L) {
            return "Sunset";
        }
        if (time >= 17500L && time <= 19000L) {
            return "Night";
        }
        if (time >= 22500L || time <= 1000L) {
            return "Sunrise";
        }
        return "Day";
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }
}

