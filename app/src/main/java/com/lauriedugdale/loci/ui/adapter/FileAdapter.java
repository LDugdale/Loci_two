package com.lauriedugdale.loci.ui.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import com.lauriedugdale.loci.R;
import com.lauriedugdale.loci.data.DataUtils;
import com.lauriedugdale.loci.data.dataobjects.GeoEntry;
import com.lauriedugdale.loci.data.dataobjects.User;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by mnt_x on 26/06/2017.
 */

public class FileAdapter extends RecyclerView.Adapter<FileAdapter.ViewHolder> {

    //TODO add delete, locate and edit buttons (only edit if within distance)

    // Store the context and cursor for easy access
    private Context mContext;
    private List<GeoEntry> mFiles;
    private DataUtils mDataUtils;

    /**
     * Entry adapter constructor
     *
     * @param context
     */
    public FileAdapter(Context context) {
        this.mContext = context;
        mFiles = new ArrayList<GeoEntry>();
        mDataUtils = new DataUtils(context);
    }

    public void addToFiles(GeoEntry entry){
        mFiles.add(entry);
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
    public void onBindViewHolder(FileAdapter.ViewHolder viewHolder, final int position) {

        final GeoEntry entry = mFiles.get(position);

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
        mDataUtils.getFilePic(viewHolder.mFilePic, entry.getFilePath(), entryImage, entry.getFileType());
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
        public TextView mTitle;
        public ImageView mFilePic;


        public ViewHolder(View itemView) {
            super(itemView);

            // Find the UI elements
            mTitle = (TextView) itemView.findViewById(R.id.if_name);
            mFilePic = (ImageView) itemView.findViewById(R.id.if_file_pic);
        }
    }
}
