package com.lauriedugdale.loci.ui.fragment.upload;

import android.content.res.Resources;
import android.graphics.Point;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.maps.android.SphericalUtil;
import com.lauriedugdale.loci.HideOverlayView;
import com.lauriedugdale.loci.R;
import com.lauriedugdale.loci.data.DataUtils;
import com.lauriedugdale.loci.data.EntryDatabase;
import com.lauriedugdale.loci.data.dataobjects.Group;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by mnt_x on 22/07/2017.
 */

public class UploadPageTwoFragment extends Fragment implements UploadPageOneFragment.OnNextButtonClickedListener,
                                                                OnMapReadyCallback,GoogleApiClient.ConnectionCallbacks,
                                                                GoogleMap.OnCameraChangeListener,
                                                                GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = UploadPageTwoFragment.class.getSimpleName();

    private GoogleMap mMap;
    private GoogleApiClient mGoogleApiClient;
    private HideOverlayView hideView;
    private Location mCurrentLocation;
    private Marker mMarker;

    private ImageView mDone;

    // potential map types its possible to display
    private final int[] MAP_TYPES = { GoogleMap.MAP_TYPE_SATELLITE,
            GoogleMap.MAP_TYPE_NORMAL,
            GoogleMap.MAP_TYPE_HYBRID,
            GoogleMap.MAP_TYPE_TERRAIN,
            GoogleMap.MAP_TYPE_NONE };

    private int curMapTypeIndex = 1; // chosen map type from MAP_TYPES

    private EntryDatabase mEntryDatabase; // handles data transactions with firebase

    public static UploadPageTwoFragment create(){
        return new UploadPageTwoFragment();
    }

    public GoogleMap getMap() {
        return mMap;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_upload_page_two, container, false);

        mEntryDatabase = new EntryDatabase(getActivity());

        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setHasOptionsMenu(true);

        hideView = (HideOverlayView) view.findViewById(R.id.hideview);
        mDone = (ImageView) view.findViewById(R.id.au_done);

        // set up google maps API
        mGoogleApiClient = new GoogleApiClient.Builder( getActivity() )
                .addConnectionCallbacks( this )
                .addOnConnectionFailedListener( this )
                .addApi( LocationServices.API )
                .build();
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

        getMap().setOnCameraChangeListener(this);

    }
    @Override
    public void onResume() {
        super.onResume();
        setUpMapIfNeeded();
    }

    private void setUpMapIfNeeded() {
        if (mMap != null) {
            return;
        }

        // get map fragment
        SupportMapFragment smf = ((SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.upload_map));
        smf.getMapAsync(this);
    }

    @Override
    public void onStart() {
        super.onStart();
        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();
        }
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
        FusedLocationProviderClient fusedLocationClient;
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(getActivity());

        fusedLocationClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location != null) {
                    initCamera( location );
                }
            }
        });

    }

    @Override
    public void onCameraChange(final CameraPosition cameraPosition) {

        LatLng markerPos = new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());

        if (mMarker == null) {
            mMarker = mMap.addMarker(new MarkerOptions().position(markerPos).draggable(true));
        }

        Projection projection = mMap.getProjection();
        float radius = 150f; // meters
        Point centerPoint = projection.toScreenLocation(cameraPosition.target);
        Point radiusPoint = projection.toScreenLocation( SphericalUtil.computeOffset(cameraPosition.target, radius, 90));
        float radiusPx = (float) Math.sqrt(Math.pow(centerPoint.x - radiusPoint.x, 2));

        hideView.reDraw(projection.toScreenLocation(mMarker.getPosition()), radiusPx);
    }

    private void initCamera( Location location ) {

        mCurrentLocation = location;
        CameraPosition position = CameraPosition.builder()
                .target( new LatLng( location.getLatitude(),
                        location.getLongitude() ) )
                .zoom( 16.8f )
                .bearing( 0.0f )
                .tilt( 0f )
                .build();

        getMap().animateCamera(CameraUpdateFactory.newCameraPosition(position), null);
        getMap().setMapType( MAP_TYPES[curMapTypeIndex] );
        getMap().getUiSettings().setCompassEnabled(false);
        getMap().getUiSettings().setMapToolbarEnabled(false);
        getMap().setBuildingsEnabled(false);
        getMap().getUiSettings().setMyLocationButtonEnabled(false);
        getMap().getUiSettings().setScrollGesturesEnabled(false);

        getMap().setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
            @Override
            public void onMarkerDragStart(Marker m) {
            }

            @Override
            public void onMarkerDragEnd(Marker m) {
                mCurrentLocation.setLatitude(m.getPosition().latitude);
                mCurrentLocation.setLongitude(m.getPosition().longitude);
            }

            @Override
            public void onMarkerDrag(Marker m) {
            }
        });
    }

    @Override
    public void onNextButtonClicked(final int permissionType, final String title, final String description, final Uri uploadData, final int dataType, final Group group) {

        mDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (uploadData == null){
                    System.out.println("ALKFJASKLFJALKFJSAKLFJ : " + permissionType);
                    mEntryDatabase.uploadEntryWithoutFile(
                            permissionType,
                            title,
                            description,
                            dataType,
                            group,
                            mCurrentLocation
                    );
                    getActivity().finish();
                } else {

                    mEntryDatabase.uploadEntryWithFile(
                            permissionType,
                            title,
                            description,
                            uploadData,
                            dataType,
                            group,
                            mCurrentLocation
                    );
                    getActivity().finish();
                }
            }
        });
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }


    @Override
    public void onConnectionSuspended(int i) {

    }

}
