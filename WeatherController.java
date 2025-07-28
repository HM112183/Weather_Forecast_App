package com.weatherapp;

import com.google.gson.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.io.*;
import java.net.*;
import java.util.*;

public class WeatherController {

    @FXML private ComboBox<String> cityComboBox;
    @FXML private Label temperatureLabel, conditionLabel, humidityLabel;
    @FXML private ImageView weatherIcon;

    @FXML private TableView<ForecastData> forecastTable;
    @FXML private TableColumn<ForecastData, String> dayColumn;
    @FXML private TableColumn<ForecastData, Double> tempColumn;
    @FXML private TableColumn<ForecastData, String> conditionColumn;

    @FXML private LineChart<String, Number> tempChart;

    @FXML private ToggleButton themeToggle;

    private final String API_KEY = "Replace with your OpenWeatherMap API key";
    private final ObservableList<String> favoriteCities = FXCollections.observableArrayList("Mumbai", "Delhi", "Bangalore", "Ahmedabad");

    @FXML
    public void initialize() {
        // Setup Table Columns
        dayColumn.setCellValueFactory(new PropertyValueFactory<>("date"));
        tempColumn.setCellValueFactory(new PropertyValueFactory<>("avgTemp"));
        conditionColumn.setCellValueFactory(new PropertyValueFactory<>("condition"));

        // Setup ComboBox
        cityComboBox.setItems(favoriteCities);
        cityComboBox.setEditable(true);

        // Load Theme after Scene is ready
        themeToggle.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                newScene.getStylesheets().add(getClass().getResource("/light-theme.css").toExternalForm());
                themeToggle.setText("Dark Mode");
                themeToggle.setSelected(false);
            }
        });

        // Auto-detect city
        String autoCity = getCityFromIP();
        if (autoCity != null) {
            if (!favoriteCities.contains(autoCity)) favoriteCities.add(autoCity);
            cityComboBox.setValue(autoCity);
            getWeather();
        }
    }

    @FXML
    private void toggleTheme() {
        Scene scene = themeToggle.getScene();
        if (themeToggle.isSelected()) {
            scene.getStylesheets().clear();
            scene.getStylesheets().add(getClass().getResource("/dark-theme.css").toExternalForm());
            themeToggle.setText("Light Mode");
        } else {
            scene.getStylesheets().clear();
            scene.getStylesheets().add(getClass().getResource("/light-theme.css").toExternalForm());
            themeToggle.setText("Dark Mode");
        }
    }

    @FXML
    public void getWeather() {
        String city = cityComboBox.getEditor().getText().trim();
        if (city.isEmpty()) return;

        try {
            String urlStr = "https://api.openweathermap.org/data/2.5/forecast?q=" + URLEncoder.encode(city, "UTF-8") + "&appid=" + API_KEY + "&units=metric";
            URL url = new URL(urlStr);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder jsonBuilder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                jsonBuilder.append(line);
            }

            JsonObject json = JsonParser.parseString(jsonBuilder.toString()).getAsJsonObject();
            JsonArray list = json.getAsJsonArray("list");

            // Current weather from first entry
            JsonObject first = list.get(0).getAsJsonObject();
            double temp = first.getAsJsonObject("main").get("temp").getAsDouble();
            int humidity = first.getAsJsonObject("main").get("humidity").getAsInt();
            String condition = first.getAsJsonArray("weather").get(0).getAsJsonObject().get("main").getAsString();
            String iconCode = first.getAsJsonArray("weather").get(0).getAsJsonObject().get("icon").getAsString();

            temperatureLabel.setText(temp + "°C");
            humidityLabel.setText("Humidity: " + humidity + "%");
            conditionLabel.setText(condition);
            weatherIcon.setImage(new Image("https://openweathermap.org/img/wn/" + iconCode + "@2x.png"));

            // Forecast - one per day
            List<ForecastData> forecastList = new ArrayList<>();
            for (int i = 0; i < list.size(); i += 8) {
                JsonObject obj = list.get(i).getAsJsonObject();
                String date = obj.get("dt_txt").getAsString().split(" ")[0];
                double avgTemp = obj.getAsJsonObject("main").get("temp").getAsDouble();
                String desc = obj.getAsJsonArray("weather").get(0).getAsJsonObject().get("main").getAsString();

                forecastList.add(new ForecastData(date, avgTemp, desc));
            }

            forecastTable.getItems().setAll(forecastList);
            updateChart(forecastList);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void addCityToFavorites() {
        String city = cityComboBox.getEditor().getText().trim();
        if (!city.isEmpty() && !favoriteCities.contains(city)) {
            favoriteCities.add(city);
            cityComboBox.setValue(city);
        }
    }

    @FXML
    private void removeCityFromFavorites() {
        String city = cityComboBox.getEditor().getText().trim();
        if (!city.isEmpty() && favoriteCities.contains(city)) {
            favoriteCities.remove(city);
            cityComboBox.getEditor().clear();
        }
    }

    private void updateChart(List<ForecastData> forecastList) {
        tempChart.getData().clear();
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        for (ForecastData data : forecastList) {
            series.getData().add(new XYChart.Data<>(data.getDate(), data.getAvgTemp()));
        }
        tempChart.getData().add(series);
    }

    private String getCityFromIP() {
        try {
            URL url = new URL("http://ip-api.com/json/");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }

            JsonObject json = JsonParser.parseString(response.toString()).getAsJsonObject();
            return json.get("city").getAsString();

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
