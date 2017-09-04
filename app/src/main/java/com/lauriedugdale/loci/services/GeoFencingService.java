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

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.lauriedugdale.loci.data.UserDatabase;
import com.lauriedugdale.loci.data.dataobjects.GeoEntry;
import com.lauriedugdale.loci.utils.GeofencingUtils;
import com.lauriedugdale.loci.utils.LocationUtils;

import java.util.ArrayList;

/**
 * This service is respomsible for monitoring the users location and periodically downloading nearby entries
 * The nearby entries are added to the geofencing client, when a user enters a geofence boundary GeoFenceIntentService
 * is called to launch a notification.
 *
 * @author Laurie Dugdale
 */
public class GeoFencingService extends Service implements OnCompleteListener<Void> {
    //TODO This class has two purposes - providing location to the "NearMeFragment" and Geofencing consider splitting
    //TODO Close this class shutting down all the open API connections
    //TODO Find a way to fetch location when app is not running
    //TODO mark in database an entry has been viewed
    private static final String TAG = GeoFencingService.class.getSimpleName();

    // set the intervals
    private static final long INTERVAL = 100000;
    private static final long FASTEST_INTERVAL = 50000;

    GoogleApiClient mGogleApiClient = null;
    private GeofencingClient mGeofencingClient; // Geofencing API access
    private ArrayList<Geofence> mGeofenceList; // List of geofences used
    private PendingIntent mGeofencePendingIntent; // Used when requesting to add or remove geofences
    private UserDatabase mUserDatabase; // accessing the UserDatabase
    private Bundle mGeoEntries; // Geo entries to be bundled for the GeoFenceIntentService
    private Location mLocation; // current location

    public GeoFencingService() {
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand");

        mUserDatabase = new UserDatabase(this);
        mGeofenceList = new ArrayList<>();
        mGeofencePendingIntent = null;
        mGeoEntries = new Bundle();

        // set up google api client
        mGogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                    @Override
                    public void onConnected(@Nullable Bundle bundle) {
                        startLocationMonitoring();
                        retrieveEntries();
                    }

                    @Override
                    public void onConnectionSuspended(int i) {
                    }
                })
                .addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
                    }
                }).build();

        // connect the google api client
        mGogleApiClient.connect();

        mGeofencingClient = LocationServices.getGeofencingClient(this);
        return Service.START_NOT_STICKY;
    }

    @Override
    public void onComplete(@NonNull Task<Void> task) {
        if (task.isSuccessful()) {

        } else {
            Log.w(TAG, "task unsuccessful");
        }
    }

    /**
     * Start location monitoring
     */
    private void startLocationMonitoring(){
        Log.d(TAG, "startLocationMonitoring");
        try {
            LocationRequest locationRequest = LocationRequest.create()
                    .setInterval(INTERVAL)
                    .setFastestInterval(FASTEST_INTERVAL)
                    .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

            LocationServices.FusedLocationApi.requestLocationUpdates(mGogleApiClient,
                    locationRequest, new LocationListener() {
                        @Override
                        public void onLocationChanged(Location location) {
                            Log.d(TAG, "location update lat/long " + location.getLatitude() + "" + location.getLongitude());

                            setLocation(location);
//                            Intent intent = new Intent("location_update");
//                            intent.putExtra("latitude", location.getLatitude());
//                            intent.putExtra("longitude", location.getLongitude());
//                            LocalBroadcastManager.getInstance(GeoFencingService.this).sendBroadcast(intent);
                        }
                    });

        } catch (SecurityException e){
            Log.e(TAG, "SecurityException - " + e.getMessage());
        }
    }

    /**
     * This method builds a GeofencingRequest. It is passed the list of geofences to be monitored
     * and specifies how they should be triggered.
     */
    private GeofencingRequest getGeofencingRequest() {

        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();

        // triggers a GEOFENCE_TRANSITION_ENTER notificatio when a geofence is added
        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER);
        // adds the geofences to be monitored
        builder.addGeofences(mGeofenceList);

        return builder.build();
    }

    /**
     *
     * @param location
     */
    public void setLocation(Location location){

        if (mLocation == null){
            mLocation = location;
            retrieveEntries();
        } else if (LocationUtils.getDistanceInMeters(mLocation.getLatitude(), mLocation.getLongitude(), location.getLatitude(), location.getLongitude()) > 1609.34){
            mLocation.setLatitude(location.getLatitude());
            mLocation.setLongitude(location.getLongitude());
            retrieveEntries();
        }
    }

    /**
     * downloads the entries from the database and add them to the Geofencing client
     */
    public void retrieveEntries(){

        if (mLocation == null){
            return;
        }

        final String currentUID = mUserDatabase.getCurrentUID();

        DatabaseReference database = FirebaseDatabase.getInstance().getReference("entry_location");
        GeoFire geoFire = new GeoFire(database);
        GeoQuery geoQuery = geoFire.queryAtLocation(new GeoLocation(mLocation.getLatitude(), mLocation.getLongitude()), 8); // 5 mile radius

        final DatabaseReference ref = FirebaseDatabase.getInstance().getReference("entry_permission");
        final DatabaseReference eRef = FirebaseDatabase.getInstance().getReference("entries");

        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, final GeoLocation location) {

                ref.child(currentUID + "/" + key).addListenerForSingleValueEvent(new ValueEventListener() {

                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (!dataSnapshot.hasChildren()){
                            return;
                        }
                        String entryKey = dataSnapshot.getKey();
                        eRef.child(entryKey).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                GeoEntry entry = dataSnapshot.getValue(GeoEntry.class);
                                if (entry == null) {
                                    return;
                                }

                                if (!entry.getCreator().equals(currentUID)){
                                    addToGeofenceList(entry);
                                    addGeofences();
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                    }
                });
            }

            @Override
            public void onKeyExited(String key) {
                Log.d(TAG, String.format("Key %s is no longer in the search area", key));
            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {
                Log.d(TAG, String.format("Key %s moved within the search area to [%f,%f]", key, location.latitude, location.longitude));
            }

            @Override
            public void onGeoQueryReady() {
                Log.d(TAG, "All initial data has been loaded and events have been fired!");
            }

            @Override
            public void onGeoQueryError(DatabaseError error) {
                Log.d(TAG, "There was an error with this query: " + error);
            }
        });
    }

    /**
     * adds a GeoEntry too the geofence list
     *
     * @param entry GeoEntry to add
     */
    private void addToGeofenceList(GeoEntry entry){

        // add to the bundle to pass it to the geofence intent servcice
        mGeoEntries.putParcelable(entry.getEntryID(), entry);

        mGeofenceList.add(new Geofence.Builder()
                // set the id the same as the GeoEntry id to easily match them
                .setRequestId(entry.getEntryID())
                .setCircularRegion(
                        entry.getLatitude(),
                        entry.getLongitude(),
                        GeofencingUtils.GEOFENCE_RADIUS_IN_METERS
                )
                .setExpirationDuration(GeofencingUtils.GEOFENCE_EXPIRATION_IN_MILLISECONDS)
                // setting tracking too entry and exit of a geofence
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER |
                        Geofence.GEOFENCE_TRANSITION_EXIT)
                .build());
    }

    /**
     * Creates a PendingIntent, that is used when adding or removing a geofence.
     *
     * @return Pending intent that launches the GeoFennceIntentService
     */
    private PendingIntent getGeofencePendingIntent() {
        // Reuse the PendingIntent if we already have it.
        if (mGeofencePendingIntent != null) {
            return mGeofencePendingIntent;
        }
        Intent intent = new Intent(this, GeofenceIntentService.class);
        // put the geofence bundle in the intent
        intent.putExtra("entries", mGeoEntries);

        return PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }
    /**
     * Adds the geofences.
     */
    @SuppressWarnings("MissingPermission")
    private void addGeofences() {
        if (mGeofenceList.isEmpty()){
            return;
        }

        mGeofencingClient.addGeofences(getGeofencingRequest(), getGeofencePendingIntent())
                .addOnCompleteListener(this);
    }

    /**
     * Removes the geofences.
     */
    @SuppressWarnings("MissingPermission")
    private void removeGeofences() {
        if (mGeofenceList.isEmpty()){
            return;
        }
        mGeofencingClient.removeGeofences(getGeofencePendingIntent()).addOnCompleteListener(this);
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
//        googleApiClient.disconnect();
    }
}
