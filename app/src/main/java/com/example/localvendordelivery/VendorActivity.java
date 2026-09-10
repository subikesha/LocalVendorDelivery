package com.example.localvendordelivery;

import android.app.AlertDialog;
import android.content.Context;
import android.database.Cursor;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
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

public class VendorActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;
    private CloudSyncManager cloudSyncManager;
    private int currentVendorUserId = -1;
    private String currentVendorUsername = "";
    private double vendorLat = 12.9716;
    private double vendorLng = 77.5946;

    private View layoutAuthVendor, layoutMainVendor;
    private EditText etVendorUsername, etVendorPassword;
    private ListView lvVendorOrders;

    private List<OrderModel> ordersList = new ArrayList<>();
    private OrdersAdapter ordersAdapter;

    private Timer cloudPollTimer;
    private int lastKnownOrderCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vendor);

        dbHelper = new DatabaseHelper(this);
        cloudSyncManager = CloudSyncManager.getInstance(this);

        layoutAuthVendor = findViewById(R.id.layoutAuthVendor);
        layoutMainVendor = findViewById(R.id.layoutMainVendor);

        etVendorUsername = findViewById(R.id.etVendorUsername);
        etVendorPassword = findViewById(R.id.etVendorPassword);
        Button btnVendorLogin = findViewById(R.id.btnVendorLogin);

        TextView tvWelcomeVendor = findViewById(R.id.tvWelcomeVendor);
        Button btnVendorLogout = findViewById(R.id.btnVendorLogout);

        lvVendorOrders = findViewById(R.id.lvVendorOrders);
        ordersAdapter = new OrdersAdapter(this, ordersList);
        lvVendorOrders.setAdapter(ordersAdapter);

        btnVendorLogin.setOnClickListener(v -> {
            String user = etVendorUsername.getText().toString().trim();
            String pass = etVendorPassword.getText().toString().trim();
            if (user.isEmpty() || pass.isEmpty()) {
                Toast.makeText(this, "Please enter your vendor credentials", Toast.LENGTH_SHORT).show();
                return;
            }

            Cursor cursor = dbHelper.loginUser(user, pass, "VENDOR");
            if (cursor != null && cursor.moveToFirst()) {
                currentVendorUserId = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_ID));
                currentVendorUsername = user;
                vendorLat = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_LATITUDE));
                vendorLng = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_LONGITUDE));
                cursor.close();

                tvWelcomeVendor.setText("Shop Dashboard (" + currentVendorUsername + ")");
                layoutAuthVendor.setVisibility(View.GONE);
                layoutMainVendor.setVisibility(View.VISIBLE);
                loadOrders();
                startLiveCloudPolling();
            } else {
                if (cursor != null) cursor.close();
                // Check Cloud for newly created vendor credentials
                cloudSyncManager.authenticateUserWithCloud(user, pass, "VENDOR", new CloudSyncManager.SyncCallback<Boolean>() {
                    @Override
                    public void onSuccess(Boolean success) {
                        if (success) {
                            Cursor c = dbHelper.loginUser(user, pass, "VENDOR");
                            if (c != null && c.moveToFirst()) {
                                currentVendorUserId = c.getInt(c.getColumnIndexOrThrow(DatabaseHelper.COL_ID));
                                currentVendorUsername = user;
                                vendorLat = c.getDouble(c.getColumnIndexOrThrow(DatabaseHelper.COL_LATITUDE));
                                vendorLng = c.getDouble(c.getColumnIndexOrThrow(DatabaseHelper.COL_LONGITUDE));
                                c.close();

                                tvWelcomeVendor.setText("Shop Dashboard (" + currentVendorUsername + ")");
                                layoutAuthVendor.setVisibility(View.GONE);
                                layoutMainVendor.setVisibility(View.VISIBLE);
                                loadOrders();
                                startLiveCloudPolling();
                                Toast.makeText(VendorActivity.this, "Welcome! Synced from Cloud.", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(VendorActivity.this, "Invalid credentials! Contact Admin to create a vendor account.", Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onError(String error) {
                        Toast.makeText(VendorActivity.this, "Login Failed: " + error, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        btnVendorLogout.setOnClickListener(v -> {
            stopLiveCloudPolling();
            currentVendorUserId = -1;
            layoutAuthVendor.setVisibility(View.VISIBLE);
            layoutMainVendor.setVisibility(View.GONE);
        });
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
                        runOnUiThread(() -> loadOrders());
                    }

                    @Override
                    public void onError(String error) {
                    }
                });
            }
        }, 0, 3000); // Sync cloud orders every 3 seconds
    }

    private void stopLiveCloudPolling() {
        if (cloudPollTimer != null) {
            cloudPollTimer.cancel();
            cloudPollTimer = null;
        }
    }

    private void loadOrders() {
        ordersList.clear();
        Cursor cursor = dbHelper.getOrdersForVendor(currentVendorUserId);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_ID));
                int custId = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_ORDER_CUSTOMER_ID));
                int delivId = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_ORDER_DELIVERY_ID));
                String status = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_ORDER_STATUS));
                String items = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_ORDER_ITEMS));
                double total = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_ORDER_TOTAL));

                ordersList.add(new OrderModel(id, custId, delivId, status, items, total));
            }
            cursor.close();
        }

        // Notify if new order arrived
        if (ordersList.size() > lastKnownOrderCount && lastKnownOrderCount != 0) {
            Toast.makeText(this, "🔔 New Customer Order Received!", Toast.LENGTH_LONG).show();
        }
        lastKnownOrderCount = ordersList.size();

        ordersAdapter.notifyDataSetChanged();
    }

    private void showAssignDeliveryDialog(int orderId) {
        List<DeliveryPartnerModel> partners = new ArrayList<>();
        Cursor cursor = dbHelper.getOnlineDeliveryPartners();
        if (cursor != null) {
            while (cursor.moveToNext()) {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_ID));
                String name = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_USERNAME));
                String fullName = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_FULL_NAME));
                double lat = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_LATITUDE));
                double lng = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_LONGITUDE));

                float[] distResults = new float[1];
                Location.distanceBetween(vendorLat, vendorLng, lat, lng, distResults);
                double distanceKm = distResults[0] / 1000.0;

                String display = (fullName != null ? fullName : name) + " (@" + name + ")";
                partners.add(new DeliveryPartnerModel(id, display, distanceKm));
            }
            cursor.close();
        }

        if (partners.isEmpty()) {
            Toast.makeText(this, "No online delivery partners registered in system! Ask Admin to add drivers.", Toast.LENGTH_LONG).show();
            return;
        }

        // Sort by distance (closest first)
        partners.sort((p1, p2) -> Double.compare(p1.distanceKm, p2.distanceKm));

        String[] partnerNames = new String[partners.size()];
        for (int i = 0; i < partners.size(); i++) {
            partnerNames[i] = partners.get(i).name + " (" + String.format("%.2f", partners.get(i).distanceKm) + " km away)";
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("🛵 Assign Nearby Delivery Partner");
        builder.setItems(partnerNames, (dialog, which) -> {
            int selectedPartnerId = partners.get(which).id;
            boolean assigned = dbHelper.assignDeliveryPartner(orderId, selectedPartnerId);
            if (assigned) {
                cloudSyncManager.updateOrderStatusOnCloud(orderId, "ASSIGNED", selectedPartnerId);
                Toast.makeText(this, "Order #" + orderId + " Assigned to " + partners.get(which).name, Toast.LENGTH_SHORT).show();
                loadOrders();
            } else {
                Toast.makeText(this, "Failed to assign partner", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopLiveCloudPolling();
    }

    // --- Inner Models & Adapters ---

    private static class OrderModel {
        int id;
        int custId;
        int delivId;
        String status;
        String items;
        double total;

        OrderModel(int id, int custId, int delivId, String status, String items, double total) {
            this.id = id;
            this.custId = custId;
            this.delivId = delivId;
            this.status = status;
            this.items = items;
            this.total = total;
        }
    }

    private static class DeliveryPartnerModel {
        int id;
        String name;
        double distanceKm;

        DeliveryPartnerModel(int id, String name, double distanceKm) {
            this.id = id;
            this.name = name;
            this.distanceKm = distanceKm;
        }
    }

    private class OrdersAdapter extends ArrayAdapter<OrderModel> {
        OrdersAdapter(Context context, List<OrderModel> list) {
            super(context, 0, list);
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_vendor_order, parent, false);
            }

            OrderModel order = getItem(position);
            TextView tvOrderId = convertView.findViewById(R.id.tvOrderId);
            TextView tvOrderStatus = convertView.findViewById(R.id.tvOrderStatus);
            TextView tvOrderItems = convertView.findViewById(R.id.tvOrderItems);
            TextView tvOrderTotal = convertView.findViewById(R.id.tvOrderTotal);

            if (order != null) {
                tvOrderId.setText("Order #" + order.id);
                tvOrderStatus.setText(order.status);
                tvOrderItems.setText("Items: " + order.items);
                tvOrderTotal.setText("Total: ₹" + order.total);

                if ("ACCEPTED".equalsIgnoreCase(order.status) || "DELIVERING".equalsIgnoreCase(order.status)) {
                    tvOrderStatus.setBackgroundResource(R.drawable.bg_chip_blue);
                } else if ("DELIVERED".equalsIgnoreCase(order.status)) {
                    tvOrderStatus.setBackgroundResource(R.drawable.bg_chip_green);
                } else {
                    tvOrderStatus.setBackgroundResource(R.drawable.bg_chip_orange);
                }

                convertView.setOnClickListener(v -> {
                    if (order.status.equals("PENDING")) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                        builder.setTitle("🔔 New Order Request #" + order.id);
                        builder.setMessage("Items: " + order.items + "\nTotal: ₹" + order.total);
                        builder.setPositiveButton("Accept Order", (dialog, which) -> {
                            dbHelper.updateOrderStatus(order.id, "ACCEPTED");
                            cloudSyncManager.updateOrderStatusOnCloud(order.id, "ACCEPTED", 0);
                            Toast.makeText(getContext(), "Order Accepted!", Toast.LENGTH_SHORT).show();
                            loadOrders();
                            showAssignDeliveryDialog(order.id);
                        });
                        builder.setNegativeButton("Reject Order", (dialog, which) -> {
                            dbHelper.updateOrderStatus(order.id, "REJECTED");
                            cloudSyncManager.updateOrderStatusOnCloud(order.id, "REJECTED", 0);
                            Toast.makeText(getContext(), "Order Rejected", Toast.LENGTH_SHORT).show();
                            loadOrders();
                        });
                        builder.show();
                    } else if (order.status.equals("ACCEPTED")) {
                        showAssignDeliveryDialog(order.id);
                    } else {
                        Toast.makeText(getContext(), "Order is currently " + order.status, Toast.LENGTH_SHORT).show();
                    }
                });
            }

            return convertView;
        }
    }
}
