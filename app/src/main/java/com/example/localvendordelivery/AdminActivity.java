package com.example.localvendordelivery;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.card.MaterialCardView;

public class AdminActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;
    private View layoutAdminAuth, layoutAdminMain;
    private EditText etAdminUsername, etAdminPassword;
    private TextView tvAdminStatVendors, tvAdminStatProducts, tvAdminStatOrders;
    private boolean isAdminLoggedIn = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        dbHelper = new DatabaseHelper(this);

        layoutAdminAuth = findViewById(R.id.layoutAdminAuth);
        layoutAdminMain = findViewById(R.id.layoutAdminMain);

        etAdminUsername = findViewById(R.id.etAdminUsername);
        etAdminPassword = findViewById(R.id.etAdminPassword);
        Button btnAdminLogin = findViewById(R.id.btnAdminLogin);
        Button btnAdminLogout = findViewById(R.id.btnAdminLogout);

        tvAdminStatVendors = findViewById(R.id.tvAdminStatVendors);
        tvAdminStatProducts = findViewById(R.id.tvAdminStatProducts);
        tvAdminStatOrders = findViewById(R.id.tvAdminStatOrders);

        ImageButton btnAdminBack = findViewById(R.id.btnAdminBack);
        MaterialCardView cardRegisterVendor = findViewById(R.id.cardRegisterVendor);
        MaterialCardView cardRegisterDelivery = findViewById(R.id.cardRegisterDelivery);
        MaterialCardView cardAddProduct = findViewById(R.id.cardAddProduct);
        MaterialCardView cardSystemOverview = findViewById(R.id.cardSystemOverview);

        btnAdminLogin.setOnClickListener(v -> {
            String user = etAdminUsername.getText().toString().trim();
            String pass = etAdminPassword.getText().toString().trim();

            if (user.isEmpty() || pass.isEmpty()) {
                Toast.makeText(this, "Please enter admin username and password", Toast.LENGTH_SHORT).show();
                return;
            }

            Cursor cursor = dbHelper.loginUser(user, pass, "ADMIN");
            if (cursor != null && cursor.moveToFirst()) {
                cursor.close();
                isAdminLoggedIn = true;
                layoutAdminAuth.setVisibility(View.GONE);
                layoutAdminMain.setVisibility(View.VISIBLE);
                loadQuickStats();
                Toast.makeText(this, "Welcome, Administrator!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Invalid Admin credentials (Default: admin / admin)", Toast.LENGTH_SHORT).show();
            }
        });

        btnAdminLogout.setOnClickListener(v -> {
            isAdminLoggedIn = false;
            etAdminPassword.setText("");
            layoutAdminAuth.setVisibility(View.VISIBLE);
            layoutAdminMain.setVisibility(View.GONE);
        });

        btnAdminBack.setOnClickListener(v -> finish());

        cardRegisterVendor.setOnClickListener(v -> {
            Intent intent = new Intent(AdminActivity.this, AdminRegisterVendorActivity.class);
            startActivity(intent);
        });

        cardRegisterDelivery.setOnClickListener(v -> {
            Intent intent = new Intent(AdminActivity.this, AdminRegisterDeliveryActivity.class);
            startActivity(intent);
        });

        cardAddProduct.setOnClickListener(v -> {
            Intent intent = new Intent(AdminActivity.this, AdminAddProductActivity.class);
            startActivity(intent);
        });

        cardSystemOverview.setOnClickListener(v -> {
            Intent intent = new Intent(AdminActivity.this, AdminOverviewActivity.class);
            startActivity(intent);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isAdminLoggedIn) {
            loadQuickStats();
        }
    }

    private void loadQuickStats() {
        // Vendors count
        int vendorCount = 0;
        Cursor vCursor = dbHelper.getAllVendors();
        if (vCursor != null) {
            vendorCount = vCursor.getCount();
            vCursor.close();
        }
        tvAdminStatVendors.setText(String.valueOf(vendorCount));

        // Products count
        int productCount = 0;
        Cursor pCursor = dbHelper.getReadableDatabase().rawQuery("SELECT * FROM " + DatabaseHelper.TABLE_PRODUCTS, null);
        if (pCursor != null) {
            productCount = pCursor.getCount();
            pCursor.close();
        }
        tvAdminStatProducts.setText(String.valueOf(productCount));

        // Orders count
        int orderCount = 0;
        Cursor oCursor = dbHelper.getReadableDatabase().rawQuery("SELECT * FROM " + DatabaseHelper.TABLE_ORDERS, null);
        if (oCursor != null) {
            orderCount = oCursor.getCount();
            oCursor.close();
        }
        tvAdminStatOrders.setText(String.valueOf(orderCount));
    }
}
