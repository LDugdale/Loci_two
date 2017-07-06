package com.lauriedugdale.loci.utils;

import android.content.Context;
import android.location.Location;
import android.widget.TextView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.lauriedugdale.loci.data.DataUtils;
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

    public static float[] WSG84toECEF(double latitude, double longitude, double altitude) {
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
}