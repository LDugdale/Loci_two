package com.lauriedugdale.loci.ui.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.lauriedugdale.loci.R;
import com.lauriedugdale.loci.data.UserDatabase;
import com.lauriedugdale.loci.data.dataobjects.User;
import com.lauriedugdale.loci.utils.DataUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by mnt_x on 22/06/2017.
 */

public class NotificationFriendsAdapter extends RecyclerView.Adapter<NotificationFriendsAdapter.ViewHolder> {


    // Store the context and cursor for easy access
    private Context mContext;
    private List<User> mUsers;
    private UserDatabase mUserDatabase;

    /**
     * Entry adapter constructor
     *
     * @param context
     */
    public NotificationFriendsAdapter(Context context) {
        this.mContext = context;
        mUsers = new ArrayList<User>();
        mUserDatabase = new UserDatabase(context);
    }

    public void addToUsers(User user){
        mUsers.add(user);
    }

    public void clearData(){
        mUsers.clear();
        notifyDataSetChanged();
    }

    public void removeEntry(int position){
        mUsers.remove(position);
        notifyItemRemoved(position);
    }

    @Override
    /**
     * Inflates a layout depending on its position and returns a ViewHolder
     */
    public NotificationFriendsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View contactView = null;


        // inflate second item layout & return that viewHolder
        contactView = inflater.inflate(R.layout.item_request_entry, parent, false);


        // Return a new holder instance
        return new NotificationFriendsAdapter.ViewHolder(contactView);
    }

    @Override
    /**
     * Populates data into the layout through the viewholder
     */
    public void onBindViewHolder(NotificationFriendsAdapter.ViewHolder viewHolder, final int position) {

        final User user = mUsers.get(position);

        // set username
        viewHolder.mName.setText(user.getUsername());

        // set profile picture
        mUserDatabase.downloadProfilePic(viewHolder.mProfilePic, R.drawable.default_profile);

        viewHolder.mAddButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mUserDatabase.uploadFriend(user);
                removeEntry(position);
            }
        });
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
    class ViewHolder extends RecyclerView.ViewHolder{

        // The UI elements
        public TextView mName;
        public ImageView mProfilePic;
        public Button mAddButton;
        public Button mRejectButton;


        public ViewHolder(View itemView) {
            super(itemView);

            // Find the UI elements
            mName = (TextView) itemView.findViewById(R.id.ire_name);
            mProfilePic = (ImageView) itemView.findViewById(R.id.ire_profile_pic);
            mAddButton = (Button) itemView.findViewById(R.id.ire_accept_button);
            mRejectButton = (Button) itemView.findViewById(R.id.ire_reject_button);

        }
    }
}
