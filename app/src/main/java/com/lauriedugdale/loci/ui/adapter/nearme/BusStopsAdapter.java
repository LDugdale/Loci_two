package com.lauriedugdale.loci.ui.adapter.nearme;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.lauriedugdale.loci.R;
import com.lauriedugdale.loci.data.DataUtils;
import com.lauriedugdale.loci.data.EntryStorage;
import com.lauriedugdale.loci.data.dataobjects.BusStop;
import com.lauriedugdale.loci.data.dataobjects.GeoEntry;
import com.lauriedugdale.loci.ui.activity.MainActivity;
import com.lauriedugdale.loci.utils.LocationUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Created by mnt_x on 31/07/2017.
 */

public class BusStopsAdapter extends RecyclerView.Adapter<BusStopsAdapter.ViewHolder> {

    // Store the context and cursor for easy access
    private Context mContext;
    private List<BusStop> mStops;

    /**
     * NearMeAdapter constructor
     *
     * @param context
     */
    public BusStopsAdapter(Context context) {
        this.mContext = context;
        mStops = new ArrayList<BusStop>();
    }

    public void addToStops(BusStop stop){
        mStops.add(stop);
        notifyDataSetChanged();
    }

    public boolean hasStops(){
        if(mStops.size() > 0){
            return true;
        }

        return false;
    }

    public void clearData(){
        mStops.clear();
        notifyDataSetChanged();
    }

    public void removeEntry(int position){
        mStops.remove(position);
        notifyItemRemoved(position);
    }

    @Override
    /**
     * Inflates a layout depending on its position and returns a ViewHolder
     */
    public BusStopsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View contactView = null;
        // inflate second item layout & return that viewHolder
        contactView = inflater.inflate(R.layout.item_bus_stop, parent, false);
        // Return a new holder instance
        return new BusStopsAdapter.ViewHolder(contactView);
    }

    @Override
    /**
     * Populates data into the layout through the viewholder
     */
    public void onBindViewHolder(final BusStopsAdapter.ViewHolder viewHolder, int position) {

        final BusStop busStop = mStops.get(position);

        viewHolder.mName.setText(busStop.getName());

        // Set distance
        LocationUtils.displayDistance(viewHolder.mDistance, mContext, busStop.getLatitude(), busStop.getLongitude());
        // set author
        viewHolder.mLocality.setText(busStop.getLocality());

        viewHolder.mLocateFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(mContext, MainActivity.class);
                intent.setAction("single_entry");
                intent.putExtra("entry", mStops.get(viewHolder.getAdapterPosition()));
                mContext.startActivity(intent);
            }
        });
    }

    @Override
    /**
     * return the total count of items in the list
     */
    public int getItemCount() {
        if (mStops == null) {
            return 0;
        }
        return mStops.size();
    }

    /**
     * CLASS
     * Used to cache the views within the layout for quick access
     */
    class ViewHolder extends RecyclerView.ViewHolder{

        // The UI elements
        public TextView mDistance;
        public TextView mName;
        public ImageView mLocateFile;
        public TextView mLocality;



        public ViewHolder(View itemView) {
            super(itemView);

            // Find the UI elements
            mName = (TextView) itemView.findViewById(R.id.ibs_name);
            mLocateFile = (ImageView) itemView.findViewById(R.id.if_locate_file);
            mDistance = (TextView) itemView.findViewById(R.id.ibs_distance);
            mLocality = (TextView) itemView.findViewById(R.id.ibs_locality);

        }
    }
}
