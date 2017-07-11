package com.lauriedugdale.loci.ui.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.lauriedugdale.loci.R;
import com.lauriedugdale.loci.data.dataobjects.User;
import com.lauriedugdale.loci.data.DataUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by mnt_x on 21/06/2017.
 */

public class FriendsAdapter extends RecyclerView.Adapter<FriendsAdapter.ViewHolder> {


    // Store the context and cursor for easy access
    private Context mContext;
    private List<User> mUsers;

    private DataUtils mDataUtils;

    // This interface handles clicks on items within this Adapter. This is populated from the constructor
    // Call the instance in this variable to call the onClick method whenever and item is clicked in the list.
    final private SocialAdapterOnClickHandler mClickHandler;

    /**
     * The interface that receives onClick messages.
     */
    public interface SocialAdapterOnClickHandler {
        void onSocialClick(User user);
    }

    /**
     * Entry adapter constructor
     *
     * @param context
     * @param clickHandler
     */
    public FriendsAdapter(Context context, SocialAdapterOnClickHandler clickHandler) {
        this.mContext = context;
        this.mClickHandler = clickHandler;
        mUsers = new ArrayList<User>();
        mDataUtils = new DataUtils(context);
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

        User user = mUsers.get(position);

        // set username
        viewHolder.mName.setText(user.getUsername());

        // set profile picture
        mDataUtils.getProfilePic(viewHolder.mProfilePic, R.drawable.default_profile);
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

    /**
     * CLASS
     * Used to cache the views within the layout for quick access
     */
    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        // The UI elements
        public TextView mName;
        public ImageView mProfilePic;

        public ViewHolder(View itemView) {
            super(itemView);

            // Find the UI elements
            mName = (TextView) itemView.findViewById(R.id.ise_name);
            mProfilePic = (ImageView) itemView.findViewById(R.id.ise_profile_pic);

            // set the listener as this class
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int pos = getAdapterPosition();
            mClickHandler.onSocialClick(mUsers.get(pos));
        }
    }
}