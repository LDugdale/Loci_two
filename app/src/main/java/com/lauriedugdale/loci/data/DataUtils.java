package com.lauriedugdale.loci.data;

import android.content.Context;
import android.location.Location;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.lauriedugdale.loci.GeoEntry;
import com.lauriedugdale.loci.User;
import com.lauriedugdale.loci.ui.adapter.SocialAdapter;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by mnt_x on 14/06/2017.
 */

public class DataUtils {

    private Context mContext;

    // int representation of what media the entry contains
    public static final int NO_MEDIA = 0;
    public static final int IMAGE = 1;
    public static final int AUDIO = 2;

    private FusedLocationProviderClient mFusedLocationClient;

    private DatabaseReference mDatabase;
    private FirebaseStorage mStorage;

    // Stores multiple geo entries
    private Map<String, GeoEntry> mEntryMap;

    // stores a single GeoEntry
    private GeoEntry mGeoEntry;



    public DataUtils(Context context) {
        mContext = context;
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mStorage = FirebaseStorage.getInstance();
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(context);
        // TODO maybe put entry map in main fragment
        mEntryMap = new HashMap<String, GeoEntry>();
    }

    public Map<String, GeoEntry> getEntryList() {
        return this.mEntryMap;
    }

    public GeoEntry getGeoEntry() {
        return mGeoEntry;
    }

    public String getCurrentUID() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String uid = null;
        if (user != null) {
            uid = user.getUid();
        }
        return uid;
    }

    public void writeNewUser(String email) {
        User user = new User(email);
        mDatabase.child("users").child(getCurrentUID()).setValue(user);
    }

    public void writeNewFile(final String title, final String description, final Uri path, final int type) {
        StorageReference storageRef = mStorage.getReference();

        StorageReference ref = storageRef.child(getCurrentUID() + "/type/" +  + new Date().getTime());
        UploadTask uploadTask = ref.putFile(path);

        // Register observers to listen for when the download is done or if it fails
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle unsuccessful uploads
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                // taskSnapshot.getMetadata() contains file metadata such as size, content-type, and download URL.
                final Uri downloadUrl = taskSnapshot.getDownloadUrl();

                mFusedLocationClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        // Got last known location. In some rare situations this can be null.
                        if (location != null) {
                            GeoEntry user = new GeoEntry(title, description, location.getLatitude(), location.getLongitude(), downloadUrl.toString(), type);
                            DatabaseReference entryRef = mDatabase.child("files");
                            DatabaseReference pushEntryRef = entryRef.push();
                            user.setEntryID(pushEntryRef.getKey());
                            pushEntryRef.setValue(user);
                        }
                    }
                });
            }
        });
    }

    public void writeNewFile(final String title, final String description, final int type) {

        mFusedLocationClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                // Got last known location. In some rare situations this can be null.
                if (location != null) {
                    GeoEntry user = new GeoEntry(title, description, location.getLatitude(), location.getLongitude(), "", type);
                    DatabaseReference entryRef = mDatabase.child("files");
                    DatabaseReference pushEntryRef = entryRef.push();
                    user.setEntryID(pushEntryRef.getKey());
                    pushEntryRef.setValue(user);
                }
            }
        });
    }

    public void fetchUserFriends(final SocialAdapter adapter, final SocialAdapter.SocialAdapterOnClickHandler clickHandler){

//        // Read from the database
//        FirebaseDatabase database = FirebaseDatabase.getInstance();
//        DatabaseReference myRef = database.getReference("users");
//
//        myRef.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot) {
//
//                ArrayList<User> values = new ArrayList<User>();
//
//
//                for (DataSnapshot entry: dataSnapshot.getChildren()) {
////                    System.out.println(entry.getValue());
//                    User user = dataSnapshot.getValue(User.class);
//                    System.out.println(user.email);
//                    values.add(user);
//                }
//                // This method is called once with the initial value and again
//                // whenever data at this location is updated.
//
//
//                SocialAdapter adapter = new SocialAdapter(mContext, values, clickHandler);
//                recyclerView.setAdapter(adapter);
//            }
//
//            @Override
//            public void onCancelled(DatabaseError error) {
//                // Failed to read value
//
//            }
//        });

        mDatabase.child("users").addChildEventListener(new ChildEventListener() {

            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String prevChildKey) {
                for (DataSnapshot entry: dataSnapshot.getChildren()) {
                    User user = dataSnapshot.getValue(User.class);
                    adapter.addToUsers(user);
                }
                // notify the adapter that data has been changed in order for it to be displayed in recyclerview
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }



    public void readAllEntries(double latitudeStart, double latitudeEnd){

        mDatabase.child("files").orderByChild("latitude").startAt(latitudeStart).endAt(latitudeEnd).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String prevChildKey) {
                GeoEntry file = dataSnapshot.getValue(GeoEntry.class);
                mEntryMap.put(dataSnapshot.getKey(), file);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    /**
     * For reading a single GeoEntry
     * @param id
     */
    public void readEntry(final ImageView image, String id, String path){

        mDatabase.child("files").orderByChild("entryID").equalTo(id).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String prevChildKey) {
//                System.out.println("datasnapshot : " + dataSnapshot.getKey());
                GeoEntry entry = dataSnapshot.getValue(GeoEntry.class);
                StorageReference storageRef = mStorage.getReferenceFromUrl(entry.getFilePath());

                Glide.with(mContext)
                        .using(new FirebaseImageLoader())
                        .load(storageRef)
                        .into(image);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }


}
