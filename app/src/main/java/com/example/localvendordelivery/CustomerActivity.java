package com.example.localvendordelivery;

import android.app.AlertDialog;
import android.content.Context;
import android.database.Cursor;
import android.location.Location;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.card.MaterialCardView;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class CustomerActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;
    private CloudSyncManager cloudSyncManager;
    private int currentUserId = -1;
    private String currentUsername = "";
    private String currentUserFullName = "";
    private String currentUserAddress = "12th Main, Bangalore";
    private double userLatitude = 12.9780;
    private double userLongitude = 77.5900;

    // View Containers
    private View layoutAuth, layoutMain;
    private View panelCategories, panelShops, panelProducts, panelTracking;

    // Auth Views
    private TextView tvAuthTitle, tvAuthSubtitle;
    private LinearLayout layoutRegisterFields;
    private EditText etCustFullName, etCustPhone, etCustAddress, etCustUsername, etCustPassword;
    private Button btnCustLogin, btnToggleRegisterMode;
    private boolean isRegisterMode = false;

    // Categories
    private ListView lvCategories;
    private List<CategoryModel> categoriesList = new ArrayList<>();
    private CategoriesAdapter categoriesAdapter;
    private String selectedCategory = "";

    // Shops
    private TextView tvChosenCategoryTitle;
    private ListView lvShops;
    private List<ShopModel> shopsList = new ArrayList<>();
    private ShopsAdapter shopsAdapter;
    private int selectedVendorUserId = -1;
    private String selectedVendorShopName = "";
    private double selectedVendorLat = 12.9716;
    private double selectedVendorLng = 77.5946;

    // Products
    private TextView tvShopDetailTitle;
    private ListView lvProducts;
    private List<ProductModel> productsList = new ArrayList<>();
    private ProductsAdapter productsAdapter;
    private Map<Integer, Integer> cart = new HashMap<>(); // ProductID -> Quantity

    // Floating Sticky Cart Bar
    private MaterialCardView cardFloatingCartBar;
    private TextView tvFloatingCartTotal;
    private Button btnOpenCartModal;

    // Tracking & Radar Navigation
    private TextView tvTrackingStatus, tvTrackingDetails, tvCustDeliveryLocation;
    private Button btnRateDriver;
    private LiveNavRadarView navRadarCust;
    private int activeTrackingOrderId = -1;
    private String lastKnownOrderStatus = "";
    private Timer trackingTimer;
    private Timer cloudPollTimer;
    private double currentSimProgress = 0.0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer);

        dbHelper = new DatabaseHelper(this);
        cloudSyncManager = CloudSyncManager.getInstance(this);

        // Bind View Containers
        layoutAuth = findViewById(R.id.layoutAuth);
        layoutMain = findViewById(R.id.layoutMain);
        panelCategories = findViewById(R.id.panelCategories);
        panelShops = findViewById(R.id.panelShops);
        panelProducts = findViewById(R.id.panelProducts);
        panelTracking = findViewById(R.id.panelTracking);

        // Auth
        tvAuthTitle = findViewById(R.id.tvAuthTitle);
        tvAuthSubtitle = findViewById(R.id.tvAuthSubtitle);
        layoutRegisterFields = findViewById(R.id.layoutRegisterFields);
        etCustFullName = findViewById(R.id.etCustFullName);
        etCustPhone = findViewById(R.id.etCustPhone);
        etCustAddress = findViewById(R.id.etCustAddress);
        etCustUsername = findViewById(R.id.etCustUsername);
        etCustPassword = findViewById(R.id.etCustPassword);
        btnCustLogin = findViewById(R.id.btnCustLogin);
        btnToggleRegisterMode = findViewById(R.id.btnToggleRegisterMode);

        // Header
        TextView tvWelcomeCust = findViewById(R.id.tvWelcomeCust);
        tvCustDeliveryLocation = findViewById(R.id.tvCustDeliveryLocation);
        Button btnCustLogout = findViewById(R.id.btnCustLogout);
        Button btnViewActiveTracking = findViewById(R.id.btnViewActiveTracking);

        // Floating Sticky Cart Bar
        cardFloatingCartBar = findViewById(R.id.cardFloatingCartBar);
        tvFloatingCartTotal = findViewById(R.id.tvFloatingCartTotal);
        btnOpenCartModal = findViewById(R.id.btnOpenCartModal);

        // Categories
        lvCategories = findViewById(R.id.lvCategories);
        categoriesAdapter = new CategoriesAdapter(this, categoriesList);
        lvCategories.setAdapter(categoriesAdapter);

        // Shops
        tvChosenCategoryTitle = findViewById(R.id.tvChosenCategoryTitle);
        lvShops = findViewById(R.id.lvShops);
        shopsAdapter = new ShopsAdapter(this, shopsList);
        lvShops.setAdapter(shopsAdapter);
        ImageButton btnBackToCategories = findViewById(R.id.btnBackToCategories);

        // Products
        tvShopDetailTitle = findViewById(R.id.tvShopDetailTitle);
        lvProducts = findViewById(R.id.lvProducts);
        productsAdapter = new ProductsAdapter(this, productsList);
        lvProducts.setAdapter(productsAdapter);
        ImageButton btnBackToShops = findViewById(R.id.btnBackToShops);

        // Tracking
        tvTrackingStatus = findViewById(R.id.tvTrackingStatus);
        tvTrackingDetails = findViewById(R.id.tvTrackingDetails);
        btnRateDriver = findViewById(R.id.btnRateDriver);
        navRadarCust = findViewById(R.id.navRadarCust);
        ImageButton btnBackToCategoriesFromTracking = findViewById(R.id.btnBackToCategoriesFromTracking);

        // --- Auth Mode Toggle (Login vs Register) ---
        btnToggleRegisterMode.setOnClickListener(v -> {
            isRegisterMode = !isRegisterMode;
            if (isRegisterMode) {
                tvAuthTitle.setText("📝 New Customer Registration");
                tvAuthSubtitle.setText("Enter your details and delivery address to create an account");
                layoutRegisterFields.setVisibility(View.VISIBLE);
                btnCustLogin.setText("Register & Create Account");
                btnToggleRegisterMode.setText("Already have an account? Login");
            } else {
                tvAuthTitle.setText("🛍️ Customer Portal");
                tvAuthSubtitle.setText("Login to order from nearby local vendors");
                layoutRegisterFields.setVisibility(View.GONE);
                btnCustLogin.setText("Login to Account");
                btnToggleRegisterMode.setText("New Customer? Register with Delivery Address");
            }
        });

        btnCustLogin.setOnClickListener(v -> {
            String user = etCustUsername.getText().toString().trim();
            String pass = etCustPassword.getText().toString().trim();

            if (isRegisterMode) {
                String fullName = etCustFullName.getText().toString().trim();
                String phone = etCustPhone.getText().toString().trim();
                String address = etCustAddress.getText().toString().trim();

                if (user.isEmpty() || pass.isEmpty() || fullName.isEmpty() || address.isEmpty()) {
                    Toast.makeText(this, "Please fill in all registration fields including delivery address", Toast.LENGTH_SHORT).show();
                    return;
                }

                boolean success = dbHelper.registerCustomer(user, pass, fullName, phone, address, 12.9780, 77.5900);
                if (success) {
                    // Sync to Cloud
                    cloudSyncManager.pushUserToCloud(user, pass, "CUSTOMER", fullName, phone, address, 12.9780, 77.5900);
                    Toast.makeText(this, "Registration Successful! Logging in...", Toast.LENGTH_SHORT).show();
                    isRegisterMode = false;
                    btnToggleRegisterMode.performClick();
                } else {
                    Toast.makeText(this, "Username already taken! Please choose another.", Toast.LENGTH_SHORT).show();
                }
            } else {
                if (user.isEmpty() || pass.isEmpty()) {
                    Toast.makeText(this, "Please enter your username and password", Toast.LENGTH_SHORT).show();
                    return;
                }

                Cursor cursor = dbHelper.loginUser(user, pass, "CUSTOMER");
                if (cursor != null && cursor.moveToFirst()) {
                    currentUserId = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_ID));
                    currentUsername = user;
                    currentUserFullName = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_FULL_NAME));
                    String addr = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_ADDRESS));
                    if (addr != null && !addr.isEmpty()) currentUserAddress = addr;
                    userLatitude = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_LATITUDE));
                    userLongitude = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_LONGITUDE));
                    cursor.close();

                    tvWelcomeCust.setText("Welcome, " + (currentUserFullName != null ? currentUserFullName : currentUsername) + "!");
                    tvCustDeliveryLocation.setText("📍 " + currentUserAddress);
                    showCategoriesScreen();
                    startLiveCloudPolling();
                } else {
                    if (cursor != null) cursor.close();
                    // Fallback to Cloud Auth
                    cloudSyncManager.authenticateUserWithCloud(user, pass, "CUSTOMER", new CloudSyncManager.SyncCallback<Boolean>() {
                        @Override
                        public void onSuccess(Boolean success) {
                            if (success) {
                                Cursor c = dbHelper.loginUser(user, pass, "CUSTOMER");
                                if (c != null && c.moveToFirst()) {
                                    currentUserId = c.getInt(c.getColumnIndexOrThrow(DatabaseHelper.COL_ID));
                                    currentUsername = user;
                                    currentUserFullName = c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COL_FULL_NAME));
                                    String addr = c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COL_ADDRESS));
                                    if (addr != null && !addr.isEmpty()) currentUserAddress = addr;
                                    c.close();

                                    tvWelcomeCust.setText("Welcome, " + (currentUserFullName != null ? currentUserFullName : currentUsername) + "!");
                                    tvCustDeliveryLocation.setText("📍 " + currentUserAddress);
                                    showCategoriesScreen();
                                    startLiveCloudPolling();
                                    Toast.makeText(CustomerActivity.this, "Welcome! Synced from Cloud.", Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                Toast.makeText(CustomerActivity.this, "Invalid credentials! Please register a customer account.", Toast.LENGTH_LONG).show();
                            }
                        }

                        @Override
                        public void onError(String error) {
                            Toast.makeText(CustomerActivity.this, "Login Failed: " + error, Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });

        btnCustLogout.setOnClickListener(v -> {
            stopTrackingTimer();
            stopLiveCloudPolling();
            currentUserId = -1;
            layoutAuth.setVisibility(View.VISIBLE);
            layoutMain.setVisibility(View.GONE);
            cardFloatingCartBar.setVisibility(View.GONE);
        });

        btnViewActiveTracking.setOnClickListener(v -> {
            if (activeTrackingOrderId != -1) {
                showTrackingScreen(activeTrackingOrderId);
            }
        });

        btnBackToCategories.setOnClickListener(v -> showCategoriesScreen());

        btnBackToShops.setOnClickListener(v -> {
            panelCategories.setVisibility(View.GONE);
            panelShops.setVisibility(View.VISIBLE);
            panelProducts.setVisibility(View.GONE);
            panelTracking.setVisibility(View.GONE);
        });

        btnBackToCategoriesFromTracking.setOnClickListener(v -> showCategoriesScreen());

        // Open Floating Cart Dialog
        btnOpenCartModal.setOnClickListener(v -> showCartModalDialog());

        // Category Click Handler
        lvCategories.setOnItemClickListener((parent, view, position, id) -> {
            CategoryModel cat = categoriesList.get(position);
            selectedCategory = cat.name;
            tvChosenCategoryTitle.setText(cat.name);
            loadShopsForCategory(selectedCategory);

            panelCategories.setVisibility(View.GONE);
            panelShops.setVisibility(View.VISIBLE);
            panelProducts.setVisibility(View.GONE);
            panelTracking.setVisibility(View.GONE);
        });

        // Shop Click Handler
        lvShops.setOnItemClickListener((parent, view, position, id) -> {
            ShopModel shop = shopsList.get(position);
            selectedVendorUserId = shop.userId;
            selectedVendorShopName = shop.name;
            selectedVendorLat = shop.lat;
            selectedVendorLng = shop.lng;

            tvShopDetailTitle.setText(selectedVendorShopName);
            loadProductsForShop(selectedVendorUserId);

            panelCategories.setVisibility(View.GONE);
            panelShops.setVisibility(View.GONE);
            panelProducts.setVisibility(View.VISIBLE);
            panelTracking.setVisibility(View.GONE);
        });

        // Rating & Feedback Submission
        btnRateDriver.setOnClickListener(v -> showRatingDialog(activeTrackingOrderId));

        loadCategories();
    }

    private void showCategoriesScreen() {
        layoutAuth.setVisibility(View.GONE);
        layoutMain.setVisibility(View.VISIBLE);
        panelCategories.setVisibility(View.VISIBLE);
        panelShops.setVisibility(View.GONE);
        panelProducts.setVisibility(View.GONE);
        panelTracking.setVisibility(View.GONE);
        checkActiveOrders();
    }

    private void loadCategories() {
        categoriesList.clear();
        categoriesList.add(new CategoryModel("Restaurants & Food", "🍽️", "Pizzas, burgers, sandwiches & hot meals"));
        categoriesList.add(new CategoryModel("Supermarket & Grocery", "🛒", "Daily essentials, dairy, bread, eggs & grains"));
        categoriesList.add(new CategoryModel("Bakery & Sweets", "🥖", "Cakes, pastries, fresh cookies & desserts"));
        categoriesList.add(new CategoryModel("Fresh Fruits & Vegetables", "🍎", "Farm organic vegetables, fresh apples & fruits"));
        categoriesList.add(new CategoryModel("Pharmacy & Healthcare", "💊", "Medicines, first aid, supplements & wellness"));
        categoriesList.add(new CategoryModel("General Stores", "📦", "Household goods, stationary & snacks"));
        categoriesAdapter.notifyDataSetChanged();
    }

    private void loadShopsForCategory(String category) {
        shopsList.clear();
        Cursor cursor = dbHelper.getVendorsByCategory(category);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                int userId = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_VENDOR_USER_ID));
                String name = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_SHOP_NAME));
                double lat = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_VENDOR_LAT));
                double lng = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_VENDOR_LNG));
                String addr = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_VENDOR_ADDRESS));

                float[] results = new float[1];
                Location.distanceBetween(userLatitude, userLongitude, lat, lng, results);
                double distanceKm = results[0] / 1000.0;

                shopsList.add(new ShopModel(userId, name, addr, distanceKm, lat, lng));
            }
            cursor.close();
        }

        if (shopsList.isEmpty()) {
            Cursor allCursor = dbHelper.getAllVendors();
            if (allCursor != null) {
                while (allCursor.moveToNext()) {
                    int userId = allCursor.getInt(allCursor.getColumnIndexOrThrow(DatabaseHelper.COL_VENDOR_USER_ID));
                    String name = allCursor.getString(allCursor.getColumnIndexOrThrow(DatabaseHelper.COL_SHOP_NAME));
                    double lat = allCursor.getDouble(allCursor.getColumnIndexOrThrow(DatabaseHelper.COL_VENDOR_LAT));
                    double lng = allCursor.getDouble(allCursor.getColumnIndexOrThrow(DatabaseHelper.COL_VENDOR_LNG));
                    String addr = allCursor.getString(allCursor.getColumnIndexOrThrow(DatabaseHelper.COL_VENDOR_ADDRESS));

                    float[] results = new float[1];
                    Location.distanceBetween(userLatitude, userLongitude, lat, lng, results);
                    double distanceKm = results[0] / 1000.0;

                    shopsList.add(new ShopModel(userId, name, addr, distanceKm, lat, lng));
                }
                allCursor.close();
            }
        }

        shopsAdapter.notifyDataSetChanged();
    }

    private void loadProductsForShop(int vendorUserId) {
        productsList.clear();
        Cursor cursor = dbHelper.getProductsForVendor(vendorUserId);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_ID));
                String name = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_PROD_NAME));
                double price = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_PROD_PRICE));
                int qty = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_PROD_QUANTITY));
                String imgCode = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_PROD_IMAGE));
                String desc = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_PROD_DESC));

                productsList.add(new ProductModel(id, name, price, qty, imgCode, desc));
            }
            cursor.close();
        }
        productsAdapter.notifyDataSetChanged();
    }

    private void updateCartSummary() {
        int itemsCount = 0;
        double total = 0.0;
        for (Map.Entry<Integer, Integer> entry : cart.entrySet()) {
            int pId = entry.getKey();
            int qty = entry.getValue();
            for (ProductModel p : productsList) {
                if (p.id == pId) {
                    itemsCount += qty;
                    total += (p.price * qty);
                }
            }
        }

        if (itemsCount > 0) {
            cardFloatingCartBar.setVisibility(View.VISIBLE);
            tvFloatingCartTotal.setText("🛒 Cart: " + itemsCount + " items | Total: ₹" + total);
        } else {
            cardFloatingCartBar.setVisibility(View.GONE);
        }
    }

    // --- Interactive Modal Cart Dialog ---
    private void showCartModalDialog() {
        if (cart.isEmpty()) {
            Toast.makeText(this, "Your cart is empty! Add products first.", Toast.LENGTH_SHORT).show();
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("🛒 Your Shopping Cart");

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(30, 20, 30, 20);

        final TextView tvItemsList = new TextView(this);
        tvItemsList.setTextSize(14f);
        tvItemsList.setTextColor(getResources().getColor(R.color.colorTextPrimary));

        StringBuilder sb = new StringBuilder();
        double grandTotal = 0.0;
        final StringBuilder orderItemsStr = new StringBuilder();

        for (Map.Entry<Integer, Integer> entry : cart.entrySet()) {
            int pId = entry.getKey();
            int qty = entry.getValue();
            for (ProductModel p : productsList) {
                if (p.id == pId) {
                    double lineTotal = p.price * qty;
                    grandTotal += lineTotal;
                    sb.append("• ").append(p.name).append(" (x").append(qty).append(") - ₹").append(lineTotal).append("\n");
                    orderItemsStr.append(p.name).append(" (x").append(qty).append("), ");
                }
            }
        }

        sb.append("\n📍 Delivery To: ").append(currentUserAddress);
        sb.append("\n💵 Grand Total: ₹").append(grandTotal);
        tvItemsList.setText(sb.toString());
        layout.addView(tvItemsList);

        final double finalGrandTotal = grandTotal;
        builder.setView(layout);

        builder.setPositiveButton("⚡ Place Order (₹" + finalGrandTotal + ")", (dialog, which) -> {
            long orderId = dbHelper.placeOrder(currentUserId, selectedVendorUserId, orderItemsStr.toString(), finalGrandTotal, currentUserAddress, userLatitude, userLongitude);
            if (orderId != -1) {
                cloudSyncManager.pushOrderToCloud((int) orderId, currentUserId, selectedVendorUserId, orderItemsStr.toString(), finalGrandTotal, currentUserAddress, userLatitude, userLongitude);

                Toast.makeText(this, "🎉 Order Placed Successfully! ID: #" + orderId, Toast.LENGTH_LONG).show();
                cart.clear();
                updateCartSummary();
                activeTrackingOrderId = (int) orderId;
                findViewById(R.id.btnViewActiveTracking).setVisibility(View.VISIBLE);
                showTrackingScreen(activeTrackingOrderId);
            } else {
                Toast.makeText(this, "Failed to place order. Please try again.", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Clear Cart", (dialog, which) -> {
            cart.clear();
            updateCartSummary();
            Toast.makeText(this, "Cart cleared", Toast.LENGTH_SHORT).show();
        });

        builder.setNeutralButton("Keep Shopping", null);
        builder.show();
    }

    // --- Real-time Order Tracking & Radar Updating ---

    private void startLiveCloudPolling() {
        stopLiveCloudPolling();
        cloudPollTimer = new Timer();
        cloudPollTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                cloudSyncManager.pullOrdersFromCloud(new CloudSyncManager.SyncCallback<Boolean>() {
                    @Override
                    public void onSuccess(Boolean result) {
                        runOnUiThread(() -> checkActiveOrders());
                    }

                    @Override
                    public void onError(String error) {
                    }
                });
            }
        }, 0, 3000);
    }

    private void stopLiveCloudPolling() {
        if (cloudPollTimer != null) {
            cloudPollTimer.cancel();
            cloudPollTimer = null;
        }
    }

    private void checkActiveOrders() {
        Cursor cursor = dbHelper.getOrdersForCustomer(currentUserId);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                int oId = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_ID));
                String status = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_ORDER_STATUS));

                if (oId == activeTrackingOrderId && !status.equals(lastKnownOrderStatus) && !lastKnownOrderStatus.isEmpty()) {
                    showStatusChangeNotification(status);
                }
                lastKnownOrderStatus = status;

                activeTrackingOrderId = oId;
                findViewById(R.id.btnViewActiveTracking).setVisibility(View.VISIBLE);
            }
            cursor.close();
        }
    }

    private void showStatusChangeNotification(String status) {
        String msg = "Your order status is now: " + status;
        if ("ACCEPTED".equalsIgnoreCase(status)) msg = "🎉 Great news! The vendor has accepted your order.";
        if ("REJECTED".equalsIgnoreCase(status)) msg = "❌ Sorry, the vendor could not accept your order at this time.";
        if ("ASSIGNED".equalsIgnoreCase(status)) msg = "🛵 A nearby delivery partner has been assigned to your order.";
        if ("DELIVERING".equalsIgnoreCase(status)) msg = "🚀 Your order is out for delivery! Live tracking available.";
        if ("DELIVERED".equalsIgnoreCase(status)) msg = "✅ Your order has been delivered! Please rate your delivery partner.";

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Order Status Update");
        builder.setMessage(msg);
        builder.setPositiveButton("OK", null);
        builder.show();
    }

    private void showTrackingScreen(int orderId) {
        panelCategories.setVisibility(View.GONE);
        panelShops.setVisibility(View.GONE);
        panelProducts.setVisibility(View.GONE);
        panelTracking.setVisibility(View.VISIBLE);

        startTrackingTimer(orderId);
    }

    private void startTrackingTimer(int orderId) {
        stopTrackingTimer();
        trackingTimer = new Timer();
        trackingTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(() -> refreshTrackingMap(orderId));
            }
        }, 0, 2000);
    }

    private void stopTrackingTimer() {
        if (trackingTimer != null) {
            trackingTimer.cancel();
            trackingTimer = null;
        }
    }

    private void refreshTrackingMap(int orderId) {
        Cursor cursor = dbHelper.getOrderDetails(orderId);
        if (cursor != null && cursor.moveToFirst()) {
            String status = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_ORDER_STATUS));
            String items = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_ORDER_ITEMS));
            double total = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_ORDER_TOTAL));
            int delivPartnerId = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_ORDER_DELIVERY_ID));
            int rating = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_ORDER_RATING));
            cursor.close();

            tvTrackingStatus.setText("Status: " + status);
            tvTrackingDetails.setText("📋 Order #" + orderId + " | Status: " + status + "\n" +
                    "🛒 Items: " + items + "\n" +
                    "💵 Total: ₹" + total);

            if ("DELIVERED".equalsIgnoreCase(status)) {
                btnRateDriver.setVisibility(rating == 0 ? View.VISIBLE : View.GONE);
                if (rating > 0) {
                    tvTrackingDetails.append("\n⭐ You rated this delivery " + rating + "/5 stars!");
                }
                currentSimProgress = 1.0;
            } else if ("DELIVERING".equalsIgnoreCase(status)) {
                btnRateDriver.setVisibility(View.GONE);
                if (currentSimProgress < 0.95) currentSimProgress += 0.1;
            } else if ("ASSIGNED".equalsIgnoreCase(status)) {
                currentSimProgress = 0.2;
            } else {
                currentSimProgress = 0.0;
            }

            String driverName = "Driver Assigned";
            double driverLat = selectedVendorLat;
            double driverLng = selectedVendorLng;

            if (delivPartnerId > 0) {
                Cursor dCursor = dbHelper.getUserById(delivPartnerId);
                if (dCursor != null && dCursor.moveToFirst()) {
                    driverLat = dCursor.getDouble(dCursor.getColumnIndexOrThrow(DatabaseHelper.COL_LATITUDE));
                    driverLng = dCursor.getDouble(dCursor.getColumnIndexOrThrow(DatabaseHelper.COL_LONGITUDE));
                    driverName = dCursor.getString(dCursor.getColumnIndexOrThrow(DatabaseHelper.COL_USERNAME));
                    dCursor.close();
                }
            }

            navRadarCust.updateLocations(
                    selectedVendorLat, selectedVendorLng, selectedVendorShopName,
                    userLatitude, userLongitude, currentUserAddress,
                    driverLat, driverLng, driverName,
                    status, currentSimProgress
            );
        } else {
            if (cursor != null) cursor.close();
            stopTrackingTimer();
        }
    }

    private void showRatingDialog(int orderId) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("⭐ Rate & Review Delivery Partner");

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(40, 20, 40, 20);

        final RatingBar ratingBar = new RatingBar(this);
        ratingBar.setNumStars(5);
        ratingBar.setStepSize(1.0f);
        ratingBar.setRating(5.0f);
        layout.addView(ratingBar);

        final EditText etFeedback = new EditText(this);
        etFeedback.setHint("Write feedback (e.g. Fast delivery, friendly driver!)");
        etFeedback.setBackgroundResource(R.drawable.bg_edit_text);
        etFeedback.setPadding(20, 20, 20, 20);
        layout.addView(etFeedback);

        builder.setView(layout);

        builder.setPositiveButton("Submit Review", (dialog, which) -> {
            int rating = (int) ratingBar.getRating();
            String feedback = etFeedback.getText().toString().trim();

            dbHelper.submitOrderRating(orderId, rating, feedback);
            cloudSyncManager.submitRatingToCloud(orderId, rating, feedback);
            Toast.makeText(this, "Thank you for your rating & feedback!", Toast.LENGTH_LONG).show();
            btnRateDriver.setVisibility(View.GONE);
        });

        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopTrackingTimer();
        stopLiveCloudPolling();
    }

    // --- Models & Adapters ---

    private static class CategoryModel {
        String name;
        String icon;
        String desc;

        CategoryModel(String name, String icon, String desc) {
            this.name = name;
            this.icon = icon;
            this.desc = desc;
        }
    }

    private class CategoriesAdapter extends ArrayAdapter<CategoryModel> {
        CategoriesAdapter(Context context, List<CategoryModel> list) {
            super(context, 0, list);
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_category, parent, false);
            }
            CategoryModel cat = getItem(position);
            TextView tvIcon = convertView.findViewById(R.id.tvCatIcon);
            TextView tvName = convertView.findViewById(R.id.tvCatName);
            TextView tvDesc = convertView.findViewById(R.id.tvCatDesc);

            if (cat != null) {
                tvIcon.setText(cat.icon);
                tvName.setText(cat.name);
                tvDesc.setText(cat.desc);
            }
            return convertView;
        }
    }

    private static class ShopModel {
        int userId;
        String name;
        String address;
        double distanceKm;
        double lat, lng;

        ShopModel(int userId, String name, String address, double distanceKm, double lat, double lng) {
            this.userId = userId;
            this.name = name;
            this.address = address;
            this.distanceKm = distanceKm;
            this.lat = lat;
            this.lng = lng;
        }
    }

    private class ShopsAdapter extends ArrayAdapter<ShopModel> {
        ShopsAdapter(Context context, List<ShopModel> list) {
            super(context, 0, list);
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_shop, parent, false);
            }
            ShopModel shop = getItem(position);
            TextView tvName = convertView.findViewById(R.id.tvShopName);
            TextView tvAddress = convertView.findViewById(R.id.tvShopAddress);
            TextView tvDistance = convertView.findViewById(R.id.tvShopDistance);

            if (shop != null) {
                tvName.setText(shop.name);
                tvAddress.setText(shop.address);
                tvDistance.setText("📍 " + String.format("%.2f", shop.distanceKm) + " km away");
            }
            return convertView;
        }
    }

    private static class ProductModel {
        int id;
        String name;
        double price;
        int quantity;
        String imageCode;
        String description;

        ProductModel(int id, String name, double price, int quantity, String imageCode, String description) {
            this.id = id;
            this.name = name;
            this.price = price;
            this.quantity = quantity;
            this.imageCode = imageCode;
            this.description = description;
        }
    }

    private class ProductsAdapter extends ArrayAdapter<ProductModel> {
        ProductsAdapter(Context context, List<ProductModel> list) {
            super(context, 0, list);
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_product, parent, false);
            }
            ProductModel p = getItem(position);
            TextView tvEmoji = convertView.findViewById(R.id.tvProdEmoji);
            TextView tvName = convertView.findViewById(R.id.tvProdName);
            TextView tvDesc = convertView.findViewById(R.id.tvProdDesc);
            TextView tvPrice = convertView.findViewById(R.id.tvProdPrice);
            TextView tvStock = convertView.findViewById(R.id.tvProdStock);
            Button btnAdd = convertView.findViewById(R.id.btnAddToCart);

            if (p != null) {
                tvEmoji.setText(ProductVisualHelper.getEmojiForImageCode(p.imageCode));
                tvName.setText(p.name);
                tvDesc.setText(p.description);
                tvPrice.setText("₹" + p.price);
                tvStock.setText("• Stock: " + p.quantity);

                btnAdd.setOnClickListener(v -> {
                    int currentQty = cart.containsKey(p.id) ? cart.get(p.id) : 0;
                    cart.put(p.id, currentQty + 1);
                    updateCartSummary();
                    Toast.makeText(getContext(), "Added " + p.name + " to cart! Tap 'View Cart' below to checkout.", Toast.LENGTH_SHORT).show();
                });
            }
            return convertView;
        }
    }
}
