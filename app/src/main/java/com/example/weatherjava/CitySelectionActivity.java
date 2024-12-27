package com.example.weatherjava;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class CitySelectionActivity extends AppCompatActivity {

    private EditText etCityName;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_city_selection);

        etCityName = findViewById(R.id.et_city_name);
        Button btnSearchCity = findViewById(R.id.btn_search_city);

        btnSearchCity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String cityName = etCityName.getText().toString().trim();
                if (!cityName.isEmpty()) {
                    // Передать выбранный город в MainActivity
                    Intent resultIntent = new Intent();
                    resultIntent.putExtra("SELECTED_CITY", cityName);
                    setResult(RESULT_OK, resultIntent);
                    finish();
                } else {
                    Toast.makeText(CitySelectionActivity.this, "Please enter a city name", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}