package com.lauriedugdale.loci.ui.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.lauriedugdale.loci.EntriesDownloadedListener;
import com.lauriedugdale.loci.R;
import com.lauriedugdale.loci.data.EntryDatabase;
import com.lauriedugdale.loci.data.EntryStorage;
import com.lauriedugdale.loci.data.TransportRest;
import com.lauriedugdale.loci.data.dataobjects.GeoEntry;
import com.lauriedugdale.loci.ui.adapter.nearme.BusStopsAdapter;
import com.lauriedugdale.loci.ui.adapter.nearme.NearMeEntryAdapter;

import java.util.ArrayList;
import java.util.Random;


/**
 * @author Laurie Dugdale
 */

public class NearMeFragment extends BaseFragment implements EntriesDownloadedListener, TransportRest.BusStopsDownloadedListener {

    public static final String TAG = "NearMeFragment";

    private EntryDatabase mEntryDatabase;
    private EntryStorage mEntryStorage;
    private TransportRest transportRest;

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
    private ConstraintLayout mFriendsWrapper;
    private ConstraintLayout mGroupsWrapper;
    private ConstraintLayout mAnyoneWrapper;
    private ConstraintLayout mBussesWrapper;


    private RecyclerView mFriendsRecyclerView;
    private RecyclerView mGroupsRecyclerView;
    private RecyclerView mAnyoneRecyclerView;
    private RecyclerView mBusRecyclerView;
    private NearMeEntryAdapter mFriendsAdapter;
    private NearMeEntryAdapter mGroupsAdapter;
    private NearMeEntryAdapter mAnyoneAdapter;
    private BusStopsAdapter mBusStopAdapter;


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
        transportRest = new TransportRest();

        mLocation = new Location("current_location");

        mFriends = new ArrayList<>();
        mGroups = new ArrayList<>();
        mAnyone = new ArrayList<>();
        mHeroImages = new ArrayList<>();
    }

    public void downloadBusStops(){
        mBusStopAdapter.clearData();
        transportRest.getBusStops(mLatitude, mLongitude, 5000, mBusStopAdapter, this);
    }

    public void downloadEntries(){
        if (mLocation == null) {
            return;
        }
        emptyLists();
        mEntryDatabase.downloadNearMe(mLocation, mHeroImages,
                                            mFriendsAdapter,
                                            mGroupsAdapter,
                                            mAnyoneAdapter,
                                            this);
    }

    @Override
    public void onEntriesDownloaded() {

        if (mFriendsAdapter.hasEntries()){
            mFriendsWrapper.setVisibility(View.VISIBLE);
        } else {
            mFriendsWrapper.setVisibility(View.GONE);
        }

        if (mGroupsAdapter.hasEntries()){
            mGroupsWrapper.setVisibility(View.VISIBLE);
        } else {
            mGroupsWrapper.setVisibility(View.GONE);
        }

        if (mAnyoneAdapter.hasEntries()){
            mAnyoneWrapper.setVisibility(View.VISIBLE);
        } else {
            mAnyoneWrapper.setVisibility(View.GONE);
        }

        addHeroImages();
    }


    @Override
    public void onBusstopsDownloaded() {
        if (mBusStopAdapter.hasStops()){
            mBussesWrapper.setVisibility(View.VISIBLE);
        } else {
            mBussesWrapper.setVisibility(View.GONE);
        }
    }

    private void emptyLists(){
        mHeroImages.clear();
        mFriendsAdapter.clearData();
        mGroupsAdapter.clearData();
        mAnyoneAdapter.clearData();
    }

    public void addHeroImages(){

        if (mHeroImages.size() >= 1 && mHeroImages.size() <= 2 ){

            mEntryStorage.getFilePic(mHeroOne, mHeroImages.get(0));
            mHeroOne.setVisibility(View.VISIBLE);
        } else if(mHeroImages.size() >= 3){

            final int[] ints = new Random().ints(0, mHeroImages.size()).distinct().limit(3).toArray();
            mEntryStorage.getFilePic(mHeroOne, mHeroImages.get(ints[0]));
            mEntryStorage.getFilePic(mHeroTwo, mHeroImages.get(ints[1]));
            mEntryStorage.getFilePic(mHeroThree, mHeroImages.get(ints[2]));
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
        mFriendsWrapper = (ConstraintLayout) rootView.findViewById(R.id.friends_post_wrapper);
        mGroupsWrapper = (ConstraintLayout) rootView.findViewById(R.id.groups_post_wrapper);
        mAnyoneWrapper = (ConstraintLayout) rootView.findViewById(R.id.anyone_post_wrapper);
        mBussesWrapper = (ConstraintLayout) rootView.findViewById(R.id.bus_stop_wrapper);

        mFriendsRecyclerView = (RecyclerView) rootView.findViewById(R.id.rv_friends_entries);
        mGroupsRecyclerView = (RecyclerView) rootView.findViewById(R.id.rv_group_entries);
        mAnyoneRecyclerView = (RecyclerView) rootView.findViewById(R.id.rv_anyone_entries);
        mBusRecyclerView = (RecyclerView) rootView.findViewById(R.id.rv_bus_stops);

        mFriendsRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, true));
        mGroupsRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, true));
        mAnyoneRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, true));
        mBusRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, true));

        mFriendsAdapter = new NearMeEntryAdapter(getActivity(), mFriends);
        mFriendsRecyclerView.setAdapter(mFriendsAdapter);
        mGroupsAdapter = new NearMeEntryAdapter(getActivity(), mGroups);
        mGroupsRecyclerView.setAdapter(mGroupsAdapter);
        mAnyoneAdapter = new NearMeEntryAdapter(getActivity(), mAnyone);
        mAnyoneRecyclerView.setAdapter(mAnyoneAdapter);
        mBusStopAdapter = new BusStopsAdapter(getActivity());
        mBusRecyclerView.setAdapter(mBusStopAdapter);

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

        FusedLocationProviderClient fusedLocationClient = LocationServices.getFusedLocationProviderClient(getActivity());
        fusedLocationClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                // Got last known location. In some rare situations this can be null.
                if (location != null) {
                    mLatitude = location.getLatitude();
                    mLongitude = location.getLongitude();
                    mLocation.setLatitude(mLatitude);
                    mLocation.setLongitude(mLongitude);
                    downloadBusStops();
                    downloadEntries();
                }
            }
        });

        IntentFilter filter = new IntentFilter("location_update");
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mDataReceiver, filter);
    }

    @Override
    public void onPause() {
        super.onPause();
        emptyLists();
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
