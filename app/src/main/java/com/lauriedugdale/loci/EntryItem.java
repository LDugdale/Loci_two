package com.lauriedugdale.loci;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;

/**
 * Created by mnt_x on 28/06/2017.
 */

public class EntryItem implements ClusterItem {

    private final LatLng mPosition;
    private final String mTitle;
    private int fileType;

    public EntryItem(double lat, double lng, String mTitle, int fileType) {
        this.mTitle = mTitle;
        this.fileType = fileType;
        mPosition = new LatLng(lat, lng);
    }

    @Override
    public LatLng getPosition() {
        return mPosition;
    }

    public String getTitle() {
        return mTitle;
    }

    public int getFileType() {
        return fileType;
    }
}
