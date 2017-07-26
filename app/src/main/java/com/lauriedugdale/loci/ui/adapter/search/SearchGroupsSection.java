package com.lauriedugdale.loci.ui.adapter.search;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.lauriedugdale.loci.R;
import com.lauriedugdale.loci.data.DataUtils;
import com.lauriedugdale.loci.data.dataobjects.Group;
import com.lauriedugdale.loci.ui.activity.social.GroupProfileActivity;
import com.lauriedugdale.loci.ui.activity.social.UserProfileActivity;

import java.util.ArrayList;
import java.util.List;

import io.github.luizgrp.sectionedrecyclerviewadapter.SectionParameters;
import io.github.luizgrp.sectionedrecyclerviewadapter.StatelessSection;

/**
 * Created by mnt_x on 17/07/2017.
 */

public class SearchGroupsSection  extends StatelessSection {

    private Context mContext;
    private List<Group> mGroups;
    private DataUtils mDataUtils;

    public SearchGroupsSection(Context context) {
        // call constructor with layout resources for this Section header and items
        super(new SectionParameters.Builder(R.layout.search_item_group_section)
                .headerResourceId(R.layout.search_header_group_section)
                .build());

        mContext = context;
        mGroups = new ArrayList<Group>();
        mDataUtils = new DataUtils(context);

    }

    public void addToGroups(Group group) {
        mGroups.add(group);
        System.out.println(mGroups.size());
    }

    public void clearData(){
        mGroups.clear();
    }

    @Override
    public int getContentItemsTotal() {
        if (mGroups == null) {
            return 0;
        }
        return mGroups.size();
    }

    @Override
    public RecyclerView.ViewHolder getItemViewHolder(View view) {
        return new SearchGroupsViewholder(view);
    }

    @Override
    public void onBindItemViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        SearchGroupsSection.SearchGroupsViewholder groupsViewholder = (SearchGroupsSection.SearchGroupsViewholder) viewHolder;
        final Group group = mGroups.get(position);

        // set groupname
        groupsViewholder.mGroupName.setText(group.getGroupName());
        // set profile picture
        mDataUtils.getGroupPic(groupsViewholder.mGroupPic, R.drawable.default_profile, group.getProfilePicturePath());
        groupsViewholder.mRootView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                Intent intent = new Intent(mContext, GroupProfileActivity.class);
                intent.putExtra(Intent.ACTION_OPEN_DOCUMENT, group);
                mContext.startActivity(intent);
            }
        });
    }

    class SearchGroupsViewholder extends RecyclerView.ViewHolder {

        // The UI elements
        public TextView mGroupName;
        public ImageView mGroupPic;
        public View mRootView;

        public SearchGroupsViewholder(View itemView) {
            super(itemView);

            // Find the UI elements
            mGroupName = (TextView) itemView.findViewById(R.id.ig_group_name);
            mGroupPic = (ImageView) itemView.findViewById(R.id.ig_group_pic);
            mRootView = itemView;
        }
    }
}