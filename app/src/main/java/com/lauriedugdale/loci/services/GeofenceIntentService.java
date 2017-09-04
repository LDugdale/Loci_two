package com.lauriedugdale.loci.services;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.Log;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;
import com.lauriedugdale.loci.R;
import com.lauriedugdale.loci.data.UserDatabase;
import com.lauriedugdale.loci.data.dataobjects.GeoEntry;
import com.lauriedugdale.loci.ui.activity.MainActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Handles the displaying of a notification given the triggered geofences
 * 
 * @author Laurie Dugdale
 */
public class GeofenceIntentService extends IntentService {

    private static final String TAG = GeofenceIntentService.class.getSimpleName();

    private Bundle mGeoEntries;

    public GeofenceIntentService(){
        super(TAG);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);

        mGeoEntries = intent.getBundleExtra("entries");

        // Get the transition type.
        int geofenceTransition = geofencingEvent.getGeofenceTransition();

        //TODO if exit remove?
        // if the current transition is entering or exiting
        if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER ||
                geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT) {

            // Get the triggered geofences
            List<Geofence> triggeringGeofences = geofencingEvent.getTriggeringGeofences();

            // Send notification and log the transition details.
            sendNotification(triggeringGeofences);
        }
    }

    /**
     * builds a notification and constructs intent when the notification is clicked
     *
     * @param triggeringGeofences a List of geofences that have been triggered
     */
    private void sendNotification(List<Geofence> triggeringGeofences) {

        ArrayList<GeoEntry> geofenceEntries = new ArrayList<>();

        for (Geofence g : triggeringGeofences){
           geofenceEntries.add((GeoEntry)mGeoEntries.getParcelable(g.getRequestId()));
        }

        String title = "";
        String description = "";

        if (triggeringGeofences.size() == 1){
            GeoEntry entry = mGeoEntries.getParcelable(triggeringGeofences.get(0).getRequestId());
            if (entry == null){
                return;
            }
            title = entry.getTitle();
            description = entry.getCreatorName();
        } else {
            title = "Your friends have left some entries in this area!";
            description = "view them now";
        }

        // Create intent that starts the MainActivity
        Intent notificationIntent = new Intent(getApplicationContext(), MainActivity.class);
        notificationIntent.putExtra("list", geofenceEntries);
        PendingIntent pIntent = PendingIntent.getActivity(this, (int) System.currentTimeMillis(), notificationIntent, 0);

        // construct task
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(MainActivity.class);
        stackBuilder.addNextIntent(notificationIntent);
        PendingIntent notificationPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        UserDatabase userDatabase = new UserDatabase(this);
        // Get a compatible notification builder
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);

        // Build the notification
        builder.setSmallIcon(R.mipmap.o)
//                .setLargeIcon(BitmapFactory.decodeResource(getResources(),R.mipmap.o))
                .setColor(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimary))
                .setContentTitle(title)
                .setContentText(description)
                .addAction(R.drawable.ic_add_circle, "View", pIntent)
                .setContentIntent(notificationPendingIntent);

        // dismiss the notification when touched
        builder.setAutoCancel(true);

        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(0, builder.build());
    }
}
