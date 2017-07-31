package com.lauriedugdale.loci;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;
import com.lauriedugdale.loci.data.dataobjects.GeoEntry;

/**
 * Created by mnt_x on 28/06/2017.
 */

public class EntryItem implements ClusterItem {

    private final LatLng mPosition;
    private final String mTitle;
    private int fileType;
    private GeoEntry geoEntry;


    public EntryItem(double lat, double lng, String mTitle, int fileType, GeoEntry geoEntry) {
        this.mTitle = mTitle;
        this.fileType = fileType;
        mPosition = new LatLng(lat, lng);
        this.geoEntry = geoEntry;

    }

    public GeoEntry getGeoEntry() {
        return geoEntry;
    }

    public void setGeoEntry(GeoEntry geoEntry) {
        this.geoEntry = geoEntry;
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
