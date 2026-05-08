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
    private static final int HEADER_HEIGHT = 46;
    private static final int FOOTER_HEIGHT = 46;
    private static final int CONTENT_PADDING_X = 18;
    private static final int CONTENT_PADDING_Y = 14;

    private static final int OVERLAY_TOP = 0x7A0B1420;
    private static final int OVERLAY_BOTTOM = 0xB805070A;
    private static final int PANEL_COLOR = 0xE0121620;
    private static final int PANEL_BORDER = 0xA03D4652;
    private static final int HEADER_COLOR = 0xF0161C28;
    private static final int CARD_COLOR = 0xCC0E131A;
    private static final int CARD_BORDER = 0x5A4C5663;
    private static final int DIVIDER = 0x334C5663;

    private static final int TEXT_MUTED = 0xFFB9C1CD;
    private static final int TEXT_FAINT = 0xFF8B93A0;
    private static final int GREEN = 0xFF52D568;
    private static final int GOLD = 0xFFFFD45A;
    private static final int BLUE = 0xFFBFD9FF;
    private static final int PURPLE = 0xFFC58BFF;

    private List<String> biomeIds = List.of("minecraft:plains");
    private String biomeSearch = "";
    private boolean biomeSearchFocused = false;
    private int scrollOffset = 0;

    public AtmosphereScreen() {
        super(Text.literal("Atmosphere"));
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
        int scrollY = scrollY(panelY);
        int scrollHeight = scrollHeight(viewportHeight);
        int contentX = panelX + CONTENT_PADDING_X;
        int contentWidth = panelWidth - CONTENT_PADDING_X * 2;
        int contentY = scrollY + CONTENT_PADDING_Y - scrollOffset;
        scrollOffset = clamp(scrollOffset, 0, maxScroll());

        addHeaderButtons(panelX, panelY, panelWidth);
        addFooterButtons(panelX, panelY, panelWidth);

        int y = contentY;
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

        y += 28;
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

        y += 82;
        if (visible(y, CONTROL_HEIGHT)) {
            addDrawableChild(new TimeSliderWidget(contentX, y, Math.max(100, contentWidth - 88), CONTROL_HEIGHT, TimeWeatherBiomeControlMod.STATE.targetTime()));
        }

        y += 92;
        if (visible(y, CONTROL_HEIGHT)) {
            int buttonWidth = Math.max(58, (contentWidth - 12) / 4);
            addWeatherButton(contentX, y, buttonWidth, WeatherMode.VANILLA);
            addWeatherButton(contentX + buttonWidth + 4, y, buttonWidth, WeatherMode.CLEAR);
            addWeatherButton(contentX + (buttonWidth + 4) * 2, y, buttonWidth, WeatherMode.RAIN);
            addWeatherButton(contentX + (buttonWidth + 4) * 3, y, contentWidth - (buttonWidth + 4) * 3, WeatherMode.THUNDER);
        }

        y += 74;
        if (visible(y, CONTROL_HEIGHT)) {
            addDrawableChild(ButtonWidget.builder(Text.literal(TimeWeatherBiomeControlMod.STATE.smoothTransitions() ? "On" : "Off"), button -> {
                TimeWeatherBiomeControlMod.STATE.setSmoothTransitions(!TimeWeatherBiomeControlMod.STATE.smoothTransitions());
                clearAndInit();
            }).dimensions(contentX + contentWidth - 64, y, 64, CONTROL_HEIGHT).build());
        }

        y += 74;
        if (visible(y, CONTROL_HEIGHT)) {
            addDrawableChild(new TransitionSpeedSliderWidget(contentX, y, Math.max(100, contentWidth - 88), CONTROL_HEIGHT, TimeWeatherBiomeControlMod.STATE.transitionSpeed()));
        }

        y += 76;
        /*if (visible(y, CONTROL_HEIGHT)) {
            addDrawableChild(ButtonWidget.builder(Text.literal("Save Preset"), button -> {
                TimeWeatherBiomeControlMod.STATE.saveCurrentPreset("Preset " + (TimeWeatherBiomeControlMod.STATE.presets().size() + 1));
                clearAndInit();
            }).dimensions(contentX, y, contentWidth, CONTROL_HEIGHT).build());
        }*/

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
        if (isInScrollArea(mouseX, mouseY)) {
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
        int scrollY = scrollY(panelY);
        int scrollHeight = scrollHeight(viewportHeight);
        int contentX = panelX + CONTENT_PADDING_X;
        int contentWidth = panelWidth - CONTENT_PADDING_X * 2;
        int contentY = scrollY + CONTENT_PADDING_Y - scrollOffset;

        drawPanel(context, panelX, panelY, panelWidth, viewportHeight);

        context.enableScissor(panelX + 1, scrollY + 1, panelX + panelWidth - 1, scrollY + scrollHeight - 1);
        drawCards(context, contentX, contentY, contentWidth);
        context.disableScissor();

        super.render(context, mouseX, mouseY, delta);

        drawHeader(context, panelX, panelY, panelWidth);
        drawFooterNote(context);

        context.enableScissor(panelX + 1, scrollY + 1, panelX + panelWidth - 1, scrollY + scrollHeight - 1);
        drawContentText(context, contentX, contentY, contentWidth);
        context.disableScissor();

        drawScrollBar(context, panelX + panelWidth - 7, scrollY, scrollHeight);
    }

    private void addHeaderButtons(int panelX, int panelY, int panelWidth) {
        addDrawableChild(ButtonWidget.builder(Text.literal("X"), button -> close())
                .dimensions(panelX + panelWidth - 26, panelY + 12, 14, 14).build());
    }

    private void addFooterButtons(int panelX, int panelY, int panelWidth) {
        int footerY = panelY + viewportHeight() - FOOTER_HEIGHT + 12;
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
        context.fillGradient(0, 0, width, height, OVERLAY_TOP, OVERLAY_BOTTOM);
    }

    private void drawPanel(DrawContext context, int x, int y, int panelWidth, int panelHeight) {
        context.fill(x, y, x + panelWidth, y + panelHeight, PANEL_COLOR);
        context.drawBorder(x, y, panelWidth, panelHeight, PANEL_BORDER);
        context.fill(x + 1, y + 1, x + panelWidth - 1, y + HEADER_HEIGHT, HEADER_COLOR);
        context.fill(x + 1, y + panelHeight - FOOTER_HEIGHT, x + panelWidth - 1, y + panelHeight - 1, HEADER_COLOR);
        context.fill(x + 1, y + HEADER_HEIGHT, x + panelWidth - 1, y + HEADER_HEIGHT + 1, DIVIDER);
        context.fill(x + 1, y + panelHeight - FOOTER_HEIGHT - 1, x + panelWidth - 1, y + panelHeight - FOOTER_HEIGHT, DIVIDER);
    }

    private void drawCards(DrawContext context, int x, int y, int contentWidth) {
        drawCard(context, x, y - 8, contentWidth, 78);
        drawCard(context, x, y + 84, contentWidth, 96);
        drawCard(context, x, y + 196, contentWidth, 66);
        drawCard(context, x, y + 276, contentWidth, 72);
        drawCard(context, x, y + 362, contentWidth, 70);
        drawCard(context, x, y + 446, contentWidth, presetContentHeight());

        int presetY = y + presetStartOffset();
        for (AtmospherePreset preset : TimeWeatherBiomeControlMod.STATE.presets()) {
            drawCard(context, x + 10, presetY, contentWidth - 20, 50);
            presetY += 58;
        }
    }

    private void drawCard(DrawContext context, int x, int y, int cardWidth, int cardHeight) {
        context.fill(x, y, x + cardWidth, y + cardHeight, CARD_COLOR);
        context.drawBorder(x, y, cardWidth, cardHeight, CARD_BORDER);
    }

    private void drawContentText(DrawContext context, int x, int y, int contentWidth) {
        context.drawTextWithShadow(textRenderer, Text.literal("Biome"), x + 12, y + 4, GREEN);
        context.drawTextWithShadow(textRenderer, Text.literal("Filter then choose a client-only biome."), x + 12, y + 54, TEXT_MUTED);

        context.drawTextWithShadow(textRenderer, Text.literal("Time"), x + 12, y + 96, GOLD);
        drawValueBox(context, x + contentWidth - 82, y + 112, 70, CONTROL_HEIGHT, String.valueOf(TimeWeatherBiomeControlMod.STATE.targetTime()));
        context.drawTextWithShadow(textRenderer, Text.literal("0"), x + 12, y + 138, TEXT_FAINT);
        context.drawCenteredTextWithShadow(textRenderer, Text.literal(timePhaseLabel(TimeWeatherBiomeControlMod.STATE.targetTime())), x + contentWidth / 2, y + 138, TEXT_MUTED);
        context.drawTextWithShadow(textRenderer, Text.literal("24000"), x + contentWidth - 54, y + 138, TEXT_FAINT);
        context.drawTextWithShadow(textRenderer, Text.literal("Lighting and sky angle."), x + 12, y + 160, TEXT_MUTED);

        context.drawTextWithShadow(textRenderer, Text.literal("Weather"), x + 12, y + 208, BLUE);
        context.drawTextWithShadow(textRenderer, Text.literal("Visual rain/thunder (client-side)."), x + 12, y + 248, TEXT_MUTED);

        context.drawTextWithShadow(textRenderer, Text.literal("Transitions"), x + 12, y + 288, PURPLE);
        context.fill(x + contentWidth - 64, y + 286, x + contentWidth, y + 306, TimeWeatherBiomeControlMod.STATE.smoothTransitions() ? GREEN : 0xFF5D636B);
        context.drawTextWithShadow(textRenderer, Text.literal("Smooth blending instead of snapping."), x + 12, y + 312, TEXT_MUTED);

        context.drawTextWithShadow(textRenderer, Text.literal("Speed"), x + 12, y + 372, 0xFFE6E6E6);
        drawValueBox(context, x + contentWidth - 82, y + 378, 70, CONTROL_HEIGHT, String.format("%.2fx", TimeWeatherBiomeControlMod.STATE.transitionSpeed()));
        context.drawTextWithShadow(textRenderer, Text.literal("Lower values change more slowly."), x + 12, y + 404, TEXT_MUTED);

        context.drawTextWithShadow(textRenderer, Text.literal("Presets"), x + 12, y + 456, GOLD);
        context.drawTextWithShadow(textRenderer, Text.literal("Save the current look, then reuse it anytime."), x + 12, y + 480, TEXT_MUTED);
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
        int py = panelY();
        int scrollTop = scrollY(py);
        int scrollBottom = scrollTop + scrollHeight(viewportHeight());
        return y + widgetHeight >= scrollTop + 2 && y <= scrollBottom - 2;
    }

    private int panelY() {
        return clamp((height - 310) / 2, 26, 74);
    }

    private int panelWidth() {
        return clamp(width - 32, 270, 560);
    }

    private int panelX() {
        return (width - panelWidth()) / 2;
    }

    private int viewportHeight() {
        return clamp(height - panelY() - 24, 190, height - 24);
    }

    private int presetStartOffset() {
        return 512;
    }

    private int presetContentHeight() {
        return 64 + TimeWeatherBiomeControlMod.STATE.presets().size() * 58 + 44;
    }

    private int contentHeight() {
        return presetStartOffset() + TimeWeatherBiomeControlMod.STATE.presets().size() * 58 + 56;
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
        return mode.label();
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

    private int scrollY(int panelY) {
        return panelY + HEADER_HEIGHT;
    }

    private int scrollHeight(int viewportHeight) {
        return Math.max(60, viewportHeight - HEADER_HEIGHT - FOOTER_HEIGHT);
    }

    private boolean isInScrollArea(double mouseX, double mouseY) {
        int px = panelX();
        int py = panelY();
        int vw = panelWidth();
        int vh = viewportHeight();
        int sy = scrollY(py);
        int sh = scrollHeight(vh);
        return mouseX >= px && mouseX <= px + vw && mouseY >= sy && mouseY <= sy + sh;
    }

    private void drawHeader(DrawContext context, int panelX, int panelY, int panelWidth) {
        int titleY = panelY + 14;
        context.drawTextWithShadow(textRenderer, title, panelX + 14, titleY, 0xFFFFFFFF);
        context.drawTextWithShadow(textRenderer, Text.literal("Client-side time \u2022 weather \u2022 biome"), panelX + 14, titleY + 14, TEXT_MUTED);
    }

    private void drawFooterNote(DrawContext context) {
        context.drawCenteredTextWithShadow(textRenderer, Text.literal("Client-side only. Changes do not affect the server."), width / 2, height - 14, TEXT_FAINT);
        context.drawTextWithShadow(textRenderer, Text.literal("v1.0.0"), width - 44, height - 14, TEXT_FAINT);
    }
}

