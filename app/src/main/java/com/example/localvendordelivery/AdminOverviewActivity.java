package com.example.localvendordelivery;

import android.database.Cursor;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class AdminOverviewActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;
    private TextView tvOverviewShops, tvOverviewProducts, tvOverviewOrders;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_overview);

        dbHelper = new DatabaseHelper(this);

        ImageButton btnBackOverview = findViewById(R.id.btnBackOverview);
        tvOverviewShops = findViewById(R.id.tvOverviewShops);
        tvOverviewProducts = findViewById(R.id.tvOverviewProducts);
        tvOverviewOrders = findViewById(R.id.tvOverviewOrders);

        btnBackOverview.setOnClickListener(v -> finish());

        loadDetailedOverview();
    }

    private void loadDetailedOverview() {
        // 1. Load Shops with Categories
        StringBuilder sbShops = new StringBuilder();
        Cursor vCursor = dbHelper.getAllVendors();
        if (vCursor != null) {
            int count = 0;
            while (vCursor.moveToNext()) {
                count++;
                String shopName = vCursor.getString(vCursor.getColumnIndexOrThrow(DatabaseHelper.COL_SHOP_NAME));
                String category = vCursor.getString(vCursor.getColumnIndexOrThrow(DatabaseHelper.COL_SHOP_CATEGORY));
                String address = vCursor.getString(vCursor.getColumnIndexOrThrow(DatabaseHelper.COL_VENDOR_ADDRESS));
                double lat = vCursor.getDouble(vCursor.getColumnIndexOrThrow(DatabaseHelper.COL_VENDOR_LAT));
                double lng = vCursor.getDouble(vCursor.getColumnIndexOrThrow(DatabaseHelper.COL_VENDOR_LNG));
                int userId = vCursor.getInt(vCursor.getColumnIndexOrThrow(DatabaseHelper.COL_VENDOR_USER_ID));

                sbShops.append(count).append(". ").append(shopName).append(" [").append(category).append("]\n")
                        .append("   👤 Vendor ID: ").append(userId).append("\n")
                        .append("   📍 Address: ").append(address).append("\n")
                        .append("   🌐 GPS Coords: [").append(lat).append(", ").append(lng).append("]\n\n");
            }
            vCursor.close();
            if (count == 0) sbShops.append("No vendor shops registered.");
        }
        tvOverviewShops.setText(sbShops.toString().trim());

        // 2. Load Products with Picture emojis and stock quantity
        StringBuilder sbProducts = new StringBuilder();
        Cursor pCursor = dbHelper.getReadableDatabase().rawQuery("SELECT p.*, v.shop_name FROM " + DatabaseHelper.TABLE_PRODUCTS + " p LEFT JOIN " + DatabaseHelper.TABLE_VENDORS + " v ON p." + DatabaseHelper.COL_PROD_VENDOR_ID + " = v." + DatabaseHelper.COL_VENDOR_USER_ID, null);
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
                String shopName = pCursor.getString(pCursor.getColumnIndexOrThrow("shop_name"));
                if (shopName == null) shopName = "Vendor #" + pCursor.getInt(pCursor.getColumnIndexOrThrow(DatabaseHelper.COL_PROD_VENDOR_ID));

                sbProducts.append(count).append(". ").append(emoji).append(" ").append(name).append(" - ₹").append(price).append("\n")
                        .append("   🏬 Shop: ").append(shopName).append("\n")
                        .append("   📊 Stock Available: ").append(qty).append(" units\n")
                        .append("   ℹ️ Details: ").append(desc).append("\n\n");
            }
            pCursor.close();
            if (count == 0) sbProducts.append("No products in catalog.");
        }
        tvOverviewProducts.setText(sbProducts.toString().trim());

        // 3. Load Delivery Drivers & Customer Orders with Ratings & Feedback
        StringBuilder sbOrders = new StringBuilder();

        Cursor dCursor = dbHelper.getOnlineDeliveryPartners();
        sbOrders.append("🛵 Registered Delivery Partners:\n");
        if (dCursor != null) {
            int dCount = 0;
            while (dCursor.moveToNext()) {
                dCount++;
                String dName = dCursor.getString(dCursor.getColumnIndexOrThrow(DatabaseHelper.COL_USERNAME));
                String fullName = dCursor.getString(dCursor.getColumnIndexOrThrow(DatabaseHelper.COL_FULL_NAME));
                String phone = dCursor.getString(dCursor.getColumnIndexOrThrow(DatabaseHelper.COL_PHONE));
                double lat = dCursor.getDouble(dCursor.getColumnIndexOrThrow(DatabaseHelper.COL_LATITUDE));
                double lng = dCursor.getDouble(dCursor.getColumnIndexOrThrow(DatabaseHelper.COL_LONGITUDE));
                sbOrders.append("  • ").append(fullName != null ? fullName : dName).append(" (@").append(dName).append(")\n")
                        .append("    📞 ").append(phone != null ? phone : "N/A")
                        .append(" | GPS: [").append(lat).append(", ").append(lng).append("]\n");
            }
            dCursor.close();
            if (dCount == 0) sbOrders.append("  No delivery partners created.\n");
        }

        sbOrders.append("\n📋 Customer Orders & Delivery Feedback:\n");
        Cursor oCursor = dbHelper.getReadableDatabase().rawQuery("SELECT * FROM " + DatabaseHelper.TABLE_ORDERS + " ORDER BY " + DatabaseHelper.COL_ID + " DESC", null);
        if (oCursor != null) {
            int oCount = 0;
            while (oCursor.moveToNext()) {
                oCount++;
                int id = oCursor.getInt(oCursor.getColumnIndexOrThrow(DatabaseHelper.COL_ID));
                String status = oCursor.getString(oCursor.getColumnIndexOrThrow(DatabaseHelper.COL_ORDER_STATUS));
                double total = oCursor.getDouble(oCursor.getColumnIndexOrThrow(DatabaseHelper.COL_ORDER_TOTAL));
                String items = oCursor.getString(oCursor.getColumnIndexOrThrow(DatabaseHelper.COL_ORDER_ITEMS));
                int rating = oCursor.getInt(oCursor.getColumnIndexOrThrow(DatabaseHelper.COL_ORDER_RATING));
                String feedback = oCursor.getString(oCursor.getColumnIndexOrThrow(DatabaseHelper.COL_ORDER_FEEDBACK));

                sbOrders.append("  #").append(id).append(" [Status: ").append(status).append("] - Total: ₹").append(total).append("\n")
                        .append("    🛒 Items: ").append(items).append("\n");

                if (rating > 0) {
                    StringBuilder stars = new StringBuilder();
                    for (int s = 0; s < rating; s++) stars.append("⭐");
                    sbOrders.append("    🌟 Driver Rating: ").append(stars).append(" (").append(rating).append("/5)\n");
                    if (feedback != null && !feedback.isEmpty()) {
                        sbOrders.append("    💬 Customer Feedback: \"").append(feedback).append("\"\n");
                    }
                }
                sbOrders.append("\n");
            }
            oCursor.close();
            if (oCount == 0) sbOrders.append("  No orders placed yet.");
        }

        tvOverviewOrders.setText(sbOrders.toString().trim());
    }
}
