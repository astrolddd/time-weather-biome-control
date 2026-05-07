package net.astrolddd.twbc;

import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.text.Text;

public final class TransitionSpeedSliderWidget extends SliderWidget {
    public TransitionSpeedSliderWidget(int x, int y, int width, int height, double speed) {
        super(x, y, width, height, Text.empty(), (speed - 0.1D) / 0.9D);
        updateMessage();
    }

    @Override
    protected void updateMessage() {
        setMessage(Text.literal(""));
    }

    @Override
    protected void applyValue() {
        TimeWeatherBiomeControlMod.STATE.setTransitionSpeed(speed());
    }

    private double speed() {
        return 0.1D + value * 0.9D;
    }
}