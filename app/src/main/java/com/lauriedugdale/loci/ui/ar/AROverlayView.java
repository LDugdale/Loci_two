package com.lauriedugdale.loci.ui.ar;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.location.Location;
import android.opengl.Matrix;
import android.view.View;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.maps.android.SphericalUtil;
import com.lauriedugdale.loci.data.DataUtils;
import com.lauriedugdale.loci.data.dataobjects.FilterOptions;
import com.lauriedugdale.loci.data.dataobjects.GeoEntry;
import com.lauriedugdale.loci.utils.LocationUtils;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * @author Laurie Dugdale
 */

public class AROverlayView extends View {

    Context mContext;
    private float[] rotatedProjectionMatrix = new float[16];
    private Location mLocation;
    private DataUtils mDataUtils;
    private FilterOptions mFilterOptions;
    private HashMap<String, GeoEntry> mEntryMap;

    public AROverlayView(Context context) {
        super(context);

        this.mContext = context;
        mDataUtils = new DataUtils(context);
        mFilterOptions = new FilterOptions();
        mEntryMap = new HashMap<String, GeoEntry>();
    }

    public void updateRotatedProjectionMatrix(float[] rotatedProjectionMatrix) {
        this.rotatedProjectionMatrix = rotatedProjectionMatrix;
        this.invalidate();
    }

    public void updateCurrentLocation(Location currentLocation){
        this.mLocation = currentLocation;
        getEntries();
        this.invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (mLocation == null) {
            return;
        }

        final int radius = 30;
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.WHITE);
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
        paint.setTextSize(60);


        Iterator it = mEntryMap.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry) it.next();
            GeoEntry entry = (GeoEntry) pair.getValue();

            float[] currentLocationInECEF = LocationUtils.WSG84toECEF(mLocation.getLatitude(), mLocation.getLongitude(), mLocation.getAltitude());
            float[] pointInECEF = LocationUtils.WSG84toECEF(entry.getLatitude(), entry.getLongitude(), entry.getAltitude());
            float[] pointInENU = LocationUtils.ECEFtoENU(mLocation, currentLocationInECEF, pointInECEF);

            float[] cameraCoordinateVector = new float[4];
            Matrix.multiplyMV(cameraCoordinateVector, 0, rotatedProjectionMatrix, 0, pointInENU, 0);

            // cameraCoordinateVector[2] is z, that al bggways less than 0 to display on right position
            // if z > 0, the point will display on the opposite
            if (cameraCoordinateVector[2] < 0) {
                float x  = (0.5f + cameraCoordinateVector[0]/cameraCoordinateVector[3]) * canvas.getWidth();
                float y = (0.5f - cameraCoordinateVector[1]/cameraCoordinateVector[3]) * canvas.getHeight();


                canvas.drawCircle(x, y, radius, paint);
                canvas.drawText(entry.getTitle(), x - (30 * entry.getTitle().length() / 2), y - 80, paint);
            }
        }
    }

    public void getEntries() {
        LatLng newLatLng = new LatLng(mLocation.getLatitude(), mLocation.getLongitude());
        LatLngBounds bounds = toBounds(newLatLng, 1609.34);
        mDataUtils.readAllEntriesForAR(bounds.southwest.latitude,
                bounds.northeast.latitude,
                mFilterOptions.getNumericalFromDate(),
                mFilterOptions.getNumericalToDate(),
                mFilterOptions.getCheckedTypes(),
                mEntryMap);

    }

    public LatLngBounds toBounds(LatLng center, double radius) {
        LatLng southwest = SphericalUtil.computeOffset(center, radius * Math.sqrt(2.0), 225);
        LatLng northeast = SphericalUtil.computeOffset(center, radius * Math.sqrt(2.0), 45);
        return new LatLngBounds(southwest, northeast);
    }
}
