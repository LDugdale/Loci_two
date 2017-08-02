package com.lauriedugdale.loci.utils;

import android.content.Context;
import android.icu.util.Calendar;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.SparseBooleanArray;
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


    // int representation of what media the entry contains
    public static final int NO_MEDIA = 100;
    public static final int IMAGE = 101;
    public static final int AUDIO = 102;

    // codes for permission access
    public static final int ANYONE = 200;
    public static final int FRIENDS = 201;
    public static final int NO_ONE = 202;
    public static final int GROUP = 203;

    // codes to display who file came from
    public static final int FROM_SELF = 300;
    public static final int FROM_ANYONE = 301;
    public static final int FROM_GROUP = 302;
    public static final int FROM_FRIEND = 303;
    public static final int FROM_SINGLE_USER = 304;

    /**
     * Get the date used for adding to entries in the database
     *
     * @return time in long format
     */
    public static long getDateTime(){

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

}
