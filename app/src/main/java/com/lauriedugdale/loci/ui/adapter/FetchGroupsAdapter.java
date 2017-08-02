package com.lauriedugdale.loci.ui.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;

import com.lauriedugdale.loci.R;
import com.lauriedugdale.loci.data.GroupDatabase;
import com.lauriedugdale.loci.data.UserDatabase;
import com.lauriedugdale.loci.utils.DataUtils;
import com.lauriedugdale.loci.data.dataobjects.Group;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Laurie Dugdale
 */

public class FetchGroupsAdapter extends RecyclerView.Adapter<FetchGroupsAdapter.ViewHolder> {

    // Store the context and cursor for easy access
    private Context mContext;
    private List<Group> mGroups;
    private UserDatabase mUserDatabase;

    private Group mSelectedGroup;
    public int mSelectedItem = -1;


    /**
     * Entry adapter constructor
     *
     * @param context
     */
    public FetchGroupsAdapter(Context context) {
        this.mContext = context;
        mUserDatabase = new UserDatabase(context);

        mGroups = new ArrayList<Group>();
        mGroups.add(new Group("Everyone"));
        mGroups.add(new Group("Friends"));
        mGroups.add(new Group("Just me"));
    }

    public void addToGroups(Group group){
        mGroups.add(group);
        notifyDataSetChanged();
    }

    public Group getSelectedGroup() {
        return mSelectedGroup;
    }

    @Override
    /**
     * Inflates a layout depending on its position and returns a ViewHolder
     */
    public FetchGroupsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View contactView = null;


        // inflate second item layout & return that viewHolder
        contactView = inflater.inflate(R.layout.item_select_group, parent, false);


        // Return a new holder instance
        return new FetchGroupsAdapter.ViewHolder(contactView);
    }

    @Override
    /**
     * Populates data into the layout through the viewholder
     */
    public void onBindViewHolder(FetchGroupsAdapter.ViewHolder viewHolder, int position) {

        final Group group = mGroups.get(position);

        viewHolder.mName.setText(group.getGroupName());

        mUserDatabase.downloadProfilePic(viewHolder.mProfilePic, R.drawable.default_profile);

        viewHolder.mCheckedItem.setChecked(position == mSelectedItem);


//        viewHolder.mCheckedItem.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//            @Override
//            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
////                mSelectedID = group.getGroupID();
//            }
//        });

    }


    @Override
    /**
     * return the total count of items in the list
     */
    public int getItemCount() {
        if (mGroups == null) {
            return 0;
        }
        return mGroups.size();
    }

    /**
     * CLASS
     * Used to cache the views within the layout for quick access
     */
    class ViewHolder extends RecyclerView.ViewHolder{

        // The UI elements
        public TextView mName;
        public ImageView mProfilePic;
        public RadioButton mCheckedItem;

        public ViewHolder(View itemView) {
            super(itemView);

            // Find the UI elements
            mName = (TextView) itemView.findViewById(R.id.isg_name);
            mProfilePic = (ImageView) itemView.findViewById(R.id.isg_profile_pic);
            mCheckedItem = (RadioButton) itemView.findViewById(R.id.isg_user_radiobutton);

            View.OnClickListener listener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mSelectedItem = getAdapterPosition();
                    mSelectedGroup = mGroups.get(mSelectedItem);
                    notifyItemRangeChanged(0, mGroups.size());
                }
            };
            itemView.setOnClickListener(listener);
            mCheckedItem.setOnClickListener(listener);
        }
    }
}