package com.lauriedugdale.loci.utils;

import android.content.Context;
import android.location.Location;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.maps.android.SphericalUtil;
import com.lauriedugdale.loci.data.dataobjects.GeoEntry;
import com.lauriedugdale.loci.ui.activity.entry.AudioEntryActivity;
import com.lauriedugdale.loci.ui.activity.entry.ImageEntryActivity;
import com.lauriedugdale.loci.ui.activity.entry.NoMediaActivity;

/**
 * Helper methods for location based issues
 *
 * @author Laurie Dugdale
 */

public final class LocationUtils {

    private final static double WGS84_A = 6378137.0;                  // WGS 84 semi-major axis constant in meters
    private final static double WGS84_E2 = 0.00669437999014;          // square of WGS 84 eccentricity

    public final static float MAXIMUM_DISTANCE = 50.0f; //maximum distance a used can select a marker

    public final static double QUERY_RADIUS = 1609.34;

    // prevent instantiation
    private LocationUtils(){}


    public static float[] WGS84toECEF(double latitude, double longitude, double altitude) {
        double radLat = Math.toRadians(latitude);
        double radLon = Math.toRadians(longitude);

        float clat = (float) Math.cos(radLat);
        float slat = (float) Math.sin(radLat);
        float clon = (float) Math.cos(radLon);
        float slon = (float) Math.sin(radLon);

        float N = (float) (WGS84_A / Math.sqrt(1.0 - WGS84_E2 * slat * slat));

        float x = (float) ((N + altitude) * clat * clon);
        float y = (float) ((N + altitude) * clat * slon);
        float z = (float) ((N * (1.0 - WGS84_E2) + altitude) * slat);

        return new float[] {x , y, z};
    }

    public static float[] ECEFtoENU(Location currentLocation, float[] ecefCurrentLocation, float[] ecefPOI) {
        double radLat = Math.toRadians(currentLocation.getLatitude());
        double radLon = Math.toRadians(currentLocation.getLongitude());

        float clat = (float)Math.cos(radLat);
        float slat = (float)Math.sin(radLat);
        float clon = (float)Math.cos(radLon);
        float slon = (float)Math.sin(radLon);

        float dx = ecefCurrentLocation[0] - ecefPOI[0];
        float dy = ecefCurrentLocation[1] - ecefPOI[1];
        float dz = ecefCurrentLocation[2] - ecefPOI[2];

        float east = -slon*dx + clon*dy;

        float north = -slat*clon*dx - slat*slon*dy + clat*dz;

        float up = clat*clon*dx + clat*slon*dy + slat*dz;

        return new float[] {east , north, up, 1};
    }


    public static Class getEntryDestinationClass(int entryType){
        Class destination = null;
        switch(entryType){
            case DataUtils.IMAGE:
                destination = ImageEntryActivity.class;
                break;
            case DataUtils.AUDIO:
                destination = AudioEntryActivity.class;
                break;
            default:
                destination = NoMediaActivity.class;

                break;
        }
        return destination;
    }

    public static void displayDistance(final TextView display, Context context, final double markerLat, final double markerLng){

        FusedLocationProviderClient fusedLocationClient = LocationServices.getFusedLocationProviderClient(context);
        fusedLocationClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location != null) {
                    String distance = getDistance(location.getLatitude(), location.getLongitude(), markerLat, markerLng);;
                    display.setText(distance);
                }
            }
        });
    }

    private static String getDistance(double lat1, double lng1, double lat2, double lng2) {
        float [] dist = new float[1];
        Location.distanceBetween(lat1, lng1, lat2, lng2, dist);

        String unit;
        double distance;
        if(dist[0] >= 161){
            distance = (dist[0] / 1609.344);
            unit = " miles";
        } else {
            distance = dist[0];
            unit = " meters";
        }
        distance = Math.round(distance * 100d) / 100d;
        return distance + unit;

    }

    public static float getDistanceInMeters(double lat1, double lng1, double lat2, double lng2) {
        float [] dist = new float[1];
        Location.distanceBetween(lat1, lng1, lat2, lng2, dist);
        return dist[0];
    }


    public static void checkDistance(Context context, final TextView textView, final double markerLat, final double markerLng){
        FusedLocationProviderClient fusedLocationClient = LocationServices.getFusedLocationProviderClient(context);
        fusedLocationClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                // Got last known location. In some rare situations this can be null.
                if (location != null) {
                    boolean isWithinBounds;
                    Float distance = getDistanceInMeters(location.getLatitude(), location.getLongitude(), markerLat, markerLng);
                    isWithinBounds = (distance <= MAXIMUM_DISTANCE);
                    if (isWithinBounds){
                        textView.setVisibility(View.VISIBLE);
                    } else {
                          textView.setVisibility(View.GONE);
                    }
                }
            }
        });
    }

    public static boolean isWithinBounds(Location location, GeoEntry entry){

        Float distance = getDistanceInMeters(location.getLatitude(), location.getLongitude(), entry.getLatitude(), entry.getLongitude());
        System.out.println(distance);
        return (distance <= MAXIMUM_DISTANCE);
    }

    public static LatLngBounds toBounds(double latitutude, double longitude, double radius) {

        LatLng center = new LatLng(latitutude, longitude);
        LatLng southwest = SphericalUtil.computeOffset(center, radius * Math.sqrt(2.0), 225);
        LatLng northeast = SphericalUtil.computeOffset(center, radius * Math.sqrt(2.0), 45);
        return new LatLngBounds(southwest, northeast);
    }
}