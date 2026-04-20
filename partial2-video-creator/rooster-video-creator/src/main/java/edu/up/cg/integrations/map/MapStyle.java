package edu.up.cg.integrations.map;

public enum MapStyle {
    STREETS("mapbox/streets-v12");

    private final String value;

    MapStyle(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }
}
