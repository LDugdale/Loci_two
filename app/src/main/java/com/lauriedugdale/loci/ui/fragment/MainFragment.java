package com.lauriedugdale.loci.ui.fragment;

import android.app.DatePickerDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.graphics.drawable.ColorDrawable;
import android.icu.text.SimpleDateFormat;
import android.icu.util.Calendar;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterManager;
import com.lauriedugdale.loci.EntryItem;
import com.lauriedugdale.loci.EventIconRendered;
import com.lauriedugdale.loci.data.DataUtils;
import com.lauriedugdale.loci.data.dataobjects.FilterOptions;
import com.lauriedugdale.loci.data.dataobjects.GeoEntry;
import com.lauriedugdale.loci.R;
import com.lauriedugdale.loci.data.dataobjects.Group;
import com.lauriedugdale.loci.data.dataobjects.User;
import com.lauriedugdale.loci.ui.activity.AugmentedActivity;
import com.lauriedugdale.loci.ui.activity.MainActivity;
import com.lauriedugdale.loci.utils.PopupUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;

/**
 * Created by mnt_x on 28/05/2017.
 */

public class MainFragment extends BaseFragment implements OnMapReadyCallback,GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        GoogleMap.OnInfoWindowClickListener,
        GoogleMap.OnMapLongClickListener,
        GoogleMap.OnMapClickListener,
        GoogleMap.OnMarkerClickListener {

    private static final String TAG = MainFragment.class.getSimpleName();
    // location variables
    private GoogleMap mMap;
    private GoogleApiClient mGoogleApiClient;
    private Location mCurrentLocation;

    // potential map types its possible to display
    private final int[] MAP_TYPES = { GoogleMap.MAP_TYPE_SATELLITE,
            GoogleMap.MAP_TYPE_NORMAL,
            GoogleMap.MAP_TYPE_HYBRID,
            GoogleMap.MAP_TYPE_TERRAIN,
            GoogleMap.MAP_TYPE_NONE };

    private int curMapTypeIndex = 1; // chosen map type from MAP_TYPES

    private DataUtils mDataUtils; // handles data transactions with firebase

    private HashMap<String, EntryItem> visibleMarkers; // keeps track of visible markers
    private HashMap<String, GeoEntry> mEntryMap; // keeps track of the entries downloaded from the server
    private ClusterManager<EntryItem> mClusterManager;

    private FusedLocationProviderClient mFusedLocationClient; // used for getting the current lcoation

    // time controls
    private final static String DATE_TIME = "yyyy-MM-dd HH:mm:ss";
    private final static String DATE = "dd-MM-yyyy";
    private SimpleDateFormat mDateTime;
    private Calendar mCalendar;
    private TextView mDisplayFromDate;
    private TextView mDisplayToDate;
    private DatePickerDialog mFromTimePicker;
    private DatePickerDialog mToTimePicker;

    // filter variables
    private FilterOptions mFilterOptions;
    private FilterOptions mTempFilterOptions;

    private FrameLayout mMainLayout;

    private String mCurrentlyDisplaying;
    private TextView mViewingName;
    private ImageView mViewingClose;
    private FrameLayout mViewingWindow;
    private User mUser;
    private Group mGroup;
    private GeoEntry mEntry;
    private ArrayList<GeoEntry> mGeofenceList;
    private boolean mFirstIdle;
    private boolean mDisplayingCustomEntries;



    private BroadcastReceiver mDataReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "received localbroadcast intent - " + intent.getAction());
            clearMap();
            mDisplayingCustomEntries = true;

            if("user_entries".equals(intent.getAction())) {
                mCurrentlyDisplaying = "user_entries";
                mUser = intent.getParcelableExtra("user");
                getSpecificEntries();
                mFirstIdle = true;
                ((MainActivity)getActivity()).getViewPager().setCurrentItem(1);
                setupCurrentlyViewing();
            }
            if("group_entries".equals(intent.getAction())) {
                mCurrentlyDisplaying = "group_entries";
                mGroup = intent.getParcelableExtra("group");
                getSpecificEntries();
                mFirstIdle = true;
                if(((MainActivity)getActivity()) != null) {
                    ((MainActivity) getActivity()).getViewPager().setCurrentItem(1);
                }
                setupCurrentlyViewing();
            }
            if("single_entry".equals(intent.getAction())) {
                mCurrentlyDisplaying = "single_entry";
                mEntry = intent.getParcelableExtra("entry");
                addEntry();
                if(((MainActivity)getActivity()) != null){
                    ((MainActivity) getActivity()).getViewPager().setCurrentItem(1);
                    setupCurrentlyViewing();
                }
            }
            if("geofence_entries".equals(intent.getAction())) {
                mCurrentlyDisplaying = "geofence_entries";
                mGeofenceList = intent.getParcelableArrayListExtra("entries");
                for (GeoEntry e : mGeofenceList){
                    mEntryMap.put(e.getEntryID(), e);
                }
                addAllEntriesToMap();

                getBounds();
                mFirstIdle = true;
                if(((MainActivity)getActivity()) != null) {
                    ((MainActivity) getActivity()).getViewPager().setCurrentItem(1);
                    setupCurrentlyViewing();
                }
            }
        }
    };

    public void clearMap(){
        visibleMarkers = new HashMap<String, EntryItem>();
        mEntryMap = new HashMap<String, GeoEntry>();
        mClusterManager.clearItems();
        mClusterManager.cluster();
    }

    public void setupCurrentlyViewing(){

        if (mCurrentlyDisplaying.equals("user_entries")){
            mViewingName.setText(mUser.getUsername());
        } else if (mCurrentlyDisplaying.equals("single_entry")){
            mViewingName.setText(mEntry.getTitle());
        } else if (mCurrentlyDisplaying.equals("geofence_entries")){
            mViewingName.setText("Nearby entries");
        } else if (mCurrentlyDisplaying.equals("group_entries")){
            mViewingName.setText(mGroup.getGroupName());
        }

        mViewingClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mViewingWindow.setVisibility(View.INVISIBLE);
                mDisplayingCustomEntries = false;
                mCurrentlyDisplaying ="all";
                getAllEntries();
            }
        });

        mViewingWindow.setVisibility(View.VISIBLE);
    }


    /**
     * Used to return fragment for viewpager quickly
     * @return
     */
    public static MainFragment create(){
        return new MainFragment();
    }


    public GoogleMap getMap() {
        return mMap;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // instantiate inital variables
        mDataUtils = new DataUtils(getActivity());
        visibleMarkers = new HashMap<String, EntryItem>();
        mEntryMap = new HashMap<String, GeoEntry>();
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(getActivity());
        mFilterOptions = new FilterOptions();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main, container, false);

        mMainLayout = (FrameLayout) view.findViewById(R.id.main_layout);
        mViewingWindow = (FrameLayout) view.findViewById(R.id.currently_viewing);
        mViewingName = (TextView) view.findViewById(R.id.currently_viewing_text);
        mViewingClose = (ImageView) view.findViewById(R.id.currently_viewing_close);


        mCurrentlyDisplaying = "all";

        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setHasOptionsMenu(true);

        // set up google maps API
        mGoogleApiClient = new GoogleApiClient.Builder( getActivity() )
                .addConnectionCallbacks( this )
                .addOnConnectionFailedListener( this )
                .addApi( LocationServices.API )
                .build();
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        MenuItem addFriendItem = menu.findItem(R.id.action_add_friend);
        MenuItem notificationItem = menu.findItem(R.id.action_notification);
        MenuItem addGroupItem = menu.findItem(R.id.action_add_group);

        addGroupItem.setVisible(false);
        addFriendItem.setVisible(false);
        notificationItem.setVisible(false);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == R.id.action_filter) {
            showFilterPopup(mMainLayout);
        }


        if (id == R.id.action_ar) {
            Intent intent = new Intent(getActivity(), AugmentedActivity.class);
            startActivity(intent);

            return true;
        }
        return false;
    }

    @Override
    public void onResume() {
        super.onResume();

        IntentFilter filter = new IntentFilter("user_entries");
        filter.addAction("group_entries");
        filter.addAction("geofence_entries");
        filter.addAction("single_entry");
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mDataReceiver, filter);

        setUpMapIfNeeded();
    }

    @Override
    public void onPause() {
        super.onPause();
//        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mDataReceiver);

    }

    @Override
    public void onDestroy() {
        super.onDestroy();

    }

    private void setUpMapIfNeeded() {
        if (mMap != null) {
            getAllEntries();
            return;
        }
        // get map fragment
        SupportMapFragment smf = ((SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map));
        smf.getMapAsync(this);
    }

    @Override
    public int getLayoutResId() {
        return R.layout.fragment_main;
    }

    @Override
    public void inOnCreateView(View root, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        // instantiate the google map field variable
        mMap = googleMap;

        try {
            // Customise the styling of the base map using a JSON object defined
            // in a raw resource file.
            boolean success = googleMap.setMapStyle(
                    MapStyleOptions.loadRawResourceStyle(
                            getActivity(), R.raw.map_style));

            if (!success) {
                Log.e(TAG, "Style parsing failed.");
            }
        } catch (Resources.NotFoundException e) {
            Log.e(TAG, "Can't find style. Error: ", e);
        }

        // configure cluster manager
        mClusterManager = new ClusterManager<EntryItem>(getActivity(), mMap);
        final EventIconRendered rendered = new EventIconRendered(getActivity().getApplicationContext(), googleMap, mClusterManager);
        mClusterManager.setRenderer(rendered);

        mClusterManager.setOnClusterItemClickListener(new ClusterManager.OnClusterItemClickListener<EntryItem>() {
            @Override
            public boolean onClusterItemClick(EntryItem entryItem) {
                GeoEntry currentEntry = entryItem.getGeoEntry();
                PopupUtils.showMarkerInfoPopup(getActivity(), mMainLayout, currentEntry, mDisplayingCustomEntries);
                return true;
            }
        });

        mClusterManager.setOnClusterClickListener(new ClusterManager.OnClusterClickListener<EntryItem>() {
            @Override
            public boolean onClusterClick(Cluster<EntryItem> cluster) {
                ArrayList<EntryItem> clusterList = (ArrayList)cluster.getItems();
                PopupUtils.showClusterInfoPopup(getActivity(), mMainLayout, clusterList, mDisplayingCustomEntries);
                return true;
            }
        });

        // setup listeners
        getMap().setOnCameraIdleListener(mClusterManager);
        getMap().setOnMarkerClickListener(mClusterManager);
        getMap().setOnCameraIdleListener(getCameraIdleListener());
        getMap().setOnMapLongClickListener(this);
        getMap().setOnInfoWindowClickListener(this);
        getMap().setOnMapClickListener(this);
    }

    private GoogleMap.OnCameraIdleListener getCameraIdleListener() {
        return new GoogleMap.OnCameraIdleListener() {
            @Override
            public void onCameraIdle() {
                mClusterManager.cluster();
                getAllEntries();
                addAllEntriesToMap();
                getBounds();
            }
        };
    }

    private void getAllEntries(){
        if(mCurrentlyDisplaying.equals("all")) {

            LatLngBounds bounds = mMap.getProjection().getVisibleRegion().latLngBounds;
            mDataUtils.readAllEntries(bounds.southwest.latitude,
                    bounds.northeast.latitude,
                    mFilterOptions.getNumericalFromDate(),
                    mFilterOptions.getNumericalToDate(),
                    mFilterOptions.getCheckedTypes(),
                    mEntryMap);
        }
    }

    private void getSpecificEntries(){

        if (mCurrentlyDisplaying.equals("user_entries")){
            mDataUtils.readUserEntries(mUser.getUserID(),
                    mFilterOptions.getNumericalFromDate(),
                    mFilterOptions.getNumericalToDate(),
                    mFilterOptions.getCheckedTypes(),
                    mEntryMap);
        } else if (mCurrentlyDisplaying.equals("group_entries")){
            mDataUtils.readGroupEntries(mGroup.getGroupID(),
                    mFilterOptions.getNumericalFromDate(),
                    mFilterOptions.getNumericalToDate(),
                    mFilterOptions.getCheckedTypes(),
                    mEntryMap);
        }
    }

    private void getBounds(){

        if (mCurrentlyDisplaying.equals("all") || !mFirstIdle){
            return;
        }

        if (mEntryMap.isEmpty()){
            return;
        }

        LatLngBounds.Builder bounds = new LatLngBounds.Builder();
        for (Map.Entry e: mEntryMap.entrySet()) {
            GeoEntry entry = (GeoEntry) e.getValue();
            bounds.include(new LatLng(entry.getLatitude(), entry.getLongitude()));
        }
        getMap().animateCamera(CameraUpdateFactory.newLatLngBounds(bounds.build(), 50), 1000, new GoogleMap.CancelableCallback() {
            @Override
            public void onFinish() {
                if (mCurrentlyDisplaying.equals("geofence_entries")) {
                    PopupUtils.showClusterInfoPopup(getActivity(), mMainLayout, mGeofenceList, mDisplayingCustomEntries);
                }

            }

            @Override
            public void onCancel() {

            }
        });
        mFirstIdle = false;
    }

    private void addEntry(){
        EntryItem marker = new EntryItem(mEntry.getLatitude(), mEntry.getLongitude(), mEntry.getTitle(), mEntry.getFileType(), mEntry);
        mClusterManager.addItem(marker);
        mClusterManager.cluster();

        CameraPosition position = CameraPosition.builder()
                .target( new LatLng( mEntry.getLatitude(),
                        mEntry.getLongitude() ) )
                .zoom( 16f )
                .bearing( 0.0f )
                .tilt( 0.3f )
                .build();

        getMap().animateCamera( CameraUpdateFactory.newCameraPosition( position ) );
    }

    private void addAllEntriesToMap() {

        if(this.mMap != null) {
            //This is the current viewable region of the map
            LatLngBounds bounds = this.mMap.getProjection().getVisibleRegion().latLngBounds;

            //Loop through all the items that are available to be placed on the map
            Iterator it = mEntryMap.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry pair = (Map.Entry)it.next();
                GeoEntry entry = (GeoEntry) pair.getValue();

                if(bounds.contains(new LatLng(entry.getLatitude(), entry.getLongitude()))) {
                    if(!visibleMarkers.containsKey(entry.getEntryID())) {

                        EntryItem marker = new EntryItem(entry.getLatitude(), entry.getLongitude(), entry.getTitle(), entry.getFileType(), entry);
                        visibleMarkers.put(entry.getEntryID(), marker);
                        mClusterManager.addItem(marker);
                        mClusterManager.cluster();
                    }
                } else {
                    //If the course was previously on screen
                    if(visibleMarkers.containsKey(entry.getEntryID())) {
                        mClusterManager.removeItem(visibleMarkers.get(entry.getEntryID()));
                        mClusterManager.cluster();
                        visibleMarkers.remove(entry.getEntryID());
                        // remove from iterator
                        if (mCurrentlyDisplaying == "all") {
                            it.remove();
                        }
                    }
                }
            }
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    public void onStop() {
        super.onStop();
        if( mGoogleApiClient != null && mGoogleApiClient.isConnected() ) {
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        mCurrentLocation = LocationServices
                .FusedLocationApi
                .getLastLocation( mGoogleApiClient );

        initCamera( mCurrentLocation );
    }

    private void initCamera( Location location ) {
        CameraPosition position = CameraPosition.builder()
                .target( new LatLng( location.getLatitude(),
                        location.getLongitude() ) )
                .zoom( 16f )
                .bearing( 0.0f )
                .tilt( 0.3f )
                .build();

        if(mCurrentlyDisplaying.equals("all")) {
            getMap().animateCamera(CameraUpdateFactory.newCameraPosition(position), null);
        }
        getMap().setMapType( MAP_TYPES[curMapTypeIndex] );
        getMap().getUiSettings().setCompassEnabled(false);
        getMap().getUiSettings().setMapToolbarEnabled(false);
        getMap().setBuildingsEnabled(false);
        getMap().setMyLocationEnabled(true);
        getMap().getUiSettings().setMyLocationButtonEnabled(false);

//        getMap().getUiSettings().setZoomControlsEnabled( true );
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onInfoWindowClick(Marker marker) {

    }

    @Override
    public void onMapClick(LatLng latLng) {
    }

    @Override
    public void onMapLongClick(LatLng latLng) {
    }

    @Override
    public boolean onMarkerClick(final Marker marker) {
        return false;
    }

    public void showFilterPopup(View anchorView) {

        // TODO check boxes for media types and date pickers to and from dates
        View popupView = getActivity().getLayoutInflater().inflate(R.layout.popup_filter, null);

        // PopupWindow popupWindow = new PopupWindow(popupView, RecyclerView.LayoutParams.WRAP_CONTENT, RecyclerView.LayoutParams.WRAP_CONTENT);
        final PopupWindow popupWindow = new PopupWindow(popupView, RecyclerView.LayoutParams.MATCH_PARENT, RecyclerView.LayoutParams.MATCH_PARENT , true);

        // If the PopupWindow should be focusable
        popupWindow.setFocusable(true);
        // If you need the PopupWindow to dismiss when when touched outside
        popupWindow.setBackgroundDrawable(new ColorDrawable());

        int location[] = new int[2];

        // Get the View's(the one that was clicked in the Fragment) location
        anchorView.getLocationOnScreen(location);

        // Using location, the PopupWindow will be displayed right under anchorView
        popupWindow.showAtLocation(anchorView, Gravity.CENTER, 0, 0);

        // connect time UI elements
        mDisplayFromDate = (TextView) popupView.findViewById(R.id.display_from_date);
        mDisplayToDate = (TextView) popupView.findViewById(R.id.display_to_date);

        final CheckBox checkBoxImage = (CheckBox) popupView.findViewById(R.id.checkbox_image);
        final CheckBox checkBoxAudio = (CheckBox) popupView.findViewById(R.id.checkbox_audio);
        Button button = (Button) popupView.findViewById(R.id.apply_filters);
        mTempFilterOptions = new FilterOptions();

        mDisplayToDate.setText(mFilterOptions.getToDate());
        mDisplayFromDate.setText(mFilterOptions.getFromDate());

        checkBoxImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkBoxImage.isChecked()){
                    mTempFilterOptions.getCheckedTypes().put(DataUtils.IMAGE, true);
                } else {
                    mTempFilterOptions.getCheckedTypes().put(DataUtils.IMAGE, false);
                }
            }
        });

        checkBoxAudio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkBoxAudio.isChecked()){
                    mTempFilterOptions.getCheckedTypes().put(DataUtils.AUDIO, true);
                } else {
                    mTempFilterOptions.getCheckedTypes().put(DataUtils.AUDIO, false);
                }
            }
        });

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mFilterOptions.setFromDate(mTempFilterOptions.getFromDate());
                mFilterOptions.setToDate(mTempFilterOptions.getToDate());
                mFilterOptions.setCheckedTypes(mTempFilterOptions.getCheckedTypes());
                popupWindow.dismiss();
            }
        });

        dateTimeListeners();
    }

    public void dateTimeListeners(){

        mDateTime = new SimpleDateFormat(DATE_TIME, Locale.UK);

        mCalendar = Calendar.getInstance();


        // listner for text field, launches android calendar
        mDisplayFromDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mFromTimePicker.show();
            }
        });

        // listner for text field, launches android calendar
        mDisplayToDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mToTimePicker.show();
            }
        });


        // date listner update when changed
        mFromTimePicker = new DatePickerDialog(getContext(), new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                mCalendar.set(year, monthOfYear, dayOfMonth);
                mDateTime.applyPattern(DATE);
                String fromDate = mDateTime.format(mCalendar.getTime());
                mDisplayFromDate.setText(fromDate);
                mTempFilterOptions.setFromDate(fromDate);
            }

        },mCalendar.get(Calendar.YEAR), mCalendar.get(Calendar.MONTH), mCalendar.get(Calendar.DAY_OF_MONTH));

        // date listner update when changed
        mToTimePicker = new DatePickerDialog(getContext(), new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                mCalendar.set(year, monthOfYear, dayOfMonth);
                mDateTime.applyPattern(DATE);
                String toDate = mDateTime.format(mCalendar.getTime());
                mDisplayToDate.setText(toDate);
                mTempFilterOptions.setToDate(toDate);
            }

        },mCalendar.get(Calendar.YEAR), mCalendar.get(Calendar.MONTH), mCalendar.get(Calendar.DAY_OF_MONTH));
    }
}
