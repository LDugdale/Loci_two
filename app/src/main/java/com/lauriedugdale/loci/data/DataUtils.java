package com.lauriedugdale.loci.data;

import android.content.Context;
import android.icu.util.Calendar;
import android.location.Location;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.SparseBooleanArray;
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
import com.lauriedugdale.loci.data.dataobjects.FriendRequest;
import com.lauriedugdale.loci.data.dataobjects.GeoEntry;
import com.lauriedugdale.loci.data.dataobjects.User;
import com.lauriedugdale.loci.data.dataobjects.UserFriend;
import com.lauriedugdale.loci.ui.adapter.FileAdapter;
import com.lauriedugdale.loci.ui.adapter.SelectFriendsAdapter;
import com.lauriedugdale.loci.ui.adapter.SocialAdapter;
import com.lauriedugdale.loci.ui.adapter.SocialRequestAdapter;

import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Created by mnt_x on 14/06/2017.
 */

public class DataUtils {

    // TODO check if friend before adding
    // TODO change button when search for friend if already a friend
    // TODO write reject button - remove from database on reject

    // TODO switch to geofire api to query location more effectively

    // TODO lower case everything

    // TODO stop location querying if only moved a little bit and expand the query parameter to just outside the screen view to allow for this


    private Context mContext;

    // int representation of what media the entry contains
    public static final int NO_MEDIA = 100;
    public static final int IMAGE = 101;
    public static final int AUDIO = 102;

    public static final int ANYONE = 200;
    public static final int FRIENDS = 201;
    public static final int NO_ONE = 202;



    private FusedLocationProviderClient mFusedLocationClient;

    private DatabaseReference mDatabase;
    private FirebaseStorage mStorage;
    private FirebaseUser mUser;

    public DataUtils(Context context) {
        mUser = FirebaseAuth.getInstance().getCurrentUser();
        mContext = context;
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mStorage = FirebaseStorage.getInstance();
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(context);
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
            dateInLong = sdf.parse(currentDateandTime).getTime();
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

    public void writeNewFile(final int permissions, final String title, final String description, final Uri path, final int type) {
        final String uid = getCurrentUID();

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
                            GeoEntry file = new GeoEntry(uid, title, description, location.getLatitude(), location.getLongitude(), downloadUrl.toString(), type, getDateTime());
                            DatabaseReference entryRef = mDatabase.child("files");
                            DatabaseReference pushEntryRef = entryRef.push();
                            file.setEntryID(pushEntryRef.getKey());
                            pushEntryRef.setValue(file);
                            writeAccessPermissionFriends(permissions, uid, file);
                        }
                    }
                });
            }
        });
    }

    public void writeNewFile(final int permissions, final String title, final String description, final int type) {
        final String uid = getCurrentUID();
        mFusedLocationClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                // Got last known location. In some rare situations this can be null.
                if (location != null) {
                    GeoEntry file = new GeoEntry(uid, title, description, location.getLatitude(), location.getLongitude(), "", type, getDateTime());
                    DatabaseReference entryRef = mDatabase.child("files");
                    DatabaseReference pushEntryRef = entryRef.push();
                    file.setEntryID(pushEntryRef.getKey());
                    pushEntryRef.setValue(file);
                    writeAccessPermissionFriends(permissions, uid, file);
                }
            }
        });
    }

    public void writeAccessPermissionFriends(final int permissions, String ownerID, final GeoEntry file){
        System.out.println("writeAccessPermissionFriends");
        System.out.println(permissions);
        final String fileID = file.getEntryID();
        if (permissions == ANYONE) {
            mDatabase.child("file_permission").child("anyone/" + fileID).setValue(file);
        } else if (permissions ==  NO_ONE) {
            mDatabase.child("file_permission").child(ownerID + "/" + fileID).setValue(file);
        } else if (permissions == FRIENDS) {
            mDatabase.child("file_permission").child(ownerID + "/" + fileID).setValue(file);
            final FirebaseDatabase database = FirebaseDatabase.getInstance();
            DatabaseReference ref = database.getReference("friends");
            ref.child(ownerID).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                        UserFriend friend = postSnapshot.getValue(UserFriend.class);
                        mDatabase.child("file_permission").child(friend.getUserID() + "/" + fileID).setValue(file);
                    }
                }
                @Override
                public void onCancelled(DatabaseError databaseError) {
                }
            });
        }


    }

    public void fetchFriendRequests(final SelectFriendsAdapter adapter) {
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        final DatabaseReference ref = database.getReference("users");

        mDatabase.child("friend_requests").orderByChild(getCurrentUID()).equalTo(true).addChildEventListener(new ChildEventListener() {

            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String prevChildKey) {
                String fromUser = dataSnapshot.getKey();
                ref.child(fromUser).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        User user = dataSnapshot.getValue(User.class);
                        adapter.addToUsers(user);
                        adapter.notifyDataSetChanged();

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                    }
                });

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
     * status true - pending
     * status false - accepted
     *
     * @param fromUser
     * @param fromUser
     * @param status
     */
    public void addFriendRequest( String fromUser, String toUser, boolean status) {
//        {
//            "friend_requests": {
//                // user Ids who sent request
//                "userId1": {
//                    // receiver ids
//                    "userId2": true,
//                    "userId3": true,
//                    "userId4": true
//                },
//                "userId2": {
//                    "userId3": true,
//                    "userId4": true
//                }
//            }
//        }

        // user 1 - current user - add the selected user to the current user entry
        mDatabase.child("friend_requests").child(fromUser).child(toUser).setValue(status);
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

                addFriendRequest(selectedUser.getUserID(), currentUID, false);
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

        mDatabase.child("friends").child(getCurrentUID()).addChildEventListener(new ChildEventListener() {

            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String prevChildKey) {
                UserFriend user = dataSnapshot.getValue(UserFriend.class);
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


    public void readAllEntries(double latitudeStart, double latitudeEnd, final long fromTime, final long toTime, final SparseBooleanArray typesMap, final HashMap<String, GeoEntry> entryMap){
        String currentUID = getCurrentUID();
        // Get a reference to our posts
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference ref = database.getReference("file_permission");
        ref.child(currentUID).orderByChild("latitude").startAt(latitudeStart).endAt(latitudeEnd).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    GeoEntry entry = postSnapshot.getValue(GeoEntry.class);

                    long date = entry.getUploadDate();
                    if(fromTime <= date && date <= toTime && typesMap.get(entry.getFileType())){
                        System.out.println("inside the if statement" + entry.getTitle());
                        entryMap.put(entry.getEntryID(), entry);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

        ref.child("anyone").orderByChild("latitude").startAt(latitudeStart).endAt(latitudeEnd).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    GeoEntry entry = postSnapshot.getValue(GeoEntry.class);
                    long date = entry.getUploadDate();
                    if(fromTime <= date && date <= toTime && typesMap.get(entry.getFileType())) {
                        entryMap.put(entry.getEntryID(), entry);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    public void fetchUserFiles(final FileAdapter adapter){
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference ref = database.getReference("files");
        ref.orderByChild("creator").equalTo(getCurrentUID()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    GeoEntry entry = postSnapshot.getValue(GeoEntry.class);
                    adapter.addToFiles(entry);
                    // notify the adapter that data has been changed in order for it to be displayed in recyclerview
                    adapter.notifyDataSetChanged();
                }
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
    }


    /**
     * Updates the specified image view
     * @param image
     * @param id
     * @param path
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
