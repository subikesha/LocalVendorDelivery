package com.example.localvendordelivery;

import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;
import java.util.List;

public class AdminAddProductActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;
    private Spinner spnVendors, spnProductImage;
    private EditText etProductName, etProductPrice, etProductQuantity, etProductDesc;
    private TextView tvShopProductsSummary;

    private List<Integer> vendorIdsList = new ArrayList<>();
    private List<String> vendorNamesList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_add_product);

        dbHelper = new DatabaseHelper(this);

        ImageButton btnBackAddProduct = findViewById(R.id.btnBackAddProduct);
        spnVendors = findViewById(R.id.spnVendors);
        spnProductImage = findViewById(R.id.spnProductImage);
        etProductName = findViewById(R.id.etProductName);
        etProductPrice = findViewById(R.id.etProductPrice);
        etProductQuantity = findViewById(R.id.etProductQuantity);
        etProductDesc = findViewById(R.id.etProductDesc);
        Button btnSubmitAddProduct = findViewById(R.id.btnSubmitAddProduct);
        tvShopProductsSummary = findViewById(R.id.tvShopProductsSummary);

        btnBackAddProduct.setOnClickListener(v -> finish());

        // Setup Picture Selection Spinner
        ArrayAdapter<String> imgAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, ProductVisualHelper.getAvailableImageOptions());
        imgAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnProductImage.setAdapter(imgAdapter);

        loadVendorsSpinner();

        spnVendors.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position >= 0 && position < vendorIdsList.size()) {
                    loadProductsForSelectedVendor(vendorIdsList.get(position));
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        btnSubmitAddProduct.setOnClickListener(v -> {
            if (vendorIdsList.isEmpty() || spnVendors.getSelectedItemPosition() == AdapterView.INVALID_POSITION) {
                Toast.makeText(this, "No vendor selected or available", Toast.LENGTH_SHORT).show();
                return;
            }

            int selectedVendorUserId = vendorIdsList.get(spnVendors.getSelectedItemPosition());
            String selectedImgOption = spnProductImage.getSelectedItem().toString();
            String imageCode = ProductVisualHelper.getImageCodeFromOption(selectedImgOption);

            String prodName = etProductName.getText().toString().trim();
            String priceStr = etProductPrice.getText().toString().trim();
            String qtyStr = etProductQuantity.getText().toString().trim();
            String prodDesc = etProductDesc.getText().toString().trim();

            if (prodName.isEmpty() || priceStr.isEmpty() || prodDesc.isEmpty()) {
                Toast.makeText(this, "Please fill in product name, price and description", Toast.LENGTH_SHORT).show();
                return;
            }

            int quantity = 50;
            if (!qtyStr.isEmpty()) {
                try {
                    quantity = Integer.parseInt(qtyStr);
                } catch (Exception ignored) {}
            }

            try {
                double price = Double.parseDouble(priceStr);
                boolean success = dbHelper.addProduct(selectedVendorUserId, prodName, "General", price, quantity, imageCode, prodDesc);
                if (success) {
                    // Push to Cloud so all phones see new product
                    CloudSyncManager.getInstance(this).pushProductToCloud(selectedVendorUserId, prodName, "General", price, quantity, imageCode, prodDesc);

                    Toast.makeText(this, "Product added & synced to Cloud!", Toast.LENGTH_SHORT).show();
                    etProductName.setText("");
                    etProductPrice.setText("");
                    etProductQuantity.setText("");
                    etProductDesc.setText("");
                    loadProductsForSelectedVendor(selectedVendorUserId);
                } else {
                    Toast.makeText(this, "Failed to add product", Toast.LENGTH_SHORT).show();
                }
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Invalid price format. Must be a number.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadVendorsSpinner() {
        vendorIdsList.clear();
        vendorNamesList.clear();

        Cursor cursor = dbHelper.getAllVendors();
        if (cursor != null) {
            while (cursor.moveToNext()) {
                int vendorUserId = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_VENDOR_USER_ID));
                String shopName = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_SHOP_NAME));
                vendorIdsList.add(vendorUserId);
                vendorNamesList.add(shopName + " (Vendor ID: " + vendorUserId + ")");
            }
            cursor.close();
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, vendorNamesList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnVendors.setAdapter(adapter);

        if (!vendorIdsList.isEmpty()) {
            loadProductsForSelectedVendor(vendorIdsList.get(0));
        } else {
            tvShopProductsSummary.setText("No vendors registered yet. Please register a vendor first.");
        }
    }

    private void loadProductsForSelectedVendor(int vendorUserId) {
        StringBuilder sb = new StringBuilder();
        Cursor pCursor = dbHelper.getProductsForVendor(vendorUserId);
        if (pCursor != null) {
            int count = 0;
            while (pCursor.moveToNext()) {
                count++;
                String name = pCursor.getString(pCursor.getColumnIndexOrThrow(DatabaseHelper.COL_PROD_NAME));
                double price = pCursor.getDouble(pCursor.getColumnIndexOrThrow(DatabaseHelper.COL_PROD_PRICE));
                int qty = pCursor.getInt(pCursor.getColumnIndexOrThrow(DatabaseHelper.COL_PROD_QUANTITY));
                String imgCode = pCursor.getString(pCursor.getColumnIndexOrThrow(DatabaseHelper.COL_PROD_IMAGE));
                String emoji = ProductVisualHelper.getEmojiForImageCode(imgCode);
                String desc = pCursor.getString(pCursor.getColumnIndexOrThrow(DatabaseHelper.COL_PROD_DESC));

                sb.append(emoji).append(" ").append(name).append(" - ₹").append(price)
                        .append(" [Stock: ").append(qty).append("]\n  ").append(desc).append("\n\n");
            }
            pCursor.close();

            if (count == 0) {
                tvShopProductsSummary.setText("No products added to this shop yet. Use the form above to add items.");
            } else {
                tvShopProductsSummary.setText(sb.toString().trim());
            }
        }
    }
}
