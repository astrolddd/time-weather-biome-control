package net.astrolddd.twbc;

public enum WeatherMode {
    VANILLA("Vanilla"),
    CLEAR("Clear"),
    RAIN("Rain"),
    THUNDER("Thunder");

    private final String label;

    WeatherMode(String label) {
        this.label = label;
    }

    public String label() {
        return label;
    }
}
