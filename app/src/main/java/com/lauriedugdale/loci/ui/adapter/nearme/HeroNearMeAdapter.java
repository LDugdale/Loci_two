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
import com.lauriedugdale.loci.data.EntryStorage;
import com.lauriedugdale.loci.data.dataobjects.GeoEntry;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Created by mnt_x on 01/08/2017.
 */

public class HeroNearMeAdapter extends RecyclerView.Adapter<HeroNearMeAdapter.ViewHolder> {

    //TODO add delete, locate and edit buttons (only edit if within distance)

    // Store the context and cursor for easy access
    private Context mContext;
    private List<GeoEntry> mEntries;
    private EntryStorage mEntryStorage;

    /**
     * NearMeAdapter constructor
     *
     * @param context
     */
    public HeroNearMeAdapter(Context context) {
        this.mContext = context;
        mEntries = new ArrayList<GeoEntry>();
        mEntryStorage = new EntryStorage(context);
    }

    public void addToEntries(GeoEntry entry){
        mEntries.add(entry);
        notifyDataSetChanged();
    }

    public boolean hasEntries(){
        if(mEntries.size() > 0){
            return true;
        }

        return false;
    }

    public void clearData(){
        mEntries.clear();
        notifyDataSetChanged();
    }

    public void removeEntry(int position){
        mEntries.remove(position);
        notifyItemRemoved(position);
    }

    @Override
    /**
     * Inflates a layout depending on its position and returns a ViewHolder
     */
    public HeroNearMeAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View contactView = null;
        // inflate second item layout & return that viewHolder
        contactView = inflater.inflate(R.layout.item_hero_near_me, parent, false);
        // Return a new holder instance
        return new HeroNearMeAdapter.ViewHolder(contactView);
    }

    @Override
    /**
     * Populates data into the layout through the viewholder
     */
    public void onBindViewHolder(final HeroNearMeAdapter.ViewHolder viewHolder, int position) {

        final GeoEntry entry = mEntries.get(position);

        System.out.println("THIS IS THEJSKLFJSLKFJSKF " + entry.getTitle());

        // set entry picture
        mEntryStorage.getFilePic(viewHolder.mFilePic, entry);
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
        public ImageView mFilePic;

        public ViewHolder(View itemView) {
            super(itemView);

            // Find the UI elements
            mFilePic = (ImageView) itemView.findViewById(R.id.ihnm_hero_image);
        }
    }
}