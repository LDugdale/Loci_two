package com.lauriedugdale.loci.ui.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.lauriedugdale.loci.EntryItem;
import com.lauriedugdale.loci.R;
import com.lauriedugdale.loci.data.dataobjects.GeoEntry;
import com.lauriedugdale.loci.utils.EntryUtils;
import com.lauriedugdale.loci.utils.LocationUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

/**
 * Created by mnt_x on 30/06/2017.
 */
//TODO Consider use of generics for adapter classes
public class MapClusterAdapter extends RecyclerView.Adapter<MapClusterAdapter.ViewHolder> {

    // Store the context and cursor for easy access
    private Context mContext;
    private ArrayList<EntryItem> mEntryItems;
    private ArrayList<GeoEntry> mGeoEntries;
    private DataType type;

    private enum DataType {
        EntryItem,
        GeoEntry
    }

    /**
     * Entry adapter constructor
     *
     * @param context
     */
    public MapClusterAdapter(Context context, ArrayList<?> clusterList) {
        this.mContext = context;
        if(clusterList != null && clusterList.get(0) instanceof EntryItem) {
            mEntryItems = (ArrayList<EntryItem>) clusterList;
            type = DataType.EntryItem;
        } else if (clusterList != null && clusterList.get(0) instanceof GeoEntry){
            mGeoEntries = (ArrayList<GeoEntry>) clusterList;
            type = DataType.GeoEntry;
        }
    }

    @Override
    /**
     * Inflates a layout depending on its position and returns a ViewHolder
     */
    public MapClusterAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View contactView = null;

        // inflate second item layout & return that viewHolder
        contactView = inflater.inflate(R.layout.item_cluster_entry, parent, false);


        // Return a new holder instance
        return new MapClusterAdapter.ViewHolder(contactView);
    }

    @Override
    /**
     * Populates data into the layout through the viewholder
     */
    public void onBindViewHolder(MapClusterAdapter.ViewHolder viewHolder, int position) {

        GeoEntry e = new GeoEntry();
        if (type == DataType.EntryItem) {
            EntryItem clusterItem = mEntryItems.get(position);
            e = clusterItem.getGeoEntry();
        } else {
            e = mGeoEntries.get(position);
        }

        final GeoEntry entry = e;
        // Set Title
        viewHolder.mTitle.setText(entry.getTitle());
        // Set distance
        LocationUtils.displayDistance(viewHolder.mDistance, mContext, entry.getLatitude(), entry.getLongitude());
        // set date
        String dateString = new java.text.SimpleDateFormat("EEE, d MMM 'at' HH:mm", Locale.UK).format(new Date( entry.getUploadDate()));
        viewHolder.mDate.setText(dateString);
        // set author
        viewHolder.mAuthor.setText(entry.getCreatorName());
        // check distance, if its too far away hide the show entry button
        LocationUtils.checkDistance(mContext, viewHolder.mShowEntry, entry.getLatitude(), entry.getLongitude());
        // set image
        EntryUtils.getFilePic(viewHolder.mEntryType, entry);
        // add action listner
        viewHolder.mShowEntry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent startViewEntryIntent = new Intent(mContext, LocationUtils.getEntryDestinationClass(entry.getFileType()));
                startViewEntryIntent.putExtra(Intent.ACTION_OPEN_DOCUMENT, entry);
                mContext.startActivity(startViewEntryIntent);
            }
        });
    }

    @Override
    /**
     * return the total count of items in the list
     */
    public int getItemCount() {
        if (type == DataType.EntryItem) {
            if (mEntryItems == null) {
                return 0;
            }
            return mEntryItems.size();
        } else {
            if (mGeoEntries == null) {
                return 0;
            }
            return mGeoEntries.size();
        }
    }

    /**
     * CLASS
     * Used to cache the views within the layout for quick access
     */
    class ViewHolder extends RecyclerView.ViewHolder{

        // The UI elements
        public TextView mDistance;
        public TextView mDate;
        public TextView mTitle;
        public TextView mShowEntry;
        public ImageView mEntryType;
        public TextView mAuthor;



        public ViewHolder(View itemView) {
            super(itemView);

            // Find the UI elements
            mTitle = (TextView) itemView.findViewById(R.id.cluster_info_bar_title);
            mShowEntry = (TextView) itemView.findViewById(R.id.cluster_info_bar_show_entry);
            mEntryType = (ImageView) itemView.findViewById(R.id.cluster_info_bar_type);
            mDistance = (TextView) itemView.findViewById(R.id.cluster_info_bar_marker_distance);
            mDate = (TextView) itemView.findViewById(R.id.cluster_info_bar_marker_date);
            mAuthor = (TextView) itemView.findViewById(R.id.cluster_info_bar_marker_author);

        }
    }
}