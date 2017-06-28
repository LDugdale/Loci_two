package com.lauriedugdale.loci.ui.ar;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.location.Location;
import android.support.v4.content.LocalBroadcastManager;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.maps.android.SphericalUtil;
import com.lauriedugdale.loci.data.DataUtils;
import com.lauriedugdale.loci.data.dataobjects.FilterOptions;
import com.lauriedugdale.loci.data.dataobjects.GeoEntry;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by mnt_x on 26/06/2017.
 */

public class EntrySurfaceView extends View {

    Paint mPaint = new Paint();
    private double OFFSET = 0d;
    private double mY = 0d;
    private double screenWidth, screenHeight = 0d;

    private DataUtils mDataUtils;
    private FilterOptions mFilterOptions;

    private Context mContext;
    private HashMap<String, GeoEntry> mEntryMap;

    private Location mLocation;

    private FusedLocationProviderClient mFusedLocationClient; // used for getting the initial lcoation


    // Our handler for received Intents. This will be called whenever an Intent
// with an action named "custom-event-name" is broadcasted.
    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Get extra data included in the Intent
            mLocation = (Location) intent.getExtras().get("location");
            getEntries();
        }
    };

    public EntrySurfaceView(Context c, Paint paint) {
        super(c);
    }

    public EntrySurfaceView(Context context, AttributeSet set) {
        super(context, set);

        mContext = context;
        mDataUtils = new DataUtils(mContext);
        mFilterOptions = new FilterOptions();
        mEntryMap = new HashMap<String, GeoEntry>();

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(mContext);

        // register local broadcast listener
        LocalBroadcastManager.getInstance(mContext).registerReceiver(mMessageReceiver,
                new IntentFilter("location-changed"));

        getInitialMarkers();
        mPaint.setColor(Color.GREEN);
        mPaint.setTextSize(50);
        mPaint.setStrokeWidth(getPxFromDpi(getContext(), 2));
        mPaint.setAntiAlias(true);



    }

    private void getInitialMarkers(){

        mFusedLocationClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                // Got last known location. In some rare situations this can be null.
                if (location != null) {
                    mLocation = location;
                    getEntries();
                }
            }
        });
    }

    public void getEntries() {
        LatLng newLatLng = new LatLng(mLocation.getLatitude(), mLocation.getLongitude());
        LatLngBounds bounds = toBounds(newLatLng, 1609.34);
        mDataUtils.readAllEntriesForAR(bounds.southwest.latitude,
                bounds.northeast.latitude,
                mFilterOptions.getNumericalFromDate(),
                mFilterOptions.getNumericalToDate(),
                mFilterOptions.getCheckedTypes(),
                mEntryMap);

    }

    public LatLngBounds toBounds(LatLng center, double radius) {
        LatLng southwest = SphericalUtil.computeOffset(center, radius * Math.sqrt(2.0), 225);
        LatLng northeast = SphericalUtil.computeOffset(center, radius * Math.sqrt(2.0), 45);
        return new LatLngBounds(southwest, northeast);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        Log.d("onSizeChanged", "in here w=" + w + " h=" + h);
        screenWidth = (double) w;
        screenHeight = (double) h;
    }

    @Override
    protected void onDraw(Canvas canvas) {

        float x;
        float y;

        Iterator it = mEntryMap.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry) it.next();
            GeoEntry entry = (GeoEntry) pair.getValue();

            Bitmap typeImage = entry.getImage();

            double angle = bearing(mLocation.getLatitude(), mLocation.getLongitude(), entry.getLatitude(), entry.getLongitude()) - OFFSET;
            double xPos;

            if (angle < 0)
                angle = (angle + 360) % 360;

            double posInPx = angle * (screenWidth / 90d);

            int spotCentreX = typeImage.getWidth() / 2;
            int spotCentreY = typeImage.getHeight() / 2;

            xPos = posInPx - spotCentreX;

            if (angle <= 45) {
                x = (float) ((screenWidth / 2) + xPos);
            } else if (angle >= 315) {
                x = (float) ((screenWidth / 2) - ((screenWidth * 4) - xPos));
            } else {
                x = (float) (float) (screenWidth * 9); //somewhere off the screen
            }

            y = (float) screenHeight / 2 + spotCentreY;
//            y = (float)Math.abs((screenHeight /180) * mY * 2 ) + spotCentreY;
//            System.out.println("the y value" + y);
            canvas.drawBitmap(typeImage, x, y, mPaint); //camera spot
            canvas.drawText(entry.getTitle(), x, y, mPaint); //text
        }
    }
    public void setOffset(float offset) {
        this.OFFSET = offset;
    }

    public void setY(float y){
        mY = y;
    }


//    protected double distInMetres(Point me, Point u) {
//
//        double lat1 = me.latitude;
//        double lng1 = me.longitude;
//
//        double lat2 = u.latitude;
//        double lng2 = u.longitude;
//
//        double earthRadius = 6371;
//        double dLat = Math.toRadians(lat2 - lat1);
//        double dLng = Math.toRadians(lng2 - lng1);
//        double sindLat = Math.sin(dLat / 2);
//        double sindLng = Math.sin(dLng / 2);
//        double a = Math.pow(sindLat, 2) + Math.pow(sindLng, 2) * Math.cos(lat1) * Math.cos(lat2);
//        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
//        double dist = earthRadius * c;
//
//        return dist * 1000;
//    }

    protected static double bearing(double lat1, double lon1, double lat2, double lon2) {
        double longDiff = Math.toRadians(lon2 - lon1);
        double la1 = Math.toRadians(lat1);
        double la2 = Math.toRadians(lat2);
        double y = Math.sin(longDiff) * Math.cos(la2);
        double x = Math.cos(la1) * Math.sin(la2) - Math.sin(la1) * Math.cos(la2) * Math.cos(longDiff);

        double result = Math.toDegrees(Math.atan2(y, x));
        return (result + 360.0d) % 360.0d;
    }

    public static int getPxFromDpi(Context _context, int _px){
        int value = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                (float) _px, _context.getResources().getDisplayMetrics());
        return value;

    }
}
