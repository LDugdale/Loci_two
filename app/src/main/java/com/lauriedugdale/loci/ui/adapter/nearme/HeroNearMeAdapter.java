package com.lauriedugdale.loci.ui.adapter.nearme;

import android.content.Context;
import android.content.Intent;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.lauriedugdale.loci.R;
import com.lauriedugdale.loci.data.EntryStorage;
import com.lauriedugdale.loci.data.UserDatabase;
import com.lauriedugdale.loci.data.dataobjects.GeoEntry;
import com.lauriedugdale.loci.ui.activity.MainActivity;
import com.lauriedugdale.loci.utils.LocationUtils;

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
    private UserDatabase mUserDatabase;

    /**
     * NearMeAdapter constructor
     *
     * @param context
     */
    public HeroNearMeAdapter(Context context) {
        this.mContext = context;
        mEntries = new ArrayList<GeoEntry>();
        mEntryStorage = new EntryStorage(context);
        mUserDatabase = new UserDatabase(context);
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

        // set entry picture
        mEntryStorage.getFilePic(viewHolder.mFilePic, entry);
        viewHolder.mAuthor.setText(entry.getCreatorName());
        viewHolder.mTitle.setText(entry.getTitle());

        mUserDatabase.downloadNonLoggedInProfilePic(entry.getCreator(), viewHolder.mAuthorPic, R.drawable.default_profile);

        viewHolder.mWrapper.setOnClickListener(new View.OnClickListener() {
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
        public ImageView mAuthorPic;
        public TextView mAuthor;
        public TextView mTitle;
        public ConstraintLayout mWrapper;

        public ViewHolder(View itemView) {
            super(itemView);

            // Find the UI elements
            mFilePic = (ImageView) itemView.findViewById(R.id.ihnm_hero_image);
            mAuthor = (TextView) itemView.findViewById(R.id.info_bar_marker_author);
            mAuthorPic = (ImageView) itemView.findViewById(R.id.if_author_pic);
            mTitle = (TextView) itemView.findViewById(R.id.if_name);
            mWrapper = (ConstraintLayout) itemView.findViewById(R.id.details_wrapper);
        }
    }
}