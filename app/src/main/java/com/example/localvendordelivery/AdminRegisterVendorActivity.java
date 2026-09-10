package com.example.localvendordelivery;

import android.database.Cursor;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class AdminRegisterVendorActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;
    private CloudSyncManager cloudSyncManager;
    private EditText etVendorUsername, etVendorPassword, etVendorFullName, etShopName, etShopAddress, etShopLat, etShopLng;
    private Spinner spnVendorCategory;

    public static final String[] CATEGORIES = new String[]{
            "Restaurants & Food",
            "Supermarket & Grocery",
            "Bakery & Sweets",
            "Fresh Fruits & Vegetables",
            "Pharmacy & Healthcare",
            "General Stores"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_register_vendor);

        dbHelper = new DatabaseHelper(this);
        cloudSyncManager = CloudSyncManager.getInstance(this);

        ImageButton btnBack = findViewById(R.id.btnBack);
        etVendorUsername = findViewById(R.id.etVendorUsername);
        etVendorPassword = findViewById(R.id.etVendorPassword);
        etVendorFullName = findViewById(R.id.etVendorFullName);
        spnVendorCategory = findViewById(R.id.spnVendorCategory);
        etShopName = findViewById(R.id.etShopName);
        etShopAddress = findViewById(R.id.etShopAddress);
        etShopLat = findViewById(R.id.etShopLat);
        etShopLng = findViewById(R.id.etShopLng);
        Button btnQuickCoords = findViewById(R.id.btnQuickCoords);
        Button btnSubmitRegisterVendor = findViewById(R.id.btnSubmitRegisterVendor);

        ArrayAdapter<String> catAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, CATEGORIES);
        catAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnVendorCategory.setAdapter(catAdapter);

        btnBack.setOnClickListener(v -> finish());

        btnQuickCoords.setOnClickListener(v -> {
            etShopLat.setText("12.9716");
            etShopLng.setText("77.5946");
            Toast.makeText(this, "Filled default nearby coordinates (12.9716, 77.5946)", Toast.LENGTH_SHORT).show();
        });

        btnSubmitRegisterVendor.setOnClickListener(v -> {
            String username = etVendorUsername.getText().toString().trim();
            String password = etVendorPassword.getText().toString().trim();
            String fullName = etVendorFullName.getText().toString().trim();
            String category = spnVendorCategory.getSelectedItem().toString();
            String shopName = etShopName.getText().toString().trim();
            String address = etShopAddress.getText().toString().trim();
            String latStr = etShopLat.getText().toString().trim();
            String lngStr = etShopLng.getText().toString().trim();

            if (username.isEmpty() || password.isEmpty() || fullName.isEmpty() || shopName.isEmpty() || address.isEmpty() || latStr.isEmpty() || lngStr.isEmpty()) {
                Toast.makeText(this, "Please fill in all vendor fields", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                double lat = Double.parseDouble(latStr);
                double lng = Double.parseDouble(lngStr);

                // Register vendor user in local db
                boolean userAdded = dbHelper.createVendorUser(username, password, fullName, lat, lng);
                if (userAdded) {
                    int userId = -1;
                    Cursor cursor = dbHelper.loginUser(username, password, "VENDOR");
                    if (cursor != null && cursor.moveToFirst()) {
                        userId = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_ID));
                        cursor.close();
                    }

                    if (userId != -1) {
                        boolean vendorAdded = dbHelper.addVendorShop(userId, shopName, category, lat, lng, address);
                        if (vendorAdded) {
                            // CRITICAL: Push User & Shop to Cloud so ANY phone can log in immediately!
                            cloudSyncManager.pushUserToCloud(username, password, "VENDOR", fullName, "", address, lat, lng);
                            cloudSyncManager.pushShopToCloud(userId, shopName, category, lat, lng, address);

                            Toast.makeText(this, "Vendor & Shop Registered & Synced to Cloud!", Toast.LENGTH_LONG).show();
                            clearFields();
                        } else {
                            Toast.makeText(this, "Failed to create shop profile", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(this, "Error retrieving vendor ID", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(this, "Username already exists! Choose another.", Toast.LENGTH_SHORT).show();
                }

            } catch (NumberFormatException e) {
                Toast.makeText(this, "Invalid Coordinates. Must be numbers.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void clearFields() {
        etVendorUsername.setText("");
        etVendorPassword.setText("");
        etVendorFullName.setText("");
        etShopName.setText("");
        etShopAddress.setText("");
        etShopLat.setText("");
        etShopLng.setText("");
    }
}
