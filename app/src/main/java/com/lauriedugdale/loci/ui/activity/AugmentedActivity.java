package com.lauriedugdale.loci.ui.activity;

import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.opengl.Matrix;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceView;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.lauriedugdale.loci.R;
import com.lauriedugdale.loci.data.dataobjects.CameraPoint;
import com.lauriedugdale.loci.data.dataobjects.GeoEntry;
import com.lauriedugdale.loci.ui.ar.AugmentedCamera;
import com.lauriedugdale.loci.ui.ar.AugmentedOverlayView;
import com.lauriedugdale.loci.utils.InterfaceUtils;
import com.lauriedugdale.loci.utils.PopupUtils;

import java.util.ArrayList;

public class AugmentedActivity extends AppCompatActivity implements SensorEventListener, LocationListener {

    private static final String TAG = AugmentedActivity.class.getSimpleName();

    private static final long MINIMUM_DISTANCE_FOR_UPDATES = 1000; // 10 meters
    private static final long MINIMUM_TIME_BETWEEN_UPDATES = 1000 * 60 * 10;// 1 minute

    private SurfaceView mSurfaceView;
    private FrameLayout mOverlay;
    private AugmentedOverlayView augmentedOverlayView;
    private Camera mCamera;
    private AugmentedCamera mAugmentedCamera;

    private SensorManager mSensorManager;

    private LocationManager mLocationManager;
    private Location mLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_augmented);

        // find UI elements
        mSensorManager = (SensorManager) this.getSystemService(SENSOR_SERVICE);
        mOverlay = (FrameLayout) findViewById(R.id.overlay_view);
        mSurfaceView = (SurfaceView) findViewById(R.id.camera_view);

        augmentedOverlayView = new AugmentedOverlayView(this);

        // setup toolbar
        InterfaceUtils.setUpToolbar(this, R.id.ar_toolbar, "View entries");
    }

    @Override
    public void onResume() {
        super.onResume();
        // initialise the required views and services
        initLocationService();
        startAugmentedCameraView();
        setupSensors();
        startAugmentedOverlayView();
    }

    @Override
    public void onPause() {
        // let go of the camera for other applications to use
        stopCamera();
        super.onPause();
    }

    /**
     * Start the augmented overlay view
     */
    public void startAugmentedOverlayView() {

        // if the augmented overlay view already has a parent remove the view from its parent
        if (augmentedOverlayView.getParent() != null) {
            ((ViewGroup) augmentedOverlayView.getParent()).removeView(augmentedOverlayView);
        }
        // add the augmented overlay view to the view
        mOverlay.addView(augmentedOverlayView);
    }

    /**
     * start the Augmented camera view
     */
    public void startAugmentedCameraView() {

        // if surface view already has a parent remove it from the parent
        if (mSurfaceView.getParent() != null) {
            ((ViewGroup) mSurfaceView.getParent()).removeView(mSurfaceView);
        }
        // add surface view to the overlay
        mOverlay.addView(mSurfaceView);

        // if AugmentedCamera hasnt already been created create it
        if (mAugmentedCamera == null) {
            mAugmentedCamera = new AugmentedCamera(this, mSurfaceView);
        }

        // if augmented camera view already has a parent remove it from the parent
        if (mAugmentedCamera.getParent() != null) {
            ((ViewGroup) mAugmentedCamera.getParent()).removeView(mAugmentedCamera);
        }

        // add the camera view
        mOverlay.addView(mAugmentedCamera);
        mAugmentedCamera.setKeepScreenOn(true);
        startCamera();
    }

    /**
     * start the camera
     */
    private void startCamera() {
        // if there is a camera
        int numCams = Camera.getNumberOfCameras();
        if(numCams > 0){
            try{
                // open camera and set it to the AugmentedCamera view
                mCamera = Camera.open();
                mCamera.startPreview();
                mAugmentedCamera.setCamera(mCamera);
            } catch (RuntimeException ex){
                // notify the user via toast if camera cannot be found
                Toast.makeText(this, "Cannot find the camera", Toast.LENGTH_LONG).show();
            }
        }
    }

    /**
     * Turn the camera off and let other applications use it
     */
    private void stopCamera() {

        // if camera has been created
        if(mCamera != null) {
            mCamera.stopPreview();
            mCamera.setPreviewCallback(null);
            mAugmentedCamera.setCamera(null);
            mCamera.release();
            mCamera = null;
        }
    }

    /**
     * setup the sensors we need
     */
    private void setupSensors() {
        mSensorManager.registerListener(this,
                mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR),
                SensorManager.SENSOR_DELAY_FASTEST);
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {

        // if the sensor acted up is the rotation vector
        if (sensorEvent.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR) {
            float[] rotationMatrixFromVector = new float[16];
            float[] rotatedProjectionMatrix = new float[16];
            float[] projectionMatrix = new float[16];

            // put the sensor values in the rotation matric
            SensorManager.getRotationMatrixFromVector(rotationMatrixFromVector, sensorEvent.values);

            if (mAugmentedCamera != null) {
                projectionMatrix = mAugmentedCamera.getProjectionMatrix();
            }

            Matrix.multiplyMM(rotatedProjectionMatrix, 0, projectionMatrix, 0, rotationMatrixFromVector, 0);
            augmentedOverlayView.updateRotatedProjectionMatrix(rotatedProjectionMatrix);
        }
    }

    private void initLocationService() {


        if (Build.VERSION.SDK_INT >= 23 && ContextCompat.checkSelfPermission( this, android.Manifest.permission.ACCESS_FINE_LOCATION ) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        try {
            mLocationManager = (LocationManager) this.getSystemService(this.LOCATION_SERVICE);

            // Get GPS and network status
            boolean isGPSEnabled = mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            boolean isNetworkEnabled = mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

            if (isNetworkEnabled) {
                mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                        MINIMUM_TIME_BETWEEN_UPDATES,
                        MINIMUM_DISTANCE_FOR_UPDATES, this);
                if (mLocationManager != null)   {
                    mLocation = mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                    updateLatestLocation();
                }
            }

            if (isGPSEnabled)  {
                mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                        MINIMUM_TIME_BETWEEN_UPDATES,
                        MINIMUM_DISTANCE_FOR_UPDATES, this);

                if (mLocationManager != null)  {
                    mLocation = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                    updateLatestLocation();
                }
            }
        } catch (Exception e)  {
            Log.e(TAG, e.getMessage());
        }
    }

    private void updateLatestLocation() {
        if (augmentedOverlayView !=null) {
            augmentedOverlayView.updateCurrentLocation(mLocation);
        }
    }

    public void displayEntries(ArrayList<CameraPoint> entries){

        ArrayList<GeoEntry> entryList = new ArrayList<>();

        if (entries.size() == 1){
            PopupUtils.showMarkerInfoPopup(this, this.mSurfaceView, entries.get(0).getEntry(), false);
        } else {
            for (CameraPoint cp : entries){
                entryList.add(cp.getEntry());
            }
            PopupUtils.showClusterInfoPopup(this, this.mSurfaceView, entryList, false, null);
        }
    }


    @Override
    public void onLocationChanged(Location location) {
        updateLatestLocation();
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {
    }

    @Override
    public void onProviderEnabled(String s) {
    }

    @Override
    public void onProviderDisabled(String s) {
    }
}