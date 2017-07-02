package com.lauriedugdale.loci.ui.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.lauriedugdale.loci.EntryItem;
import com.lauriedugdale.loci.R;
import com.lauriedugdale.loci.data.DataUtils;
import com.lauriedugdale.loci.data.dataobjects.GeoEntry;
import com.lauriedugdale.loci.data.dataobjects.User;
import com.lauriedugdale.loci.ui.activity.entry.AudioEntryActivity;
import com.lauriedugdale.loci.ui.activity.entry.ImageEntryActivity;
import com.lauriedugdale.loci.ui.activity.entry.NoMediaActivity;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by mnt_x on 30/06/2017.
 */
//TODO Consider use of generics for adapter classes
public class MapClusterAdapter extends RecyclerView.Adapter<MapClusterAdapter.ViewHolder> {

    // Store the context and cursor for easy access
    private Context mContext;
    private ArrayList<EntryItem> mEntries;

    /**
     * Entry adapter constructor
     *
     * @param context
     */
    public MapClusterAdapter(Context context, ArrayList<EntryItem> clusterList) {
        this.mContext = context;
        mEntries = clusterList;
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
        System.out.println("this is the position" + position);
        final EntryItem clusterItem = mEntries.get(position);
        final GeoEntry entry = clusterItem.getGeoEntry();
        System.out.println("entry title for cluster view = " +entry.getTitle() );
        viewHolder.mTitle.setText(entry.getTitle());

        viewHolder.mShowEntry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent startViewEntryIntent = new Intent(mContext, getDestination(entry));
                startViewEntryIntent.putExtra(Intent.ACTION_OPEN_DOCUMENT, entry);
                mContext.startActivity(startViewEntryIntent);
            }
        });
    }
    private Class getDestination(GeoEntry entry){
        Class destination = null;
        switch(entry.getFileType()){
            case DataUtils.IMAGE:
                destination = ImageEntryActivity.class;
                break;
            case DataUtils.AUDIO:
                destination = AudioEntryActivity.class;
                break;
            case DataUtils.NO_MEDIA:
                destination = NoMediaActivity.class;
                break;
            default:
                break;
        }

        return destination;
    }

    @Override
    /**
     * return the total count of items in the list
     */
    public int getItemCount() {
        if (mEntries == null) {
            return 0;
        }
        return mEntries.size();
    }

    /**
     * CLASS
     * Used to cache the views within the layout for quick access
     */
    class ViewHolder extends RecyclerView.ViewHolder{

        // The UI elements
        public TextView mTitle;
        public TextView mShowEntry;
        public ImageView mEntryType;


        public ViewHolder(View itemView) {
            super(itemView);

            // Find the UI elements
            mTitle = (TextView) itemView.findViewById(R.id.cluster_info_bar_title);
            mShowEntry = (TextView) itemView.findViewById(R.id.cluster_info_bar_show_entry);
            mEntryType = (ImageView) itemView.findViewById(R.id.cluster_info_bar_type);
        }
    }
}