package com.lauriedugdale.loci;

import android.content.Context;
import android.location.Location;
import android.net.Uri;
import android.support.annotation.NonNull;

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
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.util.concurrent.Executor;

/**
 * Created by mnt_x on 14/06/2017.
 */

public class DataUtils {

    private Context mContext;

    public static final int IMAGE = 1;
    public static final int AUDIO = 2;

    private FusedLocationProviderClient mFusedLocationClient;

    private DatabaseReference mDatabase;
    private FirebaseStorage mStorage;

    public DataUtils(Context context){
        mContext = context;
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mStorage = FirebaseStorage.getInstance();
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(context);
    }

    public String getCurrentUID(){
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

    public void writeNewFile(final Uri path, final int type) {

        uploadNewFile(getCurrentUID(), path);

        mFusedLocationClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                // Got last known location. In some rare situations this can be null.
                if (location != null) {
                    UserUpload user = new UserUpload(location.getLatitude(), location.getLongitude(), path.toString(), type, true);
                    mDatabase.child("files").push().setValue(user);
                }
            }
        });
    }

    private void uploadNewFile(String userId, Uri path){

        StorageReference storageRef = mStorage.getReference();

//        Uri file = Uri.fromFile(new File(path));
        StorageReference riversRef = storageRef.child(userId + "/images/");
        UploadTask uploadTask = riversRef.putFile(path);

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
                Uri downloadUrl = taskSnapshot.getDownloadUrl();
            }
        });
    }

    public void readFile(){

        mDatabase.child("files").orderByChild("latitude").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String prevChildKey) {

                UserUpload file = dataSnapshot.getValue(UserUpload.class);

                System.out.println(dataSnapshot.getKey() + " was latitude is:" + file.latitude + " longitude is:" + file.longitude);
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
