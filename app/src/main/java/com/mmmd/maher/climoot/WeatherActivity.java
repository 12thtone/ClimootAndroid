package com.mmmd.maher.climoot;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

public class WeatherActivity extends AppCompatActivity {

    final String URL_BASE = "http://api.openweathermap.org/data/2.5/forecast";
    final String URL_COORDS = "/?lat=33.5967815&lon=-83.8601827";
    final String URL_UNITS = "&units=imperial";
    final String URL_API_KEY = "&APPID=1a572d94466740559cafd15dcfd262d3";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather);

        final String url = URL_BASE + URL_COORDS + URL_UNITS + URL_API_KEY;

        final JsonObjectRequest jsonRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.v("CLIM", "Resp: " + response.toString());
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.v("CLIM", "Err: " + error.getLocalizedMessage());
            }
        });

        Volley.newRequestQueue(this).add(jsonRequest);
    }
}
