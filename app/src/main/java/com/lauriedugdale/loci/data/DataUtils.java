package com.lauriedugdale.loci.data;

import android.content.Context;
import android.icu.util.Calendar;
import android.location.Location;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.SparseBooleanArray;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

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
import com.lauriedugdale.loci.EntriesDownloadedListener;
import com.lauriedugdale.loci.data.dataobjects.CameraPoint;
import com.lauriedugdale.loci.data.dataobjects.Comment;
import com.lauriedugdale.loci.data.dataobjects.FilterOptions;
import com.lauriedugdale.loci.data.dataobjects.GeoEntry;
import com.lauriedugdale.loci.data.dataobjects.Group;
import com.lauriedugdale.loci.data.dataobjects.User;
import com.lauriedugdale.loci.data.dataobjects.UserFriend;
import com.lauriedugdale.loci.ui.adapter.CommentsAdapter;
import com.lauriedugdale.loci.ui.adapter.FetchGroupsAdapter;
import com.lauriedugdale.loci.ui.adapter.FileAdapter;
import com.lauriedugdale.loci.ui.adapter.GroupsAdapter;
import com.lauriedugdale.loci.ui.adapter.NotificationFriendsAdapter;
import com.lauriedugdale.loci.ui.adapter.SelectForGroupAdapter;
import com.lauriedugdale.loci.ui.adapter.FriendsAdapter;
import com.lauriedugdale.loci.ui.adapter.search.SearchEntriesSection;
import com.lauriedugdale.loci.ui.adapter.search.SearchGroupsSection;
import com.lauriedugdale.loci.ui.adapter.search.SearchUsersSection;
import com.lauriedugdale.loci.utils.FilterView;
import com.lauriedugdale.loci.utils.SocialUtils;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import io.github.luizgrp.sectionedrecyclerviewadapter.SectionedRecyclerViewAdapter;

/**
 * Created by mnt_x on 14/06/2017.
 */

public class DataUtils {

    // TODO write reject button - remove from database on reject
    // TODO switch to geofire api to query location more effectively
    // TODO IMPROVE SEARCH
    // TODO stop location querying if only moved a little bit and expand the query parameter to just outside the screen view to allow for this

    private Context mContext;

    // int representation of what media the entry contains
    public static final int NO_MEDIA = 100;
    public static final int IMAGE = 101;
    public static final int AUDIO = 102;

    // codes for permission access
    public static final int ANYONE = 200;
    public static final int FRIENDS = 201;
    public static final int NO_ONE = 202;
    public static final int GROUP = 203;


    private DatabaseReference mDatabase;
    private FirebaseStorage mStorage;
    private FirebaseUser mUser;

    public DataUtils(Context context) {
        mUser = FirebaseAuth.getInstance().getCurrentUser();
        mContext = context;
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mStorage = FirebaseStorage.getInstance();
    }


    /**
     * Get the date used for adding to entries in the database
     *
     * @return time in long format
     */
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

    /**
     * Gets the user ID of rhe logged in User
     * @return the user ID
     */
    public String getCurrentUID() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String uid = null;
        if (user != null) {
            uid = user.getUid();
        }
        return uid;
    }

    /**
     * Gets the user ID of rhe logged in User
     * @return the user ID
     */
    public String getCurrentUsername() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String username = null;
        if (user != null) {
            username = user.getDisplayName();
        }
        return username;
    }

    /**
     * Writes a new user to the database
     * @param username The username to be written
     * @param email The email of the username to be written
     */
    public void writeNewUser(String username, String email) {
        User user = new User(username, email, getDateTime());
        FirebaseUser userAuth = FirebaseAuth.getInstance().getCurrentUser();
        user.setUserID(userAuth.getUid());
        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder().setDisplayName(username).build();
        userAuth.updateProfile(profileUpdates);
        mDatabase.child("users").child(getCurrentUID()).setValue(user);
    }

    public void writeEntryWithFile(final int permissions, final String title, final String description, final Uri path, final int type, final Group group, final Location location) {
        final String uid = getCurrentUID();
        StorageReference storageRef = mStorage.getReference();

        StorageReference ref = storageRef.child(getCurrentUID() + "/type/"  + getDateTime());
        UploadTask uploadTask = ref.putFile(path);

        // Register observers to listen for when the download is done or if it fails
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // TODO Handle unsuccessful uploads
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                // taskSnapshot.getMetadata() contains file metadata such as size, content-type, and download URL.
                final Uri downloadUrl = taskSnapshot.getDownloadUrl();

                GeoEntry file = new GeoEntry(uid,
                        mUser.getDisplayName(),
                        title,
                        description,
                        location.getLatitude(),
                        location.getLongitude(),
                        0,
                        downloadUrl.toString(),
                        type,
                        getDateTime(),
                        group.getGroupName(),
                        group.getGroupID());

                DatabaseReference entryRef = mDatabase.child("files");
                DatabaseReference pushEntryRef = entryRef.push();
                file.setEntryID(pushEntryRef.getKey());
                pushEntryRef.setValue(file);
                writeAccessPermissionFriends(permissions, uid, file, group.getGroupID());
            }
        });
    }

    public void writeEntry(final int permissions, final String title, final String description, final int type, final Group group, Location location) {
        final String uid = getCurrentUID();

        GeoEntry file = new GeoEntry(uid,
                mUser.getDisplayName(),
                title,
                description,
                location.getLatitude(),
                location.getLongitude(),
                0,
                "",
                type,
                getDateTime(),
                group.getGroupName(),
                group.getGroupID());

        DatabaseReference entryRef = mDatabase.child("files");
        DatabaseReference pushEntryRef = entryRef.push();
        file.setEntryID(pushEntryRef.getKey());
        pushEntryRef.setValue(file);
        writeAccessPermissionFriends(permissions, uid, file, group.getGroupID());
    }

    public void writeAccessPermissionFriends(final int permissions, String ownerID, final GeoEntry file, String groupID){
        final String entryID = file.getEntryID();
        if (permissions == ANYONE) {
            mDatabase.child("file_permission").child("anyone/" + entryID).setValue(file);
        } else if (permissions ==  NO_ONE) {
            mDatabase.child("file_permission").child(ownerID + "/" + entryID).setValue(file);
        } else if (permissions == FRIENDS) {
            mDatabase.child("file_permission").child(ownerID + "/" + entryID).setValue(file);
            final FirebaseDatabase database = FirebaseDatabase.getInstance();
            DatabaseReference ref = database.getReference("friends");
            ref.child(getCurrentUID()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                        User friend = postSnapshot.getValue(User.class);
                        mDatabase.child("file_permission").child(friend.getUserID() + "/" + entryID).setValue(file);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                }
            });
        } else {
            mDatabase.child("file_permission").child(groupID + "/" + entryID).setValue(file);
        }
    }

    public void fetchFriendRequests(final NotificationFriendsAdapter adapter) {
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        final DatabaseReference ref = database.getReference("users");
        mDatabase.child("friend_requests").child(getCurrentUID()).addChildEventListener(new ChildEventListener() {

            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String prevChildKey) {
                String fromUser = dataSnapshot.getKey();
                if(!(boolean)dataSnapshot.getValue()) {
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

    public void addGroupRequest(final TextView joinButton, final Group group){
        final String currentUID = getCurrentUID();

        // Get a reference to our posts
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference ref = database.getReference("group_access");

        ref.child(currentUID).addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if (dataSnapshot.hasChild(group.getGroupID()) ){

                    joinButton.setText("Joined");
                } else {

                    joinButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            int whoCanPost = SocialUtils.VIEWER;
                            if (group.getEveryoneAdmin().equals("everyone")){
                                whoCanPost += SocialUtils.EVERYONE_POSTS;
                            }
                            mDatabase.child("group_permission").child(group.getGroupID()).child(getCurrentUID()).setValue(whoCanPost);
                            mDatabase.child("group_members").child(group.getGroupID() + "/"  + currentUID).setValue(getCurrentUsername());
                            mDatabase.child("group_access").child(getCurrentUID() + "/" + group.getGroupID()).setValue(group).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    joinButton.setText("Joined");
                                }
                            });
                        }
                    });
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    /**
     * status false - pending
     * status true - accepted
     *
     *
     * @param status
     */
    public void addFriendRequest(final TextView addButton, final String toUser, final boolean status) {
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

        final String currentUID = getCurrentUID();

        // Get a reference to our posts
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference ref = database.getReference("friend_requests");

        ref.child(currentUID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if (dataSnapshot.hasChild(toUser) ){
                    if ((boolean)dataSnapshot.child(toUser).getValue()){
                        addButton.setText("Friends");
                    } else {
                        addButton.setText("Pending");
                    }
                } else {

                    addButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            mDatabase.child("friend_requests").child(toUser).child(currentUID).setValue(status).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    addButton.setText("Pending");
                                }
                            });
                        }
                    });
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
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

                mDatabase.child("friend_requests").child(currentUID).child(selectedUser.getUserID()).setValue(true);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

    }

    public void createGroupWithoutPic(final HashMap<String, String> usersToAdd, String groupName, boolean isPrivate){

        //TODO use cloud functions to make this one upload!
        String privateOrPublic = "public";
        if(isPrivate){
            privateOrPublic = "private";
        }

        // add group to group
        final Group group = new Group(groupName);
        group.setPrivatePublic(privateOrPublic);
        DatabaseReference entryRef = mDatabase.child("groups");
        DatabaseReference pushEntryRef = entryRef.push();
        group.setGroupID(pushEntryRef.getKey());
        pushEntryRef.setValue(group);

        mDatabase.child("groups").child(group.getGroupID()).setValue(group);
        mDatabase.child("group_access").child(getCurrentUID() + "/" + group.getGroupID()).setValue(group);
        mDatabase.child("group_permission").child(group.getGroupID()).child(getCurrentUID()).setValue(SocialUtils.CREATOR);
        mDatabase.child("group_members").child(group.getGroupID() + "/"  + getCurrentUID()).setValue(getCurrentUsername());

        String userID = "";
        String username = "";
        for(Map.Entry<String,String> entry : usersToAdd.entrySet()){
            userID = entry.getKey();
            username = entry.getValue();
            mDatabase.child("group_access").child(userID + "/" + group.getGroupID()).setValue(group);
            mDatabase.child("group_permission").child(group.getGroupID()).child(userID).setValue(SocialUtils.VIEWER);
            mDatabase.child("group_members").child(group.getGroupID() + "/"  + userID).setValue(username);
        }
    }

    public void createGroupWithPic(final HashMap<String, String> usersToAdd, String groupName, Uri path, boolean isPrivate){

        //TODO use cloud functions to make this one upload!
        String privateOrPublic = "public";
        if(isPrivate){
            privateOrPublic = "private";
        }


        // add group to group
        final Group group = new Group(groupName);
        group.setPrivatePublic(privateOrPublic);
        DatabaseReference entryRef = mDatabase.child("groups");
        DatabaseReference pushEntryRef = entryRef.push();
        group.setGroupID(pushEntryRef.getKey());
        pushEntryRef.setValue(group);

        //upload group profile pic
        StorageReference storageRef = mStorage.getReference();
        StorageReference ref = storageRef.child(group.getGroupID() + "/group_picture/" + getDateTime());
        UploadTask uploadTask = ref.putFile(path);
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle unsuccessful uploads
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                // taskSnapshot.getMetadata() contains file metadata such as size, content-type, and download URL.
                mDatabase.child("groups").child(group.getGroupID()).setValue(group);
                final Uri downloadUrl = taskSnapshot.getDownloadUrl();
                group.setProfilePicturePath(downloadUrl.toString());
                mDatabase.child("group_access").child(getCurrentUID() + "/" + group.getGroupID()).setValue(group);
                mDatabase.child("group_permission").child(group.getGroupID()).child(getCurrentUID()).setValue(SocialUtils.CREATOR);
                mDatabase.child("group_members").child(group.getGroupID() + "/"  + getCurrentUID()).setValue(getCurrentUsername());

                String userID = "";
                String username = "";
                for(Map.Entry<String,String> entry : usersToAdd.entrySet()){
                    userID = entry.getKey();
                    username = entry.getValue();
                    mDatabase.child("group_access").child(userID + "/" + group.getGroupID()).setValue(group);
                    mDatabase.child("group_permission").child(group.getGroupID()).child(userID).setValue(SocialUtils.VIEWER);
                    mDatabase.child("group_members").child(group.getGroupID() + "/"  + userID).setValue(username);
                }
            }
        });
    }

    public void fetchUserAcessibleGroups(final FetchGroupsAdapter adapter){

        final String currentUID = getCurrentUID();

        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        final DatabaseReference ref = database.getReference("groups");
        final String [] gID = new String [1];
        mDatabase.child("group_permission").orderByChild(currentUID).startAt(SocialUtils.ADMIN).addChildEventListener(new ChildEventListener() {

            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String prevChildKey) {

                gID[0] = dataSnapshot.getKey();
                ref.orderByChild(gID[0]).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                            System.out.println(postSnapshot);
                            Group group = postSnapshot.getValue(Group.class);
                            adapter.addToGroups(group);
                        }
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

    public void fetchUserFriends(final SelectForGroupAdapter adapter){
        final String currentUID = getCurrentUID();
        // Get a reference to our posts
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference ref = database.getReference("friends");

        ref.child(currentUID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    User user = postSnapshot.getValue(User.class);
                    if(!currentUID.equals(user.getUserID())) {
                        adapter.addToUsers(user);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

    }

    public void fetchUserAcessibleGroups(final GroupsAdapter adapter){

        final String currentUID = getCurrentUID();
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        final DatabaseReference ref = database.getReference("groups");

        mDatabase.child("group_access").child(currentUID).addChildEventListener(new ChildEventListener() {

            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String prevChildKey) {
                Group group = dataSnapshot.getValue(Group.class);

                ref.child(group.getGroupID()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        System.out.println(dataSnapshot);
                        Group group = dataSnapshot.getValue(Group.class);
                        System.out.println("THE NAME : " + group.getGroupName());
                        adapter.addToGroups(group);
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

    public void fetchUserFriends(final FriendsAdapter adapter){

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

    public void fetchGroupProfileEntries(final FileAdapter adapter, String groupID){
        // Get a reference to our posts
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference ref = database.getReference("file_permission");
        ref.child(groupID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    GeoEntry entry = postSnapshot.getValue(GeoEntry.class);
                    adapter.addToFiles(entry);
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    public void fetchProfileEntries(final FileAdapter adapter, String userID){
        String currentUID = getCurrentUID();
        // Get a reference to our posts
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference ref = database.getReference("file_permission");
        ref.child(currentUID).orderByChild("creator").equalTo(userID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    GeoEntry entry = postSnapshot.getValue(GeoEntry.class);
                    adapter.addToFiles(entry);
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

        ref.child("anyone").orderByChild("creator").equalTo(userID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    GeoEntry entry = postSnapshot.getValue(GeoEntry.class);
                    adapter.addToFiles(entry);
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }


    public void readAllEntries(final double latitudeStart, final double latitudeEnd, final FilterOptions fo, final HashMap<String, GeoEntry> entryMap, final EntriesDownloadedListener listener){
        final String currentUID = getCurrentUID();
        final long fromTime = fo.getNumericalFromDate();
        final long toTime = fo.getNumericalToDate();
        final SparseBooleanArray typesMap = fo.getCheckedTypes();

        // Get a reference to our posts
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        final DatabaseReference ref = database.getReference("file_permission");

        if (fo.getFilterView() == FilterView.everyone) {
            ref.child(currentUID).orderByChild("latitude").startAt(latitudeStart).endAt(latitudeEnd).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                        GeoEntry entry = postSnapshot.getValue(GeoEntry.class);

                        long date = entry.getUploadDate();
                        if (fromTime <= date && date <= toTime && typesMap.get(entry.getFileType())) {
                            entryMap.put(entry.getEntryID(), entry);
                        }
                    }
                    ref.child("anyone").orderByChild("latitude").startAt(latitudeStart).endAt(latitudeEnd).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                                GeoEntry entry = postSnapshot.getValue(GeoEntry.class);
                                long date = entry.getUploadDate();
                                if (fromTime <= date && date <= toTime && typesMap.get(entry.getFileType())) {
                                    entryMap.put(entry.getEntryID(), entry);
                                }
                            }

                            final DatabaseReference gRef = database.getReference("file_permission");
                            final String [] gID = new String [1];
                            mDatabase.child("group_permission").orderByChild(currentUID).addChildEventListener(new ChildEventListener() {

                                @Override
                                public void onChildAdded(DataSnapshot dataSnapshot, String prevChildKey) {

                                    gID[0] = dataSnapshot.getKey();
                                    gRef.child(gID[0]).orderByChild("latitude").startAt(latitudeStart).endAt(latitudeEnd).addListenerForSingleValueEvent(new ValueEventListener() {

                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {

                                            for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {

                                                GeoEntry entry = postSnapshot.getValue(GeoEntry.class);
                                                entryMap.put(entry.getEntryID(), entry);
                                            }

                                            listener.onEntriesFetched();
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

        if (fo.getFilterView() == FilterView.groups) {
            final DatabaseReference gRef = database.getReference("file_permission");
            final String [] gID = new String [1];
            mDatabase.child("group_permission").orderByChild(currentUID).addChildEventListener(new ChildEventListener() {

                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String prevChildKey) {

                    gID[0] = dataSnapshot.getKey();
                    gRef.child(gID[0]).orderByChild("latitude").startAt(latitudeStart).endAt(latitudeEnd).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                                GeoEntry entry = postSnapshot.getValue(GeoEntry.class);
                                entryMap.put(entry.getEntryID(), entry);
                            }

                            listener.onEntriesFetched();

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

        if (fo.getFilterView() == FilterView.user) {
            DatabaseReference uRef = database.getReference("files");
            uRef.orderByChild("creator").equalTo(getCurrentUID()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                        GeoEntry entry = postSnapshot.getValue(GeoEntry.class);
                        entryMap.put(entry.getEntryID(), entry);
                    }

                    listener.onEntriesFetched();
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                }
            });
        }

        if (fo.getFilterView() == FilterView.friends) {
            final String uID = getCurrentUID();
            DatabaseReference fRef = database.getReference("file_permission");
            fRef.child(uID).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                        GeoEntry entry = postSnapshot.getValue(GeoEntry.class);
                        if(!entry.getCreator().equals(uID)) {
                            entryMap.put(entry.getEntryID(), entry);
                        }
                    }
                    listener.onEntriesFetched();
                }
                @Override
                public void onCancelled(DatabaseError databaseError) {
                }
            });
        }
    }

    public void readGroupEntries(String groupID, final FilterOptions fo, final HashMap<String, GeoEntry> entryMap, final EntriesDownloadedListener listener){
        // Get a reference to our posts
        final long fromTime = fo.getNumericalFromDate();
        final long toTime = fo.getNumericalToDate();
        final SparseBooleanArray typesMap = fo.getCheckedTypes();

        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference ref = database.getReference("group_files");
        ref.child(groupID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    GeoEntry entry = postSnapshot.getValue(GeoEntry.class);

                    long date = entry.getUploadDate();
                    if(fromTime <= date && date <= toTime && typesMap.get(entry.getFileType())){
                        entryMap.put(entry.getEntryID(), entry);
                    }
                }
                listener.onEntriesFetched();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

    }

    public void readUserEntries(final String userID, final FilterOptions fo, final HashMap<String, GeoEntry> entryMap, final EntriesDownloadedListener listener){
        String currentUID = getCurrentUID();
        final long fromTime = fo.getNumericalFromDate();
        final long toTime = fo.getNumericalToDate();
        final SparseBooleanArray typesMap = fo.getCheckedTypes();

        // Get a reference to our posts
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        final DatabaseReference ref = database.getReference("file_permission");
        ref.child(currentUID).orderByChild("creator").equalTo(userID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    GeoEntry entry = postSnapshot.getValue(GeoEntry.class);

                    long date = entry.getUploadDate();
                    if(fromTime <= date && date <= toTime && typesMap.get(entry.getFileType())){
                        entryMap.put(entry.getEntryID(), entry);
                    }
                }
                ref.child("anyone").orderByChild("creator").equalTo(userID).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                            GeoEntry entry = postSnapshot.getValue(GeoEntry.class);
                            long date = entry.getUploadDate();
                            if(fromTime <= date && date <= toTime && typesMap.get(entry.getFileType())) {
                                entryMap.put(entry.getEntryID(), entry);
                            }
                        }
                        listener.onEntriesFetched();

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

    public void readAllEntriesForAR(final double latitudeStart, final double latitudeEnd, final long fromTime, final long toTime, final SparseBooleanArray typesMap, final ArrayList<CameraPoint> entryList){
        String currentUID = getCurrentUID();
        // Get a reference to our posts
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        final DatabaseReference ref = database.getReference("file_permission");
        ref.child(currentUID).orderByChild("latitude").startAt(latitudeStart).endAt(latitudeEnd).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    GeoEntry entry = postSnapshot.getValue(GeoEntry.class);
                    long date = entry.getUploadDate();
                    if(fromTime <= date && date <= toTime && typesMap.get(entry.getFileType())){
                        entryList.add(new CameraPoint(entry));
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
                        entryList.add(new CameraPoint(entry));
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }


    public void fetchProfileFiles(final FileAdapter adapter, String userID){
//        final FirebaseDatabase database = FirebaseDatabase.getInstance();
//        DatabaseReference ref = database.getReference("files");
//        ref.orderByChild("creator").equalTo(userID).addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot) {
//                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
//                    GeoEntry entry = postSnapshot.getValue(GeoEntry.class);
//                    adapter.addToFiles(entry);
//                    // notify the adapter that data has been changed in order for it to be displayed in recyclerview
//                    adapter.notifyDataSetChanged();
//                }
//            }
//
//            @Override
//            public void onCancelled(DatabaseError databaseError) {
//            }
//        });


    }

    public void fetchUserFiles(final FileAdapter adapter){
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference ref = database.getReference("files");
        ref.orderByChild("creator").equalTo(getCurrentUID()).addListenerForSingleValueEvent(new ValueEventListener() {
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

    public void fetchUserSharedFiles(final FileAdapter adapter){
        final String uID = getCurrentUID();
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference ref = database.getReference("file_permission");
        ref.child(uID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    GeoEntry entry = postSnapshot.getValue(GeoEntry.class);
                    if(!entry.getCreator().equals(uID)) {
                        adapter.addToFiles(entry);
                        // notify the adapter that data has been changed in order for it to be displayed in recyclerview
                        adapter.notifyDataSetChanged();
                    }
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    /**
     * For reading a single GeoEntry
     * @param path
     */
    public void getFilePic(final ImageView image, String path, int drawableID, int type) {

        Uri filePath = Uri.parse(path);

        if(type == IMAGE){
            StorageReference storageRef = mStorage.getReferenceFromUrl(filePath.toString());
            Glide.with(mContext)
                    .using(new FirebaseImageLoader())
                    .load(storageRef)
                    .into(image);
        } else {
            image.setImageResource(drawableID);
        }

    }

    public void checkGroupAdmin(final ImageView settingsButton, String groupID){

        final String uID = getCurrentUID();
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference ref = database.getReference("group_permission");

        ref.child(groupID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if (dataSnapshot.hasChild(uID)){
                    long access = (long)dataSnapshot.child(uID).getValue();

                    if(access > 20){
                        access = access - SocialUtils.EVERYONE_POSTS;
                    }
                    if (access == SocialUtils.CREATOR || access == SocialUtils.ADMIN){
                        settingsButton.setVisibility(View.VISIBLE);
                    }
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    public void fetchUsersToSelect(final SelectForGroupAdapter adapter, final Group group){

        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        final DatabaseReference ref = database.getReference("users");
        final String [] uID = new String [1];
        final long [] access = new long [1];

        mDatabase.child("group_permission").child(group.getGroupID()).addChildEventListener(new ChildEventListener() {

            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String prevChildKey) {

                uID[0] = dataSnapshot.getKey();
                access[0] = (long) dataSnapshot.getValue();
                if (access[0] != SocialUtils.CREATOR || !uID.equals(getCurrentUID())) {

                    ref.child(uID[0]).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            User user = dataSnapshot.getValue(User.class);
                            if(access[0] == SocialUtils.ADMIN){
                                adapter.getCheckedItems().put(uID[0], user.getUsername());
                            }
                            adapter.addToUsers(user);
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                        }
                    });
                }
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

    public void changeAdminPermissions(final Group group, final HashMap<String, String> userMap){

        String uID = "";
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference ref = database.getReference("group_permission");

        ref.child(group.getGroupID()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    String uID = (String)postSnapshot.getKey();
                    long access = (long)dataSnapshot.child(uID).getValue();

                    if (userMap.containsKey(uID) && access != SocialUtils.ADMIN){
                        mDatabase.child("group_permission").child(group.getGroupID()).child(uID).setValue(SocialUtils.ADMIN);
                    } else if (!userMap.containsKey(uID) && access == SocialUtils.ADMIN){
                        mDatabase.child("group_permission").child(group.getGroupID()).child(uID).setValue(SocialUtils.VIEWER);
                    }

                }

            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    public void changeWhoPosts(final Group group){

        mDatabase.child("groups").child(group.getGroupID()).setValue(group);

        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference ref = database.getReference("group_permission");

        ref.child(group.getGroupID()).addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    String uID = postSnapshot.getKey();
                    long access = (long)dataSnapshot.child(uID).getValue();
                    mDatabase.child("group_permission").child(group.getGroupID()).child(uID).setValue(changeAccess(group, access));
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    private long changeAccess(Group group, long access){

        long val = access;

        if (access < 20 && group.getEveryoneAdmin().equals("everyone")){
            val = access + SocialUtils.EVERYONE_POSTS;
        } else if (access > 20 && group.getEveryoneAdmin().equals("admin")){
            val = access - SocialUtils.EVERYONE_POSTS;
        }

        return val;
    }

    /**
     * For reading a single GeoEntry
     */
    public void getGroupPic(final ImageView image, int drawableID, String path) {

        if(path != null){
            StorageReference storageRef = mStorage.getReferenceFromUrl(path.toString());
            Glide.with(mContext)
                    .using(new FirebaseImageLoader())
                    .load(storageRef)
                    .asBitmap()
                    .override(400, 400) // resizes the image to these dimensions (in pixel)
                    .centerCrop()
                    .into(image);
        } else {
            image.setImageResource(drawableID);
        }
    }

    public void setNewPofilePicture(final Group group, Uri path){

        StorageReference storageRef = mStorage.getReference();
        StorageReference ref = storageRef.child(group.getGroupID() + "/group_picture/" + getDateTime());
        UploadTask uploadTask = ref.putFile(path);
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {

            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                String url = taskSnapshot.getDownloadUrl().toString();
                mDatabase.child("groups/" + group.getGroupID()).child("profilePicturePath").setValue(url);
            }
        });
    }

    public void setNewPofilePicture(final Uri path){
        final String currentID = getCurrentUID();

        StorageReference storageRef = mStorage.getReference();
        StorageReference ref = storageRef.child(currentID + "/profile_picture/" + getDateTime());
        UploadTask uploadTask = ref.putFile(path);
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {

            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Uri url = taskSnapshot.getDownloadUrl();

                FirebaseUser userAuth = FirebaseAuth.getInstance().getCurrentUser();
                UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder().setPhotoUri(url).build();
                userAuth.updateProfile(profileUpdates);

                mDatabase.child("users/" + currentID).child("profilePath").setValue(url.toString());
            }
        });
    }

    public void setProfileBio(String bio){
        String userID = getCurrentUID();
        mDatabase.child("users/" + userID).child("bio").setValue(bio);
    }

    public void getProfileBio(final EditText text){
        String userID = getCurrentUID();

        // Get a reference to our posts
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference ref = database.getReference("users");

        ref.child(userID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);

                text.setText(user.getBio());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    /**
     * For reading a single GeoEntry
     */
    public void getNonLoggedInProfilePic(String userID, final ImageView image, final int drawableID) {

        // Get a reference to our posts
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference ref = database.getReference("users");

        ref.child(userID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);

                String photoUrl = user.getProfilePath();
                if(photoUrl != null){
                    StorageReference storageRef = mStorage.getReferenceFromUrl(photoUrl);
                    Glide.with(mContext)
                            .using(new FirebaseImageLoader())
                            .load(storageRef)
                            .asBitmap()
                            .override(400, 400)
                            .centerCrop()
                            .into(image);
                } else {
                    image.setImageResource(drawableID);
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });



    }

    /**
     * For reading a single GeoEntry
     */
    public void getProfilePic(final ImageView image, int drawableID) {

        if (mUser != null) {
            Uri photoUrl = mUser.getPhotoUrl();
            if(photoUrl != null){
                System.out.println(photoUrl.toString());
                StorageReference storageRef = mStorage.getReferenceFromUrl(photoUrl.toString());
                Glide.with(mContext)
                        .using(new FirebaseImageLoader())
                        .load(storageRef)
                        .asBitmap()
                        .override(400, 400) // resizes the image to these dimensions (in pixel)
                        .centerCrop()
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

    public void addComment(Comment comment, String entryID) {

        DatabaseReference entryRef = mDatabase.child("comments/" + entryID);
        DatabaseReference pushEntryRef = entryRef.push();
        comment.setCommentID(pushEntryRef.getKey());
        pushEntryRef.setValue(comment);
    }

    public void getComments(final CommentsAdapter adapter, String entryID, final EntriesDownloadedListener listener) {

        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference ref = database.getReference("comments");

        ref.child(entryID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                adapter.clearData();
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    Comment comment = postSnapshot.getValue(Comment.class);
                    adapter.addToComments(comment);
                }
                listener.onEntriesFetched();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

    }


    public void search(final SectionedRecyclerViewAdapter adapter, final SearchUsersSection usersSection, final SearchGroupsSection groupsSection, final SearchEntriesSection entriesSection, final String soFar){

        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference ref = database.getReference("users");

        ref.orderByChild("queryUsername").startAt(soFar).endAt(soFar + "\uf8ff").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                usersSection.clearData();
                adapter.notifyDataSetChanged();

                if (!dataSnapshot.hasChildren()){
                    usersSection.setVisible(false);
                } else if (dataSnapshot.hasChildren() && !usersSection.isVisible()){
                    usersSection.setVisible(true);
                }

                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    User user = postSnapshot.getValue(User.class);
                    usersSection.addToUsers(user);
                }
                // notify the adapter that data has been changed in order for it to be displayed in recyclerview
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

        String groupSoFar = "PUBLIC__" + soFar;
        ref = database.getReference("groups");
        ref.orderByChild("queryGroupName").startAt(groupSoFar).endAt(groupSoFar + "\uf8ff").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                groupsSection.clearData();
                adapter.notifyDataSetChanged();
                if (!dataSnapshot.hasChildren()){

                    groupsSection.setVisible(false);
                } else if (dataSnapshot.hasChildren() && !groupsSection.isVisible()){

                    groupsSection.setVisible(true);
                }

                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    Group group = postSnapshot.getValue(Group.class);
                    groupsSection.addToGroups(group);
                    adapter.notifyDataSetChanged();

                }
                // notify the adapter that data has been changed in order for it to be displayed in recyclerview
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

        final String currentUID = getCurrentUID();
        ref = database.getReference("file_permission");
        final DatabaseReference finalRef = ref;
        ref.child(currentUID).orderByChild("queryTitle").startAt(soFar).endAt(soFar + "\uf8ff").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(final DataSnapshot userDataSnapshot) {
                entriesSection.clearData();
                adapter.notifyDataSetChanged();
                for (DataSnapshot postSnapshot : userDataSnapshot.getChildren()) {
                    GeoEntry entry = postSnapshot.getValue(GeoEntry.class);
                    entriesSection.addToEntries(entry);
                }
                adapter.notifyDataSetChanged();

                finalRef.child("anyone").orderByChild("queryTitle").startAt(soFar).endAt(soFar + "\uf8ff").addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        if (!dataSnapshot.hasChildren() && !userDataSnapshot.hasChildren()){
                            entriesSection.setVisible(false);
                        } else if (( dataSnapshot.hasChildren() || userDataSnapshot.hasChildren() ) && !entriesSection.isVisible()){
                            entriesSection.setVisible(true);
                        }

                        for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                            GeoEntry entry = postSnapshot.getValue(GeoEntry.class);
                            entriesSection.addToEntries(entry);

                        }
                        adapter.notifyDataSetChanged();
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
}
