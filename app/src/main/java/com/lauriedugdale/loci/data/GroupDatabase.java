package com.lauriedugdale.loci.data;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.view.Menu;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.firebase.ui.storage.images.FirebaseImageLoader;
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
import com.lauriedugdale.loci.AdminCheckListener;
import com.lauriedugdale.loci.EntriesDownloadedListener;
import com.lauriedugdale.loci.R;
import com.lauriedugdale.loci.data.dataobjects.GeoEntry;
import com.lauriedugdale.loci.data.dataobjects.Group;
import com.lauriedugdale.loci.ui.adapter.FetchGroupsAdapter;
import com.lauriedugdale.loci.ui.adapter.FileAdapter;
import com.lauriedugdale.loci.ui.adapter.GroupsAdapter;
import com.lauriedugdale.loci.utils.DataUtils;
import com.lauriedugdale.loci.utils.SocialUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by mnt_x on 02/08/2017.
 */

public class GroupDatabase extends LociData {
    public GroupDatabase(Context context) {
        super(context);
    }


    /**
     * -------------------------------------------------------------------------------------
     * ------------------------------------ Group Upload -----------------------------------
     * -------------------------------------------------------------------------------------
     */
    public void uploadGroupRequest(final TextView joinButton, final Group group){
        final String currentUID = getCurrentUID();

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
                            getDatabase().child("group_permission").child(group.getGroupID()).child(getCurrentUID()).setValue(whoCanPost);
                            getDatabase().child("group_members").child(group.getGroupID() + "/"  + currentUID).setValue(getCurrentUsername());
                            getDatabase().child("group_access").child(getCurrentUID() + "/" + group.getGroupID()).setValue(group).addOnSuccessListener(new OnSuccessListener<Void>() {
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

    public void uploadGroupWithoutPic(final HashMap<String, String> usersToAdd, String groupName, boolean isPrivate){

        //TODO use cloud functions to make this one upload!
        String privateOrPublic = "public";
        if(isPrivate){
            privateOrPublic = "private";
        }

        // add group to group
        final Group group = new Group(groupName);
        group.setPrivatePublic(privateOrPublic);
        DatabaseReference entryRef = getDatabase().child("groups");
        DatabaseReference pushEntryRef = entryRef.push();
        group.setGroupID(pushEntryRef.getKey());
        pushEntryRef.setValue(group);

        getDatabase().child("groups").child(group.getGroupID()).setValue(group);
        getDatabase().child("group_access").child(getCurrentUID() + "/" + group.getGroupID()).setValue(group);
        getDatabase().child("group_permission").child(group.getGroupID()).child(getCurrentUID()).setValue(SocialUtils.CREATOR);
        getDatabase().child("group_members").child(group.getGroupID() + "/"  + getCurrentUID()).setValue(getCurrentUsername());

        String userID = "";
        String username = "";
        for(Map.Entry<String,String> entry : usersToAdd.entrySet()){
            userID = entry.getKey();
            username = entry.getValue();
            getDatabase().child("group_access").child(userID + "/" + group.getGroupID()).setValue(group);
            getDatabase().child("group_permission").child(group.getGroupID()).child(userID).setValue(SocialUtils.VIEWER);
            getDatabase().child("group_members").child(group.getGroupID() + "/"  + userID).setValue(username);
        }
    }

    public void uploadGroupWithPic(final HashMap<String, String> usersToAdd, String groupName, Uri path, boolean isPrivate){

        //TODO use cloud functions to make this one upload!
        String privateOrPublic = "public";
        if(isPrivate){
            privateOrPublic = "private";
        }


        // add group to group
        final Group group = new Group(groupName);
        group.setPrivatePublic(privateOrPublic);
        DatabaseReference entryRef = getDatabase().child("groups");
        DatabaseReference pushEntryRef = entryRef.push();
        group.setGroupID(pushEntryRef.getKey());
        pushEntryRef.setValue(group);

        //upload group profile pic
        StorageReference storageRef = getStorage().getReference();
        StorageReference ref = storageRef.child(group.getGroupID() + "/group_picture/" + DataUtils.getDateTime());
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
                getDatabase().child("groups").child(group.getGroupID()).setValue(group);
                final Uri downloadUrl = taskSnapshot.getDownloadUrl();
                group.setProfilePicturePath(downloadUrl.toString());
                getDatabase().child("group_access").child(getCurrentUID() + "/" + group.getGroupID()).setValue(group);
                getDatabase().child("group_permission").child(group.getGroupID()).child(getCurrentUID()).setValue(SocialUtils.CREATOR);
                getDatabase().child("group_members").child(group.getGroupID() + "/"  + getCurrentUID()).setValue(getCurrentUsername());

                String userID = "";
                String username = "";
                for(Map.Entry<String,String> entry : usersToAdd.entrySet()){
                    userID = entry.getKey();
                    username = entry.getValue();
                    getDatabase().child("group_access").child(userID + "/" + group.getGroupID()).setValue(group);
                    getDatabase().child("group_permission").child(group.getGroupID()).child(userID).setValue(SocialUtils.VIEWER);
                    getDatabase().child("group_members").child(group.getGroupID() + "/"  + userID).setValue(username);
                }
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
                        getDatabase().child("group_permission").child(group.getGroupID()).child(uID).setValue(SocialUtils.ADMIN);
                    } else if (!userMap.containsKey(uID) && access == SocialUtils.ADMIN){
                        getDatabase().child("group_permission").child(group.getGroupID()).child(uID).setValue(SocialUtils.VIEWER);
                    }

                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }


    public void changeAdminPermission(final Group group, final String idToCheck, final Menu menu){

        final String uID = "";
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference ref = database.getReference("group_permission");

        ref.child(group.getGroupID()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChild(idToCheck)){
                    DataSnapshot postSnapshot = dataSnapshot.child(idToCheck);

                    long access = (long)postSnapshot.getValue();

                    if (access != SocialUtils.ADMIN){
                        getDatabase().child("group_permission").child(group.getGroupID()).child(idToCheck).setValue(SocialUtils.ADMIN);
                        menu.findItem(R.id.make_admin).setTitle("Remove admin");
                    } else if (access == SocialUtils.ADMIN){
                        getDatabase().child("group_permission").child(group.getGroupID()).child(idToCheck).setValue(SocialUtils.VIEWER);
                        menu.findItem(R.id.make_admin).setTitle("Make admin");

                    }
                }

            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    public void changeWhoPosts(final Group group){

        getDatabase().child("groups").child(group.getGroupID()).setValue(group);

        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference ref = database.getReference("group_permission");

        ref.child(group.getGroupID()).addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    String uID = postSnapshot.getKey();
                    long access = (long)dataSnapshot.child(uID).getValue();
                    getDatabase().child("group_permission").child(group.getGroupID()).child(uID).setValue(changeAccess(group, access));
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


    public void uploadNewPofilePicture(final Group group, Uri path){

        StorageReference storageRef = getStorage().getReference();
        StorageReference ref = storageRef.child(group.getGroupID() + "/group_picture/" + DataUtils.getDateTime());
        UploadTask uploadTask = ref.putFile(path);
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {

            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                String url = taskSnapshot.getDownloadUrl().toString();
                getDatabase().child("groups/" + group.getGroupID()).child("profilePicturePath").setValue(url);
            }
        });
    }

    public void removeGroupMember(String gID, String uID){

        if (uID == null) {
            uID = getCurrentUID();
        }

        getDatabase().child("group_access").child(uID).setValue(null);
        getDatabase().child("group_permission").child(gID).child(uID).setValue(null);
        getDatabase().child("group_members").child(gID + "/"  + getCurrentUID()).setValue(null);

        EntryDatabase entryDatabase = new EntryDatabase(getContext());
        entryDatabase.removeGroupEntries(gID, uID);
    }
    /**
     * -------------------------------------------------------------------------------------
     * ------------------------------------ Group Download ----------------------------------
     * -------------------------------------------------------------------------------------
     */

    public void downloadUserAcessibleGroups(final FetchGroupsAdapter adapter){

        final String currentUID = getCurrentUID();

        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        final DatabaseReference ref = database.getReference("groups");
        final String [] gID = new String [1];
        getDatabase().child("group_permission").orderByChild(currentUID).startAt(SocialUtils.ADMIN).addChildEventListener(new ChildEventListener() {

            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String prevChildKey) {

                gID[0] = dataSnapshot.getKey();
                ref.orderByChild(gID[0]).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
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


    public void downloadUserAcessibleGroups(final GroupsAdapter adapter){

        final String currentUID = getCurrentUID();
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        final DatabaseReference ref = database.getReference("groups");

        getDatabase().child("group_access").child(currentUID).addChildEventListener(new ChildEventListener() {

            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String prevChildKey) {
                Group group = dataSnapshot.getValue(Group.class);

                ref.child(group.getGroupID()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Group group = dataSnapshot.getValue(Group.class);
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

    public void checkGroupAdmin(final ImageView settingsButton, String groupID, final AdminCheckListener listener){

        final String uID = getCurrentUID();
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference ref = database.getReference("group_permission");

        ref.child(groupID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                boolean isAdmin = false;
                if (dataSnapshot.hasChild(uID)){
                    long access = (long)dataSnapshot.child(uID).getValue();

                    if(access > 20){
                        access = access - SocialUtils.EVERYONE_POSTS;
                    }
                    if (access == SocialUtils.CREATOR || access == SocialUtils.ADMIN){
                        if (settingsButton != null) {
                            settingsButton.setVisibility(View.VISIBLE);
                        }
                        isAdmin = true;
                    }
                }
                if (listener != null) {
                    listener.onAdminChecked(isAdmin);
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    public void checkNonLoggedInGroupAdmin(final String uID, String groupID, final AdminCheckListener listener){

        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference ref = database.getReference("group_permission");

        ref.child(groupID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                boolean isAdmin = false;
                if (dataSnapshot.hasChild(uID)){
                    long access = (long)dataSnapshot.child(uID).getValue();

                    if(access > 20){
                        access = access - SocialUtils.EVERYONE_POSTS;
                    }
                    if (access == SocialUtils.CREATOR || access == SocialUtils.ADMIN){

                        isAdmin = true;
                    }
                }
                if (listener != null) {
                    listener.onAdminChecked(isAdmin);
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
    public void downloadGroupPic(final ImageView image, int drawableID, String path) {

        if(path != null){
            StorageReference storageRef = getStorage().getReferenceFromUrl(path.toString());
            Glide.with(getContext())
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
