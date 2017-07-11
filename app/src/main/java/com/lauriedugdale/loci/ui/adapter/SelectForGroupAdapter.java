package com.lauriedugdale.loci.ui.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.lauriedugdale.loci.R;
import com.lauriedugdale.loci.data.DataUtils;
import com.lauriedugdale.loci.data.dataobjects.User;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Laurie Dugdale
 */

public class SelectForGroupAdapter extends RecyclerView.Adapter<SelectForGroupAdapter.ViewHolder> {

    // Store the context and cursor for easy access
    private Context mContext;
    private List<User> mUsers;
    private DataUtils mDataUtils;

    private HashSet<String> mCheckedItems;


    /**
     * Entry adapter constructor
     *
     * @param context
     */
    public SelectForGroupAdapter(Context context) {
        this.mContext = context;
        mUsers = new ArrayList<User>();
        mDataUtils = new DataUtils(context);

        mCheckedItems = new HashSet<String>();
    }

    public void addToUsers(User user){
        mUsers.add(user);
        notifyDataSetChanged();
    }

    public void clearData(){
        mUsers.clear();
        notifyDataSetChanged();
    }

    public HashSet<String> getCheckedItems() {
        return mCheckedItems;
    }

    @Override
    /**
     * Inflates a layout depending on its position and returns a ViewHolder
     */
    public SelectForGroupAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View contactView = null;


        // inflate second item layout & return that viewHolder
        contactView = inflater.inflate(R.layout.item_select_for_group, parent, false);


        // Return a new holder instance
        return new SelectForGroupAdapter.ViewHolder(contactView);
    }

    @Override
    /**
     * Populates data into the layout through the viewholder
     */
    public void onBindViewHolder(SelectForGroupAdapter.ViewHolder viewHolder, int position) {

        final User user = mUsers.get(position);
        // set username
        viewHolder.mName.setText(user.getUsername());

        mDataUtils.getProfilePic(viewHolder.mProfilePic, R.drawable.default_profile);

        viewHolder.mCheckedItem.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    mCheckedItems.add(user.getUserID());
                } else {
                    mCheckedItems.remove(user.getUserID());
                }
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
        public CheckBox mCheckedItem;

        public ViewHolder(View itemView) {
            super(itemView);

            // Find the UI elements
            mName = (TextView) itemView.findViewById(R.id.isfg_name);
            mProfilePic = (ImageView) itemView.findViewById(R.id.isfg_profile_pic);
            mCheckedItem = (CheckBox) itemView.findViewById(R.id.isfg_user_checkbox);

        }
    }
}