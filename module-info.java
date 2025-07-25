module WeatherApp {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.net.http;
    requires com.google.gson;

    opens com.weatherapp to javafx.fxml, javafx.graphics;
    exports com.weatherapp;
}
