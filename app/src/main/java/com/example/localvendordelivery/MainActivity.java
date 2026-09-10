package com.example.localvendordelivery;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btnCustomer = findViewById(R.id.btnCustomer);
        Button btnVendor = findViewById(R.id.btnVendor);
        Button btnDelivery = findViewById(R.id.btnDelivery);
        Button btnAdmin = findViewById(R.id.btnAdmin);

        // Sync all cloud data on launch
        CloudSyncManager.getInstance(this).syncAllDataFromCloud(null);

        btnCustomer.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, CustomerActivity.class);
            startActivity(intent);
        });

        btnVendor.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, VendorActivity.class);
            startActivity(intent);
        });

        btnDelivery.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, DeliveryActivity.class);
            startActivity(intent);
        });

        btnAdmin.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AdminActivity.class);
            startActivity(intent);
        });
    }
}