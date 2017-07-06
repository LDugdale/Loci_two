package com.lauriedugdale.loci.services;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.lauriedugdale.loci.R;
import com.lauriedugdale.loci.data.DataUtils;
import com.lauriedugdale.loci.data.dataobjects.GeoEntry;
import com.lauriedugdale.loci.utils.GeofencingUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class GeoFencingService extends Service implements OnCompleteListener<Void> {

    public static final String TAG = "GeoFencingService";
    public static final String GEOFENCE_ID = "MyGeofenceId";
    private static final long INTERVAL = 10000;
    private static final long FASTEST_INTERVAL = 5000;

    GoogleApiClient googleApiClient = null;

    /**
     * Provides access to the Geofencing API.
     */
    private GeofencingClient mGeofencingClient;
    private ArrayList<Geofence> mGeofenceList; // List of geofences used
    private PendingIntent mGeofencePendingIntent; // Used when requesting to add or remove geofences

    private DataUtils mDataUtils;


    public GeoFencingService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();

        mDataUtils = new DataUtils(this);
        mGeofenceList = new ArrayList<>();
        mGeofencePendingIntent = null;

        googleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                    @Override
                    public void onConnected(@Nullable Bundle bundle) {
                        Log.d(TAG, "Connected to GoogleApiClient");
                    }

                    @Override
                    public void onConnectionSuspended(int i) {
                        Log.d(TAG, "Suspended connection to GoogleApiClient");
                    }
                })
                .addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
                        Log.d(TAG, "Failed to connect to GoogleApiClient - " + connectionResult.getErrorMessage());
                    }
                }).build();

        startLocationMonitoring();
        retrieveEntries();

        mGeofencingClient = LocationServices.getGeofencingClient(this);

    }

    @Override
    public void onComplete(@NonNull Task<Void> task) {

    }

    private void startLocationMonitoring(){
        Log.d(TAG, "startLocationMonitoring called");
        try {
            LocationRequest locationRequest = LocationRequest.create()
                    .setInterval(INTERVAL)
                    .setFastestInterval(FASTEST_INTERVAL)
                    .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient,
                    locationRequest, new LocationListener() {
                        @Override
                        public void onLocationChanged(Location location) {
                            Log.d(TAG, "location update lat/long " + location.getLatitude() + "" + location.getLongitude());
                        }
                    });

        } catch (SecurityException e){
            Log.d(TAG, "SecurityException - " + e.getMessage());
        }
    }

    /**
     * Builds and returns a GeofencingRequest. Specifies the list of geofences to be monitored.
     * Also specifies how the geofence notifications are initially triggered.
     */
    private GeofencingRequest getGeofencingRequest() {
        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();

        // The INITIAL_TRIGGER_ENTER flag indicates that geofencing service should trigger a
        // GEOFENCE_TRANSITION_ENTER notification when the geofence is added and if the device
        // is already inside that geofence.
        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER);

        // Add the geofences to be monitored by geofencing service.
        builder.addGeofences(mGeofenceList);

        // Return a GeofencingRequest.
        return builder.build();
    }


    public void retrieveEntries(){

        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference ref = database.getReference("file_permission");
        ref.child(mDataUtils.getCurrentUID()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    GeoEntry entry = postSnapshot.getValue(GeoEntry.class);
                    addToGeofenceList(entry);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    private void addToGeofenceList(GeoEntry entry){
        mGeofenceList.add(new Geofence.Builder()
                // identifies the geofence
                .setRequestId(entry.getEntryID())
                .setCircularRegion(
                        entry.getLatitude(),
                        entry.getLongitude(),
                        GeofencingUtils.GEOFENCE_RADIUS_IN_METERS
                )
                // track entry and exit of geofence
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER |
                        Geofence.GEOFENCE_TRANSITION_EXIT)
                .build());
    }

    /**
     * Gets a PendingIntent to send with the request to add or remove Geofences. Location Services
     * issues the Intent inside this PendingIntent whenever a geofence transition occurs for the
     * current list of geofences.
     *
     * @return A PendingIntent for the IntentService that handles geofence transitions.
     */
    private PendingIntent getGeofencePendingIntent() {
        // Reuse the PendingIntent if we already have it.
        if (mGeofencePendingIntent != null) {
            return mGeofencePendingIntent;
        }
        Intent intent = new Intent(this, GeofenceIntentService.class);
        // We use FLAG_UPDATE_CURRENT so that we get the same pending intent back when calling
        // addGeofences() and removeGeofences().
        return PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }
    /**
     * Adds geofences. This method should be called after the user has granted the location
     * permission.
     */
    @SuppressWarnings("MissingPermission")
    private void addGeofences() {
        mGeofencingClient.addGeofences(getGeofencingRequest(), getGeofencePendingIntent())
                .addOnCompleteListener(this);
    }

    /**
     * Removes geofences. This method should be called after the user has granted the location
     * permission.
     */
    @SuppressWarnings("MissingPermission")
    private void removeGeofences() {
        mGeofencingClient.removeGeofences(getGeofencePendingIntent()).addOnCompleteListener(this);
    }



//    private void populateGeofenceList(){
//        Log.d(TAG, "startGeofenceMonitoring called");
//
//
//        for (Map.Entry<String, GeoEntry> e : mGeoEntries.entrySet()) {
//            GeoEntry entry = e.getValue();
//            mGeofenceList.add(new Geofence.Builder()
//                    // identifies the geofence
//                    .setRequestId(entry.getEntryID())
//                    .setCircularRegion(
//                            entry.getLatitude(),
//                            entry.getLongitude(),
//                            GeofencingUtils.GEOFENCE_RADIUS_IN_METERS
//                    )
//                    // track entry and exit of geofence
//                    .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER |
//                            Geofence.GEOFENCE_TRANSITION_EXIT)
//                    .build());
//        }
//
//
//
//        try {
//
//            Geofence geofence = new Geofence.Builder()
//                    .setRequestId(GEOFENCE_ID)
//                    .setCircularRegion(33, -84, 100)
//                    .setExpirationDuration(Geofence.NEVER_EXPIRE)
//                    .setExpirationDuration(1000)
//                    .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_EXIT)
//                    .build();
//
//            GeofencingRequest geofenceRequest = new GeofencingRequest.Builder()
//                    .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
//                    .addGeofence(geofence).build();
//
//            Intent intent = new Intent(this, GeofenceIntentService.class);
//            PendingIntent pendingIntent = PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
//
//            if (!googleApiClient.isConnected()) {
//                Log.d(TAG, "GoogleApiClient Is not connected");
//            } else {
//                LocationServices.GeofencingApi.addGeofences(googleApiClient, geofenceRequest, pendingIntent)
//                        .setResultCallback(new ResultCallback<Status>() {
//                            @Override
//                            public void onResult(@NonNull Status status) {
//                                if (status.isSuccess()) {
//                                    Log.d(TAG, "Successfully added geofence");
//                                } else {
//                                    Log.d(TAG, "Failed to add geofence + " + status.getStatus());
//                                }
//                            }
//                        });
//            }
//        } catch (SecurityException e){
//            Log.d(TAG, "SecurityException - " + e.getMessage());
//        }
//    }

    private void stopGeofenceMonitoring(){
        Log.d(TAG, "stopGeofenceMonitoring called");
        ArrayList<String> geofenceIds = new ArrayList<>();
        geofenceIds.add(GEOFENCE_ID);
        LocationServices.GeofencingApi.removeGeofences(googleApiClient, geofenceIds);
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
