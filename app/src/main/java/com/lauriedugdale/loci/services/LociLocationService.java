package com.lauriedugdale.loci.services;

import android.Manifest;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

public class LociLocationService extends Service {

    private static final String TAG = "LociLocation";

    private LocationManager mLocationManager = null;
    private static final int LOCATION_INTERVAL = 1000;
    private static final float LOCATION_DISTANCE = 10f;

    private double mLatitude;
    private double mLongitude;
    private Location mOldLocation;
    private Location mLocation;

    private class SensedLocationListener implements LocationListener{
        Location mLastLocation;
        public SensedLocationListener(String provider) {
            Log.e(TAG, "LocationListener " + provider);
            mLastLocation = new Location(provider);
        }
        @Override
        public void onLocationChanged(Location location) {
            Log.e(TAG, "onLocationChanged: " + location);
            mLatitude = location.getLatitude();
            mLongitude = location.getLongitude();
            mOldLocation = mLocation;
            mLocation = location;
            broadcastLocation();

            mLastLocation.set(location);

            Intent broadcastIntent = new Intent("com.lauriedugdale.loci.location_change");
            broadcastIntent.putExtra("latitude", mLatitude);
            broadcastIntent.putExtra("longitude", mLongitude);
            sendBroadcast(broadcastIntent);
        }

        @Override
        public void onProviderDisabled(String provider) {
            Log.e(TAG, "onProviderDisabled: " + provider);
        }

        @Override
        public void onProviderEnabled(String provider) {
            Log.e(TAG, "onProviderEnabled: " + provider);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            Log.e(TAG, "onStatusChanged: " + provider);
        }
    }
    LocationListener[] mLocationListeners = new LocationListener[] {
            new SensedLocationListener(LocationManager.GPS_PROVIDER),
            new SensedLocationListener(LocationManager.NETWORK_PROVIDER)
    };

    public double getLatitude() {
        return mLatitude;
    }

    public double getLongitude() {
        return mLongitude;
    }

    public Location getLocation(){
        return mLocation;
    }


    /**
     * Broadcast location if travelled
     */
    private void broadcastLocation() {

        // don't broadcast if travelled less than 100 meters
//        if (getDistanceInMeters() < 100f) {
//            return;
//        }

        Intent intent = new Intent("location-changed");
        // You can also include some extra data.
        intent.putExtra("location", mLocation);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    private float getDistanceInMeters() {
        if(mOldLocation == null){
            return 0f;
        }

        float [] dist = new float[1];
        Location.distanceBetween(mLocation.getLatitude(), mLocation.getLongitude(),
                                mOldLocation.getLatitude(), mOldLocation.getLongitude(), dist);
        return dist[0];
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e(TAG, "onStartCommand");
        super.onStartCommand(intent, flags, startId);
        return START_STICKY;
    }

    @Override
    public void onCreate() {
        Log.e(TAG, "onCreate");
        initializeLocationManager();

        if ( Build.VERSION.SDK_INT >= 23 &&
                ContextCompat.checkSelfPermission( this, android.Manifest.permission.ACCESS_FINE_LOCATION ) != PackageManager.PERMISSION_GRANTED) {
            return  ;
        }

        try {
            mLocationManager.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER, LOCATION_INTERVAL, LOCATION_DISTANCE,
                    mLocationListeners[1]);
        } catch (java.lang.SecurityException ex) {
            Log.i(TAG, "fail to request location update, ignore", ex);
        } catch (IllegalArgumentException ex) {
            Log.d(TAG, "network provider does not exist, " + ex.getMessage());
        }

        try {
            mLocationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER, LOCATION_INTERVAL, LOCATION_DISTANCE,
                    mLocationListeners[0]);
        } catch (java.lang.SecurityException ex) {
            Log.i(TAG, "fail to request location update, ignore", ex);
        } catch (IllegalArgumentException ex) {
            Log.d(TAG, "gps provider does not exist " + ex.getMessage());
        }
    }

    @Override
    public void onDestroy() {
        Log.e(TAG, "onDestroy");
        super.onDestroy();
        if (mLocationManager != null) {
            for (int i = 0; i < mLocationListeners.length; i++) {
                try {
                    mLocationManager.removeUpdates(mLocationListeners[i]);
                } catch (Exception ex) {
                    Log.i(TAG, "fail to remove location listners, ignore", ex);
                }
            }
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void initializeLocationManager() {
        Log.e(TAG, "initializeLocationManager");
        if (mLocationManager == null) {
            mLocationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        }
    }



    public class LocationBinder extends Binder {

        public LociLocationService getService(){
            return LociLocationService.this;
        }
    }


}