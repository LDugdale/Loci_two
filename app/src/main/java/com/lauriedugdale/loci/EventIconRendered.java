package com.lauriedugdale.loci;

import android.content.Context;
import android.graphics.BitmapFactory;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;
import com.lauriedugdale.loci.data.DataUtils;
import com.lauriedugdale.loci.data.dataobjects.GeoEntry;

/**
 * Created by mnt_x on 28/06/2017.
 */

public class EventIconRendered extends DefaultClusterRenderer<EntryItem> {

    private Context mContext;

    public EventIconRendered(Context context, GoogleMap map, ClusterManager<EntryItem> clusterManager) {
        super(context, map, clusterManager);
        mContext = context;
    }

    @Override
    protected void onBeforeClusterItemRendered(EntryItem item, MarkerOptions markerOptions) {
        getMarkerForItem(item, markerOptions);
        markerOptions.title(item.getTitle());
        super.onBeforeClusterItemRendered(item, markerOptions);
    }
    /**
     * Change map marker according to the file type they are representing
     *
     * @param entry the GeoEntry thats attached to the map marker
     * @return
     */
    private void getMarkerForItem(EntryItem entry, MarkerOptions mo) {

        switch(entry.getFileType()){
            case DataUtils.NO_MEDIA:
                mo.icon( BitmapDescriptorFactory.fromBitmap(BitmapFactory.decodeResource( mContext.getResources(), R.mipmap.blank_marker ) ) );
                break;
            case DataUtils.IMAGE:
                mo.icon( BitmapDescriptorFactory.fromBitmap(BitmapFactory.decodeResource( mContext.getResources(), R.mipmap.image_marker ) ) );

                break;
            case DataUtils.AUDIO:
                mo.icon( BitmapDescriptorFactory.fromBitmap(BitmapFactory.decodeResource( mContext.getResources(), R.mipmap.audio_marker ) ) );
                break;
            default:
                mo.icon( BitmapDescriptorFactory.fromBitmap(BitmapFactory.decodeResource( mContext.getResources(), R.mipmap.blank_marker ) ) );
                break;
        }
    }

}