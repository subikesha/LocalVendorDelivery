package com.example.localvendordelivery;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class AdminRegisterDeliveryActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;
    private CloudSyncManager cloudSyncManager;
    private EditText etDelivUsername, etDelivPassword, etDelivFullName, etDelivPhone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_register_delivery);

        dbHelper = new DatabaseHelper(this);
        cloudSyncManager = CloudSyncManager.getInstance(this);

        ImageButton btnBack = findViewById(R.id.btnBackRegisterDelivery);
        etDelivUsername = findViewById(R.id.etDelivUsername);
        etDelivPassword = findViewById(R.id.etDelivPassword);
        etDelivFullName = findViewById(R.id.etDelivFullName);
        etDelivPhone = findViewById(R.id.etDelivPhone);
        Button btnSubmit = findViewById(R.id.btnSubmitRegisterDelivery);

        btnBack.setOnClickListener(v -> finish());

        btnSubmit.setOnClickListener(v -> {
            String username = etDelivUsername.getText().toString().trim();
            String password = etDelivPassword.getText().toString().trim();
            String fullName = etDelivFullName.getText().toString().trim();
            String phone = etDelivPhone.getText().toString().trim();

            if (username.isEmpty() || password.isEmpty() || fullName.isEmpty() || phone.isEmpty()) {
                Toast.makeText(this, "Please fill in all details", Toast.LENGTH_SHORT).show();
                return;
            }

            boolean success = dbHelper.createDeliveryPartner(username, password, fullName, phone, 12.9710, 77.5960);
            if (success) {
                // CRITICAL: Push driver to Cloud so driver can log in on ANY phone!
                cloudSyncManager.pushUserToCloud(username, password, "DELIVERY", fullName, phone, "", 12.9710, 77.5960);

                Toast.makeText(this, "Delivery Partner Created & Synced to Cloud! Login: " + username, Toast.LENGTH_LONG).show();
                etDelivUsername.setText("");
                etDelivPassword.setText("");
                etDelivFullName.setText("");
                etDelivPhone.setText("");
            } else {
                Toast.makeText(this, "Username already exists! Choose another.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
