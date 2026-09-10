package com.example.localvendordelivery;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "LocalVendorDelivery_v3.db";
    private static final int DATABASE_VERSION = 3;

    // Table names
    public static final String TABLE_USERS = "users";
    public static final String TABLE_VENDORS = "vendors";
    public static final String TABLE_PRODUCTS = "products";
    public static final String TABLE_ORDERS = "orders";
    public static final String TABLE_FEEDBACK = "feedback";

    // Common column
    public static final String COL_ID = "id";

    // Users Table Columns
    public static final String COL_USERNAME = "username";
    public static final String COL_PASSWORD = "password";
    public static final String COL_ROLE = "role"; // CUSTOMER, VENDOR, DELIVERY, ADMIN
    public static final String COL_FULL_NAME = "full_name";
    public static final String COL_PHONE = "phone";
    public static final String COL_ADDRESS = "address";
    public static final String COL_LATITUDE = "latitude";
    public static final String COL_LONGITUDE = "longitude";

    // Vendors Table Columns
    public static final String COL_VENDOR_USER_ID = "user_id";
    public static final String COL_SHOP_NAME = "shop_name";
    public static final String COL_SHOP_CATEGORY = "category"; // Restaurants, Groceries, Bakery, Fruits & Veg, Pharmacy, General
    public static final String COL_VENDOR_LAT = "latitude";
    public static final String COL_VENDOR_LNG = "longitude";
    public static final String COL_VENDOR_ADDRESS = "address";

    // Products Table Columns
    public static final String COL_PROD_VENDOR_ID = "vendor_id";
    public static final String COL_PROD_NAME = "name";
    public static final String COL_PROD_CATEGORY = "category";
    public static final String COL_PROD_PRICE = "price";
    public static final String COL_PROD_QUANTITY = "quantity";
    public static final String COL_PROD_IMAGE = "image_code"; // visual template tag or URL
    public static final String COL_PROD_DESC = "description";

    // Orders Table Columns
    public static final String COL_ORDER_CUSTOMER_ID = "customer_id";
    public static final String COL_ORDER_VENDOR_ID = "vendor_id";
    public static final String COL_ORDER_DELIVERY_ID = "delivery_partner_id";
    public static final String COL_ORDER_STATUS = "status"; // PENDING, ACCEPTED, REJECTED, ASSIGNED, DELIVERING, DELIVERED
    public static final String COL_ORDER_ITEMS = "items";
    public static final String COL_ORDER_TOTAL = "total_price";
    public static final String COL_CUST_ADDRESS = "cust_address";
    public static final String COL_CUST_LAT = "cust_latitude";
    public static final String COL_CUST_LNG = "cust_longitude";
    public static final String COL_ORDER_RATING = "rating";
    public static final String COL_ORDER_FEEDBACK = "feedback";
    public static final String COL_ORDER_TIME = "order_time";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create Users Table
        db.execSQL("CREATE TABLE " + TABLE_USERS + " (" +
                COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_USERNAME + " TEXT UNIQUE, " +
                COL_PASSWORD + " TEXT, " +
                COL_ROLE + " TEXT, " +
                COL_FULL_NAME + " TEXT, " +
                COL_PHONE + " TEXT, " +
                COL_ADDRESS + " TEXT, " +
                COL_LATITUDE + " REAL, " +
                COL_LONGITUDE + " REAL)");

        // Create Vendors Table
        db.execSQL("CREATE TABLE " + TABLE_VENDORS + " (" +
                COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_VENDOR_USER_ID + " INTEGER, " +
                COL_SHOP_NAME + " TEXT, " +
                COL_SHOP_CATEGORY + " TEXT, " +
                COL_VENDOR_LAT + " REAL, " +
                COL_VENDOR_LNG + " REAL, " +
                COL_VENDOR_ADDRESS + " TEXT)");

        // Create Products Table
        db.execSQL("CREATE TABLE " + TABLE_PRODUCTS + " (" +
                COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_PROD_VENDOR_ID + " INTEGER, " +
                COL_PROD_NAME + " TEXT, " +
                COL_PROD_CATEGORY + " TEXT, " +
                COL_PROD_PRICE + " REAL, " +
                COL_PROD_QUANTITY + " INTEGER DEFAULT 100, " +
                COL_PROD_IMAGE + " TEXT, " +
                COL_PROD_DESC + " TEXT)");

        // Create Orders Table
        db.execSQL("CREATE TABLE " + TABLE_ORDERS + " (" +
                COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_ORDER_CUSTOMER_ID + " INTEGER, " +
                COL_ORDER_VENDOR_ID + " INTEGER, " +
                COL_ORDER_DELIVERY_ID + " INTEGER, " +
                COL_ORDER_STATUS + " TEXT, " +
                COL_ORDER_ITEMS + " TEXT, " +
                COL_ORDER_TOTAL + " REAL, " +
                COL_CUST_ADDRESS + " TEXT, " +
                COL_CUST_LAT + " REAL, " +
                COL_CUST_LNG + " REAL, " +
                COL_ORDER_RATING + " INTEGER DEFAULT 0, " +
                COL_ORDER_FEEDBACK + " TEXT, " +
                COL_ORDER_TIME + " TEXT)");

        seedDefaultData(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_VENDORS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PRODUCTS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ORDERS);
        onCreate(db);
    }

    private void seedDefaultData(SQLiteDatabase db) {
        // Admin
        db.execSQL("INSERT INTO " + TABLE_USERS + " (username, password, role, full_name) VALUES ('admin', 'admin', 'ADMIN', 'System Administrator')");

        // Vendor 1: Pizza Palace (Category: Restaurants & Food) - ID: 2
        db.execSQL("INSERT INTO " + TABLE_USERS + " (username, password, role, full_name, latitude, longitude) VALUES ('pizza_vendor', 'password', 'VENDOR', 'Mario Rossi', 12.9716, 77.5946)");
        db.execSQL("INSERT INTO " + TABLE_VENDORS + " (user_id, shop_name, category, latitude, longitude, address) VALUES (2, 'Pizza Palace', 'Restaurants & Food', 12.9716, 77.5946, 'Downtown Food Street')");

        // Products for Pizza Palace
        db.execSQL("INSERT INTO " + TABLE_PRODUCTS + " (vendor_id, name, category, price, quantity, image_code, description) VALUES (2, 'Garlic Bread Sticks', 'Food', 80.0, 40, 'garlic_bread', 'Crispy buttery garlic bread with herbs')");
        db.execSQL("INSERT INTO " + TABLE_PRODUCTS + " (vendor_id, name, category, price, quantity, image_code, description) VALUES (2, 'Classic Margherita Pizza', 'Food', 150.0, 30, 'pizza', 'Cheesy mozzarella with basil & tomato sauce')");
        db.execSQL("INSERT INTO " + TABLE_PRODUCTS + " (vendor_id, name, category, price, quantity, image_code, description) VALUES (2, 'Farmhouse Veggie Pizza', 'Food', 220.0, 25, 'pizza', 'Loaded with crisp bell peppers, onions & olives')");
        db.execSQL("INSERT INTO " + TABLE_PRODUCTS + " (vendor_id, name, category, price, quantity, image_code, description) VALUES (2, 'Spicy Pepperoni Pizza', 'Food', 280.0, 20, 'pizza', 'Topped with premium pepperoni slices')");

        // Vendor 2: Super Grocery Mart (Category: Supermarket & Grocery) - ID: 3
        db.execSQL("INSERT INTO " + TABLE_USERS + " (username, password, role, full_name, latitude, longitude) VALUES ('grocery_vendor', 'password', 'VENDOR', 'David Miller', 12.9725, 77.5930)");
        db.execSQL("INSERT INTO " + TABLE_VENDORS + " (user_id, shop_name, category, latitude, longitude, address) VALUES (3, 'Super Grocery Mart', 'Supermarket & Grocery', 12.9725, 77.5930, '7th Cross Avenue')");

        // Products for Super Grocery Mart
        db.execSQL("INSERT INTO " + TABLE_PRODUCTS + " (vendor_id, name, category, price, quantity, image_code, description) VALUES (3, 'Whole Wheat Bread 400g', 'Grocery', 35.0, 60, 'bread', 'Freshly baked wholesome brown bread')");
        db.execSQL("INSERT INTO " + TABLE_PRODUCTS + " (vendor_id, name, category, price, quantity, image_code, description) VALUES (3, 'Organic Milk 1L', 'Dairy', 58.0, 80, 'milk', 'Farm fresh pasteurized full cream milk')");
        db.execSQL("INSERT INTO " + TABLE_PRODUCTS + " (vendor_id, name, category, price, quantity, image_code, description) VALUES (3, 'Farm Fresh Eggs (6 pcs)', 'Grocery', 65.0, 50, 'eggs', 'High protein brown eggs')");
        db.execSQL("INSERT INTO " + TABLE_PRODUCTS + " (vendor_id, name, category, price, quantity, image_code, description) VALUES (3, 'Basmati Rice 1kg', 'Grocery', 110.0, 45, 'rice', 'Aromatic long grain premium basmati')");

        // Vendor 3: Royal Bakery (Category: Bakery & Sweets) - ID: 4
        db.execSQL("INSERT INTO " + TABLE_USERS + " (username, password, role, full_name, latitude, longitude) VALUES ('bakery_vendor', 'password', 'VENDOR', 'Sarah Baker', 12.9740, 77.5955)");
        db.execSQL("INSERT INTO " + TABLE_VENDORS + " (user_id, shop_name, category, latitude, longitude, address) VALUES (4, 'Royal Bakery & Cafe', 'Bakery & Sweets', 12.9740, 77.5955, 'Baker Square, Main Road')");

        // Products for Royal Bakery
        db.execSQL("INSERT INTO " + TABLE_PRODUCTS + " (vendor_id, name, category, price, quantity, image_code, description) VALUES (4, 'Butter Croissant', 'Bakery', 45.0, 35, 'croissant', 'Flaky golden buttery French croissant')");
        db.execSQL("INSERT INTO " + TABLE_PRODUCTS + " (vendor_id, name, category, price, quantity, image_code, description) VALUES (4, 'Chocolate Truffle Pastry', 'Bakery', 75.0, 25, 'cake', 'Rich dark chocolate layered pastry')");
        db.execSQL("INSERT INTO " + TABLE_PRODUCTS + " (vendor_id, name, category, price, quantity, image_code, description) VALUES (4, 'Red Velvet Cake 500g', 'Bakery', 350.0, 15, 'cake', 'Soft red velvet sponge with cream cheese')");

        // Vendor 4: Green Garden Organics (Category: Fresh Fruits & Vegetables) - ID: 5
        db.execSQL("INSERT INTO " + TABLE_USERS + " (username, password, role, full_name, latitude, longitude) VALUES ('fruits_vendor', 'password', 'VENDOR', 'Ramesh Kumar', 12.9695, 77.5910)");
        db.execSQL("INSERT INTO " + TABLE_VENDORS + " (user_id, shop_name, category, latitude, longitude, address) VALUES (5, 'Green Garden Organics', 'Fresh Fruits & Vegetables', 12.9695, 77.5910, 'Farmer Market Road')");

        // Products for Green Garden
        db.execSQL("INSERT INTO " + TABLE_PRODUCTS + " (vendor_id, name, category, price, quantity, image_code, description) VALUES (5, 'Fresh Tomatoes 1kg', 'Vegetables', 30.0, 90, 'vegetables', 'Plump red ripe local tomatoes')");
        db.execSQL("INSERT INTO " + TABLE_PRODUCTS + " (vendor_id, name, category, price, quantity, image_code, description) VALUES (5, 'Organic Potatoes 1kg', 'Vegetables', 35.0, 100, 'vegetables', 'Clean farm harvested potatoes')");
        db.execSQL("INSERT INTO " + TABLE_PRODUCTS + " (vendor_id, name, category, price, quantity, image_code, description) VALUES (5, 'Fresh Bananas 1 Dozen', 'Fruits', 50.0, 50, 'fruits', 'Sweet natural yellow bananas')");
        db.execSQL("INSERT INTO " + TABLE_PRODUCTS + " (vendor_id, name, category, price, quantity, image_code, description) VALUES (5, 'Royal Gala Apples 1kg', 'Fruits', 140.0, 40, 'fruits', 'Crunchy imported sweet apples')");

        // Delivery Partners (Created by Admin)
        db.execSQL("INSERT INTO " + TABLE_USERS + " (username, password, role, full_name, phone, latitude, longitude) VALUES ('delivery_boy_1', 'password', 'DELIVERY', 'Alex Johnson', '9876543210', 12.9710, 77.5960)");
        db.execSQL("INSERT INTO " + TABLE_USERS + " (username, password, role, full_name, phone, latitude, longitude) VALUES ('delivery_boy_2', 'password', 'DELIVERY', 'Rahul Sharma', '9876543211', 12.9740, 77.5920)");
    }

    // --- User Queries ---

    public boolean registerCustomer(String username, String password, String fullName, String phone, String address, double lat, double lng) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_USERNAME, username);
        cv.put(COL_PASSWORD, password);
        cv.put(COL_ROLE, "CUSTOMER");
        cv.put(COL_FULL_NAME, fullName);
        cv.put(COL_PHONE, phone);
        cv.put(COL_ADDRESS, address);
        cv.put(COL_LATITUDE, lat);
        cv.put(COL_LONGITUDE, lng);

        long result = db.insert(TABLE_USERS, null, cv);
        return result != -1;
    }

    public boolean createDeliveryPartner(String username, String password, String fullName, String phone, double lat, double lng) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_USERNAME, username);
        cv.put(COL_PASSWORD, password);
        cv.put(COL_ROLE, "DELIVERY");
        cv.put(COL_FULL_NAME, fullName);
        cv.put(COL_PHONE, phone);
        cv.put(COL_LATITUDE, lat);
        cv.put(COL_LONGITUDE, lng);

        long result = db.insert(TABLE_USERS, null, cv);
        return result != -1;
    }

    public boolean createVendorUser(String username, String password, String fullName, double lat, double lng) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_USERNAME, username);
        cv.put(COL_PASSWORD, password);
        cv.put(COL_ROLE, "VENDOR");
        cv.put(COL_FULL_NAME, fullName);
        cv.put(COL_LATITUDE, lat);
        cv.put(COL_LONGITUDE, lng);

        long result = db.insert(TABLE_USERS, null, cv);
        return result != -1;
    }

    public Cursor loginUser(String username, String password, String role) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_USERS + " WHERE " +
                COL_USERNAME + " =? AND " + COL_PASSWORD + " =? AND " + COL_ROLE + " =?",
                new String[]{username, password, role});
    }

    public Cursor getUserById(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_USERS + " WHERE " + COL_ID + " = " + userId, null);
    }

    // --- Vendor & Shop Queries ---

    public boolean addVendorShop(int userId, String shopName, String category, double lat, double lng, String address) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_VENDOR_USER_ID, userId);
        cv.put(COL_SHOP_NAME, shopName);
        cv.put(COL_SHOP_CATEGORY, category);
        cv.put(COL_VENDOR_LAT, lat);
        cv.put(COL_VENDOR_LNG, lng);
        cv.put(COL_VENDOR_ADDRESS, address);

        long result = db.insert(TABLE_VENDORS, null, cv);
        return result != -1;
    }

    public Cursor getAllVendors() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_VENDORS, null);
    }

    public Cursor getVendorsByCategory(String category) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_VENDORS + " WHERE " + COL_SHOP_CATEGORY + " =?", new String[]{category});
    }

    // --- Product Queries ---

    public boolean addProduct(int vendorUserId, String name, String category, double price, int quantity, String imageCode, String description) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_PROD_VENDOR_ID, vendorUserId);
        cv.put(COL_PROD_NAME, name);
        cv.put(COL_PROD_CATEGORY, category);
        cv.put(COL_PROD_PRICE, price);
        cv.put(COL_PROD_QUANTITY, quantity);
        cv.put(COL_PROD_IMAGE, imageCode);
        cv.put(COL_PROD_DESC, description);

        long result = db.insert(TABLE_PRODUCTS, null, cv);
        return result != -1;
    }

    public Cursor getProductsForVendor(int vendorUserId) {
        SQLiteDatabase db = this.getReadableDatabase();
        // Mandatory requirement: sorted from Lowest Price to Highest Price
        return db.rawQuery("SELECT * FROM " + TABLE_PRODUCTS + " WHERE " + COL_PROD_VENDOR_ID + " = " + vendorUserId + " ORDER BY " + COL_PROD_PRICE + " ASC", null);
    }

    // --- Order Queries ---

    public long placeOrder(int custId, int vendorUserId, String items, double total, String custAddress, double custLat, double custLng) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_ORDER_CUSTOMER_ID, custId);
        cv.put(COL_ORDER_VENDOR_ID, vendorUserId);
        cv.put(COL_ORDER_DELIVERY_ID, 0); // Unassigned initially
        cv.put(COL_ORDER_STATUS, "PENDING");
        cv.put(COL_ORDER_ITEMS, items);
        cv.put(COL_ORDER_TOTAL, total);
        cv.put(COL_CUST_ADDRESS, custAddress);
        cv.put(COL_CUST_LAT, custLat);
        cv.put(COL_CUST_LNG, custLng);
        cv.put(COL_ORDER_TIME, String.valueOf(System.currentTimeMillis()));

        return db.insert(TABLE_ORDERS, null, cv);
    }

    public Cursor getOrdersForVendor(int vendorUserId) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_ORDERS + " WHERE " + COL_ORDER_VENDOR_ID + " = " + vendorUserId + " ORDER BY " + COL_ID + " DESC", null);
    }

    public Cursor getOrdersForCustomer(int custId) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_ORDERS + " WHERE " + COL_ORDER_CUSTOMER_ID + " = " + custId + " ORDER BY " + COL_ID + " DESC", null);
    }

    public Cursor getOrdersForDelivery(int deliveryId) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_ORDERS + " WHERE " + COL_ORDER_DELIVERY_ID + " = " + deliveryId + " ORDER BY " + COL_ID + " DESC", null);
    }

    public boolean updateOrderStatus(int orderId, String status) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_ORDER_STATUS, status);
        return db.update(TABLE_ORDERS, cv, COL_ID + " =?", new String[]{String.valueOf(orderId)}) > 0;
    }

    public boolean assignDeliveryPartner(int orderId, int deliveryId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_ORDER_DELIVERY_ID, deliveryId);
        cv.put(COL_ORDER_STATUS, "ASSIGNED");
        return db.update(TABLE_ORDERS, cv, COL_ID + " =?", new String[]{String.valueOf(orderId)}) > 0;
    }

    public boolean submitOrderRating(int orderId, int rating, String feedback) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_ORDER_RATING, rating);
        cv.put(COL_ORDER_FEEDBACK, feedback);
        return db.update(TABLE_ORDERS, cv, COL_ID + " =?", new String[]{String.valueOf(orderId)}) > 0;
    }

    public Cursor getOrderDetails(int orderId) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_ORDERS + " WHERE " + COL_ID + " = " + orderId, null);
    }

    // --- Delivery Tracking & GPS ---

    public boolean updateDeliveryLocation(int deliveryId, double lat, double lng) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_LATITUDE, lat);
        cv.put(COL_LONGITUDE, lng);
        return db.update(TABLE_USERS, cv, COL_ID + " =? AND " + COL_ROLE + " = 'DELIVERY'", new String[]{String.valueOf(deliveryId)}) > 0;
    }

    public Cursor getOnlineDeliveryPartners() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_USERS + " WHERE " + COL_ROLE + " = 'DELIVERY'", null);
    }

    // --- Direct Cloud Sync Overwrites ---
    public void upsertUserFromCloud(int id, String username, String password, String role, String fullName, String phone, String address, double lat, double lng) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        if (id > 0) cv.put(COL_ID, id);
        cv.put(COL_USERNAME, username);
        cv.put(COL_PASSWORD, password);
        cv.put(COL_ROLE, role);
        cv.put(COL_FULL_NAME, fullName);
        cv.put(COL_PHONE, phone);
        cv.put(COL_ADDRESS, address);
        cv.put(COL_LATITUDE, lat);
        cv.put(COL_LONGITUDE, lng);

        int rows = db.update(TABLE_USERS, cv, COL_USERNAME + " =?", new String[]{username});
        if (rows == 0) {
            db.insertWithOnConflict(TABLE_USERS, null, cv, SQLiteDatabase.CONFLICT_REPLACE);
        }
    }

    public void upsertShopFromCloud(int id, int userId, String shopName, String category, double lat, double lng, String address) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        if (id > 0) cv.put(COL_ID, id);
        cv.put(COL_VENDOR_USER_ID, userId);
        cv.put(COL_SHOP_NAME, shopName);
        cv.put(COL_SHOP_CATEGORY, category);
        cv.put(COL_VENDOR_LAT, lat);
        cv.put(COL_VENDOR_LNG, lng);
        cv.put(COL_VENDOR_ADDRESS, address);

        int rows = db.update(TABLE_VENDORS, cv, COL_VENDOR_USER_ID + " = " + userId, null);
        if (rows == 0) {
            db.insert(TABLE_VENDORS, null, cv);
        }
    }

    public void upsertProductFromCloud(int id, int vendorUserId, String name, String category, double price, int quantity, String imageCode, String description) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        if (id > 0) cv.put(COL_ID, id);
        cv.put(COL_PROD_VENDOR_ID, vendorUserId);
        cv.put(COL_PROD_NAME, name);
        cv.put(COL_PROD_CATEGORY, category);
        cv.put(COL_PROD_PRICE, price);
        cv.put(COL_PROD_QUANTITY, quantity);
        cv.put(COL_PROD_IMAGE, imageCode);
        cv.put(COL_PROD_DESC, description);

        int rows = 0;
        if (id > 0) {
            rows = db.update(TABLE_PRODUCTS, cv, COL_ID + " = " + id, null);
        }
        if (rows == 0) {
            db.insert(TABLE_PRODUCTS, null, cv);
        }
    }

    public void upsertOrderFromCloud(int orderId, int custId, int vendorId, int delivId, String status, String items, double total, String custAddr, double custLat, double custLng, int rating, String feedback) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_ID, orderId);
        cv.put(COL_ORDER_CUSTOMER_ID, custId);
        cv.put(COL_ORDER_VENDOR_ID, vendorId);
        cv.put(COL_ORDER_DELIVERY_ID, delivId);
        cv.put(COL_ORDER_STATUS, status);
        cv.put(COL_ORDER_ITEMS, items);
        cv.put(COL_ORDER_TOTAL, total);
        cv.put(COL_CUST_ADDRESS, custAddr);
        cv.put(COL_CUST_LAT, custLat);
        cv.put(COL_CUST_LNG, custLng);
        cv.put(COL_ORDER_RATING, rating);
        cv.put(COL_ORDER_FEEDBACK, feedback);

        int rows = db.update(TABLE_ORDERS, cv, COL_ID + " = " + orderId, null);
        if (rows == 0) {
            db.insert(TABLE_ORDERS, null, cv);
        }
    }
}
