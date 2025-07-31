package com.weatherapp;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;
import java.text.SimpleDateFormat;

import com.google.gson.JsonObject;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;


public class ApiService {
    private static final String API_KEY = "Replace with your key";

    public static WeatherData getWeather(String city) {
        try {
            String apiUrl = "https://api.openweathermap.org/data/2.5/weather?q=" +
                    city + "&appid=" + API_KEY + "&units=metric";

            HttpURLConnection conn = (HttpURLConnection) new URL(apiUrl).openConnection();
            conn.setRequestMethod("GET");

            InputStreamReader reader = new InputStreamReader(conn.getInputStream());
            JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();

            double temp = json.getAsJsonObject("main").get("temp").getAsDouble();
            int humidity = json.getAsJsonObject("main").get("humidity").getAsInt();
            String desc = json.getAsJsonArray("weather")
                              .get(0).getAsJsonObject().get("description").getAsString();
            String iconCode = json.getAsJsonArray("weather")
                                  .get(0).getAsJsonObject().get("icon").getAsString();

            return new WeatherData(temp, desc, humidity, iconCode);

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

        public static List<ForecastData> getForecast(String city) {
            List<ForecastData> forecastList = new ArrayList<>();

            try {
                String apiUrl = "https://api.openweathermap.org/data/2.5/forecast?q=" +
                        city + "&appid=" + API_KEY + "&units=metric";

                HttpURLConnection conn = (HttpURLConnection) new URL(apiUrl).openConnection();
                conn.setRequestMethod("GET");

                InputStreamReader reader = new InputStreamReader(conn.getInputStream());
                JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();
                JsonArray list = json.getAsJsonArray("list");

                Map<String, List<Double>> tempMap = new LinkedHashMap<>();
                Map<String, String> conditionMap = new LinkedHashMap<>();

                SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                SimpleDateFormat outputFormat = new SimpleDateFormat("EEE");

                for (JsonElement element : list) {
                    JsonObject item = element.getAsJsonObject();
                    String dateText = item.get("dt_txt").getAsString();

                    Date date = inputFormat.parse(dateText);
                    String day = outputFormat.format(date);

                    double temp = item.getAsJsonObject("main").get("temp").getAsDouble();
                    String condition = item.getAsJsonArray("weather")
                                           .get(0).getAsJsonObject()
                                           .get("main").getAsString();

                    tempMap.putIfAbsent(day, new ArrayList<>());
                    tempMap.get(day).add(temp);
                    conditionMap.putIfAbsent(day, condition);
                }

                for (String day : tempMap.keySet()) {
                    List<Double> temps = tempMap.get(day);
                    double avg = temps.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
                    forecastList.add(new ForecastData(day, avg, conditionMap.get(day)));
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

            return forecastList;
        }
    }
