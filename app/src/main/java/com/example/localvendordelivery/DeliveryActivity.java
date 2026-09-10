package com.example.localvendordelivery;

import android.app.AlertDialog;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class DeliveryActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;
    private CloudSyncManager cloudSyncManager;
    private int currentDeliveryId = -1;
    private String currentDeliveryName = "";
    private double currentLat = 12.9710;
    private double currentLng = 77.5960;

    private View layoutAuthDelivery, layoutMainDelivery;
    private View panelDelivOrders, panelDelivNavigation;

    private EditText etDelivUsername, etDelivPassword;
    private ListView lvDelivOrders;

    private List<OrderModel> deliveryOrders = new ArrayList<>();
    private DeliveryOrdersAdapter ordersAdapter;

    private LiveNavRadarView navRadarDeliv;
    private OrderModel activeNavigationOrder;

    // Simulation fields
    private Timer simulationTimer;
    private Timer cloudPollTimer;
    private int simStep = 0;
    private final int TOTAL_SIM_STEPS = 10;
    private double startSimLat, startSimLng;
    private double destSimLat, destSimLng;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delivery);

        dbHelper = new DatabaseHelper(this);
        cloudSyncManager = CloudSyncManager.getInstance(this);

        layoutAuthDelivery = findViewById(R.id.layoutAuthDelivery);
        layoutMainDelivery = findViewById(R.id.layoutMainDelivery);
        panelDelivOrders = findViewById(R.id.panelDelivOrders);
        panelDelivNavigation = findViewById(R.id.panelDelivNavigation);

        etDelivUsername = findViewById(R.id.etDelivUsername);
        etDelivPassword = findViewById(R.id.etDelivPassword);
        Button btnDelivLogin = findViewById(R.id.btnDelivLogin);

        TextView tvWelcomeDeliv = findViewById(R.id.tvWelcomeDeliv);
        Button btnDelivLogout = findViewById(R.id.btnDelivLogout);
        Button btnViewDriverFeedback = findViewById(R.id.btnViewDriverFeedback);

        lvDelivOrders = findViewById(R.id.lvDelivOrders);
        ordersAdapter = new DeliveryOrdersAdapter(this, deliveryOrders);
        lvDelivOrders.setAdapter(ordersAdapter);

        navRadarDeliv = findViewById(R.id.navRadarDeliv);

        ImageButton btnBackToDelivList = findViewById(R.id.btnBackToDelivList);
        Button btnSimulateMove = findViewById(R.id.btnSimulateMove);
        Button btnMarkDelivered = findViewById(R.id.btnMarkDelivered);

        btnDelivLogin.setOnClickListener(v -> {
            String user = etDelivUsername.getText().toString().trim();
            String pass = etDelivPassword.getText().toString().trim();
            if (user.isEmpty() || pass.isEmpty()) {
                Toast.makeText(this, "Please enter your driver username and password", Toast.LENGTH_SHORT).show();
                return;
            }

            Cursor cursor = dbHelper.loginUser(user, pass, "DELIVERY");
            if (cursor != null && cursor.moveToFirst()) {
                currentDeliveryId = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_ID));
                currentDeliveryName = user;
                currentLat = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_LATITUDE));
                currentLng = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_LONGITUDE));
                cursor.close();

                tvWelcomeDeliv.setText("Delivery Driver: " + currentDeliveryName);
                layoutAuthDelivery.setVisibility(View.GONE);
                layoutMainDelivery.setVisibility(View.VISIBLE);
                panelDelivOrders.setVisibility(View.VISIBLE);
                panelDelivNavigation.setVisibility(View.GONE);
                loadAssignedOrders();
                startLiveCloudPolling();
            } else {
                if (cursor != null) cursor.close();
                // Check Cloud for driver created on another phone
                cloudSyncManager.authenticateUserWithCloud(user, pass, "DELIVERY", new CloudSyncManager.SyncCallback<Boolean>() {
                    @Override
                    public void onSuccess(Boolean success) {
                        if (success) {
                            Cursor c = dbHelper.loginUser(user, pass, "DELIVERY");
                            if (c != null && c.moveToFirst()) {
                                currentDeliveryId = c.getInt(c.getColumnIndexOrThrow(DatabaseHelper.COL_ID));
                                currentDeliveryName = user;
                                currentLat = c.getDouble(c.getColumnIndexOrThrow(DatabaseHelper.COL_LATITUDE));
                                currentLng = c.getDouble(c.getColumnIndexOrThrow(DatabaseHelper.COL_LONGITUDE));
                                c.close();

                                tvWelcomeDeliv.setText("Delivery Driver: " + currentDeliveryName);
                                layoutAuthDelivery.setVisibility(View.GONE);
                                layoutMainDelivery.setVisibility(View.VISIBLE);
                                panelDelivOrders.setVisibility(View.VISIBLE);
                                panelDelivNavigation.setVisibility(View.GONE);
                                loadAssignedOrders();
                                startLiveCloudPolling();
                                Toast.makeText(DeliveryActivity.this, "Welcome! Synced from Cloud.", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(DeliveryActivity.this, "Login Failed! Driver credentials must be created by Admin.", Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onError(String error) {
                        Toast.makeText(DeliveryActivity.this, "Login Error: " + error, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        btnDelivLogout.setOnClickListener(v -> {
            stopSimulation();
            stopLiveCloudPolling();
            currentDeliveryId = -1;
            layoutAuthDelivery.setVisibility(View.VISIBLE);
            layoutMainDelivery.setVisibility(View.GONE);
        });

        btnBackToDelivList.setOnClickListener(v -> {
            stopSimulation();
            panelDelivOrders.setVisibility(View.VISIBLE);
            panelDelivNavigation.setVisibility(View.GONE);
            loadAssignedOrders();
        });

        // Driver Feedback Viewer Dialog
        btnViewDriverFeedback.setOnClickListener(v -> showFeedbackDialog());

        btnSimulateMove.setOnClickListener(v -> {
            if (activeNavigationOrder == null) return;
            startGPSRouteSimulation();
        });

        btnMarkDelivered.setOnClickListener(v -> {
            if (activeNavigationOrder == null) return;
            dbHelper.updateOrderStatus(activeNavigationOrder.id, "DELIVERED");
            cloudSyncManager.updateOrderStatusOnCloud(activeNavigationOrder.id, "DELIVERED", currentDeliveryId);
            Toast.makeText(this, "Order #" + activeNavigationOrder.id + " Delivered Successfully!", Toast.LENGTH_LONG).show();
            stopSimulation();
            panelDelivOrders.setVisibility(View.VISIBLE);
            panelDelivNavigation.setVisibility(View.GONE);
            loadAssignedOrders();
        });
    }

    private void showFeedbackDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("⭐ My Customer Ratings & Reviews");

        StringBuilder sb = new StringBuilder();
        int totalReviews = 0;
        int ratingSum = 0;

        Cursor cursor = dbHelper.getReadableDatabase().rawQuery("SELECT * FROM " + DatabaseHelper.TABLE_ORDERS + " WHERE " + DatabaseHelper.COL_ORDER_DELIVERY_ID + " = " + currentDeliveryId + " AND " + DatabaseHelper.COL_ORDER_RATING + " > 0", null);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                totalReviews++;
                int id = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_ID));
                int rating = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_ORDER_RATING));
                String feedback = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_ORDER_FEEDBACK));
                ratingSum += rating;

                StringBuilder stars = new StringBuilder();
                for (int s = 0; s < rating; s++) stars.append("⭐");

                sb.append("Order #").append(id).append(": ").append(stars).append(" (").append(rating).append("/5)\n");
                if (feedback != null && !feedback.isEmpty()) {
                    sb.append("💬 \"").append(feedback).append("\"\n");
                }
                sb.append("\n");
            }
            cursor.close();
        }

        if (totalReviews == 0) {
            builder.setMessage("No customer reviews received yet. Complete delivered orders to receive ratings!");
        } else {
            double avg = (double) ratingSum / totalReviews;
            String header = "📊 Overall Rating: " + String.format("%.1f", avg) + " / 5.0 (" + totalReviews + " ratings)\n\n";
            builder.setMessage(header + sb.toString().trim());
        }

        builder.setPositiveButton("Close", null);
        builder.show();
    }

    private void startLiveCloudPolling() {
        stopLiveCloudPolling();
        cloudPollTimer = new Timer();
        cloudPollTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                cloudSyncManager.pullOrdersFromCloud(new CloudSyncManager.SyncCallback<Boolean>() {
                    @Override
                    public void onSuccess(Boolean result) {
                        runOnUiThread(() -> loadAssignedOrders());
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

    private void loadAssignedOrders() {
        deliveryOrders.clear();
        Cursor cursor = dbHelper.getOrdersForDelivery(currentDeliveryId);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_ID));
                int custId = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_ORDER_CUSTOMER_ID));
                int vendorId = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_ORDER_VENDOR_ID));
                String status = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_ORDER_STATUS));
                String items = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_ORDER_ITEMS));
                double total = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_ORDER_TOTAL));
                double custLat = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_CUST_LAT));
                double custLng = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_CUST_LNG));

                deliveryOrders.add(new OrderModel(id, custId, vendorId, status, items, total, custLat, custLng));
            }
            cursor.close();
        }
        ordersAdapter.notifyDataSetChanged();
    }

    private void openNavigation(OrderModel order) {
        activeNavigationOrder = order;
        panelDelivOrders.setVisibility(View.GONE);
        panelDelivNavigation.setVisibility(View.VISIBLE);

        double vendorLat = 12.9716;
        double vendorLng = 77.5946;
        String shopName = "Vendor Shop";
        Cursor vCursor = dbHelper.getReadableDatabase().rawQuery("SELECT * FROM " + DatabaseHelper.TABLE_VENDORS + " WHERE " + DatabaseHelper.COL_VENDOR_USER_ID + " = " + order.vendorId, null);
        if (vCursor != null && vCursor.moveToFirst()) {
            vendorLat = vCursor.getDouble(vCursor.getColumnIndexOrThrow(DatabaseHelper.COL_VENDOR_LAT));
            vendorLng = vCursor.getDouble(vCursor.getColumnIndexOrThrow(DatabaseHelper.COL_VENDOR_LNG));
            shopName = vCursor.getString(vCursor.getColumnIndexOrThrow(DatabaseHelper.COL_SHOP_NAME));
            vCursor.close();
        }

        startSimLat = vendorLat;
        startSimLng = vendorLng;
        destSimLat = order.custLat != 0.0 ? order.custLat : 12.9780;
        destSimLng = order.custLng != 0.0 ? order.custLng : 77.5900;

        navRadarDeliv.updateLocations(
                vendorLat, vendorLng, shopName,
                destSimLat, destSimLng, "Customer Delivery Location",
                currentLat, currentLng, "You (" + currentDeliveryName + ")",
                order.status, 0.0
        );
    }

    private void startGPSRouteSimulation() {
        stopSimulation();
        simStep = 0;
        simulationTimer = new Timer();
        simulationTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(() -> {
                    if (simStep <= TOTAL_SIM_STEPS) {
                        double fraction = (double) simStep / TOTAL_SIM_STEPS;
                        currentLat = startSimLat + fraction * (destSimLat - startSimLat);
                        currentLng = startSimLng + fraction * (destSimLng - startSimLng);

                        dbHelper.updateDeliveryLocation(currentDeliveryId, currentLat, currentLng);
                        cloudSyncManager.pushDriverLocationToCloud(currentDeliveryId, currentLat, currentLng);

                        navRadarDeliv.updateLocations(
                                startSimLat, startSimLng, "Vendor Shop",
                                destSimLat, destSimLng, "Customer Delivery Address",
                                currentLat, currentLng, "You (" + currentDeliveryName + ")",
                                "DELIVERING", fraction
                        );

                        Toast.makeText(DeliveryActivity.this, "Simulating route step " + simStep + "/" + TOTAL_SIM_STEPS, Toast.LENGTH_SHORT).show();
                        simStep++;
                    } else {
                        stopSimulation();
                        Toast.makeText(DeliveryActivity.this, "Arrived at Customer location! Tap 'Mark Delivered'.", Toast.LENGTH_LONG).show();
                    }
                });
            }
        }, 0, 2000);
    }

    private void stopSimulation() {
        if (simulationTimer != null) {
            simulationTimer.cancel();
            simulationTimer = null;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopSimulation();
        stopLiveCloudPolling();
    }

    // --- Inner Models & Adapters ---

    private static class OrderModel {
        int id;
        int custId;
        int vendorId;
        String status;
        String items;
        double total;
        double custLat, custLng;

        OrderModel(int id, int custId, int vendorId, String status, String items, double total, double custLat, double custLng) {
            this.id = id;
            this.custId = custId;
            this.vendorId = vendorId;
            this.status = status;
            this.items = items;
            this.total = total;
            this.custLat = custLat;
            this.custLng = custLng;
        }
    }

    private class DeliveryOrdersAdapter extends ArrayAdapter<OrderModel> {
        DeliveryOrdersAdapter(Context context, List<OrderModel> list) {
            super(context, 0, list);
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_delivery_order, parent, false);
            }

            OrderModel order = getItem(position);
            TextView tvDelivOrderId = convertView.findViewById(R.id.tvDelivOrderId);
            TextView tvDelivOrderStatus = convertView.findViewById(R.id.tvDelivOrderStatus);
            TextView tvDelivOrderItems = convertView.findViewById(R.id.tvDelivOrderItems);
            TextView tvDelivOrderTotal = convertView.findViewById(R.id.tvDelivOrderTotal);

            if (order != null) {
                tvDelivOrderId.setText("Task #" + order.id);
                tvDelivOrderStatus.setText(order.status);
                tvDelivOrderItems.setText("Items: " + order.items);
                tvDelivOrderTotal.setText("Total: ₹" + order.total);

                if ("DELIVERING".equalsIgnoreCase(order.status)) {
                    tvDelivOrderStatus.setBackgroundResource(R.drawable.bg_chip_orange);
                } else if ("DELIVERED".equalsIgnoreCase(order.status)) {
                    tvDelivOrderStatus.setBackgroundResource(R.drawable.bg_chip_green);
                } else {
                    tvDelivOrderStatus.setBackgroundResource(R.drawable.bg_chip_blue);
                }

                convertView.setOnClickListener(v -> {
                    if (order.status.equals("ASSIGNED")) {
                        dbHelper.updateOrderStatus(order.id, "DELIVERING");
                        cloudSyncManager.updateOrderStatusOnCloud(order.id, "DELIVERING", currentDeliveryId);
                        Toast.makeText(getContext(), "Task Accepted! Route navigation open.", Toast.LENGTH_SHORT).show();
                        loadAssignedOrders();
                        openNavigation(order);
                    } else if (order.status.equals("DELIVERING")) {
                        openNavigation(order);
                    } else {
                        Toast.makeText(getContext(), "Task is " + order.status, Toast.LENGTH_SHORT).show();
                    }
                });
            }

            return convertView;
        }
    }
}
