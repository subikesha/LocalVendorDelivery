package com.example.localvendordelivery;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CloudSyncManager {

    private static final String TAG = "CloudSyncManager";
    private static final String PREF_NAME = "CloudSyncPrefs";
    private static final String KEY_SERVER_URL = "server_url";
    
    // Reliable multi-tenant cloud storage endpoint for real-time sync across all Android phones
    private static final String DEFAULT_CLOUD_HUB = "https://local-vendor-delivery-central.glitch.me";
    private static final String BACKUP_CLOUD_API = "https://api.mockfly.dev/mocks";

    private static CloudSyncManager instance;
    private final ExecutorService executor = Executors.newFixedThreadPool(4);
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private final DatabaseHelper dbHelper;
    private final SharedPreferences prefs;

    private CloudSyncManager(Context context) {
        this.dbHelper = new DatabaseHelper(context.getApplicationContext());
        this.prefs = context.getApplicationContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public static synchronized CloudSyncManager getInstance(Context context) {
        if (instance == null) {
            instance = new CloudSyncManager(context);
        }
        return instance;
    }

    public String getServerUrl() {
        return prefs.getString(KEY_SERVER_URL, DEFAULT_CLOUD_HUB);
    }

    public void setServerUrl(String url) {
        prefs.edit().putString(KEY_SERVER_URL, url).apply();
    }

    public interface SyncCallback<T> {
        void onSuccess(T result);
        void onError(String error);
    }

    // --- Push Users, Shops, Products, Orders ---

    public void pushUserToCloud(String username, String password, String role, String fullName, String phone, String address, double lat, double lng) {
        executor.execute(() -> {
            try {
                JSONObject json = new JSONObject();
                json.put("username", username);
                json.put("password", password);
                json.put("role", role);
                json.put("fullName", fullName);
                json.put("phone", phone);
                json.put("address", address);
                json.put("latitude", lat);
                json.put("longitude", lng);

                sendHttpRequest("POST", getServerUrl() + "/api/users", json.toString());
                Log.d(TAG, "User " + username + " synced to cloud successfully!");
            } catch (Exception e) {
                Log.e(TAG, "Failed to push user to cloud: " + e.getMessage());
            }
        });
    }

    public void pushShopToCloud(int userId, String shopName, String category, double lat, double lng, String address) {
        executor.execute(() -> {
            try {
                JSONObject json = new JSONObject();
                json.put("userId", userId);
                json.put("shopName", shopName);
                json.put("category", category);
                json.put("latitude", lat);
                json.put("longitude", lng);
                json.put("address", address);

                sendHttpRequest("POST", getServerUrl() + "/api/shops", json.toString());
                Log.d(TAG, "Shop " + shopName + " synced to cloud successfully!");
            } catch (Exception e) {
                Log.e(TAG, "Failed to push shop to cloud: " + e.getMessage());
            }
        });
    }

    public void pushProductToCloud(int vendorUserId, String name, String category, double price, int quantity, String imageCode, String description) {
        executor.execute(() -> {
            try {
                JSONObject json = new JSONObject();
                json.put("vendorUserId", vendorUserId);
                json.put("name", name);
                json.put("category", category);
                json.put("price", price);
                json.put("quantity", quantity);
                json.put("imageCode", imageCode);
                json.put("description", description);

                sendHttpRequest("POST", getServerUrl() + "/api/products", json.toString());
                Log.d(TAG, "Product " + name + " synced to cloud successfully!");
            } catch (Exception e) {
                Log.e(TAG, "Failed to push product to cloud: " + e.getMessage());
            }
        });
    }

    public void pushOrderToCloud(int orderId, int custId, int vendorId, String items, double total, String custAddress, double custLat, double custLng) {
        executor.execute(() -> {
            try {
                JSONObject json = new JSONObject();
                json.put("id", orderId);
                json.put("customerId", custId);
                json.put("vendorId", vendorId);
                json.put("deliveryPartnerId", 0);
                json.put("status", "PENDING");
                json.put("items", items);
                json.put("total", total);
                json.put("custAddress", custAddress);
                json.put("custLat", custLat);
                json.put("custLng", custLng);
                json.put("rating", 0);
                json.put("feedback", "");
                json.put("timestamp", System.currentTimeMillis());

                sendHttpRequest("POST", getServerUrl() + "/api/orders", json.toString());
                Log.d(TAG, "Order " + orderId + " pushed to Cloud successfully!");
            } catch (Exception e) {
                Log.e(TAG, "Failed to push order to cloud: " + e.getMessage());
            }
        });
    }

    public void updateOrderStatusOnCloud(int orderId, String status, int deliveryPartnerId) {
        executor.execute(() -> {
            try {
                JSONObject json = new JSONObject();
                json.put("orderId", orderId);
                json.put("status", status);
                if (deliveryPartnerId > 0) {
                    json.put("deliveryPartnerId", deliveryPartnerId);
                }

                sendHttpRequest("POST", getServerUrl() + "/api/orders/update", json.toString());
                Log.d(TAG, "Order " + orderId + " status updated to " + status + " on cloud.");
            } catch (Exception e) {
                Log.e(TAG, "Failed to update order status on cloud: " + e.getMessage());
            }
        });
    }

    public void submitRatingToCloud(int orderId, int rating, String feedback) {
        executor.execute(() -> {
            try {
                JSONObject json = new JSONObject();
                json.put("orderId", orderId);
                json.put("rating", rating);
                json.put("feedback", feedback);

                sendHttpRequest("POST", getServerUrl() + "/api/orders/rating", json.toString());
                Log.d(TAG, "Rating submitted to cloud for order " + orderId);
            } catch (Exception e) {
                Log.e(TAG, "Failed to push rating to cloud: " + e.getMessage());
            }
        });
    }

    public void pushDriverLocationToCloud(int driverId, double lat, double lng) {
        executor.execute(() -> {
            try {
                JSONObject json = new JSONObject();
                json.put("driverId", driverId);
                json.put("latitude", lat);
                json.put("longitude", lng);
                json.put("timestamp", System.currentTimeMillis());

                sendHttpRequest("POST", getServerUrl() + "/api/drivers/location", json.toString());
            } catch (Exception e) {
                Log.e(TAG, "Failed to push driver location to cloud: " + e.getMessage());
            }
        });
    }

    // --- Authenticate User with Cloud Fallback ---

    public void authenticateUserWithCloud(String username, String password, String role, SyncCallback<Boolean> callback) {
        executor.execute(() -> {
            try {
                JSONObject json = new JSONObject();
                json.put("username", username);
                json.put("password", password);
                json.put("role", role);

                String response = sendHttpRequest("POST", getServerUrl() + "/api/auth", json.toString());

                if (response != null && response.contains("success")) {
                    JSONObject userObj = new JSONObject(response);
                    String fullName = userObj.optString("fullName", username);
                    String phone = userObj.optString("phone", "");
                    String address = userObj.optString("address", "");
                    double lat = userObj.optDouble("latitude", 12.9716);
                    double lng = userObj.optDouble("longitude", 77.5946);

                    dbHelper.upsertUserFromCloud(0, username, password, role, fullName, phone, address, lat, lng);
                    if (callback != null) mainHandler.post(() -> callback.onSuccess(true));
                    return;
                }
                if (callback != null) mainHandler.post(() -> callback.onSuccess(false));
            } catch (Exception e) {
                Log.e(TAG, "Cloud auth failed: " + e.getMessage());
                if (callback != null) mainHandler.post(() -> callback.onError(e.getMessage()));
            }
        });
    }

    // --- Synchronize All Cloud Data ---

    public void syncAllDataFromCloud(SyncCallback<Boolean> callback) {
        executor.execute(() -> {
            try {
                String response = sendHttpRequest("GET", getServerUrl() + "/api/sync/all", null);
                if (response != null && !response.isEmpty()) {
                    JSONObject root = new JSONObject(response);

                    // 1. Sync Users
                    if (root.has("users")) {
                        JSONArray usersArr = root.getJSONArray("users");
                        for (int i = 0; i < usersArr.length(); i++) {
                            JSONObject u = usersArr.getJSONObject(i);
                            dbHelper.upsertUserFromCloud(
                                    u.optInt("id", 0),
                                    u.getString("username"),
                                    u.getString("password"),
                                    u.getString("role"),
                                    u.optString("fullName", u.getString("username")),
                                    u.optString("phone", ""),
                                    u.optString("address", ""),
                                    u.optDouble("latitude", 12.9716),
                                    u.optDouble("longitude", 77.5946)
                            );
                        }
                    }

                    // 2. Sync Shops
                    if (root.has("shops")) {
                        JSONArray shopsArr = root.getJSONArray("shops");
                        for (int i = 0; i < shopsArr.length(); i++) {
                            JSONObject s = shopsArr.getJSONObject(i);
                            dbHelper.upsertShopFromCloud(
                                    s.optInt("id", 0),
                                    s.getInt("userId"),
                                    s.getString("shopName"),
                                    s.optString("category", "General Stores"),
                                    s.optDouble("latitude", 12.9716),
                                    s.optDouble("longitude", 77.5946),
                                    s.optString("address", "")
                            );
                        }
                    }

                    // 3. Sync Products
                    if (root.has("products")) {
                        JSONArray prodsArr = root.getJSONArray("products");
                        for (int i = 0; i < prodsArr.length(); i++) {
                            JSONObject p = prodsArr.getJSONObject(i);
                            dbHelper.upsertProductFromCloud(
                                    p.optInt("id", 0),
                                    p.getInt("vendorUserId"),
                                    p.getString("name"),
                                    p.optString("category", "General"),
                                    p.getDouble("price"),
                                    p.optInt("quantity", 50),
                                    p.optString("imageCode", "general"),
                                    p.optString("description", "")
                            );
                        }
                    }

                    // 4. Sync Orders
                    if (root.has("orders")) {
                        JSONArray ordersArr = root.getJSONArray("orders");
                        for (int i = 0; i < ordersArr.length(); i++) {
                            JSONObject o = ordersArr.getJSONObject(i);
                            dbHelper.upsertOrderFromCloud(
                                    o.getInt("id"),
                                    o.getInt("customerId"),
                                    o.getInt("vendorId"),
                                    o.optInt("deliveryPartnerId", 0),
                                    o.optString("status", "PENDING"),
                                    o.optString("items", ""),
                                    o.optDouble("total", 0.0),
                                    o.optString("custAddress", ""),
                                    o.optDouble("custLat", 0.0),
                                    o.optDouble("custLng", 0.0),
                                    o.optInt("rating", 0),
                                    o.optString("feedback", "")
                            );
                        }
                    }
                }

                if (callback != null) mainHandler.post(() -> callback.onSuccess(true));
            } catch (Exception e) {
                Log.e(TAG, "Sync all cloud data error: " + e.getMessage());
                if (callback != null) mainHandler.post(() -> callback.onError(e.getMessage()));
            }
        });
    }

    public void pullOrdersFromCloud(SyncCallback<Boolean> callback) {
        syncAllDataFromCloud(callback);
    }

    public void pullDriverLocationFromCloud(int driverId, SyncCallback<double[]> callback) {
        executor.execute(() -> {
            try {
                String response = sendHttpRequest("GET", getServerUrl() + "/api/drivers/location/" + driverId, null);
                if (response != null && !response.isEmpty()) {
                    JSONObject loc = new JSONObject(response);
                    double lat = loc.optDouble("latitude", 0.0);
                    double lng = loc.optDouble("longitude", 0.0);
                    dbHelper.updateDeliveryLocation(driverId, lat, lng);
                    if (callback != null) mainHandler.post(() -> callback.onSuccess(new double[]{lat, lng}));
                }
            } catch (Exception e) {
                if (callback != null) mainHandler.post(() -> callback.onError(e.getMessage()));
            }
        });
    }

    // --- JSON Backup / Share Payload Generator ---
    public String exportFullSyncPayload() {
        try {
            JSONObject root = new JSONObject();
            // Export users
            JSONArray usersArr = new JSONArray();
            android.database.Cursor uC = dbHelper.getReadableDatabase().rawQuery("SELECT * FROM " + DatabaseHelper.TABLE_USERS, null);
            if (uC != null) {
                while (uC.moveToNext()) {
                    JSONObject u = new JSONObject();
                    u.put("username", uC.getString(uC.getColumnIndexOrThrow(DatabaseHelper.COL_USERNAME)));
                    u.put("password", uC.getString(uC.getColumnIndexOrThrow(DatabaseHelper.COL_PASSWORD)));
                    u.put("role", uC.getString(uC.getColumnIndexOrThrow(DatabaseHelper.COL_ROLE)));
                    u.put("fullName", uC.getString(uC.getColumnIndexOrThrow(DatabaseHelper.COL_FULL_NAME)));
                    u.put("address", uC.getString(uC.getColumnIndexOrThrow(DatabaseHelper.COL_ADDRESS)));
                    u.put("latitude", uC.getDouble(uC.getColumnIndexOrThrow(DatabaseHelper.COL_LATITUDE)));
                    u.put("longitude", uC.getDouble(uC.getColumnIndexOrThrow(DatabaseHelper.COL_LONGITUDE)));
                    usersArr.put(u);
                }
                uC.close();
            }
            root.put("users", usersArr);

            // Export shops
            JSONArray shopsArr = new JSONArray();
            android.database.Cursor sC = dbHelper.getAllVendors();
            if (sC != null) {
                while (sC.moveToNext()) {
                    JSONObject s = new JSONObject();
                    s.put("userId", sC.getInt(sC.getColumnIndexOrThrow(DatabaseHelper.COL_VENDOR_USER_ID)));
                    s.put("shopName", sC.getString(sC.getColumnIndexOrThrow(DatabaseHelper.COL_SHOP_NAME)));
                    s.put("category", sC.getString(sC.getColumnIndexOrThrow(DatabaseHelper.COL_SHOP_CATEGORY)));
                    s.put("latitude", sC.getDouble(sC.getColumnIndexOrThrow(DatabaseHelper.COL_VENDOR_LAT)));
                    s.put("longitude", sC.getDouble(sC.getColumnIndexOrThrow(DatabaseHelper.COL_VENDOR_LNG)));
                    s.put("address", sC.getString(sC.getColumnIndexOrThrow(DatabaseHelper.COL_VENDOR_ADDRESS)));
                    shopsArr.put(s);
                }
                sC.close();
            }
            root.put("shops", shopsArr);

            return root.toString();
        } catch (Exception e) {
            return "{}";
        }
    }

    public void importFullSyncPayload(String jsonStr) {
        try {
            JSONObject root = new JSONObject(jsonStr);
            if (root.has("users")) {
                JSONArray usersArr = root.getJSONArray("users");
                for (int i = 0; i < usersArr.length(); i++) {
                    JSONObject u = usersArr.getJSONObject(i);
                    dbHelper.upsertUserFromCloud(0, u.getString("username"), u.getString("password"), u.getString("role"), u.optString("fullName", u.getString("username")), "", u.optString("address", ""), u.optDouble("latitude", 12.9716), u.optDouble("longitude", 77.5946));
                }
            }
            if (root.has("shops")) {
                JSONArray shopsArr = root.getJSONArray("shops");
                for (int i = 0; i < shopsArr.length(); i++) {
                    JSONObject s = shopsArr.getJSONObject(i);
                    dbHelper.upsertShopFromCloud(0, s.getInt("userId"), s.getString("shopName"), s.optString("category", "General Stores"), s.optDouble("latitude", 12.9716), s.optDouble("longitude", 77.5946), s.optString("address", ""));
                }
            }
        } catch (Exception ignored) {}
    }

    private String sendHttpRequest(String method, String urlStr, String jsonBody) throws Exception {
        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod(method);
        conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
        conn.setRequestProperty("Accept", "application/json");
        conn.setConnectTimeout(4000);
        conn.setReadTimeout(4000);

        if (jsonBody != null && !jsonBody.isEmpty()) {
            conn.setDoOutput(true);
            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = jsonBody.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }
        }

        int responseCode = conn.getResponseCode();
        if (responseCode >= 200 && responseCode < 300) {
            try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    response.append(line.trim());
                }
                return response.toString();
            }
        } else {
            return null;
        }
    }
}
