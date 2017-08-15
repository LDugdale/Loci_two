package com.lauriedugdale.loci.ui.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.lauriedugdale.loci.map.EntryItem;
import com.lauriedugdale.loci.R;
import com.lauriedugdale.loci.data.UserDatabase;
import com.lauriedugdale.loci.data.dataobjects.GeoEntry;
import com.lauriedugdale.loci.utils.LocationUtils;

import java.util.ArrayList;

/**
 * The adapter for displaying either EntryItem or GeoEntry in a popupview when a cluster is clicked
 *
 * @author Laurie Dugdale
 */
public class MapClusterAdapter extends RecyclerView.Adapter<MapClusterAdapter.ViewHolder> {

    private Context mContext; // the current context
    private ArrayList<EntryItem> mEntryItems; // the stored EntryItem items
    private ArrayList<GeoEntry> mGeoEntries; // the stored GeoEntry items
    private UserDatabase mUserDatabase; // access to the User part of the database
    private DataType type; // the current data type the adapter is using

    // enum to check which data type is being stored
    private enum DataType {
        EntryItem,
        GeoEntry
    }

    /**
     * MapClusterAdapter constructor
     *
     * @param context
     */
    public MapClusterAdapter(Context context, ArrayList<?> clusterList) {
        this.mContext = context;
        mUserDatabase = new UserDatabase(context);
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

        // check the data type being stored
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
        // set author
        viewHolder.mAuthor.setText(entry.getCreatorName());
        // check distance, if its too far away hide the show entry button
        LocationUtils.checkDistance(mContext, viewHolder.mShowEntry, entry.getLatitude(), entry.getLongitude());
        // set image
        mUserDatabase.downloadNonLoggedInProfilePic(entry.getCreator(), viewHolder.mEntryAuthor, R.drawable.default_profile);
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
        public TextView mTitle;
        public TextView mShowEntry;
        public ImageView mEntryAuthor;
        public TextView mAuthor;



        public ViewHolder(View itemView) {
            super(itemView);

            // Find the UI elements
            mTitle = (TextView) itemView.findViewById(R.id.cluster_info_bar_title);
            mShowEntry = (TextView) itemView.findViewById(R.id.cluster_info_bar_show_entry);
            mEntryAuthor = (ImageView) itemView.findViewById(R.id.cluster_info_bar_type);
            mDistance = (TextView) itemView.findViewById(R.id.cluster_info_bar_marker_distance);
            mAuthor = (TextView) itemView.findViewById(R.id.cluster_info_bar_marker_author);

        }
    }
}