package com.lauriedugdale.loci.ui.ar;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.location.Location;
import android.opengl.Matrix;
import android.view.MotionEvent;
import android.view.View;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.maps.android.SphericalUtil;
import com.lauriedugdale.loci.R;
import com.lauriedugdale.loci.data.DataUtils;
import com.lauriedugdale.loci.data.dataobjects.CameraPoint;
import com.lauriedugdale.loci.data.dataobjects.FilterOptions;
import com.lauriedugdale.loci.data.dataobjects.GeoEntry;
import com.lauriedugdale.loci.ui.activity.AugmentedActivity;
import com.lauriedugdale.loci.utils.LocationUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Laurie Dugdale
 */

public class AROverlayView extends View {

    private Context mContext;

    private Paint mPaint;

    private float[] rotatedProjectionMatrix = new float[16];
    private Location mLocation;
    private Location mLocationOld;
    private DataUtils mDataUtils;
    private FilterOptions mFilterOptions;
    private ArrayList<CameraPoint> mEntryList;

    private HashMap<Integer, ArrayList<CameraPoint>> mGroupedX;

    private ArrayList <Float> mXValues;
    private int mCountX;
    private int mPosition;

    private Bitmap mSingleEntry;
    private Bitmap mMultipleEntries;
    private int mBitmapHeight;
    private int mBitmapWidth;

    private Bitmap mMulti;
    private Bitmap mAudio;
    private Bitmap mImage;
    private Bitmap mBlank;

    private int mPartition;
    private int mRadius;

    public AROverlayView(Context context) {
        super(context);

        this.mContext = context;
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

        mDataUtils = new DataUtils(context);
        mFilterOptions = new FilterOptions();
        mEntryList = new ArrayList<CameraPoint>();
        mGroupedX = new HashMap<Integer, ArrayList<CameraPoint>>();

        mSingleEntry = BitmapFactory.decodeResource(context.getResources(), R.drawable.image_marker);
        mMultipleEntries = BitmapFactory.decodeResource(context.getResources(), R.drawable.image_marker);

        mBitmapHeight = mSingleEntry.getHeight();
        mBitmapWidth = mSingleEntry.getWidth();

        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setColor(Color.WHITE);
        mPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
        mPaint.setTextSize(60);

        mXValues = new ArrayList<>();

        mMulti = BitmapFactory.decodeResource( mContext.getResources(), R.mipmap.multi_ar_marker );
        mAudio = BitmapFactory.decodeResource( mContext.getResources(), R.mipmap.audio_ar_marker );
        mImage = BitmapFactory.decodeResource( mContext.getResources(), R.mipmap.image_ar_marker );
        mBlank = BitmapFactory.decodeResource( mContext.getResources(), R.mipmap.blank_ar_marker );

        mPartition = mMulti.getWidth();
        mRadius = mPartition / 2;
    }

    public void updateRotatedProjectionMatrix(float[] rotatedProjectionMatrix) {
        this.rotatedProjectionMatrix = rotatedProjectionMatrix;
        this.invalidate();
    }

    public void updateCurrentLocation(Location currentLocation){
        this.mLocation = currentLocation;
        if(mLocationOld == null) {
            entriesQuery();
            mLocationOld = currentLocation;
        }

        getEntries();
        this.invalidate();
    }

//    public float averageX(float x){
//        int oldPosition = mPosition;
//        mPosition++;
//
//        if (mCountX < 5){
//            mCountX++;
//        }
//
//        float value = 0;
//        if (mPosition < mEntryList.size()) {
//            mPosition = 0;
//        }
//        if (oldPosition == 0) {
//
//            if (mXValues.size() < mEntryList.size() - 1 ) {
//                mXValues.add(oldPosition, x);
//                value = mXValues.get(oldPosition);
//            } else {
//                float newValue = x + mXValues.get(mXValues.size() - 1);
//                mXValues.add(oldPosition, newValue);
//                value = mXValues.get(oldPosition);
//            }
//        } else {
//            float newValue = x + mXValues.get(oldPosition - 1);
//            mXValues.add(oldPosition, newValue);
//            value = mXValues.get(oldPosition);
//        }
//
//        return value / mCountX;
//    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        mGroupedX.clear();

        if (mLocation == null) {
            return;
        }

        float x;
        float y;

        for(CameraPoint e : mEntryList) {
            GeoEntry entry = e.getEntry();

            float[] currentLocationInECEF = LocationUtils.WGS84toECEF(mLocation.getLatitude(), mLocation.getLongitude(), 0);
            float[] pointInECEF = LocationUtils.WGS84toECEF(entry.getLatitude(), entry.getLongitude(), entry.getAltitude());
            float[] pointInENU = LocationUtils.ECEFtoENU(mLocation, currentLocationInECEF, pointInECEF);

            float[] cameraCoordinateVector = new float[4];
            Matrix.multiplyMV(cameraCoordinateVector, 0, rotatedProjectionMatrix, 0, pointInENU, 0);

            // cameraCoordinateVector[2] is z, that al bggways less than 0 to display on right position
            // if z > 0, the point will display on the opposite
            if (cameraCoordinateVector[2] < 0) {
                x  = (0.5f + cameraCoordinateVector[0]/cameraCoordinateVector[3]) * canvas.getWidth();
                y  = (0.5f - cameraCoordinateVector[1]/cameraCoordinateVector[3]) * canvas.getHeight();

//                x = averageX(x);

                int key = (int)(x/mPartition);

                if (mGroupedX.get(key) == null){
                    ArrayList<CameraPoint> inside = new ArrayList<>();
                    e.setX(x);
                    e.setY(y);
                    inside.add(e);
                    mGroupedX.put(key, inside);
                } else {
                    e.setX(x);
                    e.setY(y);
                    mGroupedX.get(key).add(e);
                }
            }
        }

        for (Map.Entry<Integer, ArrayList<CameraPoint>> e : mGroupedX.entrySet()){
            int listSize = e.getValue().size();
            x = e.getValue().get(0).getX();
            y = e.getValue().get(0).getY();
            GeoEntry entry = e.getValue().get(0).getEntry();

            if (listSize == 1) {
                canvas.drawBitmap(selectBitmap(entry), x, y, mPaint);
//                canvas.drawText(entry.getTitle(),
//                                x + mRadius - 15,
//                                y - 80,
//                                mPaint);
            } else {
                String label = clusterCount(listSize);
                float labelWidth = mPaint.measureText(label);

                Paint.FontMetrics fm = mPaint.getFontMetrics();
                float labelHeight = fm.descent - fm.ascent;

                canvas.drawBitmap(mMulti, x, y, mPaint);
//                canvas.drawCircle(x, y, radius, mPaint);

                canvas.drawText(label,
                        x + mRadius - (labelWidth / 2),
                        y + (mMulti.getHeight() /2) + (labelHeight / 3),
                        mPaint);
            }
        }
    }

    public Bitmap selectBitmap(GeoEntry entry){
        switch (entry.getFileType()){
            case DataUtils.IMAGE:
                return mImage;
            case DataUtils.AUDIO:
                return  mAudio;
            default:
                return mBlank;
        }
    }

    public String clusterCount(int size){

        String value = "";
        if(size <=5) {
            value = String.valueOf(size);
        } else if ( 5 < size && size <= 10) {
            value = "5+";
        } else if ( 10 < size && size <= 15) {
            value = "10+";
        } else if ( 15 < size && size <= 20) {
            value = "15+";
        } else if ( 20 < size) {
            value = "20+";
        }

        return value;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();

        switch(event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                for (Map.Entry<Integer, ArrayList<CameraPoint>> e : mGroupedX.entrySet()) {
                    float xPosition = e.getValue().get(0).getX();
                    float yPosition = e.getValue().get(0).getY();


                    //Check if the x and y position of the touch is inside the bitmap
                    if (x > xPosition - mRadius && x < xPosition + mPartition && y > yPosition - mRadius && y < yPosition + mPartition) {
                        ((AugmentedActivity)mContext).displaySelectEntries(e.getValue());
                        return true;
                    }
                }
        }
        return false;
    }

    public void getEntries() {

        float difference = LocationUtils.getDistanceInMeters(mLocation.getLatitude(), mLocation.getLongitude(), mLocationOld.getLatitude(), mLocationOld.getLongitude());

        if(difference > 1609) {
            entriesQuery();
            mLocationOld.set(mLocation);
        }

    }

    public void entriesQuery(){
        LatLng newLatLng = new LatLng(mLocation.getLatitude(), mLocation.getLongitude());
        LatLngBounds bounds = toBounds(newLatLng, 1609.34);
        mDataUtils.readAllEntriesForAR(bounds.southwest.latitude,
                bounds.northeast.latitude,
                mFilterOptions.getNumericalFromDate(),
                mFilterOptions.getNumericalToDate(),
                mFilterOptions.getCheckedTypes(),
                mEntryList);
    }

    public LatLngBounds toBounds(LatLng center, double radius) {
        LatLng southwest = SphericalUtil.computeOffset(center, radius * Math.sqrt(2.0), 225);
        LatLng northeast = SphericalUtil.computeOffset(center, radius * Math.sqrt(2.0), 45);
        return new LatLngBounds(southwest, northeast);
    }
}
