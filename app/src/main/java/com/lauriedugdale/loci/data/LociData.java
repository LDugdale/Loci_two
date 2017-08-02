package com.lauriedugdale.loci.data;

import android.content.Context;
import android.icu.util.Calendar;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;

import java.text.ParseException;
import java.util.Locale;

/**
 * Created by mnt_x on 02/08/2017.
 */

public class LociData {

    private Context mContext;
    private DatabaseReference mDatabase;
    private FirebaseStorage mStorage;
    private FirebaseUser mUser;

    public LociData(Context context) {
        mUser = FirebaseAuth.getInstance().getCurrentUser();
        mContext = context;
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mStorage = FirebaseStorage.getInstance();
    }

    public Context getContext() {
        return mContext;
    }

    public DatabaseReference getDatabase() {
        return mDatabase;
    }

    public FirebaseStorage getStorage() {
        return mStorage;
    }

    public FirebaseUser getUser() {
        return mUser;
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
}
