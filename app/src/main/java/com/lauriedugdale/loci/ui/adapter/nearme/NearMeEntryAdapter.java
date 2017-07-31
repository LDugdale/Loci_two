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
import com.lauriedugdale.loci.data.dataobjects.GeoEntry;
import com.lauriedugdale.loci.ui.activity.MainActivity;
import com.lauriedugdale.loci.utils.LocationUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Created by mnt_x on 29/07/2017.
 */

public class NearMeEntryAdapter extends RecyclerView.Adapter<NearMeEntryAdapter.ViewHolder> {

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
    public NearMeEntryAdapter(Context context, List<GeoEntry> entries) {
        this.mContext = context;
        mEntries = new ArrayList<GeoEntry>();
        mEntryStorage = new EntryStorage(context);
        mEntries = entries;
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
    public NearMeEntryAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View contactView = null;
        // inflate second item layout & return that viewHolder
        contactView = inflater.inflate(R.layout.item_near_me_entry, parent, false);
        // Return a new holder instance
        return new NearMeEntryAdapter.ViewHolder(contactView);
    }

    @Override
    /**
     * Populates data into the layout through the viewholder
     */
    public void onBindViewHolder(final NearMeEntryAdapter.ViewHolder viewHolder, int position) {

        final GeoEntry entry = mEntries.get(position);

        viewHolder.mTitle.setText(entry.getTitle());
        int entryImage;
        switch (entry.getFileType()){
            case DataUtils.IMAGE:
                entryImage = R.drawable.ic_image;
                break;
            case DataUtils.AUDIO:
                entryImage = R.drawable.ic_audiotrack_light;
                break;
            default:
                entryImage = R.drawable.ic_text;
                break;
        }
        // set file picture
        mEntryStorage.getFilePic(viewHolder.mFilePic, entry);

        // Set distance
        LocationUtils.displayDistance(viewHolder.mDistance, mContext, entry.getLatitude(), entry.getLongitude());
        // set date
        String dateString = new java.text.SimpleDateFormat("EEE, d MMM 'at' HH:mm", Locale.UK).format(new Date( entry.getUploadDate()));
        viewHolder.mDate.setText(dateString);
        // set author
        viewHolder.mAuthor.setText(entry.getCreatorName());

        viewHolder.mLocateFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(mContext, MainActivity.class);
                intent.setAction("single_entry");
                intent.putExtra("entry", mEntries.get(viewHolder.getAdapterPosition()));
                mContext.startActivity(intent);
            }
        });
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
        public TextView mDistance;
        public TextView mDate;
        public TextView mTitle;
        public ImageView mFilePic;
        public ImageView mLocateFile;
        public TextView mAuthor;



        public ViewHolder(View itemView) {
            super(itemView);

            // Find the UI elements
            mTitle = (TextView) itemView.findViewById(R.id.if_name);
            mFilePic = (ImageView) itemView.findViewById(R.id.if_file_pic);
            mLocateFile = (ImageView) itemView.findViewById(R.id.if_locate_file);
            mDistance = (TextView) itemView.findViewById(R.id.info_bar_marker_distance);
            mDate = (TextView) itemView.findViewById(R.id.info_bar_marker_date);
            mAuthor = (TextView) itemView.findViewById(R.id.info_bar_marker_author);
        }
    }
}