package com.lauriedugdale.loci.data;

import android.content.Context;
import android.location.Location;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.lauriedugdale.loci.EntriesDownloadedListener;
import com.lauriedugdale.loci.data.dataobjects.CameraPoint;
import com.lauriedugdale.loci.data.dataobjects.FilterOptions;
import com.lauriedugdale.loci.data.dataobjects.GeoEntry;
import com.lauriedugdale.loci.data.dataobjects.Group;
import com.lauriedugdale.loci.data.dataobjects.User;
import com.lauriedugdale.loci.ui.adapter.FileAdapter;
import com.lauriedugdale.loci.ui.adapter.nearme.HeroNearMeAdapter;
import com.lauriedugdale.loci.ui.adapter.nearme.NearMeEntryAdapter;
import com.lauriedugdale.loci.utils.DataUtils;
import com.lauriedugdale.loci.utils.FilterView;
import com.lauriedugdale.loci.utils.LocationUtils;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * This class handles the uploading and downloading of entries
 *
 * @author Laurie Dugdale
 */

public class EntryDatabase extends LociData {

    private static final String TAG = EntryDatabase.class.getSimpleName();


    public EntryDatabase(Context context) {
        super(context);
    }

    /**
     * -------------------------------------------------------------------------------------
     * ----------------------------------- Entry Upload ------------------------------------
     * -------------------------------------------------------------------------------------
     */

    /**
     * uploads entries to the entry_location section of the database, for querying using the
     * geofire API.
     *
     * @param entryKey The key of the geoentry
     * @param latitude The latitude of the geoentry
     * @param longitude The Longitude of the geoentry
     */
    private void uploadEntryLocation(String entryKey, double latitude, double longitude){

        Log.d(TAG, "Uploading GeoEntry location too 'entry_location'");

        DatabaseReference database = FirebaseDatabase.getInstance().getReference("entry_location");
        GeoFire geoFire = new GeoFire(database);
        geoFire.setLocation(entryKey, new GeoLocation(latitude, longitude));
    }

    /**
     * Upload a GeoEntry that contains the Uri to a file.
     *
     * @param permissions
     * @param title
     * @param description
     * @param path
     * @param type
     * @param group
     * @param location
     */
    public void uploadEntryWithFile(final int permissions, final String title, final String description, final Uri path, final int type, final Group group, final Location location) {

        Log.d(TAG, "Uploading GeoEntry");

        final String uid = getCurrentUID();
        StorageReference storageRef = getStorage().getReference();

        StorageReference ref = storageRef.child(getCurrentUID() + "/type/"  + DataUtils.getDateTime());
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
                        getUser().getDisplayName(),
                        title,
                        description,
                        location.getLatitude(),
                        location.getLongitude(),
                        0,
                        downloadUrl.toString(),
                        type,
                        DataUtils.getDateTime(),
                        group.getGroupName(),
                        group.getGroupID(),
                        DataUtils.FROM_SELF);

                DatabaseReference entryRef = getDatabase().child("entries");
                DatabaseReference pushEntryRef = entryRef.push();
                String entryID = pushEntryRef.getKey();
                file.setEntryID(entryID);
                pushEntryRef.setValue(file);

                // organise entry in a separate place to make it easy to search the entries each use has access too
                uploadEntryAccessPermission(permissions, uid, file, group.getGroupID());
                // upload the location of geoentry
                uploadEntryLocation(entryID, location.getLatitude(), location.getLongitude());

            }
        });
    }

    /**
     * Upload a GeoEntry that does not contain the Uri to a file.
     *
     * @param permissions
     * @param title
     * @param description
     * @param type
     * @param group
     * @param location
     */
    public void uploadEntryWithoutFile(final int permissions, final String title, final String description, final int type, final Group group, Location location) {
        final String uid = getCurrentUID();

        GeoEntry entry = new GeoEntry(uid,
                getUser().getDisplayName(),
                title,
                description,
                location.getLatitude(),
                location.getLongitude(),
                0,
                "",
                type,
                DataUtils.getDateTime(),
                group.getGroupName(),
                group.getGroupID(),
                DataUtils.FROM_SELF);



        DatabaseReference entryRef = getDatabase().child("entries");
        DatabaseReference pushEntryRef = entryRef.push();
        String entryID = pushEntryRef.getKey();
        entry.setEntryID(entryID);

        // organise entry in a separate place to make it easy to search the entries each use has access too
        entry =  uploadEntryAccessPermission(permissions, uid, entry, group.getGroupID());

        pushEntryRef.setValue(entry);

        // upload the location of geoentry
        uploadEntryLocation(entryID, location.getLatitude(), location.getLongitude());

    }

    /**
     * Upload entry to extra location, listed either by the group ID, the userID or under Anyone for files that anyone can access.
     *
     * @param permissions
     * @param ownerID
     * @param file
     * @param groupID
     */
    private GeoEntry uploadEntryAccessPermission(final int permissions, String ownerID, final GeoEntry file, String groupID){
        final DatabaseReference filePermission = FirebaseDatabase.getInstance().getReference();

        final String entryID = file.getEntryID();
        if (permissions == DataUtils.ANYONE) {
            file.setFromWho(DataUtils.FROM_ANYONE);
            filePermission.child("entry_permission").child("anyone/" + entryID).setValue(true);
        } else if (permissions ==  DataUtils.NO_ONE) {
            file.setFromWho(DataUtils.FROM_SELF);
            filePermission.child("entry_permission").child(ownerID + "/" + entryID).setValue(true);
        } else if (permissions == DataUtils.FRIENDS) {
            file.setFromWho(DataUtils.FROM_SELF);
            filePermission.child("entry_permission").child(ownerID + "/" + entryID).setValue(true);
            file.setFromWho(DataUtils.FROM_FRIEND);
            final FirebaseDatabase database = FirebaseDatabase.getInstance();
            DatabaseReference ref = database.getReference("friends");
            ref.child(getCurrentUID()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                        User friend = postSnapshot.getValue(User.class);
                        filePermission.child("entry_permission").child(friend.getUserID() + "/" + entryID).setValue(true);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                }
            });
        } else {
            file.setFromWho(DataUtils.FROM_GROUP);
            filePermission.child("entry_permission").child(groupID + "/" + entryID).setValue(true);
            addEntryPermissionForGroupMembers(groupID, file, filePermission);
        }

        return file;
    }

    /**
     * Upload entry to all members of a group
     *
     * @param groupID
     * @param entry
     */
    private void addEntryPermissionForGroupMembers(String groupID, final GeoEntry entry, final DatabaseReference filePermission){

        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference ref = database.getReference("group_members");
        ref.child(groupID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    String userID = postSnapshot.getKey();
                    filePermission.child("entry_permission").child(userID + "/" + entry.getEntryID()).setValue(true);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    public void removeGroupEntries(final String gID, final String uID){

        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        final DatabaseReference ref = database.getReference("entry_permission");
        final DatabaseReference eRef = FirebaseDatabase.getInstance().getReference("entries");

        // remove entries by the id being removed from the group id in entry_permission
        ref.child(gID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (final DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    String entryKey = postSnapshot.getKey();
                    eRef.child(entryKey).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            GeoEntry entry = dataSnapshot.getValue(GeoEntry.class);
                            if (entry == null) {
                                return;
                            }

                            if (entry.getCreator().equals(uID)){
                                postSnapshot.getRef().setValue(null);
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

        eRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    GeoEntry entry = postSnapshot.getValue(GeoEntry.class);

                    if (entry.getGroupID().equals(gID)) {
                        postSnapshot.getRef().child("groupID").setValue(null);
                        postSnapshot.getRef().child("groupName").setValue(null);
                        postSnapshot.getRef().child("fromWho").setValue(DataUtils.FROM_SELF);
                    }
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

        final DatabaseReference gmRef = database.getReference("group_members");
        gmRef.child(gID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    User user = postSnapshot.getValue(User.class);

                    if (!user.getUserID().equals(getCurrentUID())) {
                        ref.child(user.getUserID()).orderByChild("creator").equalTo(uID).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                                    postSnapshot.getRef().setValue(null);
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                            }
                        });
                    }


                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    public void removeFriendEntries(final String currentUID, final String selectedUID){

        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        final DatabaseReference ref = database.getReference("entry_permission");
        final DatabaseReference eRef = FirebaseDatabase.getInstance().getReference("entries");

        // remove entries by the id being removed from the group id in entry_permission
        ref.child(currentUID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (final DataSnapshot postSnapshot : dataSnapshot.getChildren()) {

                    String entryKey = postSnapshot.getKey();
                    eRef.child(entryKey).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            GeoEntry entry = dataSnapshot.getValue(GeoEntry.class);
                            if (entry == null) {
                                return;
                            }

                            if (entry.getCreator().equals(selectedUID)){
                                postSnapshot.getRef().setValue(null);
                            }

                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

        // remove entries by the id being removed from the group id in entry_permission
        ref.child(selectedUID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (final DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    String entryKey = postSnapshot.getKey();
                    eRef.child(entryKey).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            GeoEntry entry = dataSnapshot.getValue(GeoEntry.class);
                            if (entry == null) {
                                return;
                            }

                            if (entry.getCreator().equals(currentUID)){
                                postSnapshot.getRef().setValue(null);
                            }

                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

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
     * -------------------------------------------------------------------------------------
     * ----------------------------------- Entry Download ----------------------------------
     * -------------------------------------------------------------------------------------
     */

    /**
     * Download all entries for the main map fragment
     *
     * @param center
     * @param southWest
     * @param northEast
     * @param fo
     * @param entryMap
     * @param listener
     */
    public void downloadAllEntries(LatLng center, final LatLng southWest, final LatLng northEast, final FilterOptions fo, final HashMap<String, GeoEntry> entryMap, final EntriesDownloadedListener listener){
        final String currentUID = getCurrentUID();
        final long fromTime = fo.getNumericalFromDate();
        final long toTime = fo.getNumericalToDate();
        final SparseBooleanArray typesMap = fo.getCheckedTypes();

        float meters = LocationUtils.getDistanceInMeters(southWest.latitude, southWest.longitude,
                                            northEast.latitude, northEast.longitude);
        double km = meters * 0.001;

        DatabaseReference database = FirebaseDatabase.getInstance().getReference("entry_location");
        GeoFire geoFire = new GeoFire(database);
        GeoQuery geoQuery = geoFire.queryAtLocation(new GeoLocation(center.latitude, center.longitude), km );

        final DatabaseReference ref = FirebaseDatabase.getInstance().getReference("entry_permission");
        final DatabaseReference eRef = FirebaseDatabase.getInstance().getReference("entries");


        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {
                Log.d(TAG, String.format("Key %s entered the search area at [%f,%f]", key, location.latitude, location.longitude));

                // If filter is set to view EVERYONE!
                if (fo.getFilterView() == FilterView.everyone) {

                    ref.child(currentUID + "/" + key).addListenerForSingleValueEvent(new ValueEventListener() {

                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {

                            String entryKey = dataSnapshot.getKey();
                            eRef.child(entryKey).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    GeoEntry entry = dataSnapshot.getValue(GeoEntry.class);
                                    if (entry == null) {
                                        return;
                                    }
                                    long date = entry.getUploadDate();
                                    if (fromTime <= date && date <= toTime && typesMap.get(entry.getFileType())) {
                                        entryMap.put(entry.getEntryID(), entry);
                                    }
                                    listener.onEntriesDownloaded();
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

                    ref.child("anyone/" + key).addListenerForSingleValueEvent(new ValueEventListener() {

                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {

                            String entryKey = dataSnapshot.getKey();
                            eRef.child(entryKey).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    GeoEntry entry = dataSnapshot.getValue(GeoEntry.class);
                                    if (entry == null) {
                                        return;
                                    }
                                    long date = entry.getUploadDate();
                                    if (fromTime <= date && date <= toTime && typesMap.get(entry.getFileType())) {
                                        entryMap.put(entry.getEntryID(), entry);
                                    }
                                    listener.onEntriesDownloaded();
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
                // If filter is set to view GROUPS!
                } else if (fo.getFilterView() == FilterView.groups) {
                    final String uID = getCurrentUID();
                    ref.child(uID + "/" + key).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            String entryKey = dataSnapshot.getKey();

                            if (entryKey == null){
                                return;
                            }

                            eRef.child(entryKey).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    GeoEntry entry = dataSnapshot.getValue(GeoEntry.class);
                                    if(entry.getFromWho() == DataUtils.FROM_GROUP) {

                                        long date = entry.getUploadDate();
                                        if (fromTime <= date && date <= toTime && typesMap.get(entry.getFileType())) {
                                            entryMap.put(entry.getEntryID(), entry);
                                        }
                                        listener.onEntriesDownloaded();
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
                // If filter is set to view THEIR OWN POSTS!
                } else if (fo.getFilterView() == FilterView.user) {

                    final String uID = getCurrentUID();
                    ref.child(uID + "/" + key).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {

                            String entryKey = dataSnapshot.getKey();

                            if (entryKey == null){
                                return;
                            }

                            eRef.child(entryKey).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    GeoEntry entry = dataSnapshot.getValue(GeoEntry.class);
                                    if(entry.getFromWho() == DataUtils.FROM_SELF) {


                                        entryMap.put(entry.getEntryID(), entry);
                                        listener.onEntriesDownloaded();
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
                // If filter is set to view FRIENDS!
                } else if (fo.getFilterView() == FilterView.friends) {

                    final String uID = getCurrentUID();
                    ref.child(uID).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {

                            String entryKey = dataSnapshot.getKey();

                            if (entryKey == null){
                                return;
                            }

                            eRef.child(entryKey).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    GeoEntry entry = dataSnapshot.getValue(GeoEntry.class);
                                    if(entry.getFromWho() == DataUtils.FROM_FRIEND) {

                                        long date = entry.getUploadDate();
                                        if (fromTime <= date && date <= toTime && typesMap.get(entry.getFileType())) {
                                            entryMap.put(entry.getEntryID(), entry);
                                        }
                                        listener.onEntriesDownloaded();
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
                listener.onEntriesDownloaded();
                Log.d(TAG, "All initial data has been loaded and events have been fired!");
            }

            @Override
            public void onGeoQueryError(DatabaseError error) {
                Log.d(TAG, "There was an error with this query: " + error);
            }
        });
    }


    public void downloadAllEntriesForAR(Location currentLocation, final long fromTime, final long toTime, final SparseBooleanArray typesMap, final ArrayList<CameraPoint> entryList){
        final String currentUID = getCurrentUID();

        DatabaseReference database = FirebaseDatabase.getInstance().getReference("entry_location");
        GeoFire geoFire = new GeoFire(database);
        GeoQuery geoQuery = geoFire.queryAtLocation(new GeoLocation(currentLocation.getLatitude(), currentLocation.getLongitude()), 8); // 5 mile radius

        final DatabaseReference ref = FirebaseDatabase.getInstance().getReference("entry_permission");
        final DatabaseReference eRef = FirebaseDatabase.getInstance().getReference("entries");
        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {
                Log.d(TAG, String.format("Key %s entered the search area at [%f,%f]", key, location.latitude, location.longitude));



                ref.child(currentUID + "/" + key).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        String entryKey = dataSnapshot.getKey();
                        eRef.child(entryKey).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                GeoEntry entry = dataSnapshot.getValue(GeoEntry.class);
                                if (entry == null) {
                                    return;
                                }
                                long date = entry.getUploadDate();
                                if(fromTime <= date && date <= toTime && typesMap.get(entry.getFileType())){
                                    entryList.add(new CameraPoint(entry));
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

                ref.child("anyone/" + key).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        String entryKey = dataSnapshot.getKey();
                        eRef.child(entryKey).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                GeoEntry entry = dataSnapshot.getValue(GeoEntry.class);
                                if (entry == null) {
                                    return;
                                }
                                long date = entry.getUploadDate();
                                if(fromTime <= date && date <= toTime && typesMap.get(entry.getFileType())){
                                    entryList.add(new CameraPoint(entry));
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

    public void downloadNearMe(final Location currentLocation, final HeroNearMeAdapter hAdapter, final NearMeEntryAdapter fAdapter, final NearMeEntryAdapter gAdapter, final NearMeEntryAdapter aAdapter, final EntriesDownloadedListener listener){

        final String currentUID = getCurrentUID();

        DatabaseReference database = FirebaseDatabase.getInstance().getReference("entry_location");
        GeoFire geoFire = new GeoFire(database);
        GeoQuery geoQuery = geoFire.queryAtLocation(new GeoLocation(currentLocation.getLatitude(), currentLocation.getLongitude()), 8); // 5 mile radius

        final DatabaseReference ref = FirebaseDatabase.getInstance().getReference("entry_permission");
        final DatabaseReference eRef = FirebaseDatabase.getInstance().getReference("entries");

        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, final GeoLocation location) {

                ref.child(currentUID + "/" + key).addListenerForSingleValueEvent(new ValueEventListener() {

                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        String entryKey = dataSnapshot.getKey();
                        eRef.child(entryKey).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                GeoEntry entry = dataSnapshot.getValue(GeoEntry.class);
                                if (entry == null) {
                                    return;
                                }

                                if (LocationUtils.isWithinBounds(currentLocation, entry) && entry.getFileType() == DataUtils.IMAGE) {
                                    hAdapter.addToEntries(entry);
                                    listener.onEntriesDownloaded();

                                }

                                if (entry.getFromWho() == DataUtils.FROM_FRIEND){
                                    fAdapter.addToEntries(entry);
                                } else if (entry.getFromWho() == DataUtils.FROM_GROUP){
                                    gAdapter.addToEntries(entry);
                                }

                                listener.onEntriesDownloaded();
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

                ref.child("anyone/" + key).addListenerForSingleValueEvent(new ValueEventListener() {

                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        String entryKey = dataSnapshot.getKey();
                        eRef.child(entryKey).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {

                                GeoEntry entry = dataSnapshot.getValue(GeoEntry.class);

                                if (entry == null) {
                                    return;
                                }

                                if (LocationUtils.isWithinBounds(currentLocation, entry) && entry.getFileType() == DataUtils.IMAGE) {

                                    hAdapter.addToEntries(entry);
                                    listener.onEntriesDownloaded();

                                }
                                aAdapter.addToEntries(entry);
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

    public void downloadProfileEntries(final FileAdapter adapter, String userID){
        String currentUID = getCurrentUID();
        // Get a reference to our posts
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference ref = database.getReference("entry_permission");
        final DatabaseReference eRef = FirebaseDatabase.getInstance().getReference("entries");

        ref.child(currentUID).orderByChild("creator").equalTo(userID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {

                    String entryKey = postSnapshot.getKey();
                    eRef.child(entryKey).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            GeoEntry entry = dataSnapshot.getValue(GeoEntry.class);
                            if (entry == null) {
                                return;
                            }
                            adapter.addToFiles(entry);
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
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

                    String entryKey = postSnapshot.getKey();
                    eRef.child(entryKey).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            GeoEntry entry = dataSnapshot.getValue(GeoEntry.class);
                            if (entry == null) {
                                return;
                            }
                            adapter.addToFiles(entry);
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    public void downloadGroupProfileEntries(final FileAdapter adapter, String groupID){
        // Get a reference to our posts
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference ref = database.getReference("entry_permission");
        final DatabaseReference eRef = FirebaseDatabase.getInstance().getReference("entries");

        ref.child(groupID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    String entryKey = postSnapshot.getKey();
                    eRef.child(entryKey).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            GeoEntry entry = dataSnapshot.getValue(GeoEntry.class);
                            if (entry == null) {
                                return;
                            }
                            adapter.addToFiles(entry);
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    public void downloadGroupEntries(String groupID, final FilterOptions fo, final HashMap<String, GeoEntry> entryMap, final EntriesDownloadedListener listener){
        // Get a reference to our posts
        final long fromTime = fo.getNumericalFromDate();
        final long toTime = fo.getNumericalToDate();
        final SparseBooleanArray typesMap = fo.getCheckedTypes();

        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference ref = database.getReference("entry_permission");
        final DatabaseReference eRef = FirebaseDatabase.getInstance().getReference("entries");

        ref.child(groupID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {

                    String entryKey = postSnapshot.getKey();
                    eRef.child(entryKey).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            GeoEntry entry = dataSnapshot.getValue(GeoEntry.class);
                            if (entry == null) {
                                return;
                            }
                            long date = entry.getUploadDate();
                            if(fromTime <= date && date <= toTime && typesMap.get(entry.getFileType())){
                                entryMap.put(entry.getEntryID(), entry);
                                listener.onEntriesDownloaded();
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }



    public void downloadUserEntries(final String userID, final FilterOptions fo, final HashMap<String, GeoEntry> entryMap, final EntriesDownloadedListener listener){
        String currentUID = getCurrentUID();
        final long fromTime = fo.getNumericalFromDate();
        final long toTime = fo.getNumericalToDate();
        final SparseBooleanArray typesMap = fo.getCheckedTypes();

        // Get a reference to our posts
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        final DatabaseReference ref = database.getReference("entry_permission");
        final DatabaseReference eRef = FirebaseDatabase.getInstance().getReference("entries");

        ref.child(currentUID).orderByChild("creator").equalTo(userID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {

                    String entryKey = postSnapshot.getKey();
                    eRef.child(entryKey).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            GeoEntry entry = dataSnapshot.getValue(GeoEntry.class);
                            if (entry == null) {
                                return;
                            }

                            long date = entry.getUploadDate();
                            if(fromTime <= date && date <= toTime && typesMap.get(entry.getFileType())){
                                entryMap.put(entry.getEntryID(), entry);
                                listener.onEntriesDownloaded();

                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
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
                    String entryKey = postSnapshot.getKey();
                    eRef.child(entryKey).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            GeoEntry entry = dataSnapshot.getValue(GeoEntry.class);
                            if (entry == null) {
                                return;
                            }

                            long date = entry.getUploadDate();
                            if(fromTime <= date && date <= toTime && typesMap.get(entry.getFileType())){
                                entryMap.put(entry.getEntryID(), entry);
                                listener.onEntriesDownloaded();

                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    public void downloadUserEntries(final FileAdapter adapter){
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference ref = database.getReference("entries");
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

    public void downloadUserSharedEntries(final FileAdapter adapter){
        final String uID = getCurrentUID();
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference ref = database.getReference("entry_permission");
        final DatabaseReference eRef = FirebaseDatabase.getInstance().getReference("entries");

        ref.child(uID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    String entryKey = postSnapshot.getKey();
                    eRef.child(entryKey).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            GeoEntry entry = dataSnapshot.getValue(GeoEntry.class);
                            if (entry == null) {
                                return;
                            }
                            if(!entry.getCreator().equals(uID)) {

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
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }
}
