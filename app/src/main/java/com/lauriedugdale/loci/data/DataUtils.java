package com.lauriedugdale.loci.data;

import android.content.Context;
import android.icu.text.SimpleDateFormat;
import android.icu.util.Calendar;
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
import com.google.firebase.auth.UserProfileChangeRequest;
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
import com.lauriedugdale.loci.UserFriend;
import com.lauriedugdale.loci.ui.adapter.SelectFriendsAdapter;
import com.lauriedugdale.loci.ui.adapter.SocialAdapter;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
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
    private FirebaseUser mUser;
    // Stores multiple geo entries
    private Map<String, GeoEntry> mEntryMap;

    public DataUtils(Context context) {
        mUser = FirebaseAuth.getInstance().getCurrentUser();
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

    public String getCurrentUID() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String uid = null;
        if (user != null) {
            uid = user.getUid();
        }
        return uid;
    }

    public long getDateTime(){

        Calendar c = Calendar.getInstance();
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyyMMdd HHmmss", Locale.UK);
        String currentDateandTime = sdf.format(c.getTime());
        Long dateInLong = 0L;
        try {
            Date date = sdf.parse(currentDateandTime);
            dateInLong = date.getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return dateInLong;
    }

    public void writeNewUser(String username, String email) {
        User user = new User(username, email, getDateTime());
        String currentUID = getCurrentUID();
        user.setUserID(currentUID);
//        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
//                .setDisplayName(username)
////                .setPhotoUri(Uri.parse("https://example.com/jane-q-user/profile.jpg"))
//                .build();

//        mUser.updateProfile(profileUpdates);
        mDatabase.child("users").child(currentUID).setValue(user);
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
                            GeoEntry user = new GeoEntry(getCurrentUID(), title, description, location.getLatitude(), location.getLongitude(), downloadUrl.toString(), type, getDateTime());
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
                    GeoEntry user = new GeoEntry(getCurrentUID(), title, description, location.getLatitude(), location.getLongitude(), "", type, getDateTime());
                    DatabaseReference entryRef = mDatabase.child("files");
                    DatabaseReference pushEntryRef = entryRef.push();
                    user.setEntryID(pushEntryRef.getKey());
                    pushEntryRef.setValue(user);
                }
            }
        });
    }

    public void addFriend(final User selectedUser){
        final String currentUID = getCurrentUID();


        // Get a reference to our posts
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference ref = database.getReference("users");

        ref.child(currentUID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User currentUser = dataSnapshot.getValue(User.class);
                // user 1 - current user - add the selected user to the current user entry
                mDatabase.child("friends").child(currentUID + "/" + selectedUser.getUserID()).setValue(new UserFriend(selectedUser, true));
                // user 2 - selected user - add the current user to the selected user entry
                mDatabase.child("friends").child(selectedUser.getUserID() + "/" + currentUID).setValue(new UserFriend(currentUser, true));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

    }

    public void searchUsers(final SelectFriendsAdapter adapter, String user){
        adapter.clearData();
        mDatabase.child("users").orderByChild("username").equalTo(user).addChildEventListener(new ChildEventListener() {

            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String prevChildKey) {

                User user = dataSnapshot.getValue(User.class);
                adapter.addToUsers(user);

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

    public void fetchUserFriends(final SocialAdapter adapter){

        // Get a reference to our posts
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference ref = database.getReference("friends");

//        ref.child(getCurrentUID()).orderByChild("username").addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot) {
//                UserFriend user = dataSnapshot.getValue(UserFriend.class);
//                System.out.println(user.getUsername());
//            }
//
//            @Override
//            public void onCancelled(DatabaseError databaseError) {
//            }
//        });

        mDatabase.child("friends").child(getCurrentUID()).addChildEventListener(new ChildEventListener() {

            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String prevChildKey) {

                UserFriend user = dataSnapshot.getValue(UserFriend.class);
                System.out.println(user.getUsername());
                adapter.addToUsers(user);

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



    public void readAllEntries(double latitudeStart, double latitudeEnd, final HashMap<String, GeoEntry> entryMap){

        mDatabase.child("files").orderByChild("latitude").startAt(latitudeStart).endAt(latitudeEnd).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String prevChildKey) {
                GeoEntry file = dataSnapshot.getValue(GeoEntry.class);
//                mEntryMap.put(dataSnapshot.getKey(), file);
                entryMap.put(dataSnapshot.getKey(), file);

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
    public void getProfilePic(final ImageView image, String id, int drawableID) {

        if (mUser != null) {
            // Name, email address, and profile photo Url
            Uri photoUrl = mUser.getPhotoUrl();

            if(photoUrl != null){
                StorageReference storageRef = mStorage.getReferenceFromUrl(photoUrl.toString());
                Glide.with(mContext)
                        .using(new FirebaseImageLoader())
                        .load(storageRef)
                        .into(image);
            } else {
                image.setImageResource(drawableID);
            }

        }

//        mDatabase.child("users").equalTo(id).addChildEventListener(new ChildEventListener() {
//            @Override
//            public void onChildAdded(DataSnapshot dataSnapshot, String prevChildKey) {
//                User user = dataSnapshot.getValue(User.class);
//                String path = user.getProfilePath();
//                if (path != null) {
//                    StorageReference storageRef = mStorage.getReferenceFromUrl(user.getProfilePath());
//                    Glide.with(mContext)
//                            .using(new FirebaseImageLoader())
//                            .load(storageRef)
//                            .into(image);
//                }
//            }
//
//            @Override
//            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
//
//            }
//
//            @Override
//            public void onChildRemoved(DataSnapshot dataSnapshot) {
//
//            }
//
//            @Override
//            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
//
//            }
//
//            @Override
//            public void onCancelled(DatabaseError databaseError) {
//
//            }
//        });
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
