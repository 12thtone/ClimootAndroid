package com.mmmd.maher.climoot.model;

/**
 * Created by maher on 7/3/16.
 */
public class DailyWeatherReport {

    public static final String WEATHER_TYPE_CLOUDS = "clouds";
    public static final String WEATHER_TYPE_CLEAR = "clear";
    public static final String WEATHER_TYPE_RAIN = "rain";
    public static final String WEATHER_TYPE_WIND = "wind";
    public static final String WEATHER_TYPE_SNOW = "snow";


    private String cityName;
    private String country;
    private int currentTemp;
    private int maxTemp;
    private int minTemp;
    private String weather;
    private int humidity;
    private String formattedDate;

    public DailyWeatherReport(String cityName, String country, int currentTemp, int maxTemp, int minTemp, String weather, int humidity, String formattedDate) {
        this.cityName = cityName;
        this.country = country;
        this.currentTemp = currentTemp;
        this.maxTemp = maxTemp;
        this.minTemp = minTemp;
        this.weather = weather;
        this.humidity = humidity;
        this.formattedDate = rawDateToPretty(formattedDate);
    }

    public String rawDateToPretty(String rawDate) {
        return "July 3";
    }

    public String getCityName() {
        return cityName;
    }

    public String getCountry() {
        return country;
    }

    public int getCurrentTemp() {
        return currentTemp;
    }

    public int getMaxTemp() {
        return maxTemp;
    }

    public int getMinTemp() {
        return minTemp;
    }

    public String getWeather() {
        return weather;
    }

    public int getHumidity() {
        return humidity;
    }

    public String getFormattedDate() {
        return formattedDate;
    }
}
