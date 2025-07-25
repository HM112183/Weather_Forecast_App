package com.weatherapp;

public class WeatherData {
    private double temperature;
    private String description;
    private int humidity;
    private String iconCode; // 🔽 Added for icon

    public WeatherData(double temperature, String description, int humidity, String iconCode) {
        this.temperature = temperature;
        this.description = description;
        this.humidity = humidity;
        this.iconCode = iconCode;
    }

    public double getTemperature() {
        return temperature;
    }

    public String getDescription() {
        return description;
    }

    public int getHumidity() {
        return humidity;
    }

    public String getIconCode() {
        return iconCode;
    }
}
