package com.weatherapp;

public class ForecastData {
    private String date;
    private double avgTemp;
    private String condition;

    public ForecastData(String date, double avgTemp, String condition) {
        this.date = date;
        this.avgTemp = avgTemp;
        this.condition = condition;
    }

    public String getDate() {
        return date;
    }

    public double getAvgTemp() {
        return avgTemp;
    }

    public String getCondition() {
        return condition;
    }
}
