package net.astrolddd.twbc;

import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.text.Text;

public final class TimeSliderWidget extends SliderWidget {
    public TimeSliderWidget(int x, int y, int width, int height, long time) {
        super(x, y, width, height, Text.empty(), time / 24000.0D);
        updateMessage();
    }

    @Override
    protected void updateMessage() {
        setMessage(Text.literal(""));
    }

    @Override
    protected void applyValue() {
        TimeWeatherBiomeControlMod.STATE.setTargetTime(time());
    }

    private long time() {
        return Math.min(23999L, Math.round(value * 24000.0D));
    }
}
