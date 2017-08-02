package com.lauriedugdale.loci.ui.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.lauriedugdale.loci.AccessPermission;
import com.lauriedugdale.loci.R;
import com.lauriedugdale.loci.utils.DataUtils;
import com.lauriedugdale.loci.data.EntryStorage;
import com.lauriedugdale.loci.data.dataobjects.GeoEntry;
import com.lauriedugdale.loci.ui.activity.MainActivity;
import com.lauriedugdale.loci.utils.EntryUtils;
import com.lauriedugdale.loci.utils.LocationUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Created by mnt_x on 26/06/2017.
 */

public class FileAdapter extends RecyclerView.Adapter<FileAdapter.ViewHolder> {

    //TODO add delete, locate and edit buttons (only edit if within distance)

    // Store the context and cursor for easy access
    private Context mContext;
    private List<GeoEntry> mFiles;
    private EntryStorage mEntryStorage;
    private AccessPermission mAccess;

    /**
     * Entry adapter constructor
     *
     * @param context
     */
    public FileAdapter(Context context, AccessPermission access) {
        this.mContext = context;
        mFiles = new ArrayList<GeoEntry>();
        mEntryStorage = new EntryStorage(context);
        mAccess = access;
    }

    public void addToFiles(GeoEntry entry){
        mFiles.add(entry);
        notifyDataSetChanged();
    }

    public void clearData(){
        mFiles.clear();
        notifyDataSetChanged();
    }

    public void removeEntry(int position){
        mFiles.remove(position);
        notifyItemRemoved(position);
    }

    @Override
    /**
     * Inflates a layout depending on its position and returns a ViewHolder
     */
    public FileAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View contactView = null;
        // inflate second item layout & return that viewHolder
        contactView = inflater.inflate(R.layout.item_files, parent, false);
        // Return a new holder instance
        return new FileAdapter.ViewHolder(contactView);
    }

    @Override
    /**
     * Populates data into the layout through the viewholder
     */
    public void onBindViewHolder(final FileAdapter.ViewHolder viewHolder, int position) {

        final GeoEntry entry = mFiles.get(position);

        viewHolder.mTitle.setText(entry.getTitle());

        // set file picture
        EntryUtils.getFilePic(viewHolder.mFilePic, entry);

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
            intent.putExtra("entry", mFiles.get(viewHolder.getAdapterPosition()));
            mContext.startActivity(intent);
            }
        });
    }

    @Override
    /**
     * return the total count of items in the list
     */
    public int getItemCount() {
        if (mFiles == null) {
            return 0;
        }
        return mFiles.size();
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
