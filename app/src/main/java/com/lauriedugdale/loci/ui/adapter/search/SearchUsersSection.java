package com.lauriedugdale.loci.ui.adapter.search;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.lauriedugdale.loci.R;
import com.lauriedugdale.loci.data.DataUtils;
import com.lauriedugdale.loci.data.dataobjects.User;

import java.util.ArrayList;
import java.util.List;

import io.github.luizgrp.sectionedrecyclerviewadapter.SectionParameters;
import io.github.luizgrp.sectionedrecyclerviewadapter.StatelessSection;

/**
 * Created by mnt_x on 16/07/2017.
 */

public class SearchUsersSection extends StatelessSection {

    private Context mContext;
    private List<User> mUsers;
    private DataUtils mDataUtils;

    public SearchUsersSection(Context context) {
        // call constructor with layout resources for this Section header and items
        super(new SectionParameters.Builder(R.layout.search_item_user_section)
                .headerResourceId(R.layout.search_header_user_section)
                .build());

        mContext = context;
        mUsers = new ArrayList<User>();
        mDataUtils = new DataUtils(context);

    }

    public void addToUsers(User user) {
        mUsers.add(user);
    }

    public void clearData(){
        mUsers.clear();
    }

    @Override
    public int getContentItemsTotal() {
        if (mUsers == null) {
            return 0;
        }
        return mUsers.size();    }

    @Override
    public RecyclerView.ViewHolder getItemViewHolder(View view) {
        return new SearchUsersViewholder(view);
    }

    @Override
    public void onBindItemViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        SearchUsersViewholder userViewHolder = (SearchUsersViewholder) viewHolder;

        final User user = mUsers.get(position);
        final Button addButton = userViewHolder.mAddButton;
        // set username
        userViewHolder.mName.setText(user.getUsername());

        // set profile picture
        mDataUtils.getProfilePic(userViewHolder.mProfilePic, R.drawable.default_profile);
        if(!user.getRequests().contains(mDataUtils.getCurrentUID())) {
            userViewHolder.mAddButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mDataUtils.addFriendRequest(addButton, user.getUserID(), true);
                }
            });
        } else if (user.getUserID() == mDataUtils.getCurrentUID()){
            userViewHolder.mAddButton.setVisibility(View.INVISIBLE);
        } else {
            userViewHolder.mAddButton.setText("Added");
        }
    }

    class SearchUsersViewholder extends RecyclerView.ViewHolder {

        // The UI elements
        public TextView mName;
        public ImageView mProfilePic;
        public Button mAddButton;

        public SearchUsersViewholder(View itemView) {
            super(itemView);

            // Find the UI elements
            mName = (TextView) itemView.findViewById(R.id.ise_name);
            mProfilePic = (ImageView) itemView.findViewById(R.id.ise_profile_pic);
            mAddButton = (Button) itemView.findViewById(R.id.add_user_button);
        }
    }
}
