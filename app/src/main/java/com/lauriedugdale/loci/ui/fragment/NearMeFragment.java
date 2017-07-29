package com.lauriedugdale.loci.ui.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLngBounds;
import com.lauriedugdale.loci.EntriesDownloadedListener;
import com.lauriedugdale.loci.R;
import com.lauriedugdale.loci.data.DataUtils;
import com.lauriedugdale.loci.data.EntryDatabase;
import com.lauriedugdale.loci.data.EntryStorage;
import com.lauriedugdale.loci.data.dataobjects.GeoEntry;
import com.lauriedugdale.loci.utils.LocationUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;


/**
 * @author Laurie Dugdale
 */

public class NearMeFragment extends BaseFragment implements EntriesDownloadedListener {

    public static final String TAG = "NearMeFragment";

    private EntryDatabase mEntryDatabase;
    private EntryStorage mEntryStorage;

    private ArrayList<GeoEntry> mFriends;
    private ArrayList<GeoEntry> mGroups;
    private ArrayList<GeoEntry> mAnyone;
    private ArrayList<GeoEntry> mHeroImages;

    private Location mLocation;
    private double mLatitude;
    private double mLongitude;

    private ImageView mHeroOne;
    private ImageView mHeroTwo;
    private ImageView mHeroThree;
    private LinearLayout mHeroTwoThreeWrapper;

    private BroadcastReceiver mDataReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "received location broadcast");

            if ("location_update".equals(intent.getAction())) {
                mLatitude = intent.getDoubleExtra("latitude", 0L);
                mLongitude = intent.getDoubleExtra("longitude", 0L);
                mLocation.setLatitude(mLatitude);
                mLocation.setLongitude(mLongitude);
            }

        }
    };

    public static NearMeFragment create(){
        return new NearMeFragment();
    }



    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        mEntryStorage = new EntryStorage(getActivity());
        mEntryDatabase = new EntryDatabase(getActivity());

        mLocation = new Location("current_location");

        mFriends = new ArrayList<>();
        mGroups = new ArrayList<>();
        mAnyone = new ArrayList<>();
        mHeroImages = new ArrayList<>();
    }

    public void downloadEntries(){
        if (mLocation == null) {
            return;
        }

        mEntryDatabase.downloadNearMe(mLocation, mHeroImages, mFriends, mGroups, mAnyone, this);
    }

    @Override
    public void onEntriesDownloaded() {

        addHeroImages();
    }

    public void addHeroImages(){

        GeoEntry [] temp = new GeoEntry[3];

        if (mHeroImages.size() == 1){

            mEntryStorage.getFilePic(mHeroOne, temp[0]);
            mHeroOne.setVisibility(View.VISIBLE);
        } else if(mHeroImages.size() >= 3){

            final int[] ints = new Random().ints(0, mHeroImages.size()).distinct().limit(2).toArray();
            mEntryStorage.getFilePic(mHeroTwo, temp[ints[0]]);
            mEntryStorage.getFilePic(mHeroThree, temp[ints[1]]);
            mHeroTwoThreeWrapper.setVisibility(View.VISIBLE);
        } else if (mHeroImages.size() == 0){
            mHeroOne.setVisibility(View.GONE);
            mHeroTwoThreeWrapper.setVisibility(View.GONE);
        }
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_near_me,container, false);

        mHeroOne = (ImageView) rootView.findViewById(R.id.image_hero_one);
        mHeroTwo = (ImageView) rootView.findViewById(R.id.image_hero_two);
        mHeroThree = (ImageView) rootView.findViewById(R.id.image_hero_three);
        mHeroTwoThreeWrapper = (LinearLayout) rootView.findViewById(R.id.image_hero_two_three_wrapper);

        return rootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

    }

    @Override
    public int getLayoutResId() {
        return R.layout.fragment_near_me;
    }

    @Override
    public void inOnCreateView(View root, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
    }

    @Override
    public void onResume() {
        super.onResume();

        downloadEntries();
        IntentFilter filter = new IntentFilter("location_update");
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mDataReceiver, filter);
    }

    @Override
    public void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mDataReceiver);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        MenuItem notificationItem = menu.findItem(R.id.action_notification);
        MenuItem filterItem = menu.findItem(R.id.action_filter);
        MenuItem locationItem = menu.findItem(R.id.action_location);
        MenuItem arItem = menu.findItem(R.id.action_ar);
        MenuItem addGroupItem = menu.findItem(R.id.action_add_group);

        addGroupItem.setVisible(false);
        filterItem.setVisible(false);
        locationItem.setVisible(false);
        arItem.setVisible(false);
        notificationItem.setVisible(false);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        return false;
    }

}
