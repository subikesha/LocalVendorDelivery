package com.example.localvendordelivery;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.view.View;
import androidx.annotation.Nullable;

/**
 * High-performance standalone Live GPS Radar & Route Canvas.
 * Renders smooth live navigation graphics between Vendor Shop, Delivery Partner, and Customer Home
 * without relying on external tile server permissions.
 */
public class LiveNavRadarView extends View {

    private Paint bgPaint;
    private Paint gridPaint;
    private Paint routePaint;
    private Paint markerTextPaint;
    private Paint circlePaint;

    private String vendorName = "Vendor Shop";
    private String customerAddress = "Customer Home";
    private String driverName = "Delivery Partner";

    private double startLat = 12.9716, startLng = 77.5946; // Vendor
    private double destLat = 12.9780, destLng = 77.5900;   // Customer
    private double driverLat = 12.9716, driverLng = 77.5946; // Driver
    private String orderStatus = "PENDING";
    private double progressFraction = 0.0; // 0.0 (Vendor) to 1.0 (Customer)

    public LiveNavRadarView(Context context) {
        super(context);
        init();
    }

    public LiveNavRadarView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        bgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        bgPaint.setColor(Color.parseColor("#F4F6F9"));
        bgPaint.setStyle(Paint.Style.FILL);

        gridPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        gridPaint.setColor(Color.parseColor("#E0E6ED"));
        gridPaint.setStrokeWidth(2f);
        gridPaint.setStyle(Paint.Style.STROKE);

        routePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        routePaint.setColor(Color.parseColor("#2196F3"));
        routePaint.setStrokeWidth(8f);
        routePaint.setStyle(Paint.Style.STROKE);
        routePaint.setPathEffect(new DashPathEffect(new float[]{20, 10}, 0));

        markerTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        markerTextPaint.setColor(Color.parseColor("#1E293B"));
        markerTextPaint.setTextSize(32f);
        markerTextPaint.setTextAlign(Paint.Align.CENTER);
        markerTextPaint.setFakeBoldText(true);

        circlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        circlePaint.setStyle(Paint.Style.FILL);
    }

    public void updateLocations(double vLat, double vLng, String vName,
                                double cLat, double cLng, String cAddress,
                                double dLat, double dLng, String dName,
                                String status, double progress) {
        this.startLat = vLat;
        this.startLng = vLng;
        this.vendorName = vName != null ? vName : "Vendor Shop";
        this.destLat = cLat;
        this.destLng = cLng;
        this.customerAddress = cAddress != null ? cAddress : "Customer Location";
        this.driverLat = dLat;
        this.driverLng = dLng;
        this.driverName = dName != null ? dName : "Delivery Driver";
        this.orderStatus = status != null ? status : "PENDING";
        this.progressFraction = Math.max(0.0, Math.min(1.0, progress));
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int w = getWidth();
        int h = getHeight();

        // 1. Draw modern map background with grid lines
        canvas.drawRect(0, 0, w, h, bgPaint);

        int step = 80;
        for (int x = 0; x < w; x += step) {
            canvas.drawLine(x, 0, x, h, gridPaint);
        }
        for (int y = 0; y < h; y += step) {
            canvas.drawLine(0, y, w, y, gridPaint);
        }

        // Calculate positions on canvas
        float marginX = w * 0.18f;
        PointF vendorPos = new PointF(marginX, h * 0.72f);
        PointF customerPos = new PointF(w - marginX, h * 0.28f);

        // Calculate Driver position
        float driverX = vendorPos.x + (float) progressFraction * (customerPos.x - vendorPos.x);
        float driverY = vendorPos.y + (float) progressFraction * (customerPos.y - vendorPos.y);
        PointF driverPos = new PointF(driverX, driverY);

        // 2. Draw Navigation Route Line
        canvas.drawLine(vendorPos.x, vendorPos.y, customerPos.x, customerPos.y, routePaint);

        // 3. Draw Vendor Shop Marker
        circlePaint.setColor(Color.parseColor("#E0F2FE"));
        canvas.drawCircle(vendorPos.x, vendorPos.y, 44f, circlePaint);
        circlePaint.setColor(Color.parseColor("#0284C7"));
        canvas.drawCircle(vendorPos.x, vendorPos.y, 32f, circlePaint);
        markerTextPaint.setTextSize(40f);
        canvas.drawText("🏪", vendorPos.x, vendorPos.y + 14f, markerTextPaint);

        markerTextPaint.setTextSize(26f);
        markerTextPaint.setColor(Color.parseColor("#0369A1"));
        canvas.drawText(vendorName, vendorPos.x, vendorPos.y + 70f, markerTextPaint);

        // 4. Draw Customer Home Marker
        circlePaint.setColor(Color.parseColor("#DCFCE7"));
        canvas.drawCircle(customerPos.x, customerPos.y, 44f, circlePaint);
        circlePaint.setColor(Color.parseColor("#16A34A"));
        canvas.drawCircle(customerPos.x, customerPos.y, 32f, circlePaint);
        markerTextPaint.setTextSize(40f);
        canvas.drawText("🏠", customerPos.x, customerPos.y + 14f, markerTextPaint);

        markerTextPaint.setTextSize(26f);
        markerTextPaint.setColor(Color.parseColor("#15803D"));
        canvas.drawText(customerAddress, customerPos.x, customerPos.y + 70f, markerTextPaint);

        // 5. Draw Live Moving Delivery Driver Marker
        circlePaint.setColor(Color.parseColor("#FEF3C7"));
        canvas.drawCircle(driverPos.x, driverPos.y, 52f, circlePaint);
        circlePaint.setColor(Color.parseColor("#D97706"));
        canvas.drawCircle(driverPos.x, driverPos.y, 38f, circlePaint);
        markerTextPaint.setTextSize(44f);
        canvas.drawText("🛵", driverPos.x, driverPos.y + 16f, markerTextPaint);

        markerTextPaint.setTextSize(26f);
        markerTextPaint.setColor(Color.parseColor("#B45309"));
        String dLabel = driverName + " (" + (int)(progressFraction * 100) + "% on way)";
        canvas.drawText(dLabel, driverPos.x, driverPos.y - 56f, markerTextPaint);

        // 6. Draw Status Header Pill
        Paint pillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        pillPaint.setColor(Color.parseColor("#1E293B"));
        pillPaint.setStyle(Paint.Style.FILL);
        float pillW = 340f, pillH = 64f;
        canvas.drawRoundRect(w / 2f - pillW / 2f, 24f, w / 2f + pillW / 2f, 24f + pillH, 32f, 32f, pillPaint);

        markerTextPaint.setTextSize(24f);
        markerTextPaint.setColor(Color.WHITE);
        canvas.drawText("⚡ Live Route: " + orderStatus, w / 2f, 24f + 40f, markerTextPaint);
    }
}
