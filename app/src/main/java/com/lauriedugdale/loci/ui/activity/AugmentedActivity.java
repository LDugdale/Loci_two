package com.lauriedugdale.loci.ui.activity;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.lauriedugdale.loci.R;
import com.lauriedugdale.loci.ui.ar.EntrySurfaceView;

public class AugmentedActivity extends AppCompatActivity {

    private static final String TAG = "AugmentedActivity";
    private static boolean DEBUG = false;
    private SensorManager mSensorManager;
    private Sensor mSensor;
    private EntrySurfaceView mEntrySurfaceView;

    private float[] mGravity;
    private float[] mGeomagnetic;

    private float mR[] = new float[9];
    private float mI[] = new float[9];
    private float mResults[] = new float[3];
    private static final float ALPHA = 0.25f;
    private float[] mGravSensorVals;
    private float[] mMagSensorVals;
    private static float mAzimuth;
    private static float mPitch;
    private static float mRoll;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_augmented);

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);

        mEntrySurfaceView = (EntrySurfaceView) findViewById(R.id.entrySurfaceView);
    }


    private final SensorEventListener mListener = new SensorEventListener() {
        public void onSensorChanged(SensorEvent event) {
            mEntrySurfaceView.setOffset(event.values[0]);
            mEntrySurfaceView.setY(event.values[1]);
//            System.out.println(event.values[2]);
            mEntrySurfaceView.invalidate();
        }

        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
    };


    @Override
    protected void onResume() {
        if (DEBUG)
            Log.d(TAG, "onResume");
        super.onResume();
        mSensorManager.registerListener(mListener, mSensor,
                SensorManager.SENSOR_DELAY_GAME);
    }

    @Override
    protected void onStop() {
        if (DEBUG)
            Log.d(TAG, "onStop");
//        mSensorManager.unregisterListener(mListener);
        super.onStop();
    }

}
