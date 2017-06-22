package com.lauriedugdale.loci.ui.fragment;

import android.content.Intent;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.lauriedugdale.loci.data.DataUtils;
import com.lauriedugdale.loci.GeoEntry;
import com.lauriedugdale.loci.R;
import com.lauriedugdale.loci.ui.activity.AudioEntryActivity;
import com.lauriedugdale.loci.ui.activity.FullScreenActivity;
import com.lauriedugdale.loci.ui.activity.ImageEntryActivity;

import java.util.HashMap;
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

    private static float MAXIMUM_DISTANCE = 50.0f;

    private GoogleMap mMap;

    private GoogleApiClient mGoogleApiClient;
    private Location mCurrentLocation;

    private final int[] MAP_TYPES = { GoogleMap.MAP_TYPE_SATELLITE,
            GoogleMap.MAP_TYPE_NORMAL,
            GoogleMap.MAP_TYPE_HYBRID,
            GoogleMap.MAP_TYPE_TERRAIN,
            GoogleMap.MAP_TYPE_NONE };

    private int curMapTypeIndex = 3;

    private DataUtils mDataUtils;

    private HashMap<String, Marker> visibleMarkers;

    private RelativeLayout mInfoBar;
    private TextView mInfoBarTitle;
    private ImageView mInfoBarImage;
    private TextView mInfoBarShowEntry;

    private FusedLocationProviderClient mFusedLocationClient;

    private GeoEntry mCurrentEntry;

    private boolean mIsWithinBounds;

    public static MainFragment create(){

        return new MainFragment();
    }


    public GoogleMap getMap() {
        return mMap;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main, container, false);
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(getActivity());

        mDataUtils = new DataUtils(getActivity());
        visibleMarkers = new HashMap<String, Marker>();


        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setHasOptionsMenu(true);

        mGoogleApiClient = new GoogleApiClient.Builder( getActivity() )
                .addConnectionCallbacks( this )
                .addOnConnectionFailedListener( this )
                .addApi( LocationServices.API )
                .build();

        mInfoBar = (RelativeLayout) getActivity().findViewById(R.id.info_bar);
        mInfoBarTitle = (TextView) getActivity().findViewById(R.id.info_bar_title);
        mInfoBarImage = (ImageView) getActivity().findViewById(R.id.info_bar_type);
        mInfoBarShowEntry = (TextView) getActivity().findViewById(R.id.info_bar_show_entry);
    }

    private void initListeners() {
        getMap().setOnMarkerClickListener(this);
        getMap().setOnMapLongClickListener(this);
        getMap().setOnInfoWindowClickListener(this);
        getMap().setOnMapClickListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();
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
        mMap = googleMap;
        initListeners();
//        mDataUtils.readFile();
        mMap.setOnCameraIdleListener(getCameraIdleListener());

    }

    public GoogleMap.OnCameraIdleListener getCameraIdleListener()
    {
        return new GoogleMap.OnCameraIdleListener() {
            @Override
            public void onCameraIdle() {

                LatLngBounds bounds = mMap.getProjection().getVisibleRegion().latLngBounds;
                mDataUtils.readAllEntries(bounds.southwest.latitude, bounds.northeast.latitude);

                addEntryToMap();
            }
        };
    }

    public MarkerOptions getMarkerForItem(GeoEntry entry) {

        MarkerOptions mo = new MarkerOptions();
        mo.position(new LatLng(entry.getLatitude(), entry.getLongitude())).title(entry.getTitle());

        switch(entry.getFileType()){
            case DataUtils.NO_MEDIA:
                mo.icon( BitmapDescriptorFactory.fromBitmap(BitmapFactory.decodeResource( getResources(), R.mipmap.blank_marker ) ) );
                break;
            case DataUtils.IMAGE:
                mo.icon( BitmapDescriptorFactory.fromBitmap(BitmapFactory.decodeResource( getResources(), R.mipmap.image_marker ) ) );

                break;
            case DataUtils.AUDIO:
                mo.icon( BitmapDescriptorFactory.fromBitmap(BitmapFactory.decodeResource( getResources(), R.mipmap.audio_marker ) ) );
                break;
            default:
                mo.icon( BitmapDescriptorFactory.fromBitmap(BitmapFactory.decodeResource( getResources(), R.mipmap.blank_marker ) ) );
                break;
        }

        return mo;
    }

    //Note that the type "Items" will be whatever type of object you're adding markers for so you'll
    //likely want to create a List of whatever type of items you're trying to add to the map and edit this appropriately
    //Your "Item" class will need at least a unique id, latitude and longitude.
    public void addEntryToMap() {

        if(this.mMap != null) {
            //This is the current user-viewable region of the map
            LatLngBounds bounds = this.mMap.getProjection().getVisibleRegion().latLngBounds;

            //Loop through all the items that are available to be placed on the map
            for(Map.Entry<String, GeoEntry> entry : mDataUtils.getEntryList().entrySet()) {

                //If the item is within the the bounds of the screen
                if(bounds.contains(new LatLng(entry.getValue().getLatitude(), entry.getValue().getLongitude()))) {
                    //If the item isn't already being displayed
                    if(!visibleMarkers.containsKey(entry.getValue().getEntryID())) {
                        //Add the Marker to the Map and keep track of it with the HashMap
                        //getMarkerForItem just returns a MarkerOptions object

                        Marker m = this.mMap.addMarker(getMarkerForItem(entry.getValue()));
                        m.setTag(entry.getValue());
                        this.visibleMarkers.put(entry.getValue().getEntryID(), m);
                    }
                } else {
                    //If the course was previously on screen
                    if(visibleMarkers.containsKey(entry.getValue().getEntryID())) {
                        //1. Remove the Marker from the GoogleMap
                        visibleMarkers.get(entry.getValue().getEntryID()).remove();

                        //2. Remove the reference to the Marker from the HashMap
                        visibleMarkers.remove(entry.getValue().getEntryID());
//                        mDataUtils.getEntryList().remove(entry.getValue().getId());
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

        getMap().animateCamera( CameraUpdateFactory.newCameraPosition( position ), null );

        getMap().setMapType( MAP_TYPES[curMapTypeIndex] );
        getMap().setBuildingsEnabled(true);
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
        mInfoBar.setVisibility(View.INVISIBLE);

    }

    @Override
    public void onMapLongClick(LatLng latLng) {

    }

    @Override
    public boolean onMarkerClick(final Marker marker) {
        checkDistance(marker.getPosition().latitude, marker.getPosition().longitude);
        mCurrentEntry = (GeoEntry)marker.getTag();

        GeoEntry geoMarker = (GeoEntry) marker.getTag();
        setInfoBarImage(geoMarker.getFileType());

        mInfoBarTitle.setText(geoMarker.getTitle());
        mInfoBar.setVisibility(View.VISIBLE);
        showEntry();

        return true;
    }

    private void setInfoBarImage(int type){
        // remove previous listener
        mInfoBarImage.setOnClickListener(null);
        switch(type){
            case DataUtils.NO_MEDIA:
                break;
            case DataUtils.IMAGE:
                mInfoBarImage.setImageDrawable(getResources().getDrawable(R.drawable.ic_image));
                showImage();
                break;
            case DataUtils.AUDIO:
                mInfoBarImage.setImageDrawable(getResources().getDrawable(R.drawable.ic_audiotrack));
                break;
            default:
                break;
        }
    }

    public void showImage(){

        if(!mIsWithinBounds){
            return;
        }

        mInfoBarImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent startViewEntryIntent = new Intent(getActivity(), FullScreenActivity.class);
                startViewEntryIntent.putExtra(Intent.ACTION_OPEN_DOCUMENT, mCurrentEntry);
                getActivity().startActivity(startViewEntryIntent);
            }
        });
    }

    public void playAudio(){

        if(!mIsWithinBounds){
            return;
        }

        mInfoBarImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }

    private float getDistanceInMeters(double lat1, double lng1, double lat2, double lng2) {
        float [] dist = new float[1];
        Location.distanceBetween(lat1, lng1, lat2, lng2, dist);
        return dist[0];
    }

    public void showEntry(){

        if(!mIsWithinBounds){
            return;
        }
        mInfoBarShowEntry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            Intent startViewEntryIntent = new Intent(getActivity(), getDestination());
            startViewEntryIntent.putExtra(Intent.ACTION_OPEN_DOCUMENT, mCurrentEntry);
            getActivity().startActivity(startViewEntryIntent);
            }
        });
    }

    public Class getDestination(){
        Class destination = null;
        switch(mCurrentEntry.getFileType()){
            case DataUtils.IMAGE:
                destination = ImageEntryActivity.class;
                break;
            case DataUtils.AUDIO:
                destination = AudioEntryActivity.class;
                break;
            default:
                break;
        }

        return destination;
    }

    public void checkDistance(final double markerLat, final double markerLng){

        mFusedLocationClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                // Got last known location. In some rare situations this can be null.
                if (location != null) {

                    Float distance = getDistanceInMeters(location.getLatitude(), location.getLongitude(), markerLat, markerLng);

                    mIsWithinBounds = (distance <= MAXIMUM_DISTANCE);
                }
            }
        });
    }

}
