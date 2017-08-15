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
import android.widget.LinearLayout;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.lauriedugdale.loci.listeners.EntriesDownloadedListener;
import com.lauriedugdale.loci.R;
import com.lauriedugdale.loci.data.EntryDatabase;
import com.lauriedugdale.loci.data.EntryStorage;
import com.lauriedugdale.loci.data.TransportRest;
import com.lauriedugdale.loci.ui.adapter.nearme.BusStopsAdapter;
import com.lauriedugdale.loci.ui.adapter.nearme.HeroNearMeAdapter;
import com.lauriedugdale.loci.ui.adapter.nearme.NearMeEntryAdapter;


/**
 * @author Laurie Dugdale
 */

public class NearMeFragment extends BaseFragment implements EntriesDownloadedListener, TransportRest.BusStopsDownloadedListener {

    public static final String TAG = "NearMeFragment";

    private EntryDatabase mEntryDatabase;
    private EntryStorage mEntryStorage;
    private TransportRest transportRest;

    private Location mLocation;
    private double mLatitude;
    private double mLongitude;

    private ConstraintLayout mFriendsWrapper;
    private ConstraintLayout mGroupsWrapper;
    private ConstraintLayout mAnyoneWrapper;
    private ConstraintLayout mBussesWrapper;


    private RecyclerView mFriendsRecyclerView;
    private RecyclerView mGroupsRecyclerView;
    private RecyclerView mAnyoneRecyclerView;
    private RecyclerView mBusRecyclerView;
    private RecyclerView mHeroRecyclerView;

    private NearMeEntryAdapter mFriendsAdapter;
    private NearMeEntryAdapter mGroupsAdapter;
    private NearMeEntryAdapter mAnyoneAdapter;
    private BusStopsAdapter mBusStopAdapter;
    private HeroNearMeAdapter mHeroAdapter;

    private LinearLayout mNoEntries;


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
    }

    public void downloadBusStops(){
        mBusStopAdapter.clearData();
        transportRest.getBusStops(getActivity(), mLatitude, mLongitude, 5000, mBusStopAdapter, this);
    }

    public void downloadEntries(){
        if (mLocation == null) {
            return;
        }
        emptyLists();
        mEntryDatabase.downloadNearMe(mLocation, mHeroAdapter,
                                            mFriendsAdapter,
                                            mGroupsAdapter,
                                            mAnyoneAdapter,
                                            this);
    }

    @Override
    public void onEntriesDownloaded() {

        boolean haveEntries = false;

        if (mHeroAdapter.hasEntries()){
            mHeroRecyclerView.setVisibility(View.VISIBLE);
            haveEntries = true;
        } else {
            mHeroRecyclerView.setVisibility(View.GONE);
        }

        if (mFriendsAdapter.hasEntries()){
            mFriendsWrapper.setVisibility(View.VISIBLE);
            haveEntries = true;
        } else {
            mFriendsWrapper.setVisibility(View.GONE);
        }

        if (mGroupsAdapter.hasEntries()){
            mGroupsWrapper.setVisibility(View.VISIBLE);
            haveEntries = true;
        } else {
            mGroupsWrapper.setVisibility(View.GONE);
        }

        if (mAnyoneAdapter.hasEntries()){
            mAnyoneWrapper.setVisibility(View.VISIBLE);
            haveEntries = true;
        } else {
            mAnyoneWrapper.setVisibility(View.GONE);
        }

        if (!haveEntries){
            mNoEntries.setVisibility(View.VISIBLE);
        } else {
            mNoEntries.setVisibility(View.GONE);
        }
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
        mHeroAdapter.clearData();
        mFriendsAdapter.clearData();
        mGroupsAdapter.clearData();
        mAnyoneAdapter.clearData();
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_near_me,container, false);

        mFriendsWrapper = (ConstraintLayout) rootView.findViewById(R.id.friends_post_wrapper);
        mGroupsWrapper = (ConstraintLayout) rootView.findViewById(R.id.groups_post_wrapper);
        mAnyoneWrapper = (ConstraintLayout) rootView.findViewById(R.id.anyone_post_wrapper);
        mBussesWrapper = (ConstraintLayout) rootView.findViewById(R.id.bus_stop_wrapper);

        mFriendsRecyclerView = (RecyclerView) rootView.findViewById(R.id.rv_friends_entries);
        mGroupsRecyclerView = (RecyclerView) rootView.findViewById(R.id.rv_group_entries);
        mAnyoneRecyclerView = (RecyclerView) rootView.findViewById(R.id.rv_anyone_entries);
        mBusRecyclerView = (RecyclerView) rootView.findViewById(R.id.rv_bus_stops);
        mHeroRecyclerView = (RecyclerView) rootView.findViewById(R.id.rv_hero_images);

        mFriendsRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false));
        mGroupsRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false));
        mAnyoneRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false));
        mBusRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false));
        mHeroRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false));

        mFriendsAdapter = new NearMeEntryAdapter(getActivity());
        mFriendsRecyclerView.setAdapter(mFriendsAdapter);
        mGroupsAdapter = new NearMeEntryAdapter(getActivity());
        mGroupsRecyclerView.setAdapter(mGroupsAdapter);
        mAnyoneAdapter = new NearMeEntryAdapter(getActivity());
        mAnyoneRecyclerView.setAdapter(mAnyoneAdapter);
        mBusStopAdapter = new BusStopsAdapter(getActivity());
        mBusRecyclerView.setAdapter(mBusStopAdapter);
        mHeroAdapter = new HeroNearMeAdapter(getActivity());
        mHeroRecyclerView.setAdapter(mHeroAdapter);

        mNoEntries = (LinearLayout) rootView.findViewById(R.id.no_entries);

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
