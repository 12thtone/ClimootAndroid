package com.mmmd.maher.climoot;

import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.mmmd.maher.climoot.model.DailyWeatherReport;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class WeatherActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks, LocationListener {

    final String URL_BASE = "http://api.openweathermap.org/data/2.5/forecast";
    final String URL_COORDS = "/?lat=";  //"/?lat=33.5967815&lon=-83.8601827";
    final String URL_UNITS = "&units=imperial";
    final String URL_API_KEY = "&APPID=1a572d94466740559cafd15dcfd262d3";

    private GoogleApiClient mGoogleApiClient;
    private final int PERMISSION_LOCATION = 123;
    private ArrayList<DailyWeatherReport> weatherReportList = new ArrayList<>();

    private ImageView weatherIcon;
    private ImageView weatherIconSmall;
    private TextView weatherDate;
    private TextView currentTemp;
    private TextView currentHumidity;
    private TextView cityCountry;
    private TextView weatherDescription;

    WeatherAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather);

        weatherIcon = (ImageView)findViewById(R.id.weatherIcon);
        weatherIconSmall = (ImageView)findViewById(R.id.weatherIconSmall);
        weatherDate = (TextView)findViewById(R.id.weatherDate);
        currentTemp = (TextView)findViewById(R.id.currentTemp);
        currentHumidity = (TextView)findViewById(R.id.currentHumidity);
        cityCountry = (TextView)findViewById(R.id.cityCountry);
        weatherDescription = (TextView)findViewById(R.id.weatherDescription);

        RecyclerView recyclerView = (RecyclerView)findViewById(R.id.content_weather_reports);

        mAdapter = new WeatherAdapter(weatherReportList);

        recyclerView.setAdapter(mAdapter);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);

        recyclerView.setLayoutManager(layoutManager);

        mGoogleApiClient = new GoogleApiClient.Builder(this).addApi(LocationServices.API)
                .enableAutoManage(this, this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
    }

    public void downloadWeatherData(Location location) {
        final String fullCoords = URL_COORDS + location.getLatitude() + "&lon=" + location.getLongitude();
        final String url = URL_BASE + fullCoords + URL_UNITS + URL_API_KEY;

        final JsonObjectRequest jsonRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.v("ALLOFIT", "Resp: " + response.toString());

                try {
                    JSONObject city = response.getJSONObject("city");
                    String cityName = city.getString("name");
                    String country = city.getString("country");
                    Log.v("CLIM", "Nem: " + cityName + " - Country: " + country);

                    JSONArray list = response.getJSONArray("list");
                    for (int x = 0; x < 5; x++) {
                        JSONObject obj = list.getJSONObject(x);
                        JSONObject main = obj.getJSONObject("main");
                        Double currentTemp = main.getDouble("temp");
                        Double maxTemp = main.getDouble("temp_max");
                        int humidity = main.getInt("humidity");

                        JSONArray weatherArray = obj.getJSONArray("weather");
                        JSONObject weather = weatherArray.getJSONObject(0);
                        String weatherType = weather.getString("main");

                        String rawDate = obj.getString("dt_txt");

                        DailyWeatherReport report = new DailyWeatherReport(cityName, country, currentTemp.intValue(), maxTemp.intValue(), weatherType, humidity, rawDate);
                        Log.v("JSON", "Weather from class: " + report.getWeather());
                        weatherReportList.add(report);
                    }
                } catch (JSONException e) {
                    Log.v("CLIM", "Exep: " + e.getLocalizedMessage());
                }

                updateUI();
                mAdapter.notifyDataSetChanged();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.v("CLIM", "Err: " + error.getLocalizedMessage());
            }
        });

        Volley.newRequestQueue(this).add(jsonRequest);
    }

    public void updateUI() {
        if (weatherReportList.size() > 0) {
            DailyWeatherReport report = weatherReportList.get(0);

            switch (report.getWeather()) {
                case DailyWeatherReport.WEATHER_TYPE_CLOUDS:
                    weatherIcon.setImageDrawable(getResources().getDrawable(R.drawable.cloudy));
                    break;
                case DailyWeatherReport.WEATHER_TYPE_RAIN:
                    weatherIcon.setImageDrawable(getResources().getDrawable(R.drawable.rainy));
                    break;
                default:
                    weatherIcon.setImageDrawable(getResources().getDrawable(R.drawable.sunny));
            }

            weatherDate.setText("Right Now");
            currentTemp.setText(Integer.toString(report.getCurrentTemp()) + "°");
            cityCountry.setText(report.getCityName() + ", " + report.getCountry());
            currentHumidity.setText("Humidity: " + report.getHumidity() + "%");
            weatherDescription.setText(report.getWeather());
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        downloadWeatherData(location);
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_LOCATION);

            Log.v("MY_MAPS", "Request Permissions");
        } else {
            Log.v("MY_MAPS", "Starting Locations Services from onConnected");
            startLocationServices();
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    public void startLocationServices() {
        Log.v("MY_MAPS", "Starting Locations Services Called");

        try {
            LocationRequest req = LocationRequest.create().setPriority(LocationRequest.PRIORITY_LOW_POWER);
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, req, this);
            Log.v("MY_MAPS", "Requesting location updates");
        } catch (SecurityException exception) {
            // Toast to enable location
            Log.v("MY_MAPS", exception.toString());
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case PERMISSION_LOCATION: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startLocationServices();
                    Log.v("MY_MAPS", "Permission granted - starting services");
                } else {
                    // Toast to enable location
                    Log.v("MY_MAPS", "Permission Denied");
                    Toast.makeText(this, "Please enable location so we can check your weather.", Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    public class WeatherAdapter extends RecyclerView.Adapter<WeatherReportViewHolder> {

        private  ArrayList<DailyWeatherReport> mDailyWeatherReports;

        public WeatherAdapter(ArrayList<DailyWeatherReport> dailyWeatherReports) {
            mDailyWeatherReports = dailyWeatherReports;
        }

        @Override
        public void onBindViewHolder(WeatherReportViewHolder holder, int position) {
            DailyWeatherReport report = mDailyWeatherReports.get(position);
            holder.updateUI(report);
        }

        @Override
        public int getItemCount() {
            return mDailyWeatherReports.size();
        }

        @Override
        public WeatherReportViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View card = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_weather, parent, false);
            return new WeatherReportViewHolder(card);
        }
    }

    public class WeatherReportViewHolder extends RecyclerView.ViewHolder {

        private ImageView weatherIconList;
        private TextView weatherDateList;
        private TextView weatherDescriptionList;
        private TextView tempHighList;

        public WeatherReportViewHolder(View itemView) {
            super(itemView);

            weatherIconList = (ImageView)itemView.findViewById(R.id.list_weather_icon);
            weatherDateList = (TextView)itemView.findViewById(R.id.list_weather_day);
            weatherDescriptionList = (TextView)itemView.findViewById(R.id.list_weather_description);
            tempHighList = (TextView)itemView.findViewById(R.id.list_weather_temp_high);
        }

        public void updateUI(DailyWeatherReport report) {

            weatherDateList.setText(report.getFormattedDate());
            weatherDescriptionList.setText(report.getWeather());
            tempHighList.setText(Integer.toString(report.getCurrentTemp()) + "°");

            switch (report.getWeather()) {
                case DailyWeatherReport.WEATHER_TYPE_CLOUDS:
                    weatherIconList.setImageDrawable(getResources().getDrawable(R.drawable.cloudy_mini));
                    break;
                case DailyWeatherReport.WEATHER_TYPE_RAIN:
                    weatherIconList.setImageDrawable(getResources().getDrawable(R.drawable.rainy_mini));
                    break;
                default:
                    weatherIconList.setImageDrawable(getResources().getDrawable(R.drawable.sunny_mini));
            }
        }
    }
}
