package com.lauriedugdale.loci.data;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.firebase.ui.storage.images.FirebaseImageLoader;
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
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.lauriedugdale.loci.data.dataobjects.GeoEntry;
import com.lauriedugdale.loci.data.dataobjects.Group;
import com.lauriedugdale.loci.data.dataobjects.User;
import com.lauriedugdale.loci.data.dataobjects.UserFriend;
import com.lauriedugdale.loci.ui.adapter.FileAdapter;
import com.lauriedugdale.loci.ui.adapter.FriendsAdapter;
import com.lauriedugdale.loci.ui.adapter.NotificationFriendsAdapter;
import com.lauriedugdale.loci.ui.adapter.SelectForGroupAdapter;
import com.lauriedugdale.loci.utils.DataUtils;
import com.lauriedugdale.loci.utils.SocialUtils;

/**
 * Created by mnt_x on 02/08/2017.
 */

public class UserDatabase extends LociData {

    public UserDatabase(Context context) {
        super(context);
    }


    /**
     * -------------------------------------------------------------------------------------
     * ------------------------------------ User Upload ------------------------------------
     * -------------------------------------------------------------------------------------
     */

    /**
     * Writes a new user to the database
     * @param username The username to be written
     * @param email The email of the username to be written
     */
    public void uploadNewUser(String username, String email) {
        User user = new User(username, email, DataUtils.getDateTime());
        FirebaseUser userAuth = FirebaseAuth.getInstance().getCurrentUser();
        user.setUserID(userAuth.getUid());
        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder().setDisplayName(username).build();
        userAuth.updateProfile(profileUpdates);
        getDatabase().child("users").child(getCurrentUID()).setValue(user);
    }

    /**
     * status false - pending
     * status true - accepted
     *
     *
     * @param status
     */
    public void uploadFriendRequest(final TextView addButton, final String toUser, final boolean status) {
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

        ref.child(toUser).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChild(currentUID) ){
                    if ((boolean)dataSnapshot.child(currentUID) .getValue()){
                        addButton.setText("Friends");
                    } else {
                        addButton.setText("Pending");
                    }
                } else {

                    addButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            getDatabase().child("friend_requests").child(toUser).child(currentUID).setValue(status).addOnSuccessListener(new OnSuccessListener<Void>() {
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

    public void uploadFriend(final User selectedUser){
        final String currentUID = getCurrentUID();


        // Get a reference to our posts
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference ref = database.getReference("users");

        ref.child(currentUID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User currentUser = dataSnapshot.getValue(User.class);
                // user 1 - current user - add the selected user to the current user entry
                getDatabase().child("friends").child(currentUID + "/" + selectedUser.getUserID()).setValue(new UserFriend(selectedUser, true));
                // user 2 - selected user - add the current user to the selected user entry
                getDatabase().child("friends").child(selectedUser.getUserID() + "/" + currentUID).setValue(new UserFriend(currentUser, true));

                getDatabase().child("friend_requests").child(currentUID).child(selectedUser.getUserID()).setValue(true);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    public void uploadNewPofilePicture(final Uri path){
        final String currentID = getCurrentUID();

        StorageReference storageRef = getStorage().getReference();
        StorageReference ref = storageRef.child(currentID + "/profile_picture/" + DataUtils.getDateTime());
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

                getDatabase().child("users/" + currentID).child("profilePath").setValue(url.toString());
            }
        });
    }

    public void uploadProfileBio(String bio){
        String userID = getCurrentUID();
        getDatabase().child("users/" + userID).child("bio").setValue(bio);
    }

    /**
     * -------------------------------------------------------------------------------------
     * ------------------------------------ User Download ----------------------------------
     * -------------------------------------------------------------------------------------
     */

    public void downloadFriendRequests(final NotificationFriendsAdapter adapter) {
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        final DatabaseReference ref = database.getReference("users");
        getDatabase().child("friend_requests").child(getCurrentUID()).addChildEventListener(new ChildEventListener() {

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

    public void downloadUserFriends(final SelectForGroupAdapter adapter){
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

    public void downloadUserFriends(final FriendsAdapter adapter){

        getDatabase().child("friends").child(getCurrentUID()).addChildEventListener(new ChildEventListener() {

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

    public void downloadUsersToSelect(final SelectForGroupAdapter adapter, final Group group){

        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        final DatabaseReference ref = database.getReference("users");
        final String [] uID = new String [1];
        final long [] access = new long [1];

        getDatabase().child("group_permission").child(group.getGroupID()).addChildEventListener(new ChildEventListener() {

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


    public void downloadProfileBio(final EditText text){
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
    public void downloadNonLoggedInProfilePic(String userID, final ImageView image, final int drawableID) {

        // Get a reference to our posts
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference ref = database.getReference("users");

        ref.child(userID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);

                String photoUrl = user.getProfilePath();
                if(photoUrl != null){
                    StorageReference storageRef = getStorage().getReferenceFromUrl(photoUrl);
                    Glide.with(getContext())
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
    public void downloadProfilePic(final ImageView image, int drawableID) {

        if (getUser() != null) {
            Uri photoUrl = getUser().getPhotoUrl();
            if(photoUrl != null){
                StorageReference storageRef = getStorage().getReferenceFromUrl(photoUrl.toString());
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


}
