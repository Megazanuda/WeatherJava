package com.example.weatherjava;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.weatherjava.R;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import android.widget.Button;


public class MainActivity extends AppCompatActivity {

    private TextView tvLocation, tvTemperature, tvDescription, tvHumidity, tvWind;
    private RecyclerView rvForecast;
    private FusedLocationProviderClient fusedLocationClient;

    private static final int CITY_SELECTION_REQUEST_CODE = 1;
    private String currentCity = "Moscow"; // Город по умолчанию

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btnSelectCity = findViewById(R.id.btn_select_city);
        btnSelectCity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Открыть экран выбора города
                Intent intent = new Intent(MainActivity.this, CitySelectionActivity.class);
                startActivityForResult(intent, CITY_SELECTION_REQUEST_CODE);
            }
        });

        // Загружаем погоду для текущего города
        loadWeatherData(currentCity);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CITY_SELECTION_REQUEST_CODE && resultCode == RESULT_OK) {
            if (data != null) {
                currentCity = data.getStringExtra("SELECTED_CITY");
                Toast.makeText(this, "Selected city: " + currentCity, Toast.LENGTH_SHORT).show();

                // Обновляем погоду для выбранного города
                loadWeatherData(currentCity);
            }
        }
    }

    private void loadWeatherData(String city) {
        String weatherApiUrl = "https://api.openweathermap.org/data/2.5/weather?q=" + city + "&appid=bb89f3debf7685982415db3f003d64fd&units=metric";

        // HTTP-запрос для получения данных о погоде
        new Thread(() -> {
            try {
                URL url = new URL(weatherApiUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.connect();

                InputStream inputStream = connection.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                StringBuilder result = new StringBuilder();
                String line;

                while ((line = reader.readLine()) != null) {
                    result.append(line);
                }

                // Парсим JSON (это только пример, адаптируйте под свою структуру)
                JSONObject jsonResponse = new JSONObject(result.toString());
                JSONObject main = jsonResponse.getJSONObject("main");
                String temperature = main.getString("temp");

                runOnUiThread(() -> {
                    // Обновляем UI с новой информацией о погоде
                    TextView tempView = findViewById(R.id.tv_temperature);
                    tempView.setText(temperature + "°C");
                });

            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }



    private void checkLocationPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fetchLocation();
        } else {
            locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
        }
    }

    private final ActivityResultLauncher<String> locationPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            isGranted -> {
                if (isGranted) {
                    fetchLocation();
                }
            }
    );



    private void fetchLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
            if (location != null) {
                fetchWeather(location.getLatitude(), location.getLongitude());
            }
        });
    }

    private void fetchWeather(double lat, double lon) {
        String apiKey = "bb89f3debf7685982415db3f003d64fd";
        String apiUrl = "https://api.openweathermap.org/data/2.5/weather?lat=" + lat + "&lon=" + lon + "&appid=" + apiKey + "&units=metric";

        try {
            URL url = new URL(apiUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;

                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();

                parseWeatherResponse(response.toString());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void parseWeatherResponse(String response) {
        try {
            JSONObject jsonObject = new JSONObject(response);

            String name = jsonObject.getString("name");

            JSONArray weatherArray = jsonObject.getJSONArray("weather");
            String description = weatherArray.getJSONObject(0).getString("description");

            JSONObject main = jsonObject.getJSONObject("main");
            double temp = main.getDouble("temp");
            int humidity = main.getInt("humidity");

            JSONObject wind = jsonObject.getJSONObject("wind");
            double windSpeed = wind.getDouble("speed");

            runOnUiThread(() -> updateUI(name, temp, description, humidity, windSpeed));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateUI(String name, double temp, String description, int humidity, double windSpeed) {
        tvLocation.setText(name);
        tvTemperature.setText(temp + "°C");
        tvDescription.setText(description);
        tvHumidity.setText("Humidity: " + humidity + "%");
        tvWind.setText("Wind: " + windSpeed + " m/s");
    }

    private void setupRecyclerView() {
        if (rvForecast != null) {
            rvForecast.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
            // Здесь установите адаптер для RecyclerView, если нужно
        } else {
            throw new NullPointerException("RecyclerView is null. Check your layout.");
        }
    }
}