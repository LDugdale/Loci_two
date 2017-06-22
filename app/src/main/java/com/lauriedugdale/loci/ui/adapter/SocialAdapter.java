package com.lauriedugdale.loci.ui.adapter;

import android.animation.ArgbEvaluator;
import android.content.Context;
import android.database.Cursor;
import android.graphics.PorterDuff;
import android.icu.text.SimpleDateFormat;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.lauriedugdale.loci.R;
import com.lauriedugdale.loci.User;
import com.lauriedugdale.loci.data.DataUtils;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by mnt_x on 21/06/2017.
 */

public class SocialAdapter  extends RecyclerView.Adapter<SocialAdapter.ViewHolder> {


    // Store the context and cursor for easy access
    private Context mContext;
    private List<User> mUsers;

    // This interface handles clicks on items within this Adapter. This is populated from the constructor
    // Call the instance in this variable to call the onClick method whenever and item is clicked in the list.
    final private SocialAdapterOnClickHandler mClickHandler;

    /**
     * The interface that receives onClick messages.
     */
    public interface SocialAdapterOnClickHandler {
        void onClick(long date);
    }

    /**
     * Entry adapter constructor
     *
     * @param mContext
     * @param clickHandler
     */
    public SocialAdapter(Context mContext, SocialAdapterOnClickHandler clickHandler) {
        this.mContext = mContext;
        this.mClickHandler = clickHandler;
        mUsers = new ArrayList<User>();
    }

    public void addToUsers(User user){
        mUsers.add(user);
    }

    @Override
    /**
     * Inflates a layout depending on its position and returns a ViewHolder
     */
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View contactView = null;


        // inflate second item layout & return that viewHolder
        contactView = inflater.inflate(R.layout.item_social_entry, parent, false);


        // Return a new holder instance
        return new ViewHolder(contactView);
    }

    @Override
    /**
     * Populates data into the layout through the viewholder
     */
    public void onBindViewHolder(ViewHolder viewHolder, int position) {

        viewHolder.mName.setText(mUsers.get(position).email);
//        // get values from the cursor to update the view holder
//        String date = mCursor.getString(mCursor.getColumnIndex(SensedContract.SensedEntry.COLUMN_ENTRY_DATE_TIME));
//        int happiness = mCursor.getInt(mCursor.getColumnIndex(SensedContract.SensedEntry.COLUMN_ENTRY_HAPPINESS));
//        long id = mCursor.getLong(mCursor.getColumnIndex(SensedContract.SensedEntry._ID));
//
//        // set the values from the cursor to the UI elements
//        viewHolder.mEntryHappiness.setText(String.valueOf(happiness));
//        viewHolder.mEntryDate.setText(parseDate(date));


    }


    @Override
    /**
     * return the total count of items in the list
     */
    public int getItemCount() {
        if (mUsers == null) {
            return 0;
        }
        return mUsers.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) return 1;
        else return 2;
    }

    /**
     * CLASS
     * Used to cache the views within the layout for quick access
     */
    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        // The UI elements
        public TextView mName;

        public ViewHolder(View itemView) {
            super(itemView);

            // Find the UI elements
            mName = (TextView) itemView.findViewById(R.id.ise_name);

            // set the listener as this class
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {

            // call the onClick method for the mClickHandler variable
//            mClickHandler.onClick(id);
        }
    }


}