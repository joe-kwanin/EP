package com.example.vvuexampermitapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

public class Dashboard extends AppCompatActivity {
    private ImageView face,finger;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        face = findViewById(R.id.face);
        finger = findViewById(R.id.finger);

        finger.setOnClickListener(view -> {
            Intent intent = new Intent(Dashboard.this,BluetoothReaderTestActivity.class);
            startActivity(intent);
        });

        face.setOnClickListener(view -> {
            Intent intent = new Intent(Dashboard.this,DetectorActivity.class);
            startActivity(intent);
        });
    }

    @Override
    public void onBackPressed() {
        //do nothing
    }
}