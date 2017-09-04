package com.lauriedugdale.loci.ui.adapter.social;

import android.content.Context;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.lauriedugdale.loci.R;
import com.lauriedugdale.loci.data.GroupDatabase;
import com.lauriedugdale.loci.data.dataobjects.Group;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by mnt_x on 28/06/2017.
 */

public class GroupsAdapter extends RecyclerView.Adapter<GroupsAdapter.ViewHolder> {

    private Context mContext; // the current context
    private List<Group> mGroups;// the groups being displayed

    private GroupDatabase mGroupDatabase;

    // This interface handles clicks on items within this Adapter. This is populated from the constructor
    // Call the instance in this variable to call the onClick method whenever and item is clicked in the list.
    final private GroupsAdapter.GroupAdapterOnClickHandler mClickHandler;

    /**
     * The interface that receives onClick messages.
     */
    public interface GroupAdapterOnClickHandler {
        void onGroupClick(Group group);
    }

    /**
     * Entry adapter constructor
     *
     * @param context
     * @param clickHandler
     */
    public GroupsAdapter(Context context, GroupsAdapter.GroupAdapterOnClickHandler clickHandler) {
        this.mContext = context;
        this.mClickHandler = clickHandler;
        mGroups = new ArrayList<Group>();
        mGroupDatabase = new GroupDatabase(context);
    }

    /**
     * add to Groups and notify that the data set has changed
     * @param group
     */
    public void addToGroups(Group group){
        mGroups.add(group);
        notifyDataSetChanged();
    }

    @Override
    /**
     * Inflates a layout depending on its position and returns a ViewHolder
     */
    public GroupsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View contactView = null;

        // inflate second item layout & return that viewHolder
        contactView = inflater.inflate(R.layout.item_groups, parent, false);

        // Return a new holder instance
        return new GroupsAdapter.ViewHolder(contactView);
    }

    @Override
    /**
     * Populates data into the layout through the viewholder
     */
    public void onBindViewHolder(final GroupsAdapter.ViewHolder viewHolder, final int position) {

        final Group group = mGroups.get(position);

        // set username
        viewHolder.mName.setText(group.getGroupName());
        // set profile picture
        mGroupDatabase.downloadGroupPic(viewHolder.mGroupPic, R.drawable.default_profile, group.getProfilePicturePath());
        // creates menu when options is clicked
        viewHolder.mOptions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final PopupMenu popup = new PopupMenu(mContext, viewHolder.mOptions);
                //inflate menu
                popup.inflate(R.menu.group_options);
                // listener for the menu item click
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            // remove group option
                            case R.id.remove_group:
                                // remove member from group
                                mGroupDatabase.removeGroupMember(group.getGroupID(), null);
                                mGroups.remove(position);
                                notifyDataSetChanged();
                                break;
                        }
                        return false;
                    }
                });
                popup.show();
            }
        });
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
    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        // The UI elements
        public TextView mName;
        public ImageView mGroupPic;
        public TextView mOptions;

        public ViewHolder(View itemView) {
            super(itemView);

            // Find the UI elements
            mName = (TextView) itemView.findViewById(R.id.ig_group_name);
            mGroupPic = (ImageView) itemView.findViewById(R.id.ig_group_pic);
            mOptions = (TextView) itemView.findViewById(R.id.ig_view_options);

            // set the listener as this class
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int pos = getAdapterPosition();
            mClickHandler.onGroupClick(mGroups.get(pos));
        }
    }
}